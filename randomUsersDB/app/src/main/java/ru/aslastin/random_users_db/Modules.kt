package ru.aslastin.random_users_db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    fun provideUserDao(database: UserDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): UserDatabase {
        return Room.databaseBuilder(
            appContext.applicationContext,
            UserDatabase::class.java,
            "database-users")
            .fallbackToDestructiveMigration()
            .build()
    }
}

@InstallIn(SingletonComponent::class)
@Module
object ServiceModule {

    @Provides
    @Singleton
    fun provideService(): UserService {
        return Retrofit.Builder()
            .baseUrl(UserService.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(UserService::class.java)
    }
}
