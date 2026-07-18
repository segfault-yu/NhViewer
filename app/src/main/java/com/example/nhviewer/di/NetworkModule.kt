package com.example.nhviewer.di

import com.example.nhviewer.data.remote.GalleryApi
import com.example.nhviewer.data.remote.interceptor.RateLimitInterceptor
import com.example.nhviewer.data.remote.interceptor.UserAgentInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton
import javax.inject.Named

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
    @Named("BaseClient")
    fun provideBaseOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(RateLimitInterceptor())
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

    @Provides
    @Singleton
    @Named("BaseRetrofit")
    fun provideBaseRetrofit(@Named("BaseClient") okHttpClient: OkHttpClient, json: Json): Retrofit {
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
    fun provideAuthApi(@Named("BaseRetrofit") retrofit: Retrofit): com.example.nhviewer.data.remote.AuthApi {
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
