package com.example.carwash.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carwash.ui.theme.OnSurfaceVariantDark
import com.example.carwash.ui.theme.OrangePrimary
import com.example.carwash.ui.theme.SurfaceDark

data class ChecklistItem(
    val id: String,
    val label: String
)

/**
 * Reusable multi-select bottom sheet for checklist-style flows
 * (staff selection, quality checklist, etc.).
 *
 * Uses a constrained height + LazyColumn + nestedScroll to prevent the
 * ModalBottomSheet drag gesture from fighting with content scrolling.
 *
 * @param title           Header text shown at the top.
 * @param items           Selectable checklist items.
 * @param emptyMessage    Shown when [items] is empty (e.g. "No staff available").
 * @param buttonText      Label for the confirm button.
 * @param buttonEnabled   Predicate that decides if the button is enabled based on current selection.
 * @param isSubmitting    When true, shows a spinner inside the button and disables it.
 * @param onDismiss       Called when the sheet is dismissed.
 * @param onConfirm       Called with the set of selected item IDs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistBottomSheet(
    title: String,
    items: List<ChecklistItem>,
    emptyMessage: String? = null,
    buttonText: String,
    buttonEnabled: (selectedIds: Set<String>) -> Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (selectedIds: Set<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val consumeScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = Offset(0f, available.y)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.7f)) {
            // Title
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)

            if (items.isEmpty() && emptyMessage != null) {
                // Empty state
                Text(
                    text = emptyMessage,
                    color = OnSurfaceVariantDark,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                // Scrollable checklist
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .nestedScroll(consumeScrollConnection)
                ) {
                    items(items, key = { it.id }) { item ->
                        val isChecked = item.id in selectedIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedIds = if (isChecked) selectedIds - item.id else selectedIds + item.id
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    selectedIds = if (isChecked) selectedIds - item.id else selectedIds + item.id
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = OrangePrimary,
                                    uncheckedColor = OnSurfaceVariantDark
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = item.label,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
                    }
                }
            }

            // Confirm button (always pinned at the bottom)
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f), thickness = 0.5.dp)
            Button(
                onClick = { onConfirm(selectedIds) },
                enabled = buttonEnabled(selectedIds) && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = buttonText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
