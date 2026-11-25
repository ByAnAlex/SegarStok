package com.segarstok.feature.recommendation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.segarstok.core.design.component.UrgencyBadge
import com.segarstok.core.design.component.LoadingStateContainer
import com.segarstok.core.design.theme.*
import com.segarstok.core.ui.ViewModelFactory
import com.segarstok.domain.recommendation.BatchRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    onNavigateToBatchDetail: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecommendationViewModel = viewModel(
        factory = ViewModelFactory.getInstance(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saran Tindakan",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Slate900
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CanvasBackground,
                    titleContentColor = Slate900
                )
            )
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 100.dp)
            ) {
                // Food Safety Banner
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = PrimaryTealLight,
                        border = BorderStroke(1.dp, PrimaryTealContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Prinsip Rekomendasi & Keamanan Pangan",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryTealDark
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Saran bersifat operasional berbasis aturan transparan. Produk lewat tanggal wajib dihentikan penjualannya dan tidak boleh didiskon. Aplikasi tidak mengubah harga atau stok tanpa persetujuan Anda.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = PrimaryTealDark
                                )
                            )
                        }
                    }
                }

                // Discount Rate Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Simulasi diskon promosi",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                color = Slate500
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val rates = listOf(0.10, 0.20, 0.30)
                            rates.forEach { rate ->
                                val percentText = "${(rate * 100).toInt()}%"
                                val isSelected = uiState.selectedDiscountRate == rate
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.onDiscountRateChange(rate) },
                                    label = {
                                        Text(
                                            text = percentText,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 15.sp
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(50.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryTeal,
                                        selectedLabelColor = Color.White,
                                        containerColor = CardSurface,
                                        labelColor = Slate700
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        selectedBorderColor = PrimaryTeal,
                                        borderColor = CardBorder,
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 0.dp
                                    )
                                )
                            }
                        }
                    }
                }

                if (uiState.recommendations.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = CardSurface,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada stok aktif yang memerlukan rekomendasi.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        color = Slate500
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.recommendations) { item ->
                        RecommendationCardItem(
                            item = item,
                            onClick = { onNavigateToBatchDetail(item.batchId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationCardItem(
    item: BatchRecommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daysText = when {
        item.daysLeft < 0 -> "lewat ${-item.daysLeft} hari"
        item.daysLeft == 0L -> "Hari H"
        else -> "sisa ${item.daysLeft} hari"
    }

    val actionColor = if (item.isExpiredGuardrail) ExpiredText else PrimaryTeal

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Product name + urgency badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate900
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UrgencyBadge(urgencyLevel = item.urgencyLevel)
            }

            // Row 2: Category · quantity · days
            Text(
                text = "${item.category.label} · ${item.quantity} ${item.unit} · $daysText",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = Slate500
                )
            )

            HorizontalDivider(color = DividerColor, thickness = 1.dp)

            // Action direction title
            Text(
                text = item.actionDirection,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = actionColor
                )
            )

            // Detailed advice
            Text(
                text = item.detailedAdvice,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Slate900
                )
            )

            // Category advice
            Text(
                text = "${item.category.label}: ${item.categoryAdvice}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Slate500
                )
            )

            // Promo simulation (optional)
            item.promoSimulation?.let { sim ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NormalBg,
                    border = BorderStroke(1.dp, NormalBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = sim.formattedText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                color = NormalText
                            )
                        )
                        if (sim.marginWarning != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sim.marginWarning,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    color = ExpiredText
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
