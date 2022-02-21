package com.harishtk.app.wallpick.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import kotlin.math.roundToInt

@Composable
fun DetailScreen(navController: NavController, viewModel: DetailViewModel) {

    val uiState = viewModel.uiState.collectAsState()
    var scale by remember { mutableStateOf(1F) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back",
                    modifier = Modifier.size(32.dp)
                )
            }
            val title = if (uiState.value.photo != null) {
                "@${uiState.value.photo?.photographer}"
            } else { "Details" }

            Text(
                text = title,
                style = MaterialTheme.typography.h1,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colors.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // TODO: **
        when {
            uiState.value.loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) { CircularProgressIndicator() }
            }
            uiState.value.error != null -> {
                val e = uiState.value.error!!
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) { Text(text = "${e.message}") }
            }
            else -> {
                val photo = uiState.value.photo!!
                Box {
                    Image(
                        painter = rememberImagePainter(
                            data = photo.src?.original!!,
                            builder = {
                                crossfade(true)
                            },
                        ),
                        contentDescription = photo.alt,
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .pointerInput(key1 = "pinch") {
                                detectTransformGestures { centroid, pan, zoom, rotation ->
                                    offsetX += pan.x * 2
                                    offsetY += pan.y * 2
                                    scale = when {
                                        scale < 0.5f -> 0.5f
                                        scale > 3f -> 3f
                                        else -> scale * zoom
                                    }
                                }
                            }
                            .pointerInput(key1 = "tap") {
                                detectTapGestures(
                                    onDoubleTap = {
                                        scale = if (scale != 1f) 1f else scale * 2f
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                )
                            },
                    )
                }
            }
        }
    }
}