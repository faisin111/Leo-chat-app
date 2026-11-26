package com.leochat.core.platform.di

import com.leochat.core.platform.storage.AndroidKeyValueStorage
import com.leochat.core.platform.storage.AndroidSecureStorage
import com.leochat.core.platform.storage.KeyValueStorage
import com.leochat.core.platform.storage.SecureStorage
import com.leochat.core.platform.system.AndroidSystemSettingsHelper
import com.leochat.core.platform.system.SystemSettingsHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SecureStorage> { AndroidSecureStorage(androidContext()) }
    single<KeyValueStorage> { AndroidKeyValueStorage(androidContext()) }
    single<SystemSettingsHelper> { AndroidSystemSettingsHelper(androidContext()) }
}
