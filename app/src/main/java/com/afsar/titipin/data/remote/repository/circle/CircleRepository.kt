package com.afsar.titipin.data.remote.repository.circle

import com.afsar.titipin.data.model.Circle
import com.afsar.titipin.data.model.User
import kotlinx.coroutines.flow.Flow

interface CircleRepository {

    fun createCircle(name: String, members: List<User>): Flow<Result<Boolean>>
    fun getMyCircles(): Flow<Result<List<Circle>>>
    fun getCircleDetail(circleId: String): Flow<Result<Circle>>

}