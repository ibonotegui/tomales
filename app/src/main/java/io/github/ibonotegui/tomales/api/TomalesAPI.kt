package io.github.ibonotegui.tomales.api

import io.github.ibonotegui.tomales.model.Item
import retrofit2.Call
import retrofit2.http.GET

interface TomalesAPI {
    @GET("hiring.json")
    fun getItemList(): Call<List<Item>>
}
