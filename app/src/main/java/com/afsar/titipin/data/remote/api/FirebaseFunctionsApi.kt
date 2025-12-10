package com.afsar.titipin.data.remote.api

import com.afsar.titipin.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface FirebaseFunctionsApi {
    
    @POST("generateSnapToken")
    suspend fun generateSnapToken(
        @Body request: SnapTokenRequest
    ): Response<SnapTokenResponse>
    
    @GET("checkPaymentStatus")
    suspend fun checkPaymentStatus(
        @Query("orderId") orderId: String
    ): Response<PaymentStatusResponse>
    
    @POST("disburseFunds")
    suspend fun disburseFunds(
        @Body request: DisbursementRequest
    ): Response<DisbursementResponse>
}
