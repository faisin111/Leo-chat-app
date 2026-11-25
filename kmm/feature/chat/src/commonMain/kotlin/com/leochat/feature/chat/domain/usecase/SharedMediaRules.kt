package com.leochat.feature.chat.domain.usecase

import com.leochat.feature.chat.presentation.sharedmedia.SharedMediaScreen
import com.leochat.feature.chat.domain.model.Message

/** Media, files, and links found in a conversation's loaded messages. */
data class SharedMediaBuckets(
    val media: List<Message> = emptyList(),
    val files: List<Message> = emptyList(),
    val links: List<Message> = emptyList(),
)

/** Classifies messages for the shared media screen. Mirrors `_isImage`, `_isFile`, `_hasLink` in `SharedMediaScreen`. */
object SharedMediaRules {
    private val imageTypes = setOf("IMAGE", "IMAGE_MESSAGE")
    private val imageExtensions = listOf(".png", ".jpg", ".jpeg", ".webp", ".gif")
    private val fileTypes = setOf("FILE", "DOCUMENT", "AUDIO", "VIDEO")
    private val fileExtensions = listOf(".pdf", ".doc", ".docx", ".zip", ".mp3", ".mp4")
    private val urlPattern = Regex("https?://\\S+")

    fun isImage(message: Message): Boolean {
        if (message.type.uppercase() in imageTypes) return true
        if (message.attachments.any { it.mimeType?.startsWith("image/", ignoreCase = true) == true }) return true
        val content = message.content.orEmpty().lowercase()
        return imageExtensions.any(content::endsWith)
    }

    fun isFile(message: Message): Boolean {
        if (isImage(message)) return false
        if (message.type.uppercase() in fileTypes) return true
        if (message.attachments.isNotEmpty()) return true
        val content = message.content.orEmpty().lowercase()
        return fileExtensions.any(content::endsWith)
    }

    fun hasLink(message: Message): Boolean {
        if (isImage(message) || isFile(message)) return false
        val content = message.content.orEmpty()
        return content.contains("http://") || content.contains("https://")
    }

    fun classify(messages: List<Message>): SharedMediaBuckets = SharedMediaBuckets(
        media = messages.filter(::isImage),
        files = messages.filter(::isFile),
        links = messages.filter(::hasLink),
    )

    /** Last path segment of the content, or the content itself. */
    fun fileName(content: String): String = if (content.contains('/')) content.substringAfterLast('/') else content

    /** First URL in the content, or the whole content when none matches. */
    fun firstUrl(content: String): String = urlPattern.find(content)?.value ?: content
}
