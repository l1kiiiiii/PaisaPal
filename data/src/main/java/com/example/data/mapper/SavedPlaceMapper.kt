package com.example.data.mapper

import com.example.data.local.entity.SavedPlaceEntity
import com.example.domain.model.SavedPlace

fun SavedPlaceEntity.toDomain(): SavedPlace {
    return SavedPlace(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        category = category,
        radius = radius
    )
}

fun SavedPlace.toEntity(): SavedPlaceEntity {
    return SavedPlaceEntity(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        category = category,
        radius = radius
    )
}
