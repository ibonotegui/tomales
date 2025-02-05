package io.github.ibonotegui.tomales.repository

class Repository(private val datasource: Datasource) {
    suspend fun getItemList() = datasource.getItemList()
}
