package com.example.bloghub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bloghub.data.BlogModel
import com.example.bloghub.ui.Routes
import com.example.bloghub.ui.components.BlogCard
import com.example.bloghub.ui.components.ConfirmationDialog
import com.example.bloghub.viewmodel.BlogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBlogsScreen(
    blogViewModel: BlogViewModel,
    onNavigate: (String) -> Unit,
    onNavigateToBlogDetail: (String) -> Unit,
    currentUserId: String?
) {
    val uiState by blogViewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<String?>(null) }

    // This observer will trigger every time the screen's lifecycle state changes.
    // We explicitly call loadMyPosts() when the screen is RESUMED.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Clear any stale errors from other screens
                blogViewModel.clearError()
                blogViewModel.loadMyPosts()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // When the composable leaves the screen, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Posts") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- FIX 1: Use the correct loading state for this screen ---
            if (uiState.myPostsLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // Show error if there is one
            else if (uiState.error != null) {
                Text(
                    text = uiState.error ?: "An unknown error occurred",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            // --- FIX 2: Check the correct list for the empty state ---
            else if (uiState.myPosts.isEmpty()) {
                Text(
                    text = "You haven't written any posts yet.\nTap the '+' button on the Home screen to get started!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- FIX 3: Use the correct list for this screen ---
                    items(uiState.myPosts) { post ->
                        MyBlogPostItem(
                            post = post,
                            currentUserId = currentUserId,
                            onPostClick = {
                                onNavigateToBlogDetail(post.id)
                            },
                            onEditClick = {
                                onNavigate(Routes.AddBlog + "?postId=${post.id}")
                            },
                            onDeleteClick = {
                                postToDelete = post.id
                                showDeleteDialog = true
                            },
                            onLikeClick = {
                                blogViewModel.toggleLike(post.id)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete Blog",
            message = "Are you sure you want to delete this Blog?",
            onConfirm = {
                postToDelete?.let { blogViewModel.deletePost(it) }
                showDeleteDialog = false
                postToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                postToDelete = null
            }
        )
    }
}

@Composable
private fun MyBlogPostItem(
    post: BlogModel,
    currentUserId: String?,
    onPostClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    Column {
        BlogCard(
            post = post,
            onClick = onPostClick,
            currentUserId = currentUserId,
            onLikeClick = onLikeClick
        )
        // Since this whole screen is "My Blogs", the buttons are always shown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Post"
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Post",
                    tint = Color.Red
                )
            }
        }
    }
}
