package com.citana.app.ui.booking

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citana.app.R
import com.citana.app.ui.components.EmptyBox
import com.citana.app.ui.components.ErrorBox
import com.citana.app.ui.components.LoadingBox
import com.citana.app.ui.components.PrimaryButton
import com.citana.app.ui.components.SectionHeader
import com.citana.app.ui.util.formatMoney
import com.citana.app.ui.util.formatSlot
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: BookViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.booked) {
        if (state.booked) onDone()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.book_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            val service = state.service
            if (service != null) {
                Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(service.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                formatMoney(service.priceCents, service.currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (state.bookFailed) {
                            Text(
                                stringResource(R.string.book_failed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        PrimaryButton(
                            text = stringResource(
                                if (state.booking) R.string.book_booking else R.string.book_confirm,
                            ),
                            onClick = viewModel::confirm,
                            loading = state.booking,
                            enabled = state.selectedSlot != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.loading -> LoadingBox()
                state.error -> ErrorBox(
                    stringResource(R.string.error_generic),
                    onRetry = viewModel::load,
                )
                else -> BookContent(
                    state = state,
                    onDate = viewModel::selectDate,
                    onSlot = viewModel::selectSlot,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BookContent(
    state: BookUiState,
    onDate: (LocalDate) -> Unit,
    onSlot: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        SectionHeader(stringResource(R.string.book_pick_date))
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.dates.forEach { date ->
                FilterChip(
                    selected = date == state.selectedDate,
                    onClick = { onDate(date) },
                    label = { Text(dateLabel(date)) },
                )
            }
        }

        SectionHeader(stringResource(R.string.book_pick_time))
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                state.slotsLoading -> Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                state.slots.isEmpty() -> EmptyBox(
                    icon = Icons.Outlined.EventBusy,
                    title = stringResource(R.string.book_no_slots),
                    body = "",
                )
                else -> FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.slots.forEach { slot ->
                        FilterChip(
                            selected = slot == state.selectedSlot,
                            onClick = { onSlot(slot) },
                            label = { Text(formatSlot(slot)) },
                        )
                    }
                }
            }
        }
    }
}

private fun dateLabel(date: LocalDate): String {
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$weekday ${date.dayOfMonth}"
}
