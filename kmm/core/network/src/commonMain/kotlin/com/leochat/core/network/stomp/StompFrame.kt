package com.leochat.core.network.stomp

/** A single STOMP 1.2 frame. */
data class StompFrame(
    val command: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
)

object StompCodec {
    private const val NULL = '\u0000'

    fun encode(frame: StompFrame): String = buildString {
        append(frame.command).append('\n')
        frame.headers.forEach { (key, value) -> append(key).append(':').append(value).append('\n') }
        append('\n').append(frame.body).append(NULL)
    }

    /** Decodes one text payload. Returns null for heart-beats and malformed input. */
    fun decode(raw: String): StompFrame? {
        val text = raw.trimStart('\n', '\r')
        if (text.isEmpty()) return null
        val crlfEnd = text.indexOf("\r\n\r\n")
        val lfEnd = text.indexOf("\n\n")
        val (headerEnd, separatorLen) = when {
            lfEnd >= 0 && (crlfEnd < 0 || lfEnd < crlfEnd) -> lfEnd to 2
            crlfEnd >= 0 -> crlfEnd to 4
            else -> -1 to 0
        }
        val head = if (headerEnd >= 0) text.substring(0, headerEnd) else text.substringBefore(NULL)
        val lines = head.split('\n').map { it.trimEnd('\r') }
        val command = lines.firstOrNull()?.takeIf { it.isNotBlank() } ?: return null
        val headers = lines.drop(1).mapNotNull { line ->
            val index = line.indexOf(':')
            if (index <= 0) null else line.substring(0, index) to line.substring(index + 1)
        }.toMap()
        val body = if (headerEnd >= 0) text.substring(headerEnd + separatorLen).substringBefore(NULL) else ""
        return StompFrame(command, headers, body)
    }
}
