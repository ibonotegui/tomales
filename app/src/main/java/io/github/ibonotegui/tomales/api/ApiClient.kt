package io.github.ibonotegui.tomales.api

import com.squareup.moshi.Moshi
import io.github.ibonotegui.tomales.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = "https://ibonotegui.github.io/labs/"
const val TIME_OUT_SECONDS = 10L

object ApiClient {

    fun getTomalesAPI(retrofit: Retrofit): TomalesAPI = retrofit.create(TomalesAPI::class.java)

    fun getOkHttpClient(): OkHttpClient.Builder {
        var client = OkHttpClient().newBuilder()
        client = client.readTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
        client = client.connectTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
        client = client.writeTimeout(TIME_OUT_SECONDS, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            client = client.addInterceptor(loggingInterceptor)
        }
        return client
    }

    fun getRetrofitInstance(client: OkHttpClient.Builder): Retrofit {
        val moshi = Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
        return Retrofit.Builder().client(client.build()).baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi)).build()
    }

}
