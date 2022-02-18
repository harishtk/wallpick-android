package com.harishtk.app.wallpick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
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
    val uiState = viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                contentPadding = PaddingValues(start = 8.dp),
                backgroundColor = Color.White,
                elevation = 0.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Browse Wallpapers",
                        style = MaterialTheme.typography.h1,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
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
            PhotosList(photos = viewModel.pagingDataFlow)
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
        color = Color.LightGray.copy(alpha = 0.5f),
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
fun PhotosList(photos: Flow<PagingData<Photo>>) {
    val lazyPhotoItems: LazyPagingItems<Photo> = photos.collectAsLazyPagingItems()

    LazyColumn {
        items(lazyPhotoItems) { photoItem ->
            PhotoItem(photo = photoItem!!)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotosList(photos: List<Photo>) {
    /*LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 128.dp)
    ) {
        items(photos) { photo ->
            PhotoItem(photo = photo)
            Spacer(modifier = Modifier.height(60.dp))
        }
    }*/
    val lazyListState = rememberLazyListState()
    var scrolledY = 0f
    var previousOffset = 0

    LazyColumn(
        contentPadding = PaddingValues(
            bottom = 16.dp
        ),
        state = lazyListState
    ) {
        /*item {
            SearchLayout(
                modifier = Modifier
                    .graphicsLayer {
                        scrolledY += lazyListState.firstVisibleItemScrollOffset - previousOffset
                        translationY = scrolledY * 0.5f
                        previousOffset = lazyListState.firstVisibleItemScrollOffset
                    }
            )
        }*/

        items(photos) { photo ->
            PhotoItem(photo = photo)
        }
    }
}

@Composable
fun PhotoItem(photo: Photo, modifier: Modifier = Modifier) {
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
                            .aspectRatio(1F),
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
            /*Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                Divider(modifier = Modifier.alignBy(HorizontalAlignmentLine(to(Text()))), color = Color.LightGray)
                Text(
                    text = "~ ${photo.photographer}",
                    style = MaterialTheme.typography.subtitle1.copy(color = Color.DarkGray, fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(horizontal = 8.dp).weight(1f)
                )
            }*/
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
                            end.linkTo(text.absoluteLeft, margin = 20.dp, goneMargin = 40.dp)
                            bottom.linkTo(parent.bottom)
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
                            bottom.linkTo(parent.bottom)
                        }
                        .wrapContentWidth(Alignment.End)
                )
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