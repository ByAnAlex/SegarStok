package com.segarstok.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.segarstok.core.design.theme.*
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.UrgencyLevel

@Composable
fun UrgencyBadge(
    urgencyLevel: UrgencyLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (urgencyLevel) {
        UrgencyLevel.NORMAL -> Triple(NormalBg, NormalBorder, NormalText)
        UrgencyLevel.ATTENTION -> Triple(AttentionBg, AttentionBorder, AttentionText)
        UrgencyLevel.NEAR -> Triple(WarningBg, WarningBorder, WarningText)
        UrgencyLevel.URGENT -> Triple(UrgentBg, UrgentBorder, UrgentText)
        UrgencyLevel.DUE_TODAY -> Triple(ExpiredBg, ExpiredBorder, ExpiredText)
        UrgencyLevel.EXPIRED -> Triple(ExpiredBg, ExpiredBorder, ExpiredText)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = urgencyLevel.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = textColor
        )
    }
}

@Composable
fun BatchStatusBadge(
    status: BatchStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (status) {
        BatchStatus.AVAILABLE -> Triple(NormalBg, NormalBorder, NormalText)
        BatchStatus.SOLD_OR_USED -> Triple(CardSurface, CardBorder, Slate500)
        BatchStatus.DISCARDED -> Triple(ExpiredBg, ExpiredBorder, ExpiredText)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = textColor
        )
    }
}
