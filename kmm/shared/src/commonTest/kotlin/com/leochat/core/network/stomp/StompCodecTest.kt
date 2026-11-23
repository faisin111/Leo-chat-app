package com.leochat.core.network.stomp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StompCodecTest {
    @Test
    fun encodeThenDecodeRoundTrips() {
        val frame = StompFrame("SEND", mapOf("destination" to "/app/chat.send"), """{"a":1}""")
        assertEquals(frame, StompCodec.decode(StompCodec.encode(frame)))
    }

    @Test
    fun decodesMessageWithColonInHeaderValue() {
        val raw = "MESSAGE\ndestination:/topic/conversations.1\nmessage-id:a:b\n\n{\"id\":\"m\"}\u0000"
        val frame = StompCodec.decode(raw)!!
        assertEquals("MESSAGE", frame.command)
        assertEquals("a:b", frame.headers["message-id"])
        assertEquals("{\"id\":\"m\"}", frame.body)
    }

    @Test
    fun heartbeatIsIgnored() {
        assertNull(StompCodec.decode("\n"))
    }
}
