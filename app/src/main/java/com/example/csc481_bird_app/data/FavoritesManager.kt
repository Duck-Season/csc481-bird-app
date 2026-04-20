package com.example.csc481_bird_app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages persistent storage for favorited bird names using DataStore.
 */
class FavoritesManager(private val context: Context) {

    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("favorite_birds")
    }

    /**
     * Emits the current set of favorite bird names.
     */
    val favoritesFlow: Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[FAVORITES_KEY] ?: emptySet()
        }

    /**
     * Adds or removes a bird name from the favorites list.
     */
    suspend fun toggleFavorite(bird: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()

            prefs[FAVORITES_KEY] =
                if (current.contains(bird)) {
                    current - bird
                } else {
                    current + bird
                }
        }
    }
}
