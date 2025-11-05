package com.cpen321.usermanagement.di

import com.cpen321.usermanagement.data.local.preferences.NhlDataManager
import com.cpen321.usermanagement.data.local.preferences.NhlDataManagerImpl
import com.cpen321.usermanagement.data.repository.AuthRepository
import com.cpen321.usermanagement.data.repository.AuthRepositoryImpl
import com.cpen321.usermanagement.data.repository.ChallengesRepository
import com.cpen321.usermanagement.data.repository.ChallengesRepositoryImpl
import com.cpen321.usermanagement.data.repository.FriendsRepository
import com.cpen321.usermanagement.data.repository.FriendsRepositoryImpl
import com.cpen321.usermanagement.data.repository.NHLRepository
import com.cpen321.usermanagement.data.repository.NHLRepositoryImpl
import com.cpen321.usermanagement.data.repository.ProfileRepository
import com.cpen321.usermanagement.data.repository.ProfileRepositoryImpl
import com.cpen321.usermanagement.data.repository.TicketsRepository
import com.cpen321.usermanagement.data.repository.TicketsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {


    @Provides
    @Singleton
    fun provideNHLRepository(
        nhlRepositoryImpl: NHLRepositoryImpl
    ): NHLRepository {
        return nhlRepositoryImpl
    }
    @Provides
    @Singleton
    fun provideAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository {
        return authRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository {
        return profileRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideFriendsRepository(
        friendsRepositoryImpl: FriendsRepositoryImpl
    ) : FriendsRepository {
        return friendsRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideTicketsRepository(
        ticketsRepositoryImpl: TicketsRepositoryImpl
    ) : TicketsRepository {
        return ticketsRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideChallengesRepository(
        challengesRepositoryImpl: ChallengesRepositoryImpl
    ) : ChallengesRepository {
        return challengesRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideNhlDataManager(
        nhlRepository: NHLRepository
    ): NhlDataManager {
        return NhlDataManagerImpl(nhlRepository)
    }
}
