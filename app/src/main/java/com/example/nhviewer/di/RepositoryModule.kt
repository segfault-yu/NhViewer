package com.example.nhviewer.di

import com.example.nhviewer.data.repository.GalleryRepositoryImpl
import com.example.nhviewer.domain.repository.GalleryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGalleryRepository(
        galleryRepositoryImpl: GalleryRepositoryImpl
    ): GalleryRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: com.example.nhviewer.data.repository.SearchRepositoryImpl
    ): com.example.nhviewer.domain.repository.SearchRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        tagRepositoryImpl: com.example.nhviewer.data.repository.TagRepositoryImpl
    ): com.example.nhviewer.domain.repository.TagRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: com.example.nhviewer.data.repository.UserRepositoryImpl
    ): com.example.nhviewer.domain.repository.UserRepository
}
