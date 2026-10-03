package com.claimsaathi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.claimsaathi.app.data.Network
import com.claimsaathi.app.data.TokenStore
import com.claimsaathi.app.ui.AddPolicyScreen
import com.claimsaathi.app.ui.AlertsTab
import com.claimsaathi.app.ui.AppTab
import com.claimsaathi.app.ui.AppVm
import com.claimsaathi.app.ui.ChatTab
import com.claimsaathi.app.ui.BankScreen
import com.claimsaathi.app.ui.ChecklistScreen
import com.claimsaathi.app.ui.ClaimSaathiTheme
import com.claimsaathi.app.ui.ClaimsTab
import com.claimsaathi.app.ui.HomeTab
import com.claimsaathi.app.ui.LoginScreen
import com.claimsaathi.app.ui.Navy
import com.claimsaathi.app.ui.OtpScreen
import com.claimsaathi.app.ui.Pale
import com.claimsaathi.app.ui.PolicyReaderScreen
import com.claimsaathi.app.ui.Primary
import com.claimsaathi.app.ui.ProfileSetupScreen
import com.claimsaathi.app.ui.ProfileTab
import com.claimsaathi.app.ui.QueriesScreen
import com.claimsaathi.app.ui.QueryDetailScreen
import com.claimsaathi.app.ui.SettlementScreen
import com.claimsaathi.app.ui.StartClaimScreen
import com.claimsaathi.app.ui.TrackingScreen
import com.claimsaathi.app.ui.ValidationScreen
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClaimSaathiTheme { Root() }
        }
    }
}

@Composable
private fun Root(vm: AppVm = viewModel()) {
    val nav = rememberNavController()
    LaunchedEffect(Unit) {
        if (TokenStore.token != null) {
            runCatching { Network.api.me() }.onSuccess { vm.signIn(TokenStore.token!!, it) }.onFailure { vm.logout() }
        }
        TokenStore.sessionExpired.collectLatest {
            vm.logout()
            nav.navigate("login") { popUpTo(0) }
        }
    }
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val showBar = route == "main"
    val askSaathi: (String, String?) -> Unit = { prompt, claimId ->
        vm.chatPrompt = prompt
        vm.chatClaimId = claimId
        vm.tab = AppTab.Assistant
        if (route != "main") nav.popBackStack("main", inclusive = false)
    }
    val slide = tween<IntOffset>(360)
    Scaffold(
        containerColor = com.claimsaathi.app.ui.AppBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                    Bar.entries.forEach { item ->
                        NavigationBarItem(
                            selected = vm.tab == item.tab,
                            onClick = { vm.tab = item.tab },
                            icon = {
                                if (item.tab == AppTab.Alerts && vm.unread > 0) {
                                    BadgedBox(badge = { Badge { Text(vm.unread.toString()) } }) {
                                        Icon(item.icon, contentDescription = item.label)
                                    }
                                } else Icon(item.icon, contentDescription = item.label)
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Navy,
                                indicatorColor = Pale
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            nav,
            startDestination = if (TokenStore.token == null) "login" else "main",
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, slide) },
            exitTransition = { fadeOut(tween(180)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, slide) },
            popEnterTransition = { fadeIn(tween(220)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, slide) },
            popExitTransition = { fadeOut(tween(180)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, slide) }
        ) {
            composable("login") { LoginScreen(vm) { nav.navigate("otp/$it") } }
            composable("otp/{phone}", listOf(navArgument("phone") { type = NavType.StringType })) { entry ->
                OtpScreen(
                    vm,
                    entry.arguments?.getString("phone").orEmpty(),
                    onBack = { nav.popBackStack() },
                    onProfile = { nav.navigate("profile") },
                    onHome = { nav.navigate("main") { popUpTo("login") { inclusive = true } } }
                )
            }
            composable("profile") {
                ProfileSetupScreen(vm) { nav.navigate("main") { popUpTo("login") { inclusive = true } } }
            }
            composable("main") {
                when (vm.tab) {
                    AppTab.Home -> HomeTab(
                        vm,
                        openClaim = { nav.navigate("claim/$it") },
                        openQuery = { nav.navigate("query/$it") },
                        openChecklist = { nav.navigate("checklist/$it") },
                        openPolicy = { nav.navigate("reader/$it") },
                        addPolicy = { nav.navigate("add-policy") },
                        openBank = { nav.navigate("bank") },
                        openProfile = { vm.tab = AppTab.Profile },
                        startClaim = { nav.navigate("start") },
                        askSaathi = { askSaathi(it, vm.home?.currentClaim?.id) }
                    )
                    AppTab.Claims -> ClaimsTab(vm) { nav.navigate("claim/$it") }
                    AppTab.Assistant -> ChatTab(vm)
                    AppTab.Alerts -> AlertsTab(vm) { nav.navigate("claim/$it") }
                    AppTab.Profile -> ProfileTab(vm, { nav.navigate("bank") }) {
                        vm.logout()
                        nav.navigate("login") { popUpTo(0) }
                    }
                }
            }
            composable("add-policy") { AddPolicyScreen(vm, { nav.popBackStack() }) { nav.navigate("reader/$it") } }
            composable("reader/{id}", listOf(navArgument("id") { type = NavType.StringType })) {
                PolicyReaderScreen(it.arguments?.getString("id").orEmpty(), { nav.popBackStack() }) {
                    vm.tab = AppTab.Assistant
                    nav.navigate("main")
                }
            }
            composable("start") { StartClaimScreen(vm, { nav.popBackStack() }) { nav.navigate("checklist/$it") } }
            composable("checklist/{id}", listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                ChecklistScreen(vm, id, { nav.popBackStack() }, { nav.navigate("validation") }, askSaathi = { askSaathi(it, id) })
            }
            composable("validation") {
                ValidationScreen(vm.lastUpload, { nav.popBackStack() }, {
                    val id = vm.lastUpload?.checklist?.claimId
                    if (!id.isNullOrBlank()) nav.navigate("claim/$id")
                })
            }
            composable("claim/{id}", listOf(navArgument("id") { type = NavType.StringType })) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                TrackingScreen(
                    id,
                    onBack = { nav.popBackStack() },
                    onQuery = { nav.navigate("queries") },
                    onSettlement = { nav.navigate("settlement/$id") },
                    onDocs = { nav.navigate("checklist/$id") },
                    askSaathi = { askSaathi(it, id) }
                )
            }
            composable("queries") { QueriesScreen({ nav.popBackStack() }) { nav.navigate("query/$it") } }
            composable("query/{id}", listOf(navArgument("id") { type = NavType.StringType })) {
                QueryDetailScreen(vm, it.arguments?.getString("id").orEmpty()) { nav.popBackStack() }
            }
            composable("settlement/{id}", listOf(navArgument("id") { type = NavType.StringType })) {
                SettlementScreen(it.arguments?.getString("id").orEmpty()) { nav.popBackStack() }
            }
            composable("bank") { BankScreen(vm, { nav.popBackStack() }) { nav.popBackStack() } }
        }
    }
}

private enum class Bar(val tab: AppTab, val label: String, val icon: ImageVector) {
    Home(AppTab.Home, "Home", Icons.Filled.Home),
    Claims(AppTab.Claims, "Claims", Icons.AutoMirrored.Outlined.ListAlt),
    Assistant(AppTab.Assistant, "Chat", Icons.Outlined.AutoAwesome),
    Alerts(AppTab.Alerts, "Alerts", Icons.Filled.Notifications),
    Profile(AppTab.Profile, "Profile", Icons.Filled.Person)
}
