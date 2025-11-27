package com.example.gamezone.ui.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun GenreSelection(
    options: List<String>,
    selectedGenres: Set<String>,
    onSelectionChange: (String, Boolean) -> Unit
) {
    val columns = options.chunked((options.size + 1) / 2)

    Row(modifier = Modifier.fillMaxWidth()) {
        columns.forEach { columnGenres ->
            Column(modifier = Modifier.weight(1f)) {
                columnGenres.forEach { genre ->
                    GenreCheckbox(
                        text = genre,
                        checked = genre in selectedGenres,
                        onCheckedChange = { onSelectionChange(genre, it) }
                    )
                }
            }
        }
        if (columns.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun GenreCheckbox(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}
