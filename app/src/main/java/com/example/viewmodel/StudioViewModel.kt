package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.builder.BuildExporter
import com.example.builder.BuildStepState
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.ProjectFileEntity
import com.example.data.ProjectRepository
import com.example.generator.SmartProjectGenerator
import com.example.runner.ConsoleLog
import com.example.runner.SandboxBridge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Screen {
    DASHBOARD,
    EDITOR,
    RUNNER,
    BUILD_MODAL
}

data class StudioUiState(
    val currentScreen: Screen = Screen.DASHBOARD,
    val activeProjectId: String? = null,
    val activeProject: ProjectEntity? = null,
    val projectFiles: List<ProjectFileEntity> = emptyList(),
    val openFiles: List<ProjectFileEntity> = emptyList(),
    val activeFile: ProjectFileEntity? = null,
    val activeFileContent: String = "",
    val hasUnsavedChanges: Boolean = false,
    val isFileExplorerOpen: Boolean = false,
    val isAiPromptDialogOpen: Boolean = false,
    val isNewProjectDialogOpen: Boolean = false,
    val isNewFileDialogOpen: Boolean = false,
    val isGamePadVisible: Boolean = true,
    val isConsoleOpen: Boolean = false,
    val consoleLogs: List<ConsoleLog> = emptyList(),
    val buildState: BuildStepState = BuildStepState.Idle,
    val isSimulatedInstallDialogOpen: Boolean = false,
    val isAppInstalled: Boolean = false,
    val installedProject: ProjectEntity? = null,
    val isStandaloneRunning: Boolean = false,
    val statusMessage: String? = null
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository
    val projects: StateFlow<List<ProjectEntity>>

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ProjectRepository(db.projectDao())
        projects = repository.allProjects.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        viewModelScope.launch {
            repository.ensureDefaultProjects()
        }
    }

    fun openProject(projectId: String) {
        viewModelScope.launch {
            repository.getProject(projectId).collect { project ->
                if (project != null) {
                    repository.getProjectFiles(projectId).collect { files ->
                        val defaultActive = files.find { it.fileType == "HTML" || it.name.endsWith(".html") }
                            ?: files.firstOrNull()

                        _uiState.value = _uiState.value.copy(
                            currentScreen = Screen.EDITOR,
                            activeProjectId = projectId,
                            activeProject = project,
                            projectFiles = files,
                            openFiles = if (defaultActive != null) listOf(defaultActive) else emptyList(),
                            activeFile = defaultActive,
                            activeFileContent = defaultActive?.content ?: "",
                            hasUnsavedChanges = false,
                            consoleLogs = emptyList()
                        )
                    }
                }
            }
        }
    }

    fun openFile(file: ProjectFileEntity) {
        // Save current changes if any before switching
        saveCurrentFile()
        val currentOpen = _uiState.value.openFiles.toMutableList()
        if (currentOpen.none { it.id == file.id }) {
            currentOpen.add(file)
        }
        _uiState.value = _uiState.value.copy(
            openFiles = currentOpen,
            activeFile = file,
            activeFileContent = file.content,
            hasUnsavedChanges = false,
            isFileExplorerOpen = false
        )
    }

    fun closeTab(fileId: String) {
        val currentOpen = _uiState.value.openFiles.filter { it.id != fileId }
        val newActive = if (_uiState.value.activeFile?.id == fileId) {
            currentOpen.lastOrNull()
        } else {
            _uiState.value.activeFile
        }
        _uiState.value = _uiState.value.copy(
            openFiles = currentOpen,
            activeFile = newActive,
            activeFileContent = newActive?.content ?: "",
            hasUnsavedChanges = false
        )
    }

    fun onContentChanged(newContent: String) {
        _uiState.value = _uiState.value.copy(
            activeFileContent = newContent,
            hasUnsavedChanges = true
        )
    }

    fun saveCurrentFile() {
        val currentFile = _uiState.value.activeFile ?: return
        val content = _uiState.value.activeFileContent
        viewModelScope.launch {
            repository.updateFileContent(currentFile.id, content)
            _uiState.value = _uiState.value.copy(hasUnsavedChanges = false)
        }
    }

    fun insertTextAtCursor(snippet: String) {
        val current = _uiState.value.activeFileContent
        val updated = current + snippet
        onContentChanged(updated)
    }

    fun createNewProject(name: String, description: String, type: String) {
        viewModelScope.launch {
            val project = repository.createProject(name, description, type)
            _uiState.value = _uiState.value.copy(isNewProjectDialogOpen = false)
            openProject(project.id)
        }
    }

    fun generateProjectFromPrompt(prompt: String, projectName: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAiPromptDialogOpen = false,
                statusMessage = "Synthesizing project from description..."
            )
            val generated = SmartProjectGenerator.createFromPrompt(prompt, projectName)
            repository.saveGeneratedProject(generated)
            _uiState.value = _uiState.value.copy(statusMessage = null)
            openProject(generated.project.id)
        }
    }

    fun createNewFile(fileName: String) {
        val projectId = _uiState.value.activeProjectId ?: return
        viewModelScope.launch {
            val newFile = repository.createFile(projectId, fileName)
            _uiState.value = _uiState.value.copy(isNewFileDialogOpen = false)
            openFile(newFile)
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
            closeTab(fileId)
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_uiState.value.activeProjectId == projectId) {
                navigateTo(Screen.DASHBOARD)
            }
        }
    }

    fun navigateTo(screen: Screen) {
        if (_uiState.value.hasUnsavedChanges) {
            saveCurrentFile()
        }
        _uiState.value = _uiState.value.copy(currentScreen = screen)
    }

    fun toggleFileExplorer(isOpen: Boolean? = null) {
        _uiState.value = _uiState.value.copy(
            isFileExplorerOpen = isOpen ?: !_uiState.value.isFileExplorerOpen
        )
    }

    fun toggleGamePad() {
        _uiState.value = _uiState.value.copy(
            isGamePadVisible = !_uiState.value.isGamePadVisible
        )
    }

    fun toggleConsole() {
        _uiState.value = _uiState.value.copy(
            isConsoleOpen = !_uiState.value.isConsoleOpen
        )
    }

    fun addConsoleLog(log: ConsoleLog) {
        _uiState.value = _uiState.value.copy(
            consoleLogs = _uiState.value.consoleLogs + log
        )
    }

    fun clearConsoleLogs() {
        _uiState.value = _uiState.value.copy(consoleLogs = emptyList())
    }

    fun setAiPromptDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAiPromptDialogOpen = isOpen)
    }

    fun setNewProjectDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isNewProjectDialogOpen = isOpen)
    }

    fun setNewFileDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isNewFileDialogOpen = isOpen)
    }

    fun startBuildPipeline() {
        val project = _uiState.value.activeProject ?: return
        val files = _uiState.value.projectFiles
        saveCurrentFile()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentScreen = Screen.BUILD_MODAL,
                buildState = BuildStepState.Idle,
                isSimulatedInstallDialogOpen = false,
                isAppInstalled = false
            )

            BuildExporter.simulateApkBuild(project, files).collect { state ->
                _uiState.value = _uiState.value.copy(buildState = state)
            }
        }
    }

    fun openInstallDialog() {
        _uiState.value = _uiState.value.copy(
            isSimulatedInstallDialogOpen = true,
            installedProject = _uiState.value.activeProject
        )
    }

    fun dismissInstallDialog() {
        _uiState.value = _uiState.value.copy(isSimulatedInstallDialogOpen = false)
    }

    fun confirmInstallApp() {
        _uiState.value = _uiState.value.copy(
            isSimulatedInstallDialogOpen = false,
            isAppInstalled = true
        )
    }

    fun launchInstalledStandaloneApp() {
        _uiState.value = _uiState.value.copy(
            isStandaloneRunning = true,
            currentScreen = Screen.RUNNER
        )
    }

    fun getCompiledHtml(): String {
        return SandboxBridge.buildStandaloneHtml(_uiState.value.projectFiles)
    }
}
