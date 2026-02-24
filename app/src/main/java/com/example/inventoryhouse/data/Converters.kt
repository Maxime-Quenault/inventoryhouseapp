package com.example.inventoryhouse.data

import androidx.room.TypeConverter
import com.example.inventoryhouse.data.enums.Location
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromLocation(value: String?): Location? {
        return value?.let { enumValueOf<Location>(it) }
    }

    @TypeConverter
    fun locationToString(location: Location?): String? {
        return location?.name
    }
}
