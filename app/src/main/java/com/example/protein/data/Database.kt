package com.example.protein.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val defaultUnitName: String, // "grams", "slice", "egg"
    val gramsPerUnit: Double // 1.0 for grams, 50.0 for egg, etc.
)

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val foodName: String,
    val kcal: Double,
    val protein: Double,
    val timestamp: Long,
    val mealSlot: String
)

@Dao
interface AppDao {
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<Food>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<Food>)

    @Query("SELECT * FROM log_entries WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun getLogsForDay(startOfDay: Long, endOfDay: Long): Flow<List<LogEntry>>

    @Insert
    suspend fun insertLog(log: LogEntry)

    @Delete
    suspend fun deleteLog(log: LogEntry)
}

@Database(entities = [Food::class, LogEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "protein_database"
                )
                .addCallback(DatabaseCallback())
                .build().also { INSTANCE = it }
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.appDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: AppDao) {
                val initialFoods = listOf(
                    Food(name = "Chicken, cooked", kcalPer100g = 165.0, proteinPer100g = 25.0, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Egg", kcalPer100g = 143.0, proteinPer100g = 12.6, defaultUnitName = "egg", gramsPerUnit = 50.0),
                    Food(name = "Dahi, plain", kcalPer100g = 60.0, proteinPer100g = 3.5, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Dahi, high-protein", kcalPer100g = 65.0, proteinPer100g = 9.0, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Paneer", kcalPer100g = 265.0, proteinPer100g = 18.0, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Roti", kcalPer100g = 297.0, proteinPer100g = 9.6, defaultUnitName = "roti", gramsPerUnit = 35.0),
                    Food(name = "Pav", kcalPer100g = 280.0, proteinPer100g = 8.5, defaultUnitName = "pav", gramsPerUnit = 30.0),
                    Food(name = "Bread", kcalPer100g = 265.0, proteinPer100g = 9.0, defaultUnitName = "slice", gramsPerUnit = 28.0),
                    Food(name = "Haleem", kcalPer100g = 145.0, proteinPer100g = 9.0, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Rice, cooked", kcalPer100g = 130.0, proteinPer100g = 2.7, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Dal, cooked", kcalPer100g = 115.0, proteinPer100g = 7.0, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Milk, full-fat", kcalPer100g = 62.0, proteinPer100g = 3.2, defaultUnitName = "ml", gramsPerUnit = 1.0),
                    Food(name = "Milk, toned", kcalPer100g = 52.0, proteinPer100g = 3.2, defaultUnitName = "ml", gramsPerUnit = 1.0),
                    Food(name = "Ghee", kcalPer100g = 900.0, proteinPer100g = 0.0, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Sugar", kcalPer100g = 400.0, proteinPer100g = 0.0, defaultUnitName = "tbsp", gramsPerUnit = 12.0),
                    Food(name = "Banana", kcalPer100g = 89.0, proteinPer100g = 1.1, defaultUnitName = "medium", gramsPerUnit = 120.0),
                    Food(name = "Apple", kcalPer100g = 52.0, proteinPer100g = 0.3, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Cucumber", kcalPer100g = 15.0, proteinPer100g = 0.7, defaultUnitName = "medium", gramsPerUnit = 150.0),
                    Food(name = "Capsicum", kcalPer100g = 20.0, proteinPer100g = 0.9, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Onion", kcalPer100g = 40.0, proteinPer100g = 1.1, defaultUnitName = "grams", gramsPerUnit = 1.0),
                    Food(name = "Barfi", kcalPer100g = 450.0, proteinPer100g = 8.0, defaultUnitName = "piece", gramsPerUnit = 30.0),
                    Food(name = "Pea protein", kcalPer100g = 390.0, proteinPer100g = 80.0, defaultUnitName = "scoop", gramsPerUnit = 30.0)
                )
                dao.insertFoods(initialFoods)
            }
        }
    }
}
