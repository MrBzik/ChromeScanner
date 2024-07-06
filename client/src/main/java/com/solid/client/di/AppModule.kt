package com.solid.client.di

import android.content.Context
import android.content.SharedPreferences
import com.solid.client.data.remote.KtorServerConnector
import com.solid.client.data.remote.ServerConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideHttpClient() : HttpClient {
        return HttpClient(CIO){
            install(WebSockets)
            install(ContentNegotiation) {
                json()
            }
        }
    }


    @Provides
    @Singleton
    fun provideServerConnector(client: HttpClient) : ServerConnector {
        return KtorServerConnector(client)
    }


    @Provides
    @Singleton
    fun providesConfigPrefs(@ApplicationContext app : Context) : SharedPreferences {
        return app.getSharedPreferences("config_prefs", Context.MODE_PRIVATE)
    }

}