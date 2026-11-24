package com.leochat.core.datastore.di

import com.leochat.core.datastore.cache.CacheStore
import com.leochat.core.datastore.cache.KeyValueCacheStore
import org.koin.dsl.module

val datastoreModule = module {
    single<CacheStore> { KeyValueCacheStore(get()) }
}
