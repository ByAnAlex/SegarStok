package com.segarstok.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.segarstok.core.design.theme.*
import com.segarstok.domain.model.BatchStatus
import com.segarstok.domain.model.UrgencyLevel

@Composable
fun UrgencyBadge(
    urgencyLevel: UrgencyLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (urgencyLevel) {
        UrgencyLevel.NORMAL -> UrgencyNormalBg to UrgencyNormalText
        UrgencyLevel.ATTENTION -> UrgencyAttentionBg to UrgencyAttentionText
        UrgencyLevel.NEAR -> UrgencyNearBg to UrgencyNearText
        UrgencyLevel.URGENT -> UrgencyUrgentBg to UrgencyUrgentText
        UrgencyLevel.DUE_TODAY -> UrgencyDueTodayBg to UrgencyDueTodayText
        UrgencyLevel.EXPIRED -> UrgencyExpiredBg to UrgencyExpiredText
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.badgeCornerRadius))
            .background(bgColor)
            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall)
    ) {
        Text(
            text = "${urgencyLevel.label} (${urgencyLevel.description})",
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
fun BatchStatusBadge(
    status: BatchStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        BatchStatus.AVAILABLE -> PrimaryGreenLight to PrimaryGreenDark
        BatchStatus.SOLD_OR_USED -> SurfaceVariantLight to TextSecondary
        BatchStatus.DISCARDED -> UrgencyUrgentBg to UrgencyUrgentText
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.badgeCornerRadius))
            .background(bgColor)
            .padding(horizontal = Spacing.small, vertical = Spacing.extraSmall)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
