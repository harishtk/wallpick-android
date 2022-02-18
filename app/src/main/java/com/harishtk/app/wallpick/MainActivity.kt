package com.harishtk.app.wallpick

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.ui.theme.WallPickTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.lang.UnsupportedOperationException

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
                    val context = LocalContext.current
                    MainScreen(context, viewModel)
                }
            }
        }

        // viewModel.fetchCuratedPhotos()
        /*viewModel.curatedPhotosResponse.observe(this) {
            Timber.d("$it")
        }*/
    }
}

@Composable
fun MainScreen(context: Context, viewModel: MainViewModel) {
    val uiState = viewModel.uiState.collectAsState()
    var topBarVisible by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                contentPadding = PaddingValues(start = 8.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp
            ) {
                AnimatedVisibility(visible = topBarVisible) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_appicon),
                            contentDescription = "Brand",
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Browse Wallpapers",
                            style = MaterialTheme.typography.h1,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colors.secondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
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

            /*if (uiState.value.loading) {
                CircularProgressIndicator() *//* TODO: extract the indicator *//*
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
            }*/
            SearchLayout(
                uiState = viewModel.uiState,
                onQueryChanged = viewModel.accept
            )
            PhotosList(photos = viewModel.pagingDataFlow) { photo ->
                val downloadIntent = Intent(Intent.ACTION_VIEW, Uri.parse(photo.src.original))
                try {
                    context.startActivity(downloadIntent)
                    /*if (downloadIntent.resolveActivity(context.packageManager) != null) {

                    } else {
                        throw UnsupportedOperationException("Photo can't be viewed")
                    }*/
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "Photo can't be viewed", Toast.LENGTH_SHORT).show()
                } catch (e: UnsupportedOperationException) {
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Composable
fun SearchLayout(
    uiState: StateFlow<UiState>,
    onQueryChanged: (UiAction.Search) -> Unit,
    modifier: Modifier = Modifier
) {
    var typedText by rememberSaveable { mutableStateOf(uiState.value.query) }

    Surface(
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colors.primaryVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .requiredHeight(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {

            if (typedText.isEmpty()) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.body1.copy(
                        color = MaterialTheme.colors.onSurface.copy(
                            ContentAlpha.medium
                        )
                    ),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = typedText,
                    onValueChange = { typed: String ->
                        typedText = typed
                        onQueryChanged(UiAction.Search(typedText))
                    },
                    /*placeholder = "Type here!",*/
                    singleLine = true,
                    /*cursorColor = MaterialTheme.colors.primary.copy(alpha = 0.5f),*/
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PhotosList(photos: Flow<PagingData<Photo>>, onDownload: (Photo) -> Unit) {
    val lazyPhotoItems: LazyPagingItems<Photo> = photos.collectAsLazyPagingItems()

    LazyColumn {
        items(lazyPhotoItems) { photoItem ->
            PhotoItem(photo = photoItem!!, onDownload = onDownload)
        }

        lazyPhotoItems.apply {
            when {
                loadState.refresh is LoadState.Loading -> {
                    item {
                        Column(
                            modifier = Modifier.fillParentMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Searching..",
                                style = MaterialTheme.typography.subtitle2,
                                modifier = Modifier.padding(16.dp)
                            )
                            CircularProgressIndicator()
                        }
                    }
                }
                loadState.append is LoadState.Loading -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Loading items...")
                            CircularProgressIndicator()
                        }
                    }
                }
                loadState.refresh is LoadState.Error -> {
                    val e = lazyPhotoItems.loadState.refresh as LoadState.Error
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillParentMaxSize()
                        ) {
                            Text(
                                text = e.error.localizedMessage!!,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(
                                onClick = { retry() },
                                shape = RoundedCornerShape(15.dp, 20.dp, 35.dp, 35.dp),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(color = MaterialTheme.colors.secondary)
                            ) {
                                Text(text = "RETRY", modifier = Modifier.padding(12.dp))
                            }
                        }
                    }

                }
                loadState.append is LoadState.Error -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Failed to load data", modifier = Modifier.padding(16.dp))
                            Button(
                                onClick = { retry() },
                                shape = RoundedCornerShape(15.dp, 20.dp, 35.dp, 35.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.secondary
                                ),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = "RETRY", modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoItem(
    photo: Photo,
    modifier: Modifier = Modifier,
    onDownload: (Photo) -> Unit
) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier.clickable { }
    ) {
        Column {
            Row {
                Box(modifier = Modifier.weight(1F)) {
                    Image(
                        painter = rememberImagePainter(
                            data = photo.src.large2x,
                            builder = {
                                transformations(RoundedCornersTransformation(0f))
                                crossfade(true)
                            },
                        ),
                        contentDescription = photo.alt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1F)
                            .clickable { expanded = !expanded },
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )
                }
            }
            /*Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = { *//*TODO*//* },) {
                    Icon(imageVector = Icons.Outlined.FavoriteBorder, contentDescription = "Favorite")
                }
                IconButton(onClick = { *//*TODO*//* }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Options")
                }
            }
            */
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val (divider, text) = createRefs()
                Divider(
                    modifier = Modifier
                        .constrainAs(divider) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            end.linkTo(text.start, margin = 4.dp, goneMargin = 40.dp)
                            bottom.linkTo(parent.bottom)
                            horizontalChainWeight = 0f
                            width = Dimension.fillToConstraints
                        },
                    color = Color.LightGray
                )
                Text(
                    text = "~ ${photo.photographer}",
                    style = MaterialTheme.typography.subtitle1.copy(
                        color = Color.DarkGray,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .constrainAs(text) {
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                            start.linkTo(divider.end)
                            bottom.linkTo(parent.bottom)
                            horizontalChainWeight = 1f
                        }
                        .wrapContentWidth(Alignment.End)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { onDownload(photo); expanded = false },
                        shape = RoundedCornerShape(15.dp, 20.dp, 35.dp, 35.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.secondary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text(text = "DOWNLOAD ORIGINAL", modifier = Modifier.padding(12.dp))
                    }
                }
            }
            Spacer(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WallPickTheme {
        // Greeting("Android")
        // SearchLayout()
    }
}