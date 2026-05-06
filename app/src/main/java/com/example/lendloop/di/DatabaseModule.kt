package com.example.lendloop.di

import android.content.Context
import androidx.room.Room
import com.example.lendloop.data.db.*
import com.example.lendloop.data.repository.AuthRepository
import com.example.lendloop.data.repository.ElectronicsRepository
import com.example.lendloop.data.repository.MpesaRepository
import com.example.lendloop.data.repository.PaymentRepository
import com.example.lendloop.data.repository.ReviewRepository
import com.example.lendloop.data.repository.TrustScoreRepository
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
    fun provideDatabase(@ApplicationContext context: Context): LendLoopDatabase {
        return Room.databaseBuilder(
            context,
            LendLoopDatabase::class.java,
            "lendloop_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }


    @Provides @Singleton
    fun provideBorrowDao(db: LendLoopDatabase): BorrowDao = db.borrowDao()

    @Provides @Singleton
    fun provideUserDao(db: LendLoopDatabase): UserDao = db.userDao()

    @Provides @Singleton
    fun provideReviewDao(db: LendLoopDatabase): ReviewDao = db.reviewDao()

    @Provides @Singleton
    fun provideTrustScoreDao(db: LendLoopDatabase): TrustScoreDao = db.trustScoreDao()

    @Provides @Singleton
    fun providePaymentDao(db: LendLoopDatabase): PaymentDao = db.paymentDao()

    @Provides @Singleton
    fun provideElectronicsDao(db: LendLoopDatabase): ElectronicsDao = db.electronicsDao()

    @Provides @Singleton
    fun provideAuthRepository(userDao: UserDao): AuthRepository =
        AuthRepository(userDao)

    @Provides @Singleton
    fun provideTrustScoreRepository(dao: TrustScoreDao): TrustScoreRepository =
        TrustScoreRepository(dao)

    @Provides @Singleton
    fun provideReviewRepository(dao: ReviewDao): ReviewRepository =
        ReviewRepository(dao)

    @Provides @Singleton
    fun providePaymentRepository(dao: PaymentDao): PaymentRepository =
        PaymentRepository(dao)

    @Provides @Singleton
    fun provideMpesaRepository(): MpesaRepository =
        MpesaRepository()

    @Provides @Singleton
    fun provideElectronicsRepository(dao: ElectronicsDao): ElectronicsRepository =
        ElectronicsRepository(dao)
}