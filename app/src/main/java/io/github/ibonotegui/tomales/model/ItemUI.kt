package io.github.ibonotegui.tomales.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class ItemUI(val item: Item, var isFavorite: MutableState<Boolean> = mutableStateOf(false))
