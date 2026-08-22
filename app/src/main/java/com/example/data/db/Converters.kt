package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.ChangeLogCategory
import com.example.data.model.ModelTier
import com.example.data.model.QualityPreference
import com.example.data.model.TaskType

class Converters {
    @TypeConverter
    fun fromTaskType(value: TaskType?): String? = value?.name

    @TypeConverter
    fun toTaskType(value: String?): TaskType? = value?.let {
        runCatching { TaskType.valueOf(it) }.getOrNull()
    }

    @TypeConverter
    fun fromQualityPreference(value: QualityPreference?): String? = value?.name

    @TypeConverter
    fun toQualityPreference(value: String?): QualityPreference? = value?.let {
        runCatching { QualityPreference.valueOf(it) }.getOrNull()
    }

    @TypeConverter
    fun fromModelTier(value: ModelTier?): String? = value?.name

    @TypeConverter
    fun toModelTier(value: String?): ModelTier? = value?.let {
        runCatching { ModelTier.valueOf(it) }.getOrNull()
    }

    @TypeConverter
    fun fromChangeLogCategory(value: ChangeLogCategory?): String? = value?.name

    @TypeConverter
    fun toChangeLogCategory(value: String?): ChangeLogCategory? = value?.let {
        runCatching { ChangeLogCategory.valueOf(it) }.getOrNull()
    }
}
