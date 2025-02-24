package io.github.ibonotegui.tomales.repository

import io.github.ibonotegui.tomales.api.TomalesAPI
import io.github.ibonotegui.tomales.model.Item

class NetworkDatasource(val tomalesAPI: TomalesAPI) : Datasource {
    override suspend fun getItemList() : List<Item> {
        val response = tomalesAPI.getItemList().execute()
        if (response.isSuccessful) {
            return if (response.body().isNullOrEmpty()) {
                emptyList()
            } else {
                response.body() as List<Item>
            }
        } else {
            throw Exception(response.message())
        }
    }
}
