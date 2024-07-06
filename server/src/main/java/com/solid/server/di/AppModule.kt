package com.solid.server.di

import android.content.Context
import android.content.SharedPreferences
import com.solid.server.shell.ShellHelper
import com.solid.server.shell.TopWuShell
import com.solid.server.utils.PORT_CONFIG
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    @Named(PORT_CONFIG)
    fun providePortConfigSharedPrefs(
        @ApplicationContext app : Context
    ) : SharedPreferences {
        return app.getSharedPreferences("port_config", Context.MODE_PRIVATE)
    }


    @Provides
    @Singleton
    fun providesShellHelper() : ShellHelper {
        return TopWuShell()
    }

}