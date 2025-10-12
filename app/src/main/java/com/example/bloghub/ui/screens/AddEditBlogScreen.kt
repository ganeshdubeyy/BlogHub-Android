package com.example.bloghub.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest // Correct import
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.bloghub.data.BlogCategory
import com.example.bloghub.ui.components.RoundedButton
import com.example.bloghub.viewmodel.BlogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBlogScreen(
	blogViewModel: BlogViewModel,
	postId: String?, // Null for "Create" mode, non-null for "Edit" mode
	onPostSaved: () -> Unit
) {
	val uiState by blogViewModel.uiState.collectAsState()
	val context = LocalContext.current

	val isEditMode = postId != null

	// --- FIX 1: Find the post in EITHER list ---
	// Search both allPosts and myPosts to find the one we need to edit.
	// The 'find' will stop as soon as it gets a match.
	val existingPost = if (isEditMode) {
		(uiState.allPosts + uiState.myPosts).find { it.id == postId }
	} else {
		null
	}

	var title by remember { mutableStateOf("") }
	var content by remember { mutableStateOf("") }
	var selectedCategory by remember { mutableStateOf(BlogCategory.GENERAL) }
	var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
	var existingImageUrl by remember { mutableStateOf<String?>(null) }
	var categoryDropdownExpanded by remember { mutableStateOf(false) }

	val photoPickerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia(),
		onResult = { uri ->
			selectedImageUri = uri
		}
	)

	// This part is now correct because `existingPost` is found correctly
	LaunchedEffect(existingPost) {
		if (isEditMode && existingPost != null) {
			title = existingPost.title
			content = existingPost.content
			selectedCategory = existingPost.categoryEnum
			existingImageUrl = existingPost.imageUrl
		}
	}

	// This checks the shared `postSaved` flag, which is correct
	LaunchedEffect(uiState.postSaved) {
		if (uiState.postSaved) {
			Toast.makeText(context, "Post saved successfully!", Toast.LENGTH_SHORT).show()
			blogViewModel.onPostSavedConsumed()
			blogViewModel.loadAllPosts()  // Refresh the lists after saving
			blogViewModel.loadMyPosts()   // Refresh the lists after saving
			onPostSaved()
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(title = { Text(if (isEditMode) "Edit Blog" else "Create Blog") })
		}
	) { padding ->
		Column(
			modifier = Modifier
				.padding(padding)
				.padding(16.dp)
				.verticalScroll(rememberScrollState())
		) {

			OutlinedTextField(
				value = title,
				onValueChange = { title = it },
				label = { Text("Blog title") },
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(16.dp))

			// Category Dropdown
			ExposedDropdownMenuBox(
				expanded = categoryDropdownExpanded,
				onExpandedChange = { categoryDropdownExpanded = it }
			) {
				OutlinedTextField(
					value = selectedCategory.displayName,
					onValueChange = {},
					readOnly = true,
					label = { Text("Category") },
					trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
					colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
					modifier = Modifier
						.fillMaxWidth()
						.menuAnchor()
				)
				ExposedDropdownMenu(
					expanded = categoryDropdownExpanded,
					onDismissRequest = { categoryDropdownExpanded = false }
				) {
					BlogCategory.values().forEach { category ->
						DropdownMenuItem(
							text = { Text(category.displayName) },
							onClick = {
								selectedCategory = category
								categoryDropdownExpanded = false
							}
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			Surface(
				modifier = Modifier
					.fillMaxWidth()
					.height(180.dp)
					.clip(RoundedCornerShape(12.dp))
					.clickable {
						photoPickerLauncher.launch(
							PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
						)
					},
				shape = RoundedCornerShape(12.dp),
				color = MaterialTheme.colorScheme.surfaceVariant,
				border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
			) {
				val imageToShow = selectedImageUri ?: existingImageUrl

				if (imageToShow != null) {
					AsyncImage(
						model = imageToShow,
						contentDescription = "Featured image",
						contentScale = ContentScale.Crop,
						modifier = Modifier.fillMaxSize()
					)
				} else {
					Box(contentAlignment = Alignment.Center) {
						Row(verticalAlignment = Alignment.CenterVertically) {
							Icon(
								imageVector = Icons.Default.AddAPhoto,
								contentDescription = "Add Photo",
								tint = MaterialTheme.colorScheme.onSurfaceVariant
							)
							Spacer(modifier = Modifier.size(8.dp))
							Text(
								text = "Tap to add featured image",
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			OutlinedTextField(
				value = content,
				onValueChange = { content = it },
				label = { Text("Blog content") },
				modifier = Modifier
					.fillMaxWidth(),
				minLines = 8
			)

			Spacer(modifier = Modifier.height(24.dp))

			// --- FIX 2: Check either loading state ---
			// The button should be disabled if either list is currently loading.
			val isLoading = uiState.allPostsLoading || uiState.myPostsLoading

			RoundedButton(
				text = if (isEditMode) "Update Post" else "Publish",
				onClick = {
					if (title.isNotBlank() && content.isNotBlank()) {
						if (isEditMode && postId != null) {
							blogViewModel.updatePost(postId, title, content, selectedCategory.name, selectedImageUri)
						} else {
							blogViewModel.createPost(title, content, selectedCategory.name, selectedImageUri)
						}
					} else {
						Toast
							.makeText(
								context,
								"Title and content cannot be empty.",
								Toast.LENGTH_SHORT
							)
							.show()
					}
				},
				enabled = !isLoading, // Use the new combined loading flag
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}
