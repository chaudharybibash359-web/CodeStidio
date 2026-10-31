package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectEntity
import com.example.data.ProjectFileEntity
import com.example.editor.CodeSyntaxVisualTransformation
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodeBackground
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeNumber
import com.example.ui.theme.VsCodeString
import com.example.ui.theme.VsCodeSuccess
import com.example.ui.theme.VsCodeSurface
import com.example.ui.theme.VsCodeSurfaceVariant
import com.example.ui.theme.VsCodeTag
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.ui.theme.VsCodeWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: ProjectEntity?,
    files: List<ProjectFileEntity>,
    openFiles: List<ProjectFileEntity>,
    activeFile: ProjectFileEntity?,
    activeFileContent: String,
    hasUnsavedChanges: Boolean,
    isFileExplorerOpen: Boolean,
    onContentChanged: (String) -> Unit,
    onSaveFile: () -> Unit,
    onOpenFile: (ProjectFileEntity) -> Unit,
    onCloseTab: (String) -> Unit,
    onInsertSnippet: (String) -> Unit,
    onToggleFileExplorer: (Boolean?) -> Unit,
    onCreateNewFile: (String) -> Unit,
    onDeleteFile: (String) -> Unit,
    onRunPreview: () -> Unit,
    onBuildApk: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<ProjectFileEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val visualTransformation = remember(activeFile?.fileType) {
        CodeSyntaxVisualTransformation(activeFile?.fileType ?: "TXT")
    }

    val linesCount = remember(activeFileContent) {
        activeFileContent.lines().size.coerceAtLeast(1)
    }

    val symbolSnippets = listOf(
        "<", ">", "/", "{", "}", "[", "]", "(", ")",
        "=", ";", ":", "\"", "'", "$", "!", "&", "|", "+", "-"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = VsCodeTextPrimary
                        )
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = project?.name ?: "Editor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = VsCodeTextPrimary,
                                maxLines = 1
                            )
                            if (hasUnsavedChanges) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(VsCodeCyan)
                                )
                            }
                        }
                        Text(
                            text = activeFile?.filePath ?: "No file open",
                            fontSize = 11.sp,
                            color = VsCodeCyan,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                },
                actions = {
                    // Save Button
                    if (hasUnsavedChanges) {
                        IconButton(
                            onClick = onSaveFile,
                            modifier = Modifier.testTag("editor_save_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save",
                                tint = VsCodeCyan
                            )
                        }
                    }

                    // File Explorer Toggle
                    IconButton(
                        onClick = { onToggleFileExplorer(true) },
                        modifier = Modifier.testTag("editor_files_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Files",
                            tint = VsCodeTextPrimary
                        )
                    }

                    // Build APK Button
                    Button(
                        onClick = onBuildApk,
                        colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("editor_build_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Build", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Run Preview Button
                    Button(
                        onClick = onRunPreview,
                        colors = ButtonDefaults.buttonColors(containerColor = VsCodeSuccess),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("editor_run_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RUN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VsCodeActivityBar
                )
            )
        },
        bottomBar = {
            // Quick Symbol & Snippet Accessory Bar above system keyboard
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VsCodeActivityBar)
                    .padding(vertical = 4.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VsCodeBlue.copy(alpha = 0.3f))
                                .clickable { onInsertSnippet("  ") }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "TAB",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = VsCodeCyan
                            )
                        }
                    }

                    items(symbolSnippets) { sym ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VsCodeSurfaceVariant)
                                .clickable { onInsertSnippet(sym) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = VsCodeTextPrimary
                            )
                        }
                    }
                }
            }
        },
        containerColor = VsCodeBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Open Tabs Bar
            if (openFiles.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VsCodeSurface)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(openFiles, key = { it.id }) { file ->
                        val isActive = file.id == activeFile?.id
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(if (isActive) VsCodeBackground else VsCodeSurfaceVariant)
                                .clickable { onOpenFile(file) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // File type color dot
                            val dotColor = when (file.fileType) {
                                "HTML" -> VsCodeTag
                                "CSS" -> VsCodeBlue
                                "JS" -> VsCodeWarning
                                else -> VsCodeTextSecondary
                            }
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = file.name,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isActive) Color.White else VsCodeTextSecondary,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Tab",
                                tint = VsCodeTextMuted,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onCloseTab(file.id) }
                            )
                        }
                    }
                }
            }

            // Editor Canvas with Line Numbers
            if (activeFile != null) {
                val verticalScroll = rememberScrollState()
                val horizontalScroll = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VsCodeBackground)
                ) {
                    // Line numbers gutter
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(42.dp)
                            .background(VsCodeSurface)
                            .verticalScroll(verticalScroll)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..linesCount) {
                            Text(
                                text = "$i",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = VsCodeTextMuted,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }

                    // Code Editor Text Field
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(verticalScroll)
                            .horizontalScroll(horizontalScroll)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        BasicTextField(
                            value = activeFileContent,
                            onValueChange = onContentChanged,
                            textStyle = TextStyle(
                                color = VsCodeTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            ),
                            cursorBrush = SolidColor(VsCodeCyan),
                            visualTransformation = visualTransformation,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("code_editor_input")
                        )
                    }
                }
            } else {
                // Empty state if no file open
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.InsertDriveFile,
                            contentDescription = null,
                            tint = VsCodeTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No file currently open",
                            color = VsCodeTextSecondary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onToggleFileExplorer(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue)
                        ) {
                            Text("Open File Explorer")
                        }
                    }
                }
            }
        }
    }

    // File Explorer Bottom Sheet
    if (isFileExplorerOpen) {
        ModalBottomSheet(
            onDismissRequest = { onToggleFileExplorer(false) },
            sheetState = sheetState,
            containerColor = VsCodeSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = VsCodeBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Project Files", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = { showNewFileDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_file_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New File", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(files, key = { it.id }) { file ->
                        val isCurrent = file.id == activeFile?.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenFile(file) }
                                .testTag("file_item_${file.name}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) VsCodeSurfaceVariant else VsCodeBackground
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (extColor, extLabel) = when (file.fileType) {
                                        "HTML" -> VsCodeTag to "HTML"
                                        "CSS" -> VsCodeBlue to "CSS"
                                        "JS" -> VsCodeWarning to "JS"
                                        "JSON" -> VsCodeString to "JSON"
                                        else -> VsCodeTextSecondary to "TXT"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(extColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = extLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = extColor)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = file.filePath,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isCurrent) Color.White else VsCodeTextPrimary,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                if (files.size > 1) {
                                    IconButton(
                                        onClick = { fileToDelete = file },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete File",
                                            tint = VsCodeTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        var fileName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New File", color = Color.White) },
            text = {
                Column {
                    Text(
                        text = "Enter file name with extension (e.g. script.js, style.css, utils.js):",
                        fontSize = 12.sp,
                        color = VsCodeTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        placeholder = { Text("filename.js") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_filename_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VsCodeBlue,
                            unfocusedBorderColor = VsCodeBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fileName.isNotBlank()) {
                            onCreateNewFile(fileName.trim())
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue),
                    modifier = Modifier.testTag("confirm_create_file_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel", color = VsCodeTextSecondary)
                }
            },
            containerColor = VsCodeSurface
        )
    }

    // Delete File Confirmation
    fileToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete ${file.name}?", color = Color.White) },
            text = {
                Text("Are you sure you want to delete this file from the project?", color = VsCodeTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFile(file.id)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel", color = VsCodeTextSecondary)
                }
            },
            containerColor = VsCodeSurface
        )
    }
}
