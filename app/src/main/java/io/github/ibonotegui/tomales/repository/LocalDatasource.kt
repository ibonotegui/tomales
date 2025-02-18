package io.github.ibonotegui.tomales.repository

import io.github.ibonotegui.tomales.model.Item
import kotlin.random.Random

class LocalDatasource : Datasource {
    override suspend fun getItemList(): List<Item> {
        val itemList = mutableListOf<Item>()
        for (i in 1..10) {
            val isFavorite = Random.nextInt(10) % 2 == 0
            itemList.add(Item(id = i, listId = i % 4, name = "Item $i", isFavorite = isFavorite))
        }
        return  itemList
    }
}
