package com.cybergah.securewipe

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class WipeViewModel : ViewModel() {

    /** PickedItem.id = uri.toString() */
    val files = mutableStateListOf<PickedItem>()

    fun addAll(context: Context, uris: List<Uri>) {
        if (uris.isEmpty()) return
        val engine = WipeEngine(context)
        val existing = files.map { it.id }.toHashSet()
        uris.forEach { u ->
            val id = u.toString()
            if (existing.add(id)) {
                files.add(PickedItem(id, engine.displayName(u), engine.sizeOf(u)))
            }
        }
    }

    fun removeAt(index: Int) {
        if (index in files.indices) files.removeAt(index)
    }

    fun clear() = files.clear()
}
