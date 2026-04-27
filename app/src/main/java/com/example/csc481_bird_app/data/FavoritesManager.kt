package com.example.csc481_bird_app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore instance for storing simple key-value pairs persistently
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

/**
 * Manages the persistent storage of favorite bird species using Jetpack DataStore.
 */
class FavoritesManager(private val context: Context) {

    companion object {
        // Key for the set of favorite bird names
        private val FAVORITES_KEY = stringSetPreferencesKey("favorite_birds")
    }

    /**
     * A flow that emits the current set of favorite birds.
     * Updates automatically when the underlying DataStore changes.
     */
    val favoritesFlow: Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[FAVORITES_KEY] ?: emptySet()
        }

    /**
     * Toggles the favorite status of a bird.
     * If it's already a favorite, it's removed; otherwise, it's added.
     */
    suspend fun toggleFavorite(bird: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()

            if (current.contains(bird)) {
                prefs[FAVORITES_KEY] = current - bird
            } else {
                prefs[FAVORITES_KEY] = current + bird
            }
        }
    }
}
