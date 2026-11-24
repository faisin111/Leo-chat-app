package com.leochat.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/** Points `Dispatchers.Main` (ViewModel scope) at this test's scheduler. Pair with [resetMainDispatcher]. */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.useTestMainDispatcher(unconfined: Boolean = false) {
    Dispatchers.setMain(if (unconfined) UnconfinedTestDispatcher(testScheduler) else StandardTestDispatcher(testScheduler))
}

@OptIn(ExperimentalCoroutinesApi::class)
fun resetMainDispatcher() = Dispatchers.resetMain()
