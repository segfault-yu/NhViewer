package com.example.nhviewer.di

import android.content.Context
import com.example.nhviewer.BuildConfig
import com.example.nhviewer.data.remote.GalleryApi
import com.example.nhviewer.data.remote.interceptor.CacheStrategyInterceptor
import com.example.nhviewer.data.remote.interceptor.ForceRefreshInterceptor
import com.example.nhviewer.data.remote.interceptor.RateLimitInterceptor
import com.example.nhviewer.data.remote.interceptor.UserAgentInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, 50L * 1024L * 1024L) // 50MB API Cache
    }

    @Provides
    @Singleton
    @Named("BaseClient")
    fun provideBaseOkHttpClient(cache: Cache): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(ForceRefreshInterceptor())
            .addInterceptor(RateLimitInterceptor())
            .addNetworkInterceptor(CacheStrategyInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthClient")
    fun provideAuthOkHttpClient(
        @Named("BaseClient") baseClient: OkHttpClient,
        authInterceptor: com.example.nhviewer.data.remote.interceptor.AuthInterceptor,
        authenticator: com.example.nhviewer.data.remote.interceptor.TokenRefreshAuthenticator
    ): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .build()
    }

    // 只挂 AuthInterceptor，不挂 Authenticator，避免 Authenticator 反过来依赖 AuthApi 形成循环依赖。
    @Provides
    @Singleton
    @Named("AuthApiClient")
    fun provideAuthApiOkHttpClient(
        @Named("BaseClient") baseClient: OkHttpClient,
        authInterceptor: com.example.nhviewer.data.remote.interceptor.AuthInterceptor
    ): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthApiRetrofit")
    fun provideAuthApiRetrofit(@Named("AuthApiClient") okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://nhentai.net/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(@Named("AuthClient") okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://nhentai.net/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideGalleryApi(@Named("AuthRetrofit") retrofit: Retrofit): GalleryApi {
        return retrofit.create(GalleryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSearchApi(@Named("AuthRetrofit") retrofit: Retrofit): com.example.nhviewer.data.remote.SearchApi {
        return retrofit.create(com.example.nhviewer.data.remote.SearchApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTagApi(@Named("AuthRetrofit") retrofit: Retrofit): com.example.nhviewer.data.remote.TagApi {
        return retrofit.create(com.example.nhviewer.data.remote.TagApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApi(@Named("AuthApiRetrofit") retrofit: Retrofit): com.example.nhviewer.data.remote.AuthApi {
        return retrofit.create(com.example.nhviewer.data.remote.AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFavoriteApi(@Named("AuthRetrofit") retrofit: Retrofit): com.example.nhviewer.data.remote.FavoriteApi {
        return retrofit.create(com.example.nhviewer.data.remote.FavoriteApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBlacklistApi(@Named("AuthRetrofit") retrofit: Retrofit): com.example.nhviewer.data.remote.BlacklistApi {
        return retrofit.create(com.example.nhviewer.data.remote.BlacklistApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentApi(@Named("AuthRetrofit") retrofit: Retrofit): com.example.nhviewer.data.remote.CommentApi {
        return retrofit.create(com.example.nhviewer.data.remote.CommentApi::class.java)
    }
}
