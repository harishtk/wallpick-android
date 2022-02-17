package com.harishtk.app.wallpick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import coil.transform.Transformation
import com.harishtk.app.wallpick.data.Result
import com.harishtk.app.wallpick.data.entity.CuratedResponse
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.data.succeeded
import com.harishtk.app.wallpick.ui.theme.WallPickTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WallPickTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting(viewModel)
                }
            }
        }

        viewModel.fetchCuratedPhotos()
        /*viewModel.curatedPhotosResponse.observe(this) {
            Timber.d("$it")
        }*/
    }
}

@Composable
fun Greeting(viewModel: MainViewModel) {
    val responseState by viewModel.curatedPhotosResponse.observeAsState()

    val uiState = viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                contentPadding = PaddingValues(start = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Browse Wallpapers", style = MaterialTheme.typography.h1, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Timber.d("$uiState")
            if (uiState.value.loading) {
                CircularProgressIndicator() /* TODO: extract the indicator */
            } else if (uiState.value.error != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                ) {
                    Text(text = "Failed to load data", modifier = Modifier.padding(16.dp))
                    Button(
                        onClick = { viewModel.accept(UiAction.Retry) },
                        shape = RoundedCornerShape(15.dp, 20.dp, 35.dp, 35.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = "RETRY", modifier = Modifier.padding(12.dp))
                    }
                }
            } else {
                if (uiState.value.photosList.isNotEmpty()) {
                    PhotosList(photos = uiState.value.photosList)
                } else {
                    Text(text = "Something went wrong")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotosList(photos: List<Photo>) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 128.dp)
    ) {
        items(photos) { photo ->
            PhotoItem(photo = photo)
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
    /*LazyColumn(
        contentPadding = PaddingValues(4.dp),
    ) {
        items(photos) { photo ->
            PhotoItem(photo = photo)
        }
    }*/
}

@Composable
fun PhotoItem(photo: Photo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable {  }
    ) {
        Column {
            Box {
                Image(
                    painter = rememberImagePainter(
                        data = photo.src.small,
                        builder = {
                            transformations(RoundedCornersTransformation(0f))
                            crossfade(true)
                        },
                    ),
                    contentDescription = photo.alt,
                    modifier = Modifier
                        .height(128.dp),
                    /*.aspectRatio(1F),*/
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WallPickTheme {
        // Greeting("Android")
    }
}