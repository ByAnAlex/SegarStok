package com.segarstok.core.design.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Palette - Deep Teal / Forest Emerald
val PrimaryTeal = Color(0xFF0F766E)
val PrimaryTealLight = Color(0xFFF0FDFA)
val PrimaryTealContainer = Color(0xFFCCFBF1)
val PrimaryTealDark = Color(0xFF115E59)

// Neutral SaaS Canvas & Text Hierarchy
val CanvasBackground = Color(0xFFF8FAFC) // Cool Off-White
val CardSurface = Color(0xFFFFFFFF)
val CardBorder = Color(0xFFE2E8F0) // 1px Hairline Slate 200
val DividerColor = Color(0xFFF1F5F9)

val Slate900 = Color(0xFF0F172A) // Primary Titles/Numbers
val Slate700 = Color(0xFF334155) // Medium Text
val Slate500 = Color(0xFF64748B) // Subtitles/Labels
val Slate400 = Color(0xFF94A3B8) // Muted Text

// Semantic Status Colors (Subtle Tints + High-Contrast Text)
// Critical / Expired (<0H)
val ExpiredBg = Color(0xFFFEF2F2)
val ExpiredBorder = Color(0xFFFECACA)
val ExpiredText = Color(0xFFDC2626)

// Urgent (0-3H)
val UrgentBg = Color(0xFFFFF1F2)
val UrgentBorder = Color(0xFFFECDD3)
val UrgentText = Color(0xFFE11D48)

// Warning / Near (4-7H)
val WarningBg = Color(0xFFFFFBEB)
val WarningBorder = Color(0xFFFDE68A)
val WarningText = Color(0xFFD97706)

// Attention (8-14H)
val AttentionBg = Color(0xFFFEFCE8)
val AttentionBorder = Color(0xFFFEF08A)
val AttentionText = Color(0xFFA16207)

// Normal / Safe (>14H)
val NormalBg = Color(0xFFF0FDF4)
val NormalBorder = Color(0xFFA7F3D0)
val NormalText = Color(0xFF15803D)

// Legacy alias mappings for backward compatibility
val PrimaryGreen = PrimaryTeal
val PrimaryGreenLight = PrimaryTealLight
val PrimaryGreenDark = PrimaryTealDark
val SecondaryTeal = PrimaryTeal
val SecondaryTealLight = PrimaryTealContainer

val BackgroundLight = CanvasBackground
val SurfaceLight = CardSurface
val SurfaceVariantLight = Color(0xFFF1F5F9)

val TextPrimary = Slate900
val TextSecondary = Slate500
val TextMuted = Slate400

val UrgencyNormalBg = NormalBg
val UrgencyNormalText = NormalText
val UrgencyAttentionBg = AttentionBg
val UrgencyAttentionText = AttentionText
val UrgencyNearBg = WarningBg
val UrgencyNearText = WarningText
val UrgencyUrgentBg = UrgentBg
val UrgencyUrgentText = UrgentText
val UrgencyDueTodayBg = ExpiredBg
val UrgencyDueTodayText = ExpiredText
val UrgencyExpiredBg = ExpiredBg
val UrgencyExpiredText = ExpiredText
