//package com.afsar.titipin.di
//
//import com.afsar.titipin.data.remote.AuthRepository
//import com.afsar.titipin.data.remote.AuthRepositoryImpl
//import com.afsar.titipin.data.remote.PaymentRepository
//import com.afsar.titipin.data.remote.PaymentRepositoryImpl
//import com.afsar.titipin.data.remote.api.FirebaseFunctionsApi
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object AppModule {
//
//    @Provides
//    @Singleton
//    fun provideFirebaseAuth(): FirebaseAuth {
//        return FirebaseAuth.getInstance()
//    }
//
//    @Provides
//    @Singleton
//    fun provideFirebaseFirestore(): FirebaseFirestore {
//        return FirebaseFirestore.getInstance()
//    }
//
//    @Provides
//    @Singleton
//    fun provideAuthRepository(
//        auth: FirebaseAuth,
//        firestore: FirebaseFirestore
//    ): AuthRepository {
//        return AuthRepositoryImpl(auth, firestore)
//    }
//
//    @Provides
//    @Singleton
//    fun providePaymentRepository(
//        api: FirebaseFunctionsApi,
//        firestore: FirebaseFirestore
//    ): PaymentRepository {
//        return PaymentRepositoryImpl(api, firestore)
//    }
//}

package com.afsar.titipin.di

import com.afsar.titipin.data.remote.AuthRepository
import com.afsar.titipin.data.remote.AuthRepositoryImpl
import com.afsar.titipin.data.remote.PaymentRepository
import com.afsar.titipin.data.remote.PaymentRepositoryImpl
import com.afsar.titipin.data.remote.api.FirebaseFunctionsApi
import com.afsar.titipin.data.remote.repository.circle.CircleRepository
import com.afsar.titipin.data.remote.repository.circle.CircleRepositoryImpl
import com.afsar.titipin.data.remote.repository.order.OrderRepository
import com.afsar.titipin.data.remote.repository.order.OrderRepositoryImpl
import com.afsar.titipin.data.remote.repository.session.SessionRepository
import com.afsar.titipin.data.remote.repository.session.SessionRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ==========================================
    // 1. FIREBASE INSTANCES
    // ==========================================

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
//
//    @Provides
//    @Singleton
//    fun provideFirebaseStorage(): FirebaseStorage {
//        return FirebaseStorage.getInstance()
//    }

    // ==========================================
    // 2. NETWORK (RETROFIT FOR PAYMENT/BACKEND)
    // ==========================================



    // ==========================================
    // 3. REPOSITORIES
    // ==========================================

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
    fun provideCircleRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): CircleRepository {
        return CircleRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideSessionRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): SessionRepository {
        return SessionRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
//        storage: FirebaseStorage // Order butuh Storage untuk upload foto
    ): OrderRepository {
        return OrderRepositoryImpl(auth, firestore) // Pastikan Impl terima Storage
    }

    @Provides
    @Singleton
    fun providePaymentRepository(
        api: FirebaseFunctionsApi, // Ini butuh Retrofit di atas
        firestore: FirebaseFirestore
    ): PaymentRepository {
        return PaymentRepositoryImpl(api, firestore)
    }
}