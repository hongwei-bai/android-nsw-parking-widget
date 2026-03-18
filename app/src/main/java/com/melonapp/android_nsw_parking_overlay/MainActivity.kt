package com.melonapp.android_nsw_parking_overlay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melonapp.android_nsw_parking_overlay.data.DataStoreManager
import com.melonapp.android_nsw_parking_overlay.data.api.RetrofitClient
import com.melonapp.android_nsw_parking_overlay.data.repository.CarParkRepository
import com.melonapp.android_nsw_parking_overlay.ui.CarParkViewModel
import com.melonapp.android_nsw_parking_overlay.ui.CarParkViewModelFactory
import com.melonapp.android_nsw_parking_overlay.ui.theme.AndroidnswparkingoverlayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = CarParkRepository(RetrofitClient.apiService)
        val dataStoreManager = DataStoreManager(applicationContext)
        val factory = CarParkViewModelFactory(repository, dataStoreManager)

        setContent {
            AndroidnswparkingoverlayTheme {
                val viewModel: CarParkViewModel = viewModel(factory = factory)
                AndroidnswparkingoverlayApp(viewModel)
            }
        }
    }
}

@Composable
fun AndroidnswparkingoverlayApp(viewModel: CarParkViewModel) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val uiState by viewModel.uiState.collectAsState()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen(viewModel, uiState)
                    AppDestinations.FAVORITES -> FavoritesScreen(viewModel, uiState)
                    AppDestinations.PROFILE -> ProfileScreen(viewModel, uiState)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: CarParkViewModel, uiState: com.melonapp.android_nsw_parking_overlay.ui.CarParkUiState) {
    var apiKeyInput by remember { mutableStateOf(uiState.apiKey) }
    var apiKeyVisible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuration", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Prevent copy/cut/paste toolbar from appearing for the API key field.
        val noTextToolbar = remember {
            object : TextToolbar {
                override val status: TextToolbarStatus = TextToolbarStatus.Hidden
                override fun hide() = Unit
                override fun showMenu(
                    rect: Rect,
                    onCopyRequested: (() -> Unit)?,
                    onPasteRequested: (() -> Unit)?,
                    onCutRequested: (() -> Unit)?,
                    onSelectAllRequested: (() -> Unit)?
                ) = Unit
            }
        }

        CompositionLocalProvider(LocalTextToolbar provides noTextToolbar) {
            DisableSelection {
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Transport for NSW API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        autoCorrectEnabled = false
                    ),
                    trailingIcon = {
                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            val iconRes = if (apiKeyVisible) android.R.drawable.ic_menu_view else android.R.drawable.ic_menu_close_clear_cancel
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = if (apiKeyVisible) "Hide API Key" else "Show API Key"
                            )
                        }
                    }
                )
            }
        }
        Button(
            onClick = { viewModel.setApiKey(apiKeyInput) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Save API Key")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Car Park Selection", style = MaterialTheme.typography.headlineSmall)
        Button(
            onClick = { viewModel.fetchFacilities() },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Fetch Facilities")
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
        }

        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(top = 16.dp)) {
            items(uiState.facilities.toList()) { (id, name) ->
                val isSelected = uiState.selectedCarParks.any { it.id == id }
                ListItem(
                    headlineContent = { Text(name) },
                    supportingContent = { Text(id) },
                    trailingContent = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggleCarParkSelection(id, name) }
                        )
                    },
                    modifier = Modifier.clickable { viewModel.toggleCarParkSelection(id, name) }
                )
            }
        }
    }
}

@Composable
fun FavoritesScreen(viewModel: CarParkViewModel, uiState: com.melonapp.android_nsw_parking_overlay.ui.CarParkUiState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Selected Car Parks", style = MaterialTheme.typography.headlineMedium)
        Text("Max 3 allowed for widget", style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = { viewModel.refreshSelectedCarParks() }) {
            Text("Refresh Now")
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
            items(uiState.selectedCarParks) { carPark ->
                ListItem(
                    headlineContent = { Text("${carPark.name} (${carPark.abbr})") },
                    supportingContent = { Text("Available: ${carPark.availableSpots}") },
                    trailingContent = {
                        IconButton(onClick = { viewModel.toggleCarParkSelection(carPark.id, carPark.name) }) {
                            Icon(painterResource(android.R.drawable.ic_menu_delete), contentDescription = "Remove")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: CarParkViewModel, uiState: com.melonapp.android_nsw_parking_overlay.ui.CarParkUiState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("NSW Parking Widget App")
        Text("Version 1.0")
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Setup", R.drawable.ic_home),
    FAVORITES("Selected", R.drawable.ic_favorite),
    PROFILE("About", R.drawable.ic_account_box),
}
