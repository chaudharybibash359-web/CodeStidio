package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectEntity
import com.example.runner.ConsoleLog
import com.example.ui.components.SandboxPreview
import com.example.ui.components.ViewportMode
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeSuccess
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunnerScreen(
    project: ProjectEntity?,
    compiledHtml: String,
    consoleLogs: List<ConsoleLog>,
    isGamePadVisible: Boolean,
    isConsoleOpen: Boolean,
    onToggleGamePad: () -> Unit,
    onToggleConsole: () -> Unit,
    onLogReceived: (ConsoleLog) -> Unit,
    onClearLogs: () -> Unit,
    onBuildApk: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    var reloadKey by remember { mutableStateOf(0) }
    var currentViewport by remember { mutableStateOf(ViewportMode.PHONE_FULL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top HUD Bar
        TopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("runner_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Editor",
                        tint = Color.White
                    )
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(VsCodeSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = project?.name ?: "Live Runner",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Sandbox Runtime • Protected",
                            fontSize = 10.sp,
                            color = VsCodeCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            actions = {
                // Viewport mode switch (Full -> Phone Frame -> Tablet Frame)
                IconButton(
                    onClick = {
                        currentViewport = when (currentViewport) {
                            ViewportMode.PHONE_FULL -> ViewportMode.PHONE_FRAME
                            ViewportMode.PHONE_FRAME -> ViewportMode.TABLET_FRAME
                            ViewportMode.TABLET_FRAME -> ViewportMode.PHONE_FULL
                        }
                    },
                    modifier = Modifier.testTag("runner_viewport_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Switch Viewport",
                        tint = if (currentViewport == ViewportMode.PHONE_FULL) VsCodeTextMuted else VsCodeCyan
                    )
                }

                // Restart / Reload button
                IconButton(
                    onClick = { reloadKey++ },
                    modifier = Modifier.testTag("runner_reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart Sandbox",
                        tint = Color.White
                    )
                }

                // Gamepad Controls Toggle
                IconButton(
                    onClick = onToggleGamePad,
                    modifier = Modifier.testTag("runner_gamepad_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = "Toggle Game Controls",
                        tint = if (isGamePadVisible) VsCodeCyan else VsCodeTextMuted
                    )
                }

                // Developer Console Toggle
                IconButton(
                    onClick = onToggleConsole,
                    modifier = Modifier.testTag("runner_console_toggle")
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Toggle Console",
                            tint = if (isConsoleOpen) VsCodeBlue else VsCodeTextMuted
                        )
                        if (consoleLogs.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(VsCodeWarning)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }

                // Build & Package shortcut
                IconButton(
                    onClick = onBuildApk,
                    modifier = Modifier.testTag("runner_build_shortcut")
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Build App",
                        tint = VsCodeBlue
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = VsCodeActivityBar
            )
        )

        // Sandbox Preview Component
        SandboxPreview(
            compiledHtml = compiledHtml,
            modifier = Modifier.weight(1f),
            isGamePadVisible = isGamePadVisible,
            isConsoleOpen = isConsoleOpen,
            consoleLogs = consoleLogs,
            viewportMode = currentViewport,
            onLogReceived = onLogReceived,
            onClearLogs = onClearLogs,
            onToggleGamePad = onToggleGamePad,
            onToggleConsole = onToggleConsole,
            reloadKey = reloadKey
        )
    }
}
