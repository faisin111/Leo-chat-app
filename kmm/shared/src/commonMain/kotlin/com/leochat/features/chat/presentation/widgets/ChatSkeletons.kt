package com.leochat.features.chat.presentation.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leochat.core.widgets.common.feedback.LeoShimmer
import com.leochat.core.widgets.common.feedback.LeoSkeleton
import com.leochat.core.widgets.common.feedback.LeoSkeletonCircle

@Composable
private fun Bar(width: Dp?, height: Dp, radius: Dp) {
    LeoSkeleton(modifier = (if (width != null) Modifier.width(width) else Modifier.fillMaxWidth()).height(height), cornerRadius = radius)
}

/** Shimmering placeholder rows for the conversation list. Mirrors `LeoConversationSkeletonList`. */
@Composable
fun ConversationSkeletonList(itemCount: Int = 6, modifier: Modifier = Modifier) {
    LeoShimmer(modifier) {
        Column {
            repeat(itemCount) {
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LeoSkeletonCircle(52.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Bar(130.dp, 16.dp, 6.dp)
                        Spacer(Modifier.height(8.dp))
                        Bar(null, 13.dp, 5.dp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Bar(36.dp, 11.dp, 4.dp)
                        Spacer(Modifier.height(8.dp))
                        LeoSkeletonCircle(18.dp)
                    }
                }
            }
        }
    }
}

/** Shimmering placeholder bubbles for the message pane. Mirrors `LeoMessageSkeletonList`. */
@Composable
fun MessageSkeletonList(modifier: Modifier = Modifier) {
    LeoShimmer(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Bar(80.dp, 12.dp, 6.dp) }
            Spacer(Modifier.height(24.dp))
            IncomingSkeleton(90.dp, 250.dp, 56.dp)
            Spacer(Modifier.height(20.dp))
            OutgoingSkeleton(210.dp, 48.dp, 44.dp)
            Spacer(Modifier.height(20.dp))
            IncomingSkeleton(70.dp, 280.dp, 74.dp)
            Spacer(Modifier.height(20.dp))
            OutgoingSkeleton(160.dp, 42.dp, 40.dp)
        }
    }
}

@Composable
private fun IncomingSkeleton(nameWidth: Dp, bubbleWidth: Dp, bubbleHeight: Dp) {
    Row(verticalAlignment = Alignment.Bottom) {
        LeoSkeletonCircle(34.dp)
        Spacer(Modifier.width(10.dp))
        Column {
            Bar(nameWidth, 11.dp, 4.dp)
            Spacer(Modifier.height(6.dp))
            Bar(bubbleWidth, bubbleHeight, 18.dp)
        }
    }
}

@Composable
private fun OutgoingSkeleton(bubbleWidth: Dp, bubbleHeight: Dp, timeWidth: Dp) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Top) {
        Bar(bubbleWidth, bubbleHeight, 18.dp)
        Spacer(Modifier.height(4.dp))
        Bar(timeWidth, 9.dp, 3.dp)
    }
}
