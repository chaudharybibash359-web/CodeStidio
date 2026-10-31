package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.builder.BuildExporter
import com.example.builder.BuildStepState
import com.example.data.ProjectEntity
import com.example.data.ProjectFileEntity
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodeBackground
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeSuccess
import com.example.ui.theme.VsCodeSurface
import com.example.ui.theme.VsCodeSurfaceVariant
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildModalScreen(
    project: ProjectEntity?,
    files: List<ProjectFileEntity>,
    buildState: BuildStepState,
    isInstallDialogOpen: Boolean,
    isAppInstalled: Boolean,
    onOpenInstallDialog: () -> Unit,
    onDismissInstallDialog: () -> Unit,
    onConfirmInstallApp: () -> Unit,
    onLaunchInstalledApp: () -> Unit,
    onPreviewApp: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current
    var isInstallingState by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("build_back_button")
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
                        Text(
                            text = "Build & Package Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = project?.name ?: "Application",
                            fontSize = 11.sp,
                            color = VsCodeCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VsCodeActivityBar
                )
            )
        },
        containerColor = VsCodeBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (buildState) {
                is BuildStepState.Idle, is BuildStepState.Progress -> {
                    val progress = if (buildState is BuildStepState.Progress) buildState.progressPercent / 100f else 0.1f
                    val stepName = if (buildState is BuildStepState.Progress) buildState.stepName else "Initializing build toolchain..."

                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(VsCodeSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = VsCodeCyan,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(60.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = null,
                            tint = VsCodeCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Packaging Mobile Application",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stepName,
                        fontSize = 13.sp,
                        color = VsCodeTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = VsCodeBlue,
                        trackColor = VsCodeSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${(progress * 100).toInt()}% Complete",
                        fontSize = 12.sp,
                        color = VsCodeCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                is BuildStepState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VsCodeSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(VsCodeSuccess, VsCodeCyan))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(VsCodeSuccess.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = VsCodeSuccess,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Build Successful!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Package generated and ready for installation & export",
                                fontSize = 12.sp,
                                color = VsCodeTextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Package details box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(VsCodeBackground, RoundedCornerShape(10.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Package Name:", fontSize = 12.sp, color = VsCodeTextSecondary)
                                    Text(buildState.apkName, fontSize = 12.sp, color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Estimated Size:", fontSize = 12.sp, color = VsCodeTextSecondary)
                                    Text("${buildState.sizeKb} KB", fontSize = 12.sp, color = VsCodeCyan)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Signing Status:", fontSize = 12.sp, color = VsCodeTextSecondary)
                                    Text("VERIFIED RELEASE SIGNED", fontSize = 11.sp, color = VsCodeSuccess, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action: Install on Device (or Open if already installed)
                            if (isAppInstalled) {
                                Button(
                                    onClick = onLaunchInstalledApp,
                                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeSuccess),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("open_installed_app_button")
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("OPEN INSTALLED APP", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 15.sp)
                                }
                            } else {
                                Button(
                                    onClick = onOpenInstallDialog,
                                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("install_on_device_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("INSTALL ON MOBILE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action: Preview Before Install
                            OutlinedButton(
                                onClick = onPreviewApp,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = VsCodeCyan),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(VsCodeCyan, VsCodeCyan))
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("preview_before_install_button")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = VsCodeCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Preview Sandbox First", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Actions: Export Source ZIP and HTML bundle
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VsCodeSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Export Options", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Export ZIP
                                Button(
                                    onClick = {
                                        project?.let { p ->
                                            val zip = BuildExporter.exportProjectZip(context, p, files)
                                            if (zip != null) {
                                                BuildExporter.shareExportedFile(context, zip, "application/zip")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("export_zip_button")
                                ) {
                                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = VsCodeCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export ZIP", fontSize = 12.sp, color = Color.White)
                                }

                                // Share HTML
                                Button(
                                    onClick = {
                                        project?.let { p ->
                                            try {
                                                val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                                                val htmlFile = File(exportDir, "${p.name.replace("\\s+".toRegex(), "_")}.html")
                                                FileOutputStream(htmlFile).use { fos ->
                                                    fos.write(buildState.standaloneHtml.toByteArray(Charsets.UTF_8))
                                                }
                                                BuildExporter.shareExportedFile(context, htmlFile, "text/html")
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_html_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = VsCodeBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Share HTML", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }

                is BuildStepState.Failure -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = VsCodeSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Build Failed", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(buildState.error, color = VsCodeTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Native-Style Android Package Installer Simulation Dialog
    if (isInstallDialogOpen) {
        AlertDialog(
            onDismissRequest = onDismissInstallDialog,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VsCodeBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = project?.name ?: "Application",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Package Installer • v1.0.0",
                            fontSize = 11.sp,
                            color = VsCodeCyan
                        )
                    }
                }
            },
            text = {
                Column {
                    Text(
                        text = "Do you want to install this application on your mobile device? It does not require any special permissions.",
                        fontSize = 13.sp,
                        color = VsCodeTextPrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = VsCodeSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verified by CodeStudio Mobile Compiler", fontSize = 11.sp, color = VsCodeSuccess)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirmInstallApp,
                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue),
                    modifier = Modifier.testTag("confirm_install_button")
                ) {
                    Text("INSTALL")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissInstallDialog) {
                    Text("CANCEL", color = VsCodeTextSecondary)
                }
            },
            containerColor = VsCodeSurface
        )
    }
}
