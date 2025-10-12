package com.example.bloghub.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bloghub.ui.screens.*
import com.example.bloghub.viewmodel.AuthViewModel
import com.example.bloghub.viewmodel.BlogViewModel
import com.example.bloghub.viewmodel.ProfileViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    val startDestination = if (authState == null) "auth_flow" else "main_flow"

    NavHost(navController = navController, startDestination = startDestination) {

        // --- AUTHENTICATION FLOW ---
        navigation(startDestination = Routes.Login, route = "auth_flow") {
            composable(Routes.Login) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("main_flow") {
                            popUpTo("auth_flow") { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Routes.SignUp)
                    }
                )
            }
            composable(Routes.SignUp) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate("main_flow") {
                            popUpTo("auth_flow") { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // --- MAIN APP FLOW ---
        navigation(startDestination = Routes.Home, route = "main_flow") {
            composable(Routes.Home) { backStackEntry ->
                // Get the nav graph's back stack entry
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                // Get the ViewModel using the parent's ViewModelStoreOwner
                val blogViewModel: BlogViewModel = viewModel(viewModelStoreOwner = parentEntry)

                HomeScreen(
                    blogViewModel = blogViewModel,
                    onNavigateToProfile = {
                        navController.navigate("profile_flow")
                    },
                    onNavigateToAddPost = {
                        navController.navigate(Routes.AddBlog)
                    },
                    onNavigateToEditPost = { postId ->
                        navController.navigate(Routes.AddBlog + "?postId=$postId")
                    },
                    onNavigateToMyBlogs = {
                        navController.navigate(Routes.MyBlogs)
                    },
                    onNavigateToBlogDetail = { postId ->
                        navController.navigate(Routes.BlogDetail + "/$postId")
                    },
                    onNavigateToNotifications = {
                        navController.navigate(Routes.Notifications)
                    }
                )
            }

            composable(
                route = Routes.AddBlog + "?postId={postId}",
                arguments = listOf(navArgument("postId") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                val blogViewModel: BlogViewModel = viewModel(viewModelStoreOwner = parentEntry)
                val postId = backStackEntry.arguments?.getString("postId")

                AddEditBlogScreen(
                    blogViewModel = blogViewModel,
                    postId = postId,
                    onPostSaved = { navController.popBackStack() }
                )
            }

            composable(Routes.MyBlogs) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                val blogViewModel: BlogViewModel = viewModel(viewModelStoreOwner = parentEntry)
                val currentUserId by authViewModel.authState.collectAsState()

                MyBlogsScreen(
                    blogViewModel = blogViewModel,
                    onNavigate = { route -> navController.navigate(route) },
                    onNavigateToBlogDetail = { postId ->
                        navController.navigate(Routes.BlogDetail + "/$postId")
                    },
                    currentUserId = currentUserId?.uid
                )
            }

            composable(
                route = Routes.BlogDetail + "/{postId}",
                arguments = listOf(navArgument("postId") {
                    type = NavType.StringType
                })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                val blogViewModel: BlogViewModel = viewModel(viewModelStoreOwner = parentEntry)
                val postId = backStackEntry.arguments?.getString("postId") ?: ""

                BlogDetailScreen(
                    postId = postId,
                    blogViewModel = blogViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.Notifications) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                val notificationViewModel: com.example.bloghub.viewmodel.NotificationViewModel = viewModel(viewModelStoreOwner = parentEntry)
                val authViewModel: AuthViewModel = viewModel()
                val currentUser by authViewModel.currentUser.collectAsState()

                NotificationScreen(
                    notificationViewModel = notificationViewModel,
                    currentUserId = currentUser?.uid,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPost = { postId ->
                        navController.navigate(Routes.BlogDetail + "/$postId")
                    }
                )
            }

            // --- NESTED PROFILE FLOW (uses the same technique) ---
            navigation(startDestination = Routes.Profile, route = "profile_flow") {
                composable(Routes.Profile) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("profile_flow")
                    }
                    val profileViewModel: ProfileViewModel = viewModel(viewModelStoreOwner = parentEntry)

                    ProfileScreen(
                        profileViewModel = profileViewModel,
                        onNavigateToEditProfile = {
                            navController.navigate(Routes.EditProfile)
                        },
                        onLogout = {
                            authViewModel.signOut()
                            navController.navigate("auth_flow") {
                                popUpTo("main_flow") { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.EditProfile) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("profile_flow")
                    }
                    val profileViewModel: ProfileViewModel = viewModel(viewModelStoreOwner = parentEntry)

                    EditProfileScreen(
                        profileViewModel = profileViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
