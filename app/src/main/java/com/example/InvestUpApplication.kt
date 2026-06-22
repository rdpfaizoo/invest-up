package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.repository.InvestmentRepository

class InvestUpApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "investup_database"
        ).fallbackToDestructiveMigration().build()
    }
    val repository by lazy { InvestmentRepository(database) }
}
