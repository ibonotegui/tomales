package io.github.ibonotegui.tomales.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun <T> SwipeToDeleteContainer(
    item: T, onDelete: (T) -> Unit, animationDuration: Int = 600, content: @Composable (T) -> Unit
) {
    var isDeleted by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        if (value == SwipeToDismissBoxValue.EndToStart) {
            isDeleted = true
            true
        } else {
            false
        }
    }, positionalThreshold = { it * .25f })

    LaunchedEffect(key1 = isDeleted) {
        if (isDeleted) {
            delay(animationDuration.toLong())
            onDelete(item)
        }
    }

    AnimatedVisibility(
        visible = !isDeleted, exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration), shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(state = dismissState, backgroundContent = {
            DeleteItemRow(swipeToDismissBoxState = dismissState, onDelete = {
                isDeleted = true
            })
        }, enableDismissFromEndToStart = true, enableDismissFromStartToEnd = false, content = {
            content(item)
        })
    }
}

@Composable
fun DeleteItemRow(swipeToDismissBoxState: SwipeToDismissBoxState, onDelete: () -> Unit) {
    var longPressed by remember { mutableStateOf(false) }
    val color = if (longPressed) {
        Color.Red
    } else {
        when (swipeToDismissBoxState.dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd -> Color.Green
            SwipeToDismissBoxValue.EndToStart -> Color.Red
            SwipeToDismissBoxValue.Settled -> Color.Transparent
        }
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    longPressed = false
                }, onLongPress = {
                    longPressed = true
                })
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Icon(Icons.Default.Delete, //Icons.Outlined.Delete
            contentDescription = "delete item",
            tint = MaterialTheme.colorScheme.background,
            modifier = Modifier.clickable {
                onDelete()
            })
    }
}
