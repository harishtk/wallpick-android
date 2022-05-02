package com.harishtk.app.wallpick.ui.screens.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.harishtk.app.wallpick.PhotoItem
import com.harishtk.app.wallpick.PhotosList
import com.harishtk.app.wallpick.UiAction
import com.harishtk.app.wallpick.UiState
import com.harishtk.app.wallpick.data.entity.Photo
import com.harishtk.app.wallpick.ui.screens.favorites.FavoriteViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

@Composable
fun FavoritesScreen(navController: NavController, viewModel: FavoriteViewModel = hiltViewModel()) {

    val favPagedFlow = viewModel.pagingFavoritePhotosFlow

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
            Text(
                text = "Recent",
                style = MaterialTheme.typography.h1,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.secondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // TODO: modify for UiState
        PhotosList(photos = favPagedFlow,
            onDownload = { photo ->

        },
        onClickImage = { photo ->

        })
    }
}

@Composable
fun PhotosList(
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
            }
        }
    }
}
