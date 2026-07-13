package com.example.congeloinventaire.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.congeloinventaire.network.ApiClient
import com.example.congeloinventaire.network.InventoryItem
import com.example.congeloinventaire.network.Recipe
import com.example.congeloinventaire.network.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryViewModel : ViewModel() {

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    init {
        loadInventory()
    }

    fun loadInventory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = ApiClient.service.getInventory()
                _inventory.value = items
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipeList = ApiClient.service.getRecipes()
                _recipes.value = recipeList
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun lookupBarcode(upc: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ApiClient.service.lookupBarcode(upc)
                _scanResult.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                // En cas d'erreur réseau ou inconnu, on simule un produit non trouvé
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
