package com.example.congeloinventaire.network

import com.example.congeloinventaire.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- DATA CLASSES ---

data class InventoryItem(
    val id: Int? = null,
    val name: String,
    val upc: String?,
    val date_added: String
)

data class ScanResult(
    val upc: String,
    val exists_in_database: Boolean,
    val off_product_name: String?,
    val local_lots: List<LocalLot>
)

data class LocalLot(
    val id: Int,
    val date_added: String
)

data class StatusResponse(
    val status: String,
    val message: String
)

data class Recipe(
    val title: String,
    val target: String,
    val freezer: List<String>,
    val fresh: List<String>,
    val steps: String
)

// --- RETROFIT INTERFACE ---

interface CongeloApiService {
    @GET("api/inventory")
    suspend fun getInventory(): List<InventoryItem>

    @POST("api/inventory/add")
    suspend fun addInventoryItem(@Body item: InventoryItem): InventoryItem

    @DELETE("api/inventory/item/{id}")
    suspend fun removeInventoryItem(@Path("id") id: Int): StatusResponse

    @GET("api/scanner/lookup/{upc}")
    suspend fun lookupBarcode(@Path("upc") upc: String): ScanResult

    @GET("api/recipes/suggestions")
    suspend fun getRecipes(): List<Recipe>
}

// --- API CLIENT OBJECT ---

object ApiClient {
    // Utilisation de l'IP du backend récupérée via BuildConfig
    private val BASE_URL = "http://${BuildConfig.BACKEND_IP}:8096/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val service: CongeloApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CongeloApiService::class.java)
    }
}
