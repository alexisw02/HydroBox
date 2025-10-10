package com.hydrobox.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import com.hydrobox.app.ui.components.HydroTopBar
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter

sealed class Route(val path: String) {

    data object Resume : Route("resume")
    data object Sensors : Route("sensors")
    data object Actuators : Route("actuators")
    data object History : Route("history")
    data object Crops : Route("crops")

    data object Account : Route("account")
    data object Settings : Route("settings")
    data object Login : Route("login")
    data object Notification : Route("notification")
}

data class BottomItem(val route: Route, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Route.Resume, "Resumen", Icons.Filled.Dashboard),
    BottomItem(Route.Sensors, "Sensores", Icons.Filled.Speed),
    BottomItem(Route.Actuators, "Actuadores", Icons.Filled.ToggleOn),
    BottomItem(Route.History, "Historial", Icons.Filled.History),
    BottomItem(Route.Crops, "Gestión", Icons.Filled.Yard)
)

data class AdminUser(
    val name: String,
    val lastName: String,
    val role: String,
    val email: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HydroNavRoot() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val admin = remember {
        AdminUser(name = "Hydro", lastName = "Admin", role = "Administrador", email = "admin@hydrobox.local")
    }
    var adminName by remember { mutableStateOf("${admin.name} ${admin.lastName}") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    fun navigateFromDrawer(route: String) {
        scope.launch { drawerState.close() }
        nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val showBottomBar = current.isInHierarchyOf(
        Route.Resume.path, Route.Sensors.path, Route.Actuators.path, Route.History.path, Route.Crops.path
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                DrawerHeader(
                    adminName = adminName,
                    avatarUri = avatarUri,
                    onAccount = { navigateFromDrawer(Route.Account.path) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                DrawerContent(
                    onResume = { navigateFromDrawer(Route.Resume.path) },
                    onAccount = { navigateFromDrawer(Route.Account.path) },
                    onSettings = { navigateFromDrawer(Route.Settings.path) },
                    onNotification = { navigateFromDrawer(Route.Notification.path) },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        nav.navigate(Route.Login.path) {
                            popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                HydroTopBar(
                    inRootTabs = showBottomBar,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateUp = { nav.navigateUp() }
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        bottomItems.forEach { item ->
                            val selected = current.isInHierarchyOf(item.route.path)
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    nav.navigate(item.route.path) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Route.Resume.path,
                modifier = Modifier.padding(padding)
            ) {
                composable(Route.Resume.path)   { ResumeScreen(paddingValues = PaddingValues()) }
                composable(Route.Sensors.path)  { SensorsScreen(paddingValues = PaddingValues()) }
                composable(Route.Actuators.path){ ActuatorsScreen(paddingValues = PaddingValues()) }
                composable(Route.History.path)  { HistoryScreen(paddingValues = PaddingValues()) }
                composable(Route.Crops.path)    { CropsScreen(paddingValues = PaddingValues()) }

                composable(Route.Account.path)  { AccountScreen(user = admin) }
                composable(Route.Settings.path) { SettingsScreen() }
                composable(Route.Notification.path) { NotificationScreen(paddingValues = PaddingValues()) }
                composable(Route.Login.path)    {
                    LoginScreen(
                        onLoggedIn = {
                            nav.navigate(Route.Resume.path) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerHeader(
    adminName: String,
    avatarUri: Uri?,
    onAccount: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (avatarUri != null) {
            Image(
                painter = rememberAsyncImagePainter(avatarUri),
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(56.dp).clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Perfil",
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Administrador", style = MaterialTheme.typography.labelMedium)
            Text(adminName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onAccount) { Text("Cuenta") }
    }
}

@Composable
private fun DrawerContent(
    onResume: () -> Unit,
    onAccount: () -> Unit,
    onSettings: () -> Unit,
    onNotification: () -> Unit,
    onLogout: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text("Resumen") },
        selected = false,
        onClick = onResume,
        icon = { Icon(Icons.Filled.Dashboard, null) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
    NavigationDrawerItem(
        label = { Text("Cuenta") },
        selected = false,
        onClick = onAccount,
        icon = { Icon(Icons.Outlined.AccountCircle, null) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
    NavigationDrawerItem(
        label = { Text("Configuración") },
        selected = false,
        onClick = onSettings,
        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
    NavigationDrawerItem(
        label = { Text("Notificación") },
        selected = false,
        onClick = onNotification,
        icon = { Icon(Icons.Filled.NotificationsActive, contentDescription = null) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
    NavigationDrawerItem(
        label = { Text("Cerrar sesión") },
        selected = false,
        onClick = onLogout,
        icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

private fun NavDestination?.isInHierarchyOf(vararg routes: String): Boolean =
    this?.hierarchy?.any { it.route in routes } == true
