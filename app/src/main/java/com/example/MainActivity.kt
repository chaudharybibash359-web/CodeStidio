package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.BuildModalScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.RunnerScreen
import com.example.ui.theme.CodeStudioTheme
import com.example.ui.theme.VsCodeBackground
import com.example.viewmodel.Screen
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodeStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = VsCodeBackground
                ) {
                    CodeStudioApp()
                }
            }
        }
    }
}

@Composable
fun CodeStudioApp(
    viewModel: StudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()

    when (uiState.currentScreen) {
        Screen.DASHBOARD -> {
            DashboardScreen(
                projects = projects,
                onOpenProject = { projectId -> viewModel.openProject(projectId) },
                onDeleteProject = { projectId -> viewModel.deleteProject(projectId) },
                onGenerateFromPrompt = { prompt, name ->
                    viewModel.generateProjectFromPrompt(prompt, name)
                },
                onCreateBlankProject = { name, desc, type ->
                    viewModel.createNewProject(name, desc, type)
                }
            )
        }

        Screen.EDITOR -> {
            BackHandler {
                viewModel.navigateTo(Screen.DASHBOARD)
            }
            EditorScreen(
                project = uiState.activeProject,
                files = uiState.projectFiles,
                openFiles = uiState.openFiles,
                activeFile = uiState.activeFile,
                activeFileContent = uiState.activeFileContent,
                hasUnsavedChanges = uiState.hasUnsavedChanges,
                isFileExplorerOpen = uiState.isFileExplorerOpen,
                onContentChanged = { newText -> viewModel.onContentChanged(newText) },
                onSaveFile = { viewModel.saveCurrentFile() },
                onOpenFile = { file -> viewModel.openFile(file) },
                onCloseTab = { fileId -> viewModel.closeTab(fileId) },
                onInsertSnippet = { snippet -> viewModel.insertTextAtCursor(snippet) },
                onToggleFileExplorer = { isOpen -> viewModel.toggleFileExplorer(isOpen) },
                onCreateNewFile = { name -> viewModel.createNewFile(name) },
                onDeleteFile = { fileId -> viewModel.deleteFile(fileId) },
                onRunPreview = { viewModel.navigateTo(Screen.RUNNER) },
                onBuildApk = { viewModel.startBuildPipeline() },
                onNavigateBack = { viewModel.navigateTo(Screen.DASHBOARD) }
            )
        }

        Screen.RUNNER -> {
            BackHandler {
                viewModel.navigateTo(Screen.EDITOR)
            }
            RunnerScreen(
                project = uiState.activeProject,
                compiledHtml = viewModel.getCompiledHtml(),
                consoleLogs = uiState.consoleLogs,
                isGamePadVisible = uiState.isGamePadVisible,
                isConsoleOpen = uiState.isConsoleOpen,
                onToggleGamePad = { viewModel.toggleGamePad() },
                onToggleConsole = { viewModel.toggleConsole() },
                onLogReceived = { log -> viewModel.addConsoleLog(log) },
                onClearLogs = { viewModel.clearConsoleLogs() },
                onBuildApk = { viewModel.startBuildPipeline() },
                onNavigateBack = { viewModel.navigateTo(Screen.EDITOR) }
            )
        }

        Screen.BUILD_MODAL -> {
            BackHandler {
                viewModel.navigateTo(Screen.EDITOR)
            }
            BuildModalScreen(
                project = uiState.activeProject,
                files = uiState.projectFiles,
                buildState = uiState.buildState,
                isInstallDialogOpen = uiState.isSimulatedInstallDialogOpen,
                isAppInstalled = uiState.isAppInstalled,
                onOpenInstallDialog = { viewModel.openInstallDialog() },
                onDismissInstallDialog = { viewModel.dismissInstallDialog() },
                onConfirmInstallApp = { viewModel.confirmInstallApp() },
                onLaunchInstalledApp = { viewModel.launchInstalledStandaloneApp() },
                onPreviewApp = { viewModel.navigateTo(Screen.RUNNER) },
                onNavigateBack = { viewModel.navigateTo(Screen.EDITOR) }
            )
        }
    }
}
