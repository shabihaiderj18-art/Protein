package com.example.protein.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.protein.data.AppDatabase
import com.example.protein.data.Food
import com.example.protein.data.LogEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val proteinTarget = 110.0 // Hardcoded for Phase 1
    val calorieTarget = 2500.0

    private val _dayStartMillis = MutableStateFlow(getStartOfDay())
    
    val todayLogs: StateFlow<List<LogEntry>> = _dayStartMillis.flatMapLatest { start ->
        dao.getLogsForDay(start, start + 86400000) // +24 hours
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalProtein = todayLogs.map { logs -> logs.sumOf { it.protein } }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
    val totalCalories = todayLogs.map { logs -> logs.sumOf { it.kcal } }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val foods: StateFlow<List<Food>> = dao.getAllFoods().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addLog(foodName: String, kcal: Double, protein: Double) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val hour = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
            val mealSlot = when (hour) {
                in 4..11 -> "Morning"
                in 12..15 -> "Afternoon"
                in 16..19 -> "Evening"
                else -> "Night"
            }
            dao.insertLog(LogEntry(foodName = foodName, kcal = kcal, protein = protein, timestamp = now, mealSlot = mealSlot))
        }
    }

    fun deleteLog(log: LogEntry) {
        viewModelScope.launch { dao.deleteLog(log) }
    }

    private fun getStartOfDay(): Long {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour < 4) cal.add(Calendar.DAY_OF_YEAR, -1) // 4am boundary
        cal.set(Calendar.HOUR_OF_DAY, 4)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
