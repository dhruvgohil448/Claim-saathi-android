package com.claimsaathi.app.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

interface ClaimSaathiApi {
    @POST("auth/otp/send")
    suspend fun sendOtp(@Body body: PhoneBody): OtpSent

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: VerifyBody): AuthResponse

    @GET("me")
    suspend fun me(): User

    @PUT("me/profile")
    suspend fun putProfile(@Body body: ProfileBody): ProfileResponse

    @PATCH("me/profile")
    suspend fun patchProfile(@Body body: ProfilePatch): ProfileResponse

    @GET("me/home")
    suspend fun home(): Home

    @GET("me/policies")
    suspend fun policies(): List<Policy>

    @GET("me/policies/{id}")
    suspend fun policy(@Path("id") id: String): Policy

    @POST("me/policies")
    suspend fun addPolicy(@Body body: AddPolicyBody): AddPolicyResponse

    @Multipart
    @POST("me/policies")
    suspend fun addPolicyPdf(
        @PartMap fields: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part file: MultipartBody.Part
    ): AddPolicyResponse

    @POST("me/policies/{id}/analyze")
    suspend fun analyze(@Path("id") id: String): PolicyAnalysis

    @GET("me/bank")
    suspend fun bank(): BankResponse

    @POST("me/bank")
    suspend fun saveBank(@Body body: BankBody): BankResponse

    @POST("claims/check-coverage")
    suspend fun checkCoverage(@Body body: CreateClaimBody): CoverageResult

    @POST("claims")
    suspend fun createClaim(@Body body: CreateClaimBody): Claim

    @GET("claims")
    suspend fun claims(@Query("status") status: String? = null): List<Claim>

    @GET("claims/{id}")
    suspend fun claim(@Path("id") id: String): Claim

    @POST("claims/{id}/preauth")
    suspend fun preauth(@Path("id") id: String): PreauthResponse

    @GET("claims/{id}/checklist")
    suspend fun checklist(@Path("id") id: String): Checklist

    @Multipart
    @POST("claims/{id}/documents")
    suspend fun upload(
        @Path("id") id: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody?
    ): UploadResponse

    @GET("documents/{id}/url")
    suspend fun documentUrl(@Path("id") id: String): SignedUrl

    @GET("claims/{id}/timeline")
    suspend fun timeline(@Path("id") id: String): Timeline

    @GET("claims/{id}/settlement")
    suspend fun settlement(@Path("id") id: String): Settlement

    @GET("queries")
    suspend fun queries(@Query("status") status: String? = "OPEN"): List<ClaimQuery>

    @GET("queries/{id}/explain")
    suspend fun explainQuery(@Path("id") id: String): QueryExplain

    @Multipart
    @POST("queries/{id}/respond")
    suspend fun respond(
        @Path("id") id: String,
        @Part("response") response: RequestBody,
        @Part file: MultipartBody.Part?,
        @Part("type") type: RequestBody?
    ): ClaimQuery

    @POST("ai/chat")
    suspend fun chat(@Body body: ChatBody): ChatReply

    @GET("notifications")
    suspend fun notifications(@Query("unread") unread: Boolean? = null): NotificationsPage

    @POST("notifications/{id}/read")
    suspend fun markRead(@Path("id") id: String): OkResponse

    @POST("notifications/read-all")
    suspend fun markAllRead(): OkResponse
}
