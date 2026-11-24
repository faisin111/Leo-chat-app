package com.leochat.core.platform.di

import org.koin.core.module.Module

/** Binds `SecureStorage` and `KeyValueStorage` for the current platform. */
expect val platformModule: Module
