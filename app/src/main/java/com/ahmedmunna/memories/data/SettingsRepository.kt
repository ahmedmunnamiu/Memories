package com.ahmedmunna.memories.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode(val label: String) { SYSTEM("System default"), LIGHT("Light"), DARK("Dark") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val columns: Int = 3,
    val sort: SortOption = SortOption.DATE,
    val sortDescending: Boolean = true,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val COLUMNS = intPreferencesKey("columns")
        val SORT = stringPreferencesKey("sort")
        val DESC = booleanPreferencesKey("sort_desc")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            dynamicColor = p[Keys.DYNAMIC] ?: true,
            columns = (p[Keys.COLUMNS] ?: 3).coerceIn(2, 5),
            sort = p[Keys.SORT]?.let { runCatching { SortOption.valueOf(it) }.getOrNull() } ?: SortOption.DATE,
            sortDescending = p[Keys.DESC] ?: true,
        )
    }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[Keys.DYNAMIC] = enabled }
    suspend fun setColumns(columns: Int) = context.dataStore.edit { it[Keys.COLUMNS] = columns.coerceIn(2, 5) }
    suspend fun setSort(sort: SortOption) = context.dataStore.edit { it[Keys.SORT] = sort.name }
    suspend fun setSortDescending(desc: Boolean) = context.dataStore.edit { it[Keys.DESC] = desc }
}
