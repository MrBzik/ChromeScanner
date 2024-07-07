package com.solid.server.di

import android.content.Context
import android.content.SharedPreferences
import com.solid.server.data.local.database.ScansDB
import com.solid.server.data.local.database.SqLightDb
import com.solid.server.data.remote.KtorServer
import com.solid.server.data.remote.ScanServer
import com.solid.server.filesarchiver.ChromeFilesArchiver
import com.solid.server.filesarchiver.ChromeFilesArchiverImpl
import com.solid.server.filescanner.ChromeFileScannerImpl
import com.solid.server.filescanner.ChromeFilesScanner
import com.solid.server.repositories.ScansRepo
import com.solid.server.shell.ShellHelper
import com.solid.server.shell.TopWuShell
import com.solid.server.utils.CURRENT_FILE_SYS
import com.solid.server.utils.PORT_CONFIG
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {


    @Provides
    @ServiceScoped
    fun providesFileScanner(
        shellHelper: ShellHelper,
        repo: ScansRepo
    ) : ChromeFilesScanner {
        return ChromeFileScannerImpl(shellHelper, repo)
    }

    @Provides
    @ServiceScoped
    fun providesScansDB(@ApplicationContext app : Context) : ScansDB {
        return SqLightDb(app)
    }

    @Provides
    @ServiceScoped
    fun providesScanServer(@Named(PORT_CONFIG) portConfPref : SharedPreferences) : ScanServer {
        return KtorServer(portConfPref)
    }

    @Provides
    @ServiceScoped
    @Named(CURRENT_FILE_SYS)
    fun providePortConfigSharedPrefs(
        @ApplicationContext app : Context
    ) : SharedPreferences {
        return app.getSharedPreferences("current_file_system", Context.MODE_PRIVATE)
    }



    @Provides
    @ServiceScoped
    fun providesFileArchiver(
        @ApplicationContext app : Context,
        shellHelper: ShellHelper,
        repo: ScansRepo
    ) : ChromeFilesArchiver {

        return ChromeFilesArchiverImpl(
            app, shellHelper, repo
        )
    }

    @Provides
    @ServiceScoped
    fun providesScansRepo(
        db: ScansDB,
        @Named(CURRENT_FILE_SYS) fileSysPref: SharedPreferences
    ) : ScansRepo {
        return ScansRepo(db, fileSysPref)

    }


}