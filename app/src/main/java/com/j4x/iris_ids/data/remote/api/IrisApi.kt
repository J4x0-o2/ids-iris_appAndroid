package com.j4x.iris_ids.data.remote.api

import com.j4x.iris_ids.data.remote.dto.ActiveSessionResponse
import com.j4x.iris_ids.data.remote.dto.EnrollRequest
import com.j4x.iris_ids.data.remote.dto.EnrollResponse
import com.j4x.iris_ids.data.remote.dto.EventRequest
import com.j4x.iris_ids.data.remote.dto.EventResponse
import com.j4x.iris_ids.data.remote.dto.HistoryEventDto
import com.j4x.iris_ids.data.remote.dto.LoginRequest
import com.j4x.iris_ids.data.remote.dto.LoginResponse
import com.j4x.iris_ids.data.remote.dto.SyncRequest
import com.j4x.iris_ids.data.remote.dto.SyncResponse
import com.j4x.iris_ids.data.remote.dto.VerifyRequest
import com.j4x.iris_ids.data.remote.dto.VerifyResponse
import com.j4x.iris_ids.data.remote.dto.WorkerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface IrisApi {
    @GET("health")
    suspend fun health(): Response<Unit>

    @POST("auth/admin/login")
    suspend fun loginAdmin(@Body body: LoginRequest): Response<LoginResponse>

    @POST("face/verify")
    suspend fun verifyFace(@Body body: VerifyRequest): Response<VerifyResponse>

    @POST("attendance/event")
    suspend fun registerEvent(@Body body: EventRequest): Response<EventResponse>

    @POST("attendance/sync")
    suspend fun syncEvents(@Body body: SyncRequest): Response<SyncResponse>

    @POST("inspector/enroll")
    suspend fun enrollWorker(@Body body: EnrollRequest): Response<EnrollResponse>

    @GET("inspector/list")
    suspend fun getWorkers(): Response<List<WorkerResponse>>

    @GET("session/active")
    suspend fun getActiveSession(): Response<ActiveSessionResponse>

    @GET("attendance/history")
    suspend fun getHistory(
        @Query("inspectorId") inspectorId: String,
        @Query("limit") limit: Int = 100,
    ): Response<List<HistoryEventDto>>
}
