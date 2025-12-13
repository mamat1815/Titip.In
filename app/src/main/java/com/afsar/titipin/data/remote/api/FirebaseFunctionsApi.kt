//package com.afsar.titipin.data.remote.api
//
//import com.afsar.titipin.data.model.*
//import retrofit2.Response
//import retrofit2.http.*
//
//interface FirebaseFunctionsApi {
//
//    @POST("generateSnapToken")
//    suspend fun generateSnapToken(
//        @Body request: SnapTokenRequest
//    ): Response<SnapTokenResponse>
//
//    @GET("checkPaymentStatus")
//    suspend fun checkPaymentStatus(
//        @Query("orderId") orderId: String
//    ): Response<PaymentStatusResponse>
//
//    @POST("disburseFunds")
//    suspend fun disburseFunds(
//        @Body request: DisbursementRequest
//    ): Response<DisbursementResponse>
//}


package com.afsar.titipin.data.remote.api

import com.afsar.titipin.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FirebaseFunctionsApi {

    @POST("generateSnapToken")
    suspend fun generateSnapToken(
        @Body request: SnapTokenRequest
    ): Response<SnapTokenResponse>

    @GET("checkPaymentStatus")
    suspend fun checkPaymentStatus(
        @Query("order_id") orderId: String // Sesuaikan query param dengan backend (snake_case)
    ): Response<PaymentStatusResponse>

    @POST("disburseFunds")
    suspend fun disburseFunds(
        @Body request: DisbursementRequest
    ): Response<DisbursementResponse>
}