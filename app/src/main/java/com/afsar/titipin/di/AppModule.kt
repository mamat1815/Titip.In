package com.afsar.titipin.di

import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.AuthRepositoryImpl
import com.afsar.titipin.data.remote.PaymentRepository
import com.afsar.titipin.data.remote.PaymentRepositoryImpl
import com.afsar.titipin.data.remote.api.FirebaseFunctionsApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun providePaymentRepository(
        api: FirebaseFunctionsApi,
        firestore: FirebaseFirestore
    ): PaymentRepository {
        return PaymentRepositoryImpl(api, firestore)
    }
}