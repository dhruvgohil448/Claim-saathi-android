package com.claimsaathi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.claimsaathi.app.ui.AppState
import com.claimsaathi.app.ui.AppTab
import com.claimsaathi.app.ui.AssistantScreen
import com.claimsaathi.app.ui.ClaimDetailScreen
import com.claimsaathi.app.ui.ClaimSaathiTheme
import com.claimsaathi.app.ui.ClaimsScreen
import com.claimsaathi.app.ui.DocsScreen
import com.claimsaathi.app.ui.HomeScreen
import com.claimsaathi.app.ui.LoginScreen
import com.claimsaathi.app.ui.Navy
import com.claimsaathi.app.ui.PolicyScreen
import com.claimsaathi.app.ui.ProfileScreen
import com.claimsaathi.app.ui.SettlementScreen
import com.claimsaathi.app.ui.StartClaimScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClaimSaathiTheme {
                ClaimSaathiRoot()
            }
        }
    }
}

@Composable
private fun ClaimSaathiRoot(state: AppState = viewModel()) {
    val nav = rememberNavController()
    val backStack = nav.currentBackStackEntryAsState()
    val route = backStack.value?.destination?.route
    val showBar = route == "main"

    Scaffold(
        containerColor = com.claimsaathi.app.ui.AppBackground,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                    TabItem.entries.forEach { item ->
                        NavigationBarItem(
                            selected = state.tab == item.tab,
                            onClick = { state.tab = item.tab },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = Navy,
                                selectedTextColor = Navy,
                                indicatorColor = com.claimsaathi.app.ui.Pale
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "login",
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen {
                    state.login()
                    nav.navigate("main") { popUpTo("login") { inclusive = true } }
                }
            }
            composable("main") {
                when (state.tab) {
                    AppTab.Home -> HomeScreen(
                        state = state,
                        onOpenClaim = { nav.navigate("claim/$it") },
                        onOpenPolicy = { nav.navigate("policy") },
                        onStartClaim = { nav.navigate("start") },
                        onOpenDocs = { state.tab = AppTab.Docs },
                        onOpenAssistant = { state.tab = AppTab.Assistant }
                    )
                    AppTab.Claims -> ClaimsScreen(state) { nav.navigate("claim/$it") }
                    AppTab.Docs -> DocsScreen(state)
                    AppTab.Assistant -> AssistantScreen(state)
                    AppTab.Profile -> ProfileScreen(state) {
                        state.logout()
                        nav.navigate("login") { popUpTo("main") { inclusive = true } }
                    }
                }
            }
            composable("policy") {
                DetailPage("Policy", onBack = { nav.popBackStack() }) {
                    PolicyScreen(state) {
                        state.tab = AppTab.Assistant
                        nav.popBackStack()
                    }
                }
            }
            composable("start") {
                DetailPage("New claim", onBack = { nav.popBackStack() }) {
                    StartClaimScreen(state) {
                        state.tab = AppTab.Claims
                        nav.popBackStack()
                    }
                }
            }
            composable(
                "claim/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString("id").orEmpty()
                DetailPage(id, onBack = { nav.popBackStack() }) {
                    ClaimDetailScreen(state, id) { nav.navigate("settlement/$id") }
                }
            }
            composable(
                "settlement/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { entry ->
                DetailPage("Settlement", onBack = { nav.popBackStack() }) {
                    SettlementScreen(state, entry.arguments?.getString("id").orEmpty())
                }
            }
        }
    }
}

@Composable
private fun DetailPage(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TextButton(onClick = onBack) {
            Text("‹  $title", color = Navy, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        }
        Box(Modifier.weight(1f)) { content() }
    }
}

private enum class TabItem(
    val tab: AppTab,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Home(AppTab.Home, "Home", Icons.Filled.Home),
    Claims(AppTab.Claims, "Claims", Icons.AutoMirrored.Outlined.ListAlt),
    Docs(AppTab.Docs, "Docs", Icons.Filled.Description),
    Assistant(AppTab.Assistant, "AI", Icons.Outlined.AutoAwesome),
    Profile(AppTab.Profile, "Profile", Icons.Filled.Person)
}
