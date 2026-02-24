package com.example.inventoryhouse.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.inventoryhouse.data.dao.ProductDao
import com.example.inventoryhouse.data.model.Product

@Database(
    entities = [Product::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
}
