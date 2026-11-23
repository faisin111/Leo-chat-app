package com.leochat.features.chat.presentation.providers

import com.leochat.features.chat.presentation.controllers.ChatShellController
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val chatModule = module {
    viewModelOf(::ChatShellController)
}
