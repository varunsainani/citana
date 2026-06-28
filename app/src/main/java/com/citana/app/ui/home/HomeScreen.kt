package com.citana.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citana.app.R
import com.citana.app.ui.components.CategoryCard
import com.citana.app.ui.components.ErrorBox
import com.citana.app.ui.components.LoadingBox
import com.citana.app.ui.components.ProviderCard
import com.citana.app.ui.components.SectionHeader

@Composable
fun HomeScreen(
    onCategory: (String) -> Unit,
    onProvider: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when {
        state.loading -> LoadingBox()
        state.error -> ErrorBox(stringResource(R.string.error_generic), onRetry = viewModel::refresh)
        else -> LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)) {
                    val greet = if (state.firstName.isBlank()) "" else " ${state.firstName}"
                    Text(
                        stringResource(R.string.home_hello, greet),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            item { SectionHeader(stringResource(R.string.home_categories)) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.categories) { category ->
                        CategoryCard(category, onClick = { onCategory(category.slug) })
                    }
                }
            }
            item { SectionHeader(stringResource(R.string.home_featured)) }
            items(state.featured) { provider ->
                ProviderCard(
                    provider,
                    onClick = { onProvider(provider.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}
