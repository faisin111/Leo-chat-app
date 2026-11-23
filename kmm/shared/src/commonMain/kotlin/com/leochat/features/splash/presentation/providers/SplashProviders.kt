package com.leochat.features.splash.presentation.providers

import com.leochat.features.splash.presentation.controllers.SplashController
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val splashModule = module {
    viewModel { SplashController(get()) }
}
