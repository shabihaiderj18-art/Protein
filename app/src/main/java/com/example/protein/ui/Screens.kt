package com.example.protein.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.protein.data.Food

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToLog: () -> Unit) {
    val protein by viewModel.totalProtein.collectAsState()
    val calories by viewModel.totalCalories.collectAsState()
    val logs by viewModel.todayLogs.collectAsState()

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTriggered = true }

    val animatedProtein by animateFloatAsState(
        targetValue = if (animationTriggered) (protein / viewModel.proteinTarget).toFloat() else 0f,
        animationSpec = tween(1000), label = "protein_ring"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToLog) {
                Icon(Icons.Default.Add, contentDescription = "Log Food")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 16.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedProtein },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 16.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${protein.toInt()}g", style = MaterialTheme.typography.displayMedium)
                        Text("/ ${viewModel.proteinTarget.toInt()}g", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { (calories / viewModel.calorieTarget).toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
                Text("${calories.toInt()} kcal / ${viewModel.calorieTarget.toInt()}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Spacer(modifier = Modifier.height(24.dp))
            }

            val grouped = logs.groupBy { it.mealSlot }
            listOf("Morning", "Afternoon", "Evening", "Night").forEach { slot ->
                grouped[slot]?.let { mealLogs ->
                    item {
                        Text(slot, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                    }
                    items(mealLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(log.foodName, style = MaterialTheme.typography.bodyLarge)
                                    Text("${log.kcal.toInt()} kcal", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${log.protein.toInt()}g P", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    IconButton(onClick = { viewModel.deleteLog(log) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (logs.isEmpty()) {
                item { Text("No meals logged yet today.", modifier = Modifier.padding(32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
fun LogScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val foods by viewModel.foods.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<Food?>(null) }
    
    val filteredFoods = foods.filter { it.name.contains(searchQuery, ignoreCase = true) }

    if (selectedFood != null) {
        var input by remember { mutableStateOf("") }
        var useGrams by remember { mutableStateOf(selectedFood!!.defaultUnitName == "grams") }
        
        val amount = input.toDoubleOrNull() ?: 0.0
        val grams = if (useGrams) amount else amount * selectedFood!!.gramsPerUnit
        val currentProtein = (grams / 100.0) * selectedFood!!.proteinPer100g
        val currentKcal = (grams / 100.0) * selectedFood!!.kcalPer100g

        AlertDialog(
            onDismissRequest = { selectedFood = null },
            title = { Text(selectedFood!!.name) },
            text = {
                Column {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Amount in ${if (useGrams) "grams" else selectedFood!!.defaultUnitName}") }
                    )
                    if (selectedFood!!.defaultUnitName != "grams") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = useGrams, onCheckedChange = { useGrams = it })
                            Text("Log in grams instead")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Protein: ${String.format("%.1f", currentProtein)}g", style = MaterialTheme.typography.titleMedium)
                    Text("Calories: ${currentKcal.toInt()} kcal")
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (amount > 0) {
                        viewModel.addLog(selectedFood!!.name, currentKcal, currentProtein)
                        onBack()
                    }
                }) { Text("Log") }
            },
            dismissButton = {
                TextButton(onClick = { selectedFood = null }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search food...") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Quick Entry Option
        Card(modifier = Modifier.fillMaxWidth().clickable { 
            // In a full version, this opens a dialog for raw numbers. Hardcoded example here for phase 1
            viewModel.addLog("Quick Entry", 0.0, 20.0)
            onBack()
        }, shape = RoundedCornerShape(12.dp)) {
            Text("Quick Add: 20g Protein", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredFoods) { food ->
                ListItem(
                    headlineContent = { Text(food.name) },
                    supportingContent = { Text("${food.proteinPer100g}g P / 100g") },
                    modifier = Modifier.clickable { selectedFood = food }
                )
            }
        }
    }
}
