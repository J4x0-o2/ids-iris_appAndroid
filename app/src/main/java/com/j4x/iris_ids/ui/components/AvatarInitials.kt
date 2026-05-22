package com.j4x.iris_ids.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.j4x.iris_ids.ui.theme.IrisLine
import com.j4x.iris_ids.ui.theme.IrisPrimary
import com.j4x.iris_ids.ui.theme.IrisPrimarySoft

@Composable
fun AvatarInitials(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val initials = name.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "?" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(IrisPrimarySoft)
            .border(1.dp, IrisLine, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = IrisPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = (size.value * 0.34f).sp,
        )
    }
}
