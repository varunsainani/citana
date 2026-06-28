package com.citana.app.ui.bookings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citana.app.R
import com.citana.app.domain.model.Booking
import com.citana.app.ui.components.EmptyBox
import com.citana.app.ui.components.LoadingBox
import com.citana.app.ui.components.SectionHeader
import com.citana.app.ui.util.formatDateTime
import com.citana.app.ui.util.formatMoney
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingsScreen(viewModel: BookingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.nav_bookings)) })
        },
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding))
            state.bookings.isEmpty() -> EmptyBox(
                icon = Icons.Outlined.EventBusy,
                title = stringResource(R.string.bookings_empty_title),
                body = stringResource(R.string.bookings_empty_body),
                modifier = Modifier.padding(padding),
            )
            else -> {
                val upcoming = state.bookings.filter(::isUpcoming)
                val past = state.bookings.filterNot(::isUpcoming)
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    if (upcoming.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.bookings_upcoming)) }
                        items(upcoming, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                cancellable = true,
                                onCancel = { viewModel.cancel(booking.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                    if (past.isNotEmpty()) {
                        item { SectionHeader(stringResource(R.string.bookings_past)) }
                        items(past, key = { it.id }) { booking ->
                            BookingCard(
                                booking = booking,
                                cancellable = false,
                                onCancel = {},
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isUpcoming(booking: Booking): Boolean =
    booking.status == "confirmed" &&
        runCatching { Instant.parse(booking.startAt).isAfter(Instant.now()) }.getOrDefault(false)

@Composable
private fun BookingCard(
    booking: Booking,
    cancellable: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(booking.providerName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        booking.serviceName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                StatusChip(booking.status, modifier = Modifier.padding(start = 8.dp))
            }
            Text(
                formatDateTime(booking.startAt),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    formatMoney(booking.priceCents, booking.currency),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                if (cancellable) {
                    OutlinedButton(onClick = onCancel) {
                        Text(stringResource(R.string.bookings_cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val confirmed = status == "confirmed"
    val color = if (confirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val label = stringResource(if (confirmed) R.string.status_confirmed else R.string.status_cancelled)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
