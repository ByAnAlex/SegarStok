package com.segarstok.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segarstok.core.design.component.EmptyStateContainer
import com.segarstok.core.design.component.LoadingStateContainer
import com.segarstok.core.design.component.UrgencyBadge
import com.segarstok.core.design.theme.*
import com.segarstok.core.ui.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddBatch: () -> Unit,
    onNavigateToBatchDetail: (Long) -> Unit,
    onNavigateToRecommendations: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo Icon "S"
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "S",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SegarStok",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "Dashboard Stok",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CanvasBackground,
                    titleContentColor = Slate900
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddBatch,
                containerColor = PrimaryTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Batch",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingStateContainer(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Hero Metric Card - "Expiry Loss Monitor"
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimasi Modal Berisiko",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate500
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = if (uiState.totalRiskValue > 0) ExpiredBg else NormalBg,
                                    border = BorderStroke(1.dp, if (uiState.totalRiskValue > 0) ExpiredBorder else NormalBorder)
                                ) {
                                    Text(
                                        text = if (uiState.totalRiskValue > 0) "Berisiko" else "Aman",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (uiState.totalRiskValue > 0) ExpiredText else NormalText,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Text(
                                text = currencyFormat.format(uiState.totalRiskValue),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.expiredRiskValue > 0) ExpiredText else Slate900,
                                    fontFeatureSettings = "tnum"
                                )
                            )

                            HorizontalDivider(color = DividerColor, thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Berisiko Aktif",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            color = Slate500
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currencyFormat.format(uiState.activeRiskValue),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate900,
                                            fontFeatureSettings = "tnum"
                                        )
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Berisiko Lewat Tgl",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            color = Slate500
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currencyFormat.format(uiState.expiredRiskValue),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (uiState.expiredRiskValue > 0) ExpiredText else Slate900,
                                            fontFeatureSettings = "tnum"
                                        )
                                    )
                                }
                            }

                            if (uiState.missingPriceBatchesCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = WarningBg,
                                    border = BorderStroke(1.dp, WarningBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Warning,
                                            contentDescription = null,
                                            tint = WarningText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${uiState.missingPriceBatchesCount} batch belum diisi harga beli per unit.",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 12.sp,
                                                color = WarningText
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Section: "Ringkasan Urgensi Kedaluwarsa"
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Ringkasan Urgensi Kedaluwarsa",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Slate900
                            )
                        )

                        // Row 1: Lewat Tgl + Hari H (2 columns)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "Lewat Tgl",
                                count = uiState.expiredCount,
                                isHighlighted = uiState.expiredCount > 0,
                                highlightBg = ExpiredBg,
                                highlightBorder = ExpiredBorder,
                                highlightTextColor = ExpiredText,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Hari H (0H)",
                                count = uiState.dueTodayCount,
                                isHighlighted = uiState.dueTodayCount > 0,
                                highlightBg = UrgentBg,
                                highlightBorder = UrgentBorder,
                                highlightTextColor = UrgentText,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Row 2: Normal (>14H) - full width
                        StatCardWide(
                            title = "Normal (>14H)",
                            count = uiState.normalCount,
                            isHighlighted = false
                        )

                        // Row 3: Perhatian (8-14H) - full width
                        StatCardWide(
                            title = "Perhatian (8–14H)",
                            count = uiState.attentionCount,
                            isHighlighted = uiState.attentionCount > 0,
                            highlightBg = AttentionBg,
                            highlightBorder = AttentionBorder,
                            highlightTextColor = AttentionText
                        )

                        // Row 4: Mendesak (1-3H) - full width
                        StatCardWide(
                            title = "Mendesak (1–3H)",
                            count = uiState.urgentCount,
                            isHighlighted = uiState.urgentCount > 0,
                            highlightBg = UrgentBg,
                            highlightBorder = UrgentBorder,
                            highlightTextColor = UrgentText
                        )

                        // Row 5: Mendekati (4-7H) - full width
                        StatCardWide(
                            title = "Mendekati (4–7H)",
                            count = uiState.nearCount,
                            isHighlighted = uiState.nearCount > 0,
                            highlightBg = WarningBg,
                            highlightBorder = WarningBorder,
                            highlightTextColor = WarningText
                        )
                    }
                }

                // 3. Saran Tindakan - full width outlined button
                item {
                    OutlinedButton(
                        onClick = onNavigateToRecommendations,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PrimaryTeal),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CardSurface,
                            contentColor = PrimaryTeal
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Saran Tindakan",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = PrimaryTeal
                            ),
                            maxLines = 1
                        )
                    }
                }

                // 4. Section Header: "Batch Prioritas (Kedaluwarsa Terdekat)"
                item {
                    Text(
                        text = "Batch Prioritas (Kedaluwarsa Terdekat)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                    )
                }

                if (uiState.priorityBatches.isEmpty()) {
                    item {
                        EmptyStateContainer(
                            title = "Belum Ada Batch Stok Tersedia",
                            message = "Mulai mencatat batch stok pertama Anda untuk memantau tanggal kedaluwarsa.",
                            actionLabel = "+ Tambah Batch",
                            onActionClick = onNavigateToAddBatch
                        )
                    }
                } else {
                    items(uiState.priorityBatches) { item ->
                        val daysLeft = item.daysLeft()
                        val formattedQuantity = formatQuantity(item.quantity, item.unit)
                        val formattedProductName = capitalizeWords(item.productName)

                        val (daysLeftText, daysLeftColor) = when {
                            daysLeft < 0 -> {
                                val absDays = NumberFormat.getNumberInstance(Locale("id", "ID")).format(-daysLeft)
                                "Lewat $absDays hari" to ExpiredText
                            }
                            daysLeft == 0L -> "Kedaluwarsa HARI INI" to ExpiredText
                            daysLeft in 1..3 -> "Sisa $daysLeft hari" to UrgentText
                            daysLeft in 4..7 -> "Sisa $daysLeft hari" to WarningText
                            daysLeft in 8..14 -> "Sisa $daysLeft hari" to AttentionText
                            else -> "Sisa $daysLeft hari" to NormalText
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToBatchDetail(item.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = CardSurface,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formattedProductName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = Slate900
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    UrgencyBadge(urgencyLevel = item.urgencyLevel())
                                }

                                Text(
                                    text = "${item.category.label} • $formattedQuantity",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        color = Slate500
                                    )
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (daysLeft <= 0) {
                                        Icon(
                                            imageVector = Icons.Outlined.Warning,
                                            contentDescription = null,
                                            tint = daysLeftColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = daysLeftText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = daysLeftColor,
                                            fontFeatureSettings = "tnum"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Padding so no content is clipped by FAB or bottom bar
                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    highlightBg: Color = ExpiredBg,
    highlightBorder: Color = ExpiredBorder,
    highlightTextColor: Color = ExpiredText
) {
    val cardBg = if (isHighlighted && count > 0) highlightBg else CardSurface
    val cardBorder = if (isHighlighted && count > 0) highlightBorder else CardBorder
    val numberColor = if (isHighlighted && count > 0) highlightTextColor else Slate900

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    color = if (isHighlighted && count > 0) highlightTextColor else Slate500
                ),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = numberColor
            )
        }
    }
}

@Composable
private fun StatCardWide(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    highlightBg: Color = ExpiredBg,
    highlightBorder: Color = ExpiredBorder,
    highlightTextColor: Color = ExpiredText
) {
    val cardBg = if (isHighlighted && count > 0) highlightBg else CardSurface
    val cardBorder = if (isHighlighted && count > 0) highlightBorder else CardBorder
    val numberColor = if (isHighlighted && count > 0) highlightTextColor else Slate900

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate500
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = numberColor
            )
        }
    }
}

private fun formatQuantity(quantity: Double, unit: String): String {
    val integerPart = quantity.toLong()
    val formattedNumber = if (quantity == integerPart.toDouble()) {
        NumberFormat.getNumberInstance(Locale("id", "ID")).format(integerPart)
    } else {
        NumberFormat.getNumberInstance(Locale("id", "ID")).format(quantity)
    }
    return "$formattedNumber $unit"
}

private fun capitalizeWords(text: String): String {
    return text.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() }
    }
}
