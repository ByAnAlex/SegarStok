package com.segarstok.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.segarstok.data.local.dao.StockBatchDao
import com.segarstok.data.local.entity.StockBatchEntity

@Database(
    entities = [StockBatchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SegarStokDatabase : RoomDatabase() {

    abstract fun stockBatchDao(): StockBatchDao

    companion object {
        @Volatile
        private var INSTANCE: SegarStokDatabase? = null

        fun getDatabase(context: Context): SegarStokDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SegarStokDatabase::class.java,
                    "segarstok_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
