package com.cpen321.usermanagement.di

import com.cpen321.usermanagement.data.remote.api.AuthInterface
import com.cpen321.usermanagement.data.remote.api.ChallengesInterface
import com.cpen321.usermanagement.data.remote.api.ImageInterface
import com.cpen321.usermanagement.data.remote.api.RetrofitClient
import com.cpen321.usermanagement.data.remote.api.UserInterface
import com.cpen321.usermanagement.data.remote.api.FriendsInterface
import com.cpen321.usermanagement.data.remote.api.TicketsInterface
import com.cpen321.usermanagement.data.remote.api.NHLInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNHLService(): NHLInterface {
        return RetrofitClient.nhlInterface
    }

    @Provides
    @Singleton
    fun provideAuthService(): AuthInterface {
        return RetrofitClient.authInterface
    }

    @Provides
    @Singleton
    fun provideUserService(): UserInterface {
        return RetrofitClient.userInterface
    }

    @Provides
    @Singleton
    fun provideMediaService(): ImageInterface {
        return RetrofitClient.imageInterface
    }

    @Provides
    @Singleton
    fun provideFriendsService(): FriendsInterface {
        return RetrofitClient.friendsInterface
    }

    @Provides
    @Singleton
    fun provideChallengesService(): ChallengesInterface {
        return RetrofitClient.challengesInterface
    }

    @Provides
    @Singleton
    fun provideTicketsService(): TicketsInterface {
        return RetrofitClient.ticketsInterface
    }
}
