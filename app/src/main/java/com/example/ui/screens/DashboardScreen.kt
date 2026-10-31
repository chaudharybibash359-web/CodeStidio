package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProjectEntity
import com.example.generator.SmartProjectGenerator
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodeBackground
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeSurface
import com.example.ui.theme.VsCodeSurfaceVariant
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    projects: List<ProjectEntity>,
    onOpenProject: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onGenerateFromPrompt: (String, String?) -> Unit,
    onCreateBlankProject: (String, String, String) -> Unit
) {
    var showPromptDialog by remember { mutableStateOf(false) }
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    val blueprints = remember { SmartProjectGenerator.getStarterBlueprints() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(VsCodeBlue, VsCodeCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Studio Logo",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "CodeStudio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = VsCodeTextPrimary
                            )
                            Text(
                                text = "Mobile IDE & AI Builder",
                                fontSize = 11.sp,
                                color = VsCodeCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VsCodeActivityBar
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPromptDialog = true },
                containerColor = VsCodeBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("ai_prompt_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Generate")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Builder", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = VsCodeBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Hero Banner: AI Prompt to App
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showPromptDialog = true }
                        .testTag("ai_builder_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = VsCodeSurface
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(VsCodeBlue, VsCodeCyan)
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(VsCodeBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = VsCodeCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "PROMPT TO APP ENGINE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VsCodeCyan
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create",
                                tint = VsCodeBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Describe anything, build instantly",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate complete HTML5 games, web apps, or mobile utilities with real code, instant preview & game controls.",
                            fontSize = 13.sp,
                            color = VsCodeTextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showPromptDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("prompt_generate_button")
                            ) {
                                Text("Prompt Generator", fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = { showNewProjectDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VsCodeTextPrimary),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(VsCodeBorder, VsCodeBorder))
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("blank_project_button")
                            ) {
                                Text("Blank Project")
                            }
                        }
                    }
                }
            }

            // Starter Blueprints
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instant Blueprints & Games",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VsCodeTextPrimary
                    )
                    Text(
                        text = "${blueprints.size} Ready",
                        fontSize = 12.sp,
                        color = VsCodeCyan
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(blueprints) { blueprint ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable {
                                    onGenerateFromPrompt(blueprint.project.description, blueprint.project.name)
                                }
                                .testTag("blueprint_${blueprint.project.name}"),
                            colors = CardDefaults.cardColors(containerColor = VsCodeSurface),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(VsCodeBorder, VsCodeBorder))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(VsCodeSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = if (blueprint.project.type == "GAME") Icons.Default.Gamepad else Icons.Default.Web
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = VsCodeBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = blueprint.project.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = blueprint.project.description,
                                    fontSize = 12.sp,
                                    color = VsCodeTextSecondary,
                                    maxLines = 2,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = blueprint.project.type,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (blueprint.project.type == "GAME") VsCodeCyan else VsCodeBlue
                                    )
                                    Text(
                                        text = "Clone & Run →",
                                        fontSize = 11.sp,
                                        color = VsCodeBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Projects
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Workspace Projects",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VsCodeTextPrimary
                    )
                    Text(
                        text = "${projects.size} items",
                        fontSize = 12.sp,
                        color = VsCodeTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (projects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = VsCodeTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No projects found",
                                color = VsCodeTextSecondary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'AI Builder' to generate your first app or game!",
                                color = VsCodeTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(projects, key = { it.id }) { project ->
                    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    val dateStr = dateFormat.format(Date(project.updatedAt))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { onOpenProject(project.id) }
                            .testTag("project_item_${project.id}"),
                        colors = CardDefaults.cardColors(containerColor = VsCodeSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(VsCodeBorder, VsCodeBorder))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VsCodeSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (project.type) {
                                    "GAME" -> Icons.Default.Gamepad
                                    "UTILITY" -> Icons.Default.Terminal
                                    else -> Icons.Default.Web
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (project.type == "GAME") VsCodeCyan else VsCodeBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = project.description.ifBlank { "Web Application" },
                                    fontSize = 12.sp,
                                    color = VsCodeTextSecondary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(VsCodeBackground)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = project.type,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VsCodeCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Updated $dateStr",
                                        fontSize = 11.sp,
                                        color = VsCodeTextMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onOpenProject(project.id) },
                                modifier = Modifier.testTag("launch_project_${project.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Open Editor",
                                    tint = VsCodeBlue
                                )
                            }

                            IconButton(
                                onClick = { projectToDelete = project },
                                modifier = Modifier.testTag("delete_project_${project.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = VsCodeTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Prompt Dialog
    if (showPromptDialog) {
        var promptText by remember { mutableStateOf("") }
        var optionalName by remember { mutableStateOf("") }

        val suggestions = listOf(
            "Retro 2D arcade space shooter",
            "Kanban task board app",
            "Flappy bird jumping game",
            "Modern glassmorphism calculator",
            "Pixel art drawing board",
            "Quiz app for web technologies"
        )

        AlertDialog(
            onDismissRequest = { showPromptDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = VsCodeCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Describe App or Game to Build", fontSize = 18.sp, color = Color.White)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter a prompt describing the features, rules, or style. The studio will synthesize complete working HTML, CSS, and JS code.",
                        fontSize = 12.sp,
                        color = VsCodeTextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        label = { Text("App or Game Description") },
                        placeholder = { Text("e.g. Build a brick breaker arcade game with score...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("ai_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VsCodeBlue,
                            unfocusedBorderColor = VsCodeBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = optionalName,
                        onValueChange = { optionalName = it },
                        label = { Text("Project Name (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_project_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VsCodeBlue,
                            unfocusedBorderColor = VsCodeBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Or choose a prompt idea:", fontSize = 11.sp, color = VsCodeCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(suggestions) { s ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(VsCodeSurfaceVariant)
                                    .clickable { promptText = s }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(text = s, fontSize = 11.sp, color = VsCodeTextPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promptText.isNotBlank()) {
                            onGenerateFromPrompt(
                                promptText,
                                optionalName.ifBlank { null }
                            )
                            showPromptDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue),
                    modifier = Modifier.testTag("generate_confirm_button")
                ) {
                    Text("Generate & Code")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromptDialog = false }) {
                    Text("Cancel", color = VsCodeTextSecondary)
                }
            },
            containerColor = VsCodeSurface
        )
    }

    // New Blank Project Dialog
    if (showNewProjectDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("WEB_APP") }

        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("New Blank Project", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Project Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_project_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VsCodeBlue,
                            unfocusedBorderColor = VsCodeBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_project_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VsCodeBlue,
                            unfocusedBorderColor = VsCodeBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("WEB_APP" to "Web App", "GAME" to "Game", "UTILITY" to "Utility").forEach { (valType, label) ->
                            val isSelected = type == valType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) VsCodeBlue else VsCodeSurfaceVariant)
                                    .clickable { type = valType }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else VsCodeTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onCreateBlankProject(name, description, type)
                            showNewProjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue),
                    modifier = Modifier.testTag("create_blank_confirm_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel", color = VsCodeTextSecondary)
                }
            },
            containerColor = VsCodeSurface
        )
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project?", color = Color.White) },
            text = {
                Text(
                    text = "Are you sure you want to delete '${project.name}' and all its source files? This cannot be undone.",
                    color = VsCodeTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(project.id)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = VsCodeTextSecondary)
                }
            },
            containerColor = VsCodeSurface
        )
    }
}
