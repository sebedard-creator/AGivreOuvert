package com.example.congeloinventaire.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.congeloinventaire.network.ApiClient
import com.example.congeloinventaire.network.InventoryItem
import com.example.congeloinventaire.network.Recipe
import com.example.congeloinventaire.network.ScanResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _recipesError = MutableStateFlow<String?>(null)
    val recipesError: StateFlow<String?> = _recipesError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    private val _isServerOnline = MutableStateFlow(true)
    val isServerOnline: StateFlow<Boolean> = _isServerOnline.asStateFlow()

    private val sharedPrefs = application.getSharedPreferences("inventory_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadInventory()
    }

    fun loadInventory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = ApiClient.service.getInventory()
                _inventory.value = items
                _isServerOnline.value = true
                // Sauvegarde dans le cache local
                val jsonString = gson.toJson(items)
                sharedPrefs.edit().putString("cached_inventory", jsonString).apply()
            } catch (e: Exception) {
                e.printStackTrace()
                _isServerOnline.value = false
                // Chargement depuis le cache local si le serveur est indisponible
                val cachedJson = sharedPrefs.getString("cached_inventory", null)
                if (cachedJson != null) {
                    try {
                        val type = object : TypeToken<List<InventoryItem>>() {}.type
                        val cachedItems: List<InventoryItem> = gson.fromJson(cachedJson, type)
                        _inventory.value = cachedItems
                    } catch (parseEx: Exception) {
                        parseEx.printStackTrace()
                        _inventory.value = emptyList()
                    }
                } else {
                    _inventory.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRecipes() {
        if (!_isServerOnline.value) {
            _recipesError.value = "Impossible de générer des recettes en mode hors ligne."
            return
        }

        _recipesError.value = null
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipeList = ApiClient.service.getRecipes()
                _recipes.value = recipeList
            } catch (e: Exception) {
                e.printStackTrace()
                _recipesError.value = if (e is SocketTimeoutException) {
                    "La génération des recettes prend trop de temps. Veuillez réessayer."
                } else {
                    "Impossible de charger les recettes. Vérifiez la connexion au serveur puis réessayez."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun lookupBarcode(upc: String) {
        if (!_isServerOnline.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ApiClient.service.lookupBarcode(upc)
                _scanResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                _scanResult.value = ScanResult(
                    upc = upc,
                    exists_in_database = false,
                    off_product_name = null,
                    local_lots = emptyList()
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
    }

    fun addItem(name: String, upc: String?) {
        if (!_isServerOnline.value) return
        viewModelScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(Date())
                
                val newItem = InventoryItem(
                    name = name,
                    upc = upc,
                    date_added = currentDate
                )
                ApiClient.service.addInventoryItem(newItem)
                loadInventory() // Rafraîchir l'inventaire
                clearScanResult()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeItem(id: Int) {
        if (!_isServerOnline.value) return
        viewModelScope.launch {
            try {
                ApiClient.service.removeInventoryItem(id)
                loadInventory() // Rafraîchir l'inventaire
                clearScanResult()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
