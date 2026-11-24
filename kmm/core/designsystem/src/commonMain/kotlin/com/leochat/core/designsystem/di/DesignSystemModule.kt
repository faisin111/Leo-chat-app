package com.leochat.core.designsystem.di

import com.leochat.core.designsystem.theme.ThemeModeStore
import org.koin.dsl.module

val designSystemModule = module {
    single { ThemeModeStore(get()) }
}
