package com.leochat.core.platform.di

import com.leochat.core.platform.storage.IosKeyValueStorage
import com.leochat.core.platform.storage.IosSecureStorage
import com.leochat.core.platform.storage.KeyValueStorage
import com.leochat.core.platform.storage.SecureStorage
import com.leochat.core.platform.system.IosSystemSettingsHelper
import com.leochat.core.platform.system.SystemSettingsHelper
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<SecureStorage> { IosSecureStorage() }
    single<KeyValueStorage> { IosKeyValueStorage() }
    single<SystemSettingsHelper> { IosSystemSettingsHelper() }
}
