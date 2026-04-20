package com.example.csc481_bird_app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesManager(private val context: Context) {

    companion object {
        private val FAVORITES_KEY = stringSetPreferencesKey("favorite_birds")
    }

    val favoritesFlow: Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[FAVORITES_KEY] ?: emptySet()
        }

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
