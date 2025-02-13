package io.github.ibonotegui.tomales.repository

import io.github.ibonotegui.tomales.api.ApiClient
import io.github.ibonotegui.tomales.model.Item

class NetworkDatasource : Datasource {
    override suspend fun getItemList() : List<Item> {
        val response = ApiClient.getTomalesAPI().getItemList().execute()
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
