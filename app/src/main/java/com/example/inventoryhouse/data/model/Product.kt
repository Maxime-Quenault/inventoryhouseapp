package com.example.inventoryhouse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.inventoryhouse.data.enums.Location
import java.time.LocalDate

@Entity(tableName = "products")
data class Product(

    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val expiredDate: LocalDate,
    val location: Location,
    val imageUrl: String,
    val quantity: Int,
    val quantityUnit: String
)
