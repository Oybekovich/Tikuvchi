package uz.tikuvchi.ui.screens.search

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uz.tikuvchi.R
import uz.tikuvchi.data.Reconnect
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.ErrorState
import uz.tikuvchi.ui.components.LabeledField
import uz.tikuvchi.ui.components.SearchBar
import uz.tikuvchi.ui.components.UstaCardItem
import uz.tikuvchi.ui.components.bottomNavSpace
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Gold400
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra600

@Composable
fun SearchScreen(
    initialText: String,
    initialCategory: Long?,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onUsta: (String) -> Unit,
) {
    val vm: SearchViewModel = viewModel(
        factory = viewModelFactory { initializer { SearchViewModel(initialText, initialCategory) } },
    )
    val s by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding().imePadding()) {
        AppHeader(title = stringResource(R.string.search_title), onBack = onBack, onProfile = onProfile)

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchBar(
                value = s.query.text,
                onValueChange = vm::setText,
                placeholder = stringResource(R.string.home_search_placeholder),
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (s.filtersOpen) Terra600 else Color.White)
                    .clickable { vm.toggleFilters() }
                    .padding(14.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_filters),
                    contentDescription = stringResource(R.string.search_filters),
                    tint = if (s.filtersOpen) Color.White else Ink700,
                )
            }
        }

        if (s.filtersOpen) Filters(s, vm)

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }

            // Xatoni "topilmadi" dan ajratamiz: aks holda ilova tarmoq uzilganida
            // "0 ta usta topildi, filtrlarni o'zgartiring" deb yanglish yo'naltiradi
            s.error -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                ErrorState(onRetry = Reconnect::request)
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    bottom = bottomNavSpace(),
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        stringResource(R.string.search_found, s.results.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink500,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }
                if (s.results.isEmpty()) {
                    item {
                        EmptyState(
                            icon = R.drawable.ic_search_off,
                            title = stringResource(R.string.search_empty),
                            hint = stringResource(R.string.search_empty_hint),
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    items(s.results, key = { it.userId }) { u ->
                        UstaCardItem(
                            usta = u,
                            onClick = { onUsta(u.userId) },
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Filters(s: SearchUiState, vm: SearchViewModel) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FilterRow(stringResource(R.string.search_category)) {
            Chip(stringResource(R.string.common_all), s.query.categoryId == null) { vm.setCategory(null) }
            s.categories.forEach { c ->
                Chip(c.name, s.query.categoryId == c.id) { vm.setCategory(c.id) }
            }
        }

        FilterRow(stringResource(R.string.search_district)) {
            Chip(stringResource(R.string.common_all), s.query.district == null) { vm.setDistrict(null) }
            s.districts.forEach { d ->
                Chip(d, s.query.district == d) { vm.setDistrict(d) }
            }
        }

        FilterRow(stringResource(R.string.search_min_rating)) {
            Chip(stringResource(R.string.common_all), s.query.minRating == null) { vm.setMinRating(null) }
            listOf(4.0, 4.5, 4.8).forEach { r ->
                Chip(
                    text = "$r",
                    selected = s.query.minRating == r,
                    leadingIcon = R.drawable.ic_star,
                    onClick = { vm.setMinRating(r) },
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                LabeledField(
                    label = stringResource(R.string.search_price_from),
                    value = s.query.minPrice?.toString().orEmpty(),
                    onValueChange = { vm.setMinPrice(it.filter(Char::isDigit).toLongOrNull()) },
                    keyboardType = KeyboardType.Number,
                )
            }
            Column(Modifier.weight(1f)) {
                LabeledField(
                    label = stringResource(R.string.search_price_to),
                    value = s.query.maxPrice?.toString().orEmpty(),
                    onValueChange = { vm.setMaxPrice(it.filter(Char::isDigit).toLongOrNull()) },
                    keyboardType = KeyboardType.Number,
                )
            }
        }

        Text(
            stringResource(R.string.search_reset),
            style = MaterialTheme.typography.labelLarge,
            color = Terra600,
            modifier = Modifier.clickable { vm.reset() }.padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun FilterRow(label: String, chips: @Composable () -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Ink500)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) { chips() }
    }
}

@Composable
private fun Chip(
    text: String,
    selected: Boolean,
    @DrawableRes leadingIcon: Int? = null,
    onClick: () -> Unit,
) {
    val fg = if (selected) Color.White else Ink700
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (selected) Terra600 else Cream200)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                // Tanlanganda oq, aks holda oltin — nishondagi yulduz bilan bir xil
                tint = if (selected) Color.White else Gold400,
                modifier = Modifier.size(13.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}
