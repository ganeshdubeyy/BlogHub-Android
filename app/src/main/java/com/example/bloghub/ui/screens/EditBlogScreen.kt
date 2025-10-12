package com.example.bloghub.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import com.example.bloghub.ui.components.RoundedButton

@Composable
fun EditBlogScreen(onNavigate: (String) -> Unit) {
    val scrollState = rememberScrollState()
    var showConfirm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("How to build a Compose Blog") }
    var content by remember {
        mutableStateOf(
            "Compose lets you build native Android UIs with less code. In this post, we refactor our BlogHub app to use Material3 components, state hoisting, and clean layouts..."
        )
    }
    val categories = remember { listOf("Tech", "Design", "Travel", "Food", "Lifestyle") }
    var selectedCategories by remember { mutableStateOf(setOf("Tech", "Design")) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Edit Blog",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Blog title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Featured image upload placeholder (pre-filled state implied)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable { /* TODO: replace image in Firebase step */ },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant ?: MaterialTheme.colorScheme.outline)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Tap to change featured image",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowChips(
            items = categories,
            selected = selectedCategories,
            onToggle = { label ->
                selectedCategories = if (selectedCategories.contains(label)) {
                    selectedCategories - label
                } else {
                    selectedCategories + label
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Blog content") },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            minLines = 6
        )

        Spacer(modifier = Modifier.height(20.dp))

        RoundedButton(
            text = "Update",
            onClick = { showConfirm = true },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Confirm Update") },
            text = { Text("Are you sure you want to update this blog post?") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false /* TODO: call ViewModel update */ }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FlowChips(
    items: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    var rowItems = mutableListOf<String>()
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, label ->
            rowItems.add(label)
            val isRowEnd = rowItems.size == 3 || index == items.lastIndex
            if (isRowEnd) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { chipLabel ->
                        FilterChip(
                            selected = selected.contains(chipLabel),
                            onClick = { onToggle(chipLabel) },
                            label = { Text(chipLabel) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                        )
                    }
                }
                rowItems = mutableListOf()
            }
        }
    }
}


