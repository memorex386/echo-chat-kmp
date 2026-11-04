package com.echochat.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Marks every avatar, so tests can find them. */
const val AvatarTestTag = "avatar"

/** A coloured circle with someone's initial. Decorative: the text beside it names the person. */
@Composable
fun Avatar(
    initial: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
            .clearAndSetSemantics { testTag = AvatarTestTag },
    ) {
        Text(
            text = initial,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontSize = with(LocalDensity.current) { (size * 0.45f).toSp() },
            fontWeight = FontWeight.Medium,
        )
    }
}
