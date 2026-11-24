package com.leochat.core.common.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Qualifier for the process-wide scope used by work that outlives any screen (realtime, calls). */
val ApplicationScope = named("applicationScope")

val commonModule = module {
    single<CoroutineScope>(ApplicationScope) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}
