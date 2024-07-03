package com.solid.server.di

import android.content.Context
import com.solid.server.data.local.database.ScansDB
import com.solid.server.data.local.database.SqLightDb
import com.solid.server.shell.ChromeFileScannerWuImpl
import com.solid.server.shell.ChromeFilesScanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    fun providesFileScanner(@ApplicationContext app : Context) : ChromeFilesScanner {
        return ChromeFileScannerWuImpl(app)
    }

    @Provides
    @ServiceScoped
    fun providesScansDB(@ApplicationContext app : Context) : ScansDB {
        return SqLightDb(app)
    }

}