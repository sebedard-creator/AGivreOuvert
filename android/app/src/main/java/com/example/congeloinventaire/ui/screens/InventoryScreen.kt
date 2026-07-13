package com.example.congeloinventaire.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.congeloinventaire.network.InventoryItem
import com.example.congeloinventaire.viewmodel.InventoryViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val inventory by viewModel.inventory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventaire") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Votre congélateur est vide.", color = Color.Black)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(inventory) { index, item ->
                    InventoryItemCard(
                        item = item,
                        index = index,
                        onDelete = { item.id?.let { viewModel.removeItem(it) } }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventoryItemCard(item: InventoryItem, index: Int = 0, onDelete: () -> Unit) {
    val backgroundColor = if (index % 2 == 0) Color.White else Color(0xFFE3F2FD) // Bleu clair alternatif
    
    // Calcul de la couleur en fonction de l'âge
    var color = Color(0xFF4CAF50) // Vert
    var ageText = "Récent"
    
    try {
        val dateAdded = LocalDate.parse(item.date_added)
        val daysOld = ChronoUnit.DAYS.between(dateAdded, LocalDate.now())
        when {
            daysOld <= 30 -> {
                color = Color(0xFF4CAF50)
                ageText = "$daysOld j."
            }
            daysOld <= 90 -> {
                color = Color(0xFFFFC107)
                ageText = "$daysOld j."
            }
            else -> {
                color = Color(0xFFF44336)
                ageText = "$daysOld j."
            }
        }
    } catch (e: Exception) {
        // Ignorer si la date est invalide
    }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Supprimer un produit", color = Color.Black) },
            text = { Text("Voulez-vous vraiment retirer ce produit de l'inventaire ?", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = { 
                    showDialog = false
                    onDelete() 
                }) {
                    Text("Supprimer", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annuler", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showDialog = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicateur de couleur
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = ageText,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
                Text(
                    text = item.date_added,
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
    }
}
