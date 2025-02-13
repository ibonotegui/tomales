package io.github.ibonotegui.tomales.repository

import io.github.ibonotegui.tomales.model.Item

interface Datasource {
    suspend fun getItemList(): List<Item>
}
