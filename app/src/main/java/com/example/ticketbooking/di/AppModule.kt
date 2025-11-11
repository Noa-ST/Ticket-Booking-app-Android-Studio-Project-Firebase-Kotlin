package com.example.ticketbooking.di

import com.example.ticketbooking.data.FilmRepository
import com.example.ticketbooking.data.seat.SeatLockRepository
import com.example.ticketbooking.data.seat.FirebaseSeatLockRepository
import com.example.ticketbooking.data.order.OrderRepository
import com.example.ticketbooking.data.order.FirebaseOrderRepository
import com.google.firebase.database.FirebaseDatabase
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
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFilmRepository(database: FirebaseDatabase): FilmRepository = FilmRepository(database)

    @Provides
    @Singleton
    fun provideSeatLockRepository(database: FirebaseDatabase): SeatLockRepository = FirebaseSeatLockRepository(database)

    @Provides
    @Singleton
    fun provideOrderRepository(database: FirebaseDatabase): OrderRepository = FirebaseOrderRepository(database)
}