package com.example.ticketbooking.di

import android.content.Context
import android.content.SharedPreferences
import com.example.ticketbooking.data.FilmRepository
import com.example.ticketbooking.data.order.FirebaseOrderRepository
import com.example.ticketbooking.data.order.OrderRepository
import com.example.ticketbooking.data.favorite.FavoritesRepository
import com.example.ticketbooking.data.favorite.SharedPreferencesFavoritesRepository
import com.example.ticketbooking.data.seat.FakeSeatLockRepository
import com.example.ticketbooking.data.seat.SeatLockRepository
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideSeatLockRepository(database: FirebaseDatabase): SeatLockRepository {
        // Dùng FakeSeatLockRepository để tránh xung đột dữ liệu Firebase trong lúc kiểm thử.
        // Khi dữ liệu thật ổn định, chuyển lại FirebaseSeatLockRepository.
        return FakeSeatLockRepository()
    }

    @Provides
    @Singleton
    fun provideOrderRepository(database: FirebaseDatabase): OrderRepository = FirebaseOrderRepository(database)

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("ticketbooking_prefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideFavoritesRepository(prefs: SharedPreferences): FavoritesRepository =
        SharedPreferencesFavoritesRepository(prefs)
}