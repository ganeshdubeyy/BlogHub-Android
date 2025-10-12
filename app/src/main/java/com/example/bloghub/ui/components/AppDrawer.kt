package com.example.bloghub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bloghub.auth.UserModel

@Composable
fun AppDrawer(
    currentUser: UserModel?,
    onNavigateToProfile: () -> Unit,
    onNavigateToMyBlogs: () -> Unit,
    onNavigateToAddPost: () -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    // Logout confirmation dialog state
    var showLogoutDialog by remember { mutableStateOf(false) }
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {
        // Header Section with User Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Profile Picture
            if (!currentUser?.profileImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = currentUser?.profileImageUrl,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
            } else {
                // Fallback: First letter of name
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.name?.firstOrNull()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name
            Text(
                text = currentUser?.name ?: "User",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            // User Email
            Text(
                text = currentUser?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }

        Divider()

        // Navigation Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // My Profile
            DrawerMenuItem(
                icon = Icons.Default.Person,
                label = "My Profile",
                onClick = {
                    onCloseDrawer()
                    onNavigateToProfile()
                }
            )

            // My Blogs
            DrawerMenuItem(
                icon = Icons.Default.Article,
                label = "My Blogs",
                onClick = {
                    onCloseDrawer()
                    onNavigateToMyBlogs()
                }
            )

            // Create Blog
            DrawerMenuItem(
                icon = Icons.Default.Add,
                label = "Create Blog",
                onClick = {
                    onCloseDrawer()
                    onNavigateToAddPost()
                }
            )
        }

        Divider()

        // Logout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            DrawerMenuItem(
                icon = Icons.Default.ExitToApp,
                label = "Logout",
                onClick = {
                    showLogoutDialog = true
                }
            )
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        ConfirmationDialog(
            title = "Logout",
            message = "Are you sure you want to Log Out?",
            onConfirm = {
                showLogoutDialog = false
                onCloseDrawer()
                onLogout()
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
