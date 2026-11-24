package com.leochat.features.ai.domain.entities

enum class AiModelIcon { Sparkles, Brain, Code, Zap }

/** Selectable intelligence engine. Mirrors Flutter `AiModelInfo`. */
data class AiModelInfo(
    val id: String,
    val name: String,
    val tagline: String,
    val badge: String,
    val contextWindow: String,
    val icon: AiModelIcon,
) {
    companion object {
        val defaultModel = AiModelInfo(
            id = "leo-ultra",
            name = "LeoChat 4.5 Ultra",
            tagline = "Flagship reasoning & multi-modal intelligence",
            badge = "Default",
            contextWindow = "128k",
            icon = AiModelIcon.Sparkles,
        )

        val models = listOf(
            defaultModel,
            AiModelInfo(
                id = "leo-reasoner",
                name = "Leo Reasoning R1",
                tagline = "Deep chain-of-thought for science, math & logic",
                badge = "Deep Think",
                contextWindow = "64k",
                icon = AiModelIcon.Brain,
            ),
            AiModelInfo(
                id = "leo-coder",
                name = "Leo Code Architect",
                tagline = "Expert full-stack programming, architecture & review",
                badge = "Coding",
                contextWindow = "128k",
                icon = AiModelIcon.Code,
            ),
            AiModelInfo(
                id = "leo-fast",
                name = "Leo Flash 3.5",
                tagline = "Instant low-latency responses for quick tasks",
                badge = "High Speed",
                contextWindow = "32k",
                icon = AiModelIcon.Zap,
            ),
        )
    }
}
