package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Memory
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ChangeLogScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LocalModelsScreen
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.OmniViewModel

enum class NavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    CHAT("Chat", Icons.Default.Forum, "nav_chat"),
    LOCAL_MODELS("Local GGUF", Icons.Default.Memory, "nav_local_models"),
    DASHBOARD("Routing & APIs", Icons.Default.Dashboard, "nav_dashboard"),
    ANALYTICS("Analytics", Icons.Default.BarChart, "nav_analytics"),
    CHANGELOG("Logs", Icons.Default.Description, "nav_changelog")
}

class MainActivity : ComponentActivity() {

    private val viewModel: OmniViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: OmniViewModel) {
    var currentNavIndex by remember { mutableIntStateOf(0) }
    val changeLogs by viewModel.changeLogs.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            Surface(
                color = BentoSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBar(
                    containerColor = BentoSurfaceVariant,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    NavDestination.entries.forEachIndexed { index, destination ->
                        val isSelected = currentNavIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentNavIndex = index },
                            icon = {
                                if (destination == NavDestination.CHANGELOG && changeLogs.isNotEmpty()) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = BentoPrimary,
                                                contentColor = androidx.compose.ui.graphics.Color.White
                                            ) {
                                                Text("${changeLogs.size}", fontSize = 10.sp)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BentoOnPrimaryContainer,
                                selectedTextColor = BentoOnPrimaryContainer,
                                unselectedIconColor = BentoTextSecondary,
                                unselectedTextColor = BentoTextSecondary,
                                indicatorColor = BentoPrimaryContainer
                            ),
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentNavIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { targetIndex ->
            when (targetIndex) {
                0 -> ChatScreen(viewModel = viewModel)
                1 -> LocalModelsScreen(viewModel = viewModel)
                2 -> DashboardScreen(viewModel = viewModel)
                3 -> AnalyticsScreen(viewModel = viewModel)
                4 -> ChangeLogScreen(viewModel = viewModel)
            }
        }
    }
}
