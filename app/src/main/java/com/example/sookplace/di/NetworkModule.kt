package com.example.sookplace.di

import android.content.Context
import com.example.sookplace.data.local.TokenManager
import com.example.sookplace.data.local.dao.FeaturedRestaurantDao
import com.example.sookplace.data.local.dao.PostDao
import com.example.sookplace.data.local.dao.RestaurantDao
import com.example.sookplace.data.local.dao.PlaceDao
import com.example.sookplace.data.local.dao.UserProfileDao
import com.example.sookplace.data.local.db.AppDatabase
import com.example.sookplace.data.remote.api.AuthApi
import com.example.sookplace.data.remote.api.CommunityApi
import com.example.sookplace.data.remote.api.RestaurantApi
import com.example.sookplace.data.remote.api.RouletteSpinApi
import com.example.sookplace.data.remote.api.SearchApi
import com.example.sookplace.data.remote.api.UserProfileApi
import com.example.sookplace.data.remote.auth.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://your.api.base.url" // <-- 실제 주소로 교체

    // AppDatabase
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    //LoggingInterceptor 제공
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val log = HttpLoggingInterceptor()
        log.level = HttpLoggingInterceptor.Level.BODY
        return log
    }

    //AuthInterceptor 제공 (토큰 자동 포함)
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        userDao: UserProfileDao,
        authApi: javax.inject.Provider<AuthApi>
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager, userDao, authApi)
    }

    //OkHttpClient 제공 (Interceptor 포함)
    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor) // <-- 여기서 토큰 자동 추가
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    //Retrofit 제공
    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //Api 제공
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserProfileApi(retrofit: Retrofit): UserProfileApi {
        return retrofit.create(UserProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRestaurantApi(retrofit: Retrofit): RestaurantApi {
        return retrofit.create(RestaurantApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRouletteSpinApi(retrofit: Retrofit): RouletteSpinApi {
        return retrofit.create(RouletteSpinApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSearchApi(retrofit: Retrofit): SearchApi {
        return retrofit.create(SearchApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCommunityApi(retrofit: Retrofit): CommunityApi {
        return retrofit.create(CommunityApi::class.java)
    }

    // Dao
    @Provides
    @Singleton
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideRestaurantDao(database: AppDatabase): RestaurantDao = database.restaurantDao()

    @Provides
    @Singleton
    fun providePostDao(database: AppDatabase): PostDao = database.postDao()

    @Provides
    @Singleton
    fun provideFeaturedRestaurantDao(database: AppDatabase): FeaturedRestaurantDao {
        return database.featuredRestaurantDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferenceDao(database: AppDatabase): PlaceDao {
        return database.userPreferenceDao()
    }
}
