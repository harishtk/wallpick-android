package com.harishtk.app.wallpick

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.rememberImagePainter
import coil.transform.RoundedCornersTransformation
import com.harishtk.app.wallpick.ui.screens.detail.DetailScreen
import com.harishtk.app.wallpick.ui.screens.detail.DetailViewModel
import com.harishtk.app.wallpick.ui.screens.favorites.FavoritesScreen
import com.harishtk.app.wallpick.ui.theme.WallPickTheme
import com.harishtk.app.wallpick.ui.widget.*
import com.pexels.api.data.entity.Photo
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import java.util.*

/**
 * TODO: refine the theme swatches
 * TODO: Use work manager to notify the user for daily suggestions and cache them in local db
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface ViewModelFactoryProvider {
        fun detailViewModelFactory(): DetailViewModel.Factory
    }

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
                    MainScreen(context)
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
fun MainScreen(context: Context) {
    var topBarVisible by rememberSaveable { mutableStateOf(true) }

    val navController = rememberNavController()
    navController.addOnDestinationChangedListener { _, destination, _ ->
        topBarVisible = (destination.route == Screen.Home.route)
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = topBarVisible,
                enter = slideInVertically() + expandVertically(),
                exit = slideOutVertically() + shrinkVertically()
            ) {
                TopAppBar(
                    contentPadding = PaddingValues(start = 8.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp
                ) {
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
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable { navController.navigate(Screen.Favorites.route) }
                        )
                    }
                }
            }
        }
    ) {
        NavHost(navController = navController, startDestination = DEFAULT_NAV_HOST) {
            composable(Screen.Home.route) {
                Home(
                    context = context,
                    navController = navController
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    navController = navController
                )
            }
            composable(
                Screen.Detail.route + "/{photoId}",
                arguments = listOf(navArgument(Screen.Detail.ARG_PHOTO_ID) {
                    type = NavType.IntType
                })
            ) {
                val photoId = it.arguments?.get(Screen.Detail.ARG_PHOTO_ID)!! as Int
                DetailScreen(navController = navController, detailViewModel(photoId = photoId))
            }
        }
    }
}

@Composable
fun Home(
    context: Context,
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        // Timber.d("$uiState")

        SearchLayout(
            uiState = uiState,
            onQueryChanged = viewModel.accept
        )
        PhotosList(
            uiState = uiState,
            photos = viewModel.pagingDataFlow,
            uiActions = viewModel.accept,
            onDownload = { photo ->
                val downloadIntent = Intent(Intent.ACTION_VIEW, Uri.parse(photo.src?.original!!))
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
                viewModel.addToFavorite(photo)
            },
            onClickImage = { photo ->
                navController.navigate(Screen.Detail.route + "/${photo.id}")
                viewModel.addToFavorite(photo)
            }
        )
    }
}

const val AutoCompleteBoxTag = "AutoCompleteBoxTag"

@Composable
fun <T : AutoCompleteEntity> AutoCompleteBox(
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    content: @Composable AutoCompleteScope<T>.() -> Unit
) {
    val autoCompleteState = remember {
        AutoCompleteState(startItems = items)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        autoCompleteState.content()
        AnimatedVisibility(visible = autoCompleteState.isSearching) {
            LazyColumn(
                modifier = Modifier.autoComplete(autoCompleteState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(autoCompleteState.filteredItems) { item ->
                    Box(modifier = Modifier.clickable { autoCompleteState.selectItem(item) }) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}

@Composable
fun ValueAutoCompleteItem(item: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = item, style = MaterialTheme.typography.subtitle2)
    }
}

@Composable
fun SearchLayout(
    uiState: State<UiState>,
    onQueryChanged: (UiAction.Search) -> Unit,
    modifier: Modifier = Modifier
) {
    var typedText by rememberSaveable { mutableStateOf(uiState.value.query) }

    val items = listOf(
        "Paulo Pereira",
        "Daenerys Targaryen",
        "Jon Snow",
        "Sansa Stark",
    )
    val autoCompleteEntities = items.asAutoCompleteEntities(
        filter = { item, query ->
            item.lowercase(Locale.getDefault())
                .startsWith(query.lowercase(Locale.getDefault()))
        }
    )

    Surface(
        // shape = RoundedCornerShape(25.dp),
        //color = MaterialTheme.colors.primaryVariant.copy(alpha = 0.5f),
        modifier = modifier.padding(16.dp)
    ) {

        Column {
            /*Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val view = LocalView.current
                SearchBar(
                    value = typedText,
                    label = "Search",
                    onValueChanged = { typedQuery ->
                        typedText = typedQuery
                    },
                    onClearClick = {
                        typedText = ""
                        onQueryChanged(UiAction.Search(""))
                    },
                    onDoneActionClick = { view.clearFocus() }
                )
            }*/
            Box(
                modifier = Modifier
                    .requiredHeight(40.dp)
                    .background(
                        color = MaterialTheme.colors.primaryVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(25.dp)
                    ),
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

                AutoCompleteBox(items = autoCompleteEntities, itemContent = {
                    ValueAutoCompleteItem(item = it.value)
                }) {
                    var value by remember { mutableStateOf("") }
                    val view = LocalView.current

                    onItemSelected { item ->
                        value = item.value
                        filter(value)
                        view.clearFocus()
                    }
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val focusRequester = FocusRequester()
                        val (ed, clearBtn, autoCompleteBox) = createRefs()
                        BasicTextField(
                            value = typedText,
                            onValueChange = { typed: String ->
                                typedText = typed
                                filter(typedText)
                                onQueryChanged(UiAction.Search(typedText))
                            },
                            /*placeholder = "Type here!",*/
                            singleLine = true,
                            /*cursorColor = MaterialTheme.colors.primary.copy(alpha = 0.5f),*/
                            textStyle = MaterialTheme.typography.subtitle1.copy(color = Color.White),
                            modifier = Modifier
                                .constrainAs(ed) {
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                    if (uiState.value.query.isNotEmpty()) {
                                        end.linkTo(clearBtn.start)
                                    } else {
                                        end.linkTo(parent.end)
                                    }
                                    bottom.linkTo(parent.bottom)
                                    width = Dimension.fillToConstraints
                                }
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    // isSearching = focusState.isFocused
                                }
                                .padding(start = 16.dp)
                        )

                        if (uiState.value.query.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .constrainAs(clearBtn) {
                                        end.linkTo(parent.end)
                                        top.linkTo(parent.top)
                                        bottom.linkTo(parent.bottom)
                                        start.linkTo(ed.end)
                                        width = Dimension.wrapContent
                                    }
                                //.padding(end = 8.dp)

                            ) {
                                IconButton(
                                    onClick = {
                                        typedText = ""
                                        onQueryChanged(
                                            UiAction.Search(
                                                typedText
                                            )
                                        )
                                        focusRequester.requestFocus()
                                        filter(typedText)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Clear Search",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colors.primaryVariant,
                                                shape = CircleShape
                                            )
                                            .padding(2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

            }
            if (!uiState.value.loading && uiState.value.query.isNotEmpty() && uiState.value.totalResults != 0) {
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        //text = "Showing ${uiState.value.totalResults} results for \"${uiState.value.query}\"!",
                        text = "About ${uiState.value.totalResults} results!",
                        style = MaterialTheme.typography.body1.copy(
                            color = MaterialTheme.colors.onSurface.copy(
                                ContentAlpha.medium
                            )
                        ),
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp)
                    )
                }
            }
        }

    }
}

@Composable
fun PhotosList(
    uiState: State<UiState>,
    photos: Flow<PagingData<Photo>>,
    uiActions: ((UiAction) -> Unit)? = null,
    onDownload: (Photo) -> Unit,
    onClickImage: (Photo) -> Unit
) {
    val lazyPhotoItems: LazyPagingItems<Photo> = photos.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState
    ) {
        items(lazyPhotoItems) { photoItem ->
            PhotoItem(photo = photoItem!!, onDownload = onDownload, onClickImage = onClickImage)
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
                            CircularProgressIndicator(color = MaterialTheme.colors.secondary)
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
                            CircularProgressIndicator(color = MaterialTheme.colors.secondary)
                        }
                    }
                }
                loadState.refresh is LoadState.Error -> {
                    uiActions?.invoke(UiAction.UpdateTotalResults(totalResults = 0))
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
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = "RETRY", modifier = Modifier.padding(12.dp))
                            }
                        }
                    }
                }
                loadState.append is LoadState.NotLoading
                        && loadState.append.endOfPaginationReached
                        && uiState.value.query.isNotEmpty() -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Looks like you've reached the end!",
                                modifier = Modifier.padding(16.dp)
                            )
                            Spacer(
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
                uiState.value.query.isEmpty() -> {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillParentMaxSize()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Promoter"
                                )
                                Text(
                                    text = "Search something!",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoItem(
    photo: Photo,
    modifier: Modifier = Modifier,
    onClickImage: (Photo) -> Unit,
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
                            data = photo.src?.large!!,
                            builder = {
                                transformations(RoundedCornersTransformation(0f))
                                crossfade(true)
                            },
                        ),
                        contentDescription = photo.alt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1F)
                            .combinedClickable(
                                onClick = { onClickImage(photo) },
                                onLongClick = { expanded = !expanded }
                            ),
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

@Composable
fun detailViewModel(photoId: Int): DetailViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        MainActivity.ViewModelFactoryProvider::class.java
    ).detailViewModelFactory()

    return viewModel(factory = DetailViewModel.provideFactory(factory, photoId = photoId))
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Favorites : Screen("favorites")
    object Detail : Screen("detail") {
        // const val ARG_PHOTO_ID = "com.harishtk.app.wallpick.args.PHOTO_ID"
        const val ARG_PHOTO_ID = "photoId"
    }
}

val DEFAULT_NAV_HOST: String = Screen.Home.route