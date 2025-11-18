package com.example.domain.repository

import com.example.domain.model.GeoLocation

interface LocationProvider {
    suspend fun getCurrentLocation(): GeoLocation?
}
