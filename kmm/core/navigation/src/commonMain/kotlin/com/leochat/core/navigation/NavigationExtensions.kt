package com.leochat.core.navigation

import androidx.navigation.NavHostController

/** `go`: replace the whole back stack (go_router `context.go`). */
fun NavHostController.goTo(route: Any) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
