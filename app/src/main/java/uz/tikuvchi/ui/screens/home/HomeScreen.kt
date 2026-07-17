package uz.tikuvchi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.tikuvchi.R
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.CategoryCard
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.components.UstaCardItem
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Terra600

@Composable
fun HomeScreen(
    onMenu: () -> Unit,
    onProfile: () -> Unit,
    onCategory: (Long) -> Unit,
    onUsta: (String) -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()

    // enableEdgeToEdge yoqilgani uchun header status bar ostiga kirib ketmasligi
    // kerak; ro'yxat esa pastdagi navigatsiya paneli ostidan silliq chiqadi
    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding()) {
        AppHeader(onMenu = onMenu, onProfile = onProfile)

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }

            s.error -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(R.string.common_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink500,
                )
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = stringResource(R.string.common_retry),
                    onClick = vm::load,
                    modifier = Modifier.fillMaxWidth(0.6f),
                )
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SectionTitle(stringResource(R.string.home_categories))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(s.categories, key = { it.id }) { c ->
                            CategoryCard(category = c, onClick = { onCategory(c.id) })
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    SectionTitle(stringResource(R.string.home_featured))
                }

                if (s.ustas.isEmpty()) {
                    item {
                        EmptyState(
                            icon = "🪡",
                            title = stringResource(R.string.home_empty_ustas),
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    items(s.ustas, key = { it.userId }) { u ->
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Ink900,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
