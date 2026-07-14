package com.example.congeloinventaire.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.congeloinventaire.viewmodel.InventoryViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    viewModel: InventoryViewModel,
    onNavigateToInventory: () -> Unit,
    onNavigateToScanner: (isAdding: Boolean) -> Unit,
    onNavigateToRecipes: () -> Unit
) {
    val inventory by viewModel.inventory.collectAsState()
    val isServerOnline by viewModel.isServerOnline.collectAsState()
    
    // Statistiques rapides
    val totalItems = inventory.size
    
    // Détermination de la fraîcheur (simplifiée pour le prototype)
    var freshCount = 0
    var medCount = 0
    var oldCount = 0
    
    val now = LocalDate.now()
    for (item in inventory) {
        try {
            val dateAdded = LocalDate.parse(item.date_added)
            val daysOld = ChronoUnit.DAYS.between(dateAdded, now)
            when {
                daysOld <= 30 -> freshCount++
                daysOld <= 90 -> medCount++
                else -> oldCount++
            }
        } catch (e: Exception) {
            freshCount++ // Fallback
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(visible = !isServerOnline) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Mode Hors-Ligne - Serveur introuvable",
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        Text(
            text = "À Givre Ouvert",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
        )
        
        Text(
            text = "$totalItems articles",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Jauge de fraîcheur simple
        if (totalItems > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
            ) {
                if (freshCount > 0) {
                    Box(modifier = Modifier.weight(freshCount.toFloat()).fillMaxHeight().background(Color(0xFF4CAF50), RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = if (freshCount == totalItems) 12.dp else 0.dp, bottomEnd = if (freshCount == totalItems) 12.dp else 0.dp)))
                }
                if (medCount > 0) {
                    Box(modifier = Modifier.weight(medCount.toFloat()).fillMaxHeight().background(Color(0xFFFFC107)))
                }
                if (oldCount > 0) {
                    Box(modifier = Modifier.weight(oldCount.toFloat()).fillMaxHeight().background(Color(0xFFF44336), RoundedCornerShape(topStart = if (oldCount == totalItems) 12.dp else 0.dp, bottomStart = if (oldCount == totalItems) 12.dp else 0.dp, topEnd = 12.dp, bottomEnd = 12.dp)))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Frais", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Moyen", color = Color(0xFFF57F17), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Vieux", color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Grille de boutons
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Ajouter",
                    icon = Icons.Filled.Add,
                    color = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF2E7D32),
                    enabled = isServerOnline,
                    onClick = { onNavigateToScanner(true) }
                )
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Retirer",
                    icon = Icons.Filled.Remove,
                    color = Color(0xFFFFEBEE),
                    iconColor = Color(0xFFC62828),
                    enabled = isServerOnline,
                    onClick = { onNavigateToScanner(false) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Inventaire",
                    icon = Icons.Filled.List,
                    color = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1565C0),
                    onClick = onNavigateToInventory
                )
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Recettes",
                    icon = Icons.Filled.Restaurant,
                    color = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFEF6C00),
                    enabled = isServerOnline,
                    onClick = {
                        viewModel.loadRecipes()
                        onNavigateToRecipes()
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    iconColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.5f
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = alpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor.copy(alpha = alpha),
                modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
            )
            Text(
                text = title,
                color = iconColor.copy(alpha = alpha),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
