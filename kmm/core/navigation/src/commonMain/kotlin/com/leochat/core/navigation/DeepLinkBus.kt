package com.leochat.core.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/** Pending deep link triggered from Android/iOS push notifications. */
sealed interface AppDeepLink {
    data class OpenConversation(
        val conversationId: String,
        val messageId: String? = null,
        val title: String? = null,
    ) : AppDeepLink
}

/** Coordinates pending notification taps and the currently visible foreground conversation. */
object DeepLinkBus {
    private val mutablePending = MutableStateFlow<AppDeepLink?>(null)
    val pending: StateFlow<AppDeepLink?> = mutablePending.asStateFlow()

    private val mutableActiveConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = mutableActiveConversationId.asStateFlow()

    fun emit(link: AppDeepLink) {
        mutablePending.value = link
    }

    fun consume(): AppDeepLink? = mutablePending.getAndUpdate { null }

    fun setActiveConversation(conversationId: String?) {
        mutableActiveConversationId.value = conversationId
    }
}
