package com.citana.app.ui.nav

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.citana.app.R
import com.citana.app.core.ThemeController
import com.citana.app.core.ThemeMode
import com.citana.app.data.auth.AuthRepository
import com.citana.app.ui.auth.AuthScreen
import com.citana.app.ui.theme.CitanaTheme
import com.citana.app.ui.booking.BookScreen
import com.citana.app.ui.bookings.BookingsScreen
import com.citana.app.ui.browse.BrowseScreen
import com.citana.app.ui.home.HomeScreen
import com.citana.app.ui.profile.ProfileScreen
import com.citana.app.ui.provider.ProviderScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

object Routes {
    const val HOME = "home"
    const val BOOKINGS = "bookings"
    const val PROFILE = "profile"
    const val BROWSE = "browse/{slug}"
    const val PROVIDER = "provider/{id}"
    const val BOOK = "book/{providerId}/{serviceId}"

    fun browse(slug: String) = "browse/$slug"
    fun provider(id: String) = "provider/$id"
    fun book(providerId: String, serviceId: String) = "book/$providerId/$serviceId"
}

@HiltViewModel
class RootViewModel @Inject constructor(
    authRepository: AuthRepository,
    themeController: ThemeController,
) : ViewModel() {
    val signedIn: StateFlow<Boolean> = authRepository.signedIn
    val themeMode: StateFlow<ThemeMode> = themeController.mode
}

@Composable
fun CitanaRoot(viewModel: RootViewModel = hiltViewModel()) {
    val signedIn by viewModel.signedIn.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    CitanaTheme(darkTheme = dark) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            if (signedIn) MainScaffold() else AuthScreen()
        }
    }
}

private data class Tab(val route: String, val icon: ImageVector, val labelRes: Int)

@Composable
private fun MainScaffold() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route

    val tabs = listOf(
        Tab(Routes.HOME, Icons.Outlined.Home, R.string.nav_home),
        Tab(Routes.BOOKINGS, Icons.Outlined.CalendarMonth, R.string.nav_bookings),
        Tab(Routes.PROFILE, Icons.Outlined.Person, R.string.nav_profile),
    )
    val showBar = tabs.any { it.route == current }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = current == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = Routes.HOME, modifier = Modifier.padding(padding)) {
            composable(Routes.HOME) {
                HomeScreen(
                    onCategory = { nav.navigate(Routes.browse(it)) },
                    onProvider = { nav.navigate(Routes.provider(it)) },
                )
            }
            composable(Routes.BOOKINGS) { BookingsScreen() }
            composable(Routes.PROFILE) { ProfileScreen() }

            composable(
                Routes.BROWSE,
                arguments = listOf(navArgument("slug") { type = NavType.StringType }),
            ) {
                BrowseScreen(
                    onProvider = { nav.navigate(Routes.provider(it)) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(
                Routes.PROVIDER,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ProviderScreen(
                    onBook = { serviceId -> nav.navigate(Routes.book(id, serviceId)) },
                    onBack = { nav.popBackStack() },
                )
            }
            composable(
                Routes.BOOK,
                arguments = listOf(
                    navArgument("providerId") { type = NavType.StringType },
                    navArgument("serviceId") { type = NavType.StringType },
                ),
            ) {
                BookScreen(
                    onDone = {
                        nav.navigate(Routes.BOOKINGS) {
                            popUpTo(Routes.HOME)
                            launchSingleTop = true
                        }
                    },
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}
