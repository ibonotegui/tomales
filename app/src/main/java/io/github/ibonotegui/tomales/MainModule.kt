package io.github.ibonotegui.tomales

import io.github.ibonotegui.tomales.api.ApiClient
import org.koin.dsl.module
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import io.github.ibonotegui.tomales.repository.NetworkDatasource
import io.github.ibonotegui.tomales.repository.LocalDatasource
import io.github.ibonotegui.tomales.repository.Datasource
import io.github.ibonotegui.tomales.repository.Repository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind

val mainModule = module {

    single { ApiClient.getOkHttpClient() }
    single { ApiClient.getRetrofitInstance(get()) }
    single { ApiClient.getTomalesAPI(get()) }

    //singleOf(::NetworkDatasource).bind<Datasource>()
//    single {
//        NetworkDatasource(get())
//    }.bind<Datasource>()

    // last module overwrites
    singleOf(::LocalDatasource).bind<Datasource>()
//    single {
//        LocalDatasource()
//    }.bind<Datasource>()

    singleOf(::Repository)
//    single {
//        Repository(get())
//    }

    viewModel {
        MainViewModel(get())
    }
}
