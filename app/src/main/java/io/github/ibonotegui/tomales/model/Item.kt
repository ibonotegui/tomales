package io.github.ibonotegui.tomales.model

data class Item(val id: Int, val listId: Int, val name: String?, var isFavorite: Boolean = false)
