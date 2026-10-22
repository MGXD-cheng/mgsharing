package com.lingxi.mgsharing.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lingxi.mgsharing.R
import com.lingxi.mgsharing.ui.screen.HistoryScreen
import com.lingxi.mgsharing.ui.screen.HomeScreen
import com.lingxi.mgsharing.ui.screen.SettingsScreen
import com.lingxi.mgsharing.viewmodel.HistoryViewModel
import com.lingxi.mgsharing.viewmodel.SettingsViewModel
import com.lingxi.mgsharing.viewmodel.ShareViewModel

/** 底部导航 Tab 定义 */
private data class TabItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem("share", R.string.tab_share, Icons.Filled.Share),
    TabItem("history", R.string.tab_history, Icons.Filled.History),
    TabItem("settings", R.string.tab_settings, Icons.Filled.Settings)
)

/**
 * 应用导航骨架 —— Scaffold + NavigationBar + NavHost 三 Tab
 */
@Composable
fun AppNavigation(
    shareViewModel: ShareViewModel,
    historyViewModel: HistoryViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "share"
        ) {
            composable("share") {
                HomeScreen(
                    viewModel = shareViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable("history") {
                HistoryScreen(
                    viewModel = historyViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            composable("settings") {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    padding = innerPadding
                )
            }
        }
    }
}