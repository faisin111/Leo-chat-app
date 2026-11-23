package com.leochat.core

import com.leochat.core.cache.CacheKeys
import com.leochat.core.cache.KeyValueCacheStore
import com.leochat.core.responsive.LeoLayout
import com.leochat.core.responsive.layoutForWidth
import com.leochat.core.storage.InMemoryKeyValueStorage
import com.leochat.core.theme.LeoThemeMode
import com.leochat.core.theme.ThemeModeStore
import com.leochat.core.widgets.common.branding.initialsFor
import androidx.compose.ui.unit.dp
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CoreTest {
    @Test
    fun breakpointsMatchFlutter() {
        assertEquals(LeoLayout.Mobile, layoutForWidth(599.dp))
        assertEquals(LeoLayout.Tablet, layoutForWidth(600.dp))
        assertEquals(LeoLayout.Desktop, layoutForWidth(1024.dp))
    }

    @Test
    fun themeModePersistsAndDefaultsToLight() {
        val storage = InMemoryKeyValueStorage()
        assertEquals(LeoThemeMode.Light, ThemeModeStore(storage).mode.value)
        ThemeModeStore(storage).setMode(LeoThemeMode.Dark)
        assertEquals(LeoThemeMode.Dark, ThemeModeStore(storage).mode.value)
    }

    @Test
    fun cacheIsScopedPerUser() {
        val cache = KeyValueCacheStore(InMemoryKeyValueStorage())
        val serializer = ListSerializer(String.serializer())
        cache.write(CacheKeys.messages("u1", "c1"), serializer, listOf("a"))
        cache.write(CacheKeys.messages("u2", "c1"), serializer, listOf("b"))
        cache.clearUserScope("u1")
        assertNull(cache.read(CacheKeys.messages("u1", "c1"), serializer))
        assertEquals(listOf("b"), cache.read(CacheKeys.messages("u2", "c1"), serializer))
    }

    @Test
    fun avatarInitials() {
        assertEquals("AL", initialsFor(" alex "))
        assertEquals("A", initialsFor("a"))
        assertNull(initialsFor("  "))
    }
}
