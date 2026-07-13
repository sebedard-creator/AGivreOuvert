package com.example.congeloinventaire.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.congeloinventaire.network.Recipe
import com.example.congeloinventaire.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Suggestions de Recettes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (recipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aucune suggestion (inventaire vide).", color = Color.Black)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Recettes IA générées avec vos produits les plus anciens pour éviter le gaspillage.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                itemsIndexed(recipes) { index, recipe ->
                    RecipeAccordion(recipe = recipe, index = index)
                }
            }
        }
    }
}

@Composable
fun RecipeAccordion(recipe: Recipe, index: Int = 0) {
    var expanded by remember { mutableStateOf(false) }
    val backgroundColor = if (index % 2 == 0) Color.White else Color(0xFFE3F2FD) // Un bleu très clair pour bien voir la différence

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recipe.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = buildAnnotatedString {
                            append(recipe.target)
                            if (recipe.fresh.isNotEmpty()) {
                                withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                    append(" (${recipe.fresh.joinToString(", ")})")
                                }
                            }
                        },
                        color = Color(0xFF1976D2), // Bleu
                        fontSize = 14.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Réduire" else "Étendre"
                )
            }

            // Content
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    Divider(modifier = Modifier.padding(bottom = 8.dp))
                    
                    Text("Du congélateur :", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                    recipe.freezer.forEach { item ->
                        Text("• $item", fontSize = 14.sp, color = Color.Black)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("À ajouter (frais) :", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                    recipe.fresh.forEach { item ->
                        Text("• $item", fontSize = 14.sp, color = Color.Black)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Étapes :", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                    Text(recipe.steps, fontSize = 14.sp, color = Color.Black)
                }
            }
        }
    }
}
