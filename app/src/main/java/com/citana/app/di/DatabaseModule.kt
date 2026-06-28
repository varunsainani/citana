package com.citana.app.di

import android.content.Context
import androidx.room.Room
import com.citana.app.data.local.CategoryDao
import com.citana.app.data.local.CitanaDb
import com.citana.app.data.local.ProviderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun db(@ApplicationContext context: Context): CitanaDb =
        Room.databaseBuilder(context, CitanaDb::class.java, "citana.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun categoryDao(db: CitanaDb): CategoryDao = db.categoryDao()

    @Provides
    fun providerDao(db: CitanaDb): ProviderDao = db.providerDao()
}
