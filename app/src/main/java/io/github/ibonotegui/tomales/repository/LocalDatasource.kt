package io.github.ibonotegui.tomales.repository

import io.github.ibonotegui.tomales.model.Item

class LocalDatasource : Datasource {
    override suspend fun getItemList(): List<Item> {
        val itemList = mutableListOf<Item>()
        for (i in 1..16) {
            itemList.add(Item(id = i, listId = i % 4, name = "Item $i"))
        }
        return itemList
    }
}
