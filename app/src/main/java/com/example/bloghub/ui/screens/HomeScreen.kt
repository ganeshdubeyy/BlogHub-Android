package com.example.bloghub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloghub.data.BlogCategory
import com.example.bloghub.data.BlogModel
import com.example.bloghub.ui.components.AppDrawer
import com.example.bloghub.ui.components.BlogCard
import com.example.bloghub.ui.components.CategoryFilterBar
import com.example.bloghub.ui.components.ConfirmationDialog
import com.example.bloghub.viewmodel.AuthViewModel
import com.example.bloghub.viewmodel.BlogViewModel
import com.example.bloghub.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    blogViewModel: BlogViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = composeViewModel(),
    onNavigateToProfile: () -> Unit,
    onNavigateToAddPost: () -> Unit,
    onNavigateToEditPost: (String) -> Unit,
    onNavigateToMyBlogs: () -> Unit,
    onNavigateToBlogDetail: (String) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val blogUiState by blogViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Load notifications
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { notificationViewModel.loadNotifications(it) }
    }
    
    // Category filter state
    var selectedCategory by remember { mutableStateOf<BlogCategory?>(null) }
    
    // Delete confirmation dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<String?>(null) }
    
    // Filter posts based on selected category
    val filteredPosts = remember(blogUiState.allPosts, selectedCategory) {
        if (selectedCategory == null) {
            blogUiState.allPosts
        } else {
            blogUiState.allPosts.filter { it.categoryEnum == selectedCategory }
        }
    }

    // This observer will trigger every time the screen's lifecycle state changes.
    // We explicitly call loadAllPosts() when the screen is RESUMED.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Clear any stale errors from other screens (like MyBlogsScreen)
                blogViewModel.clearError()
                blogViewModel.loadAllPosts()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        // When the composable leaves the screen, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentUser = currentUser,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToMyBlogs = onNavigateToMyBlogs,
                onNavigateToAddPost = onNavigateToAddPost,
                onLogout = { authViewModel.signOut() },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text("BlogHub") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToMyBlogs) {
                            Icon(
                                imageVector = Icons.Default.Article,
                                contentDescription = "Navigate to My Blogs"
                            )
                        }
                        // Notification Bell with Badge
                        BadgedBox(
                            badge = {
                                if (notificationUiState.unreadCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFEC4899), // Pink color
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (notificationUiState.unreadCount > 99) "99+" else notificationUiState.unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = onNavigateToNotifications) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Menu"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onNavigateToAddPost) {
                    Icon(Icons.Default.Add, contentDescription = "Add New Post")
                }
            }
        ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category Filter Bar
            CategoryFilterBar(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            // Content Area
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (blogUiState.allPostsLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (blogUiState.error != null) {
                    Text(
                        text = blogUiState.error ?: "An unknown error occurred",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                } else if (filteredPosts.isEmpty()) {
                    Text(
                        text = if (selectedCategory == null) {
                            "No posts yet. Be the first to create one!"
                        } else {
                            "No posts in ${selectedCategory?.displayName} category yet."
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredPosts) { post ->
                            BlogPostItem(
                                post = post,
                                currentUserId = currentUser?.uid,
                                onPostClick = { onNavigateToBlogDetail(post.id) },
                                onEditClick = { onNavigateToEditPost(post.id) },
                                onDeleteClick = {
                                    postToDelete = post.id
                                    showDeleteDialog = true
                                },
                                onLikeClick = { blogViewModel.toggleLike(post.id) }
                            )
                        }
                    }
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
fun BlogPostItem(
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
        if (post.author?.uid == currentUserId) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
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
}
