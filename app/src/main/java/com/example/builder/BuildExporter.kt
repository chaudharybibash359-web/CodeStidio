package com.example.builder

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.ProjectEntity
import com.example.data.ProjectFileEntity
import com.example.runner.SandboxBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

sealed class BuildStepState {
    object Idle : BuildStepState()
    data class Progress(val stepIndex: Int, val stepName: String, val progressPercent: Int) : BuildStepState()
    data class Success(val apkName: String, val sizeKb: Long, val standaloneHtml: String) : BuildStepState()
    data class Failure(val error: String) : BuildStepState()
}

object BuildExporter {

    fun simulateApkBuild(project: ProjectEntity, files: List<ProjectFileEntity>): Flow<BuildStepState> = flow {
        emit(BuildStepState.Progress(1, "Analyzing workspace & linting source files...", 15))
        delay(600)

        emit(BuildStepState.Progress(2, "Bundling HTML5 assets, scripts & stylesheets...", 40))
        delay(700)

        emit(BuildStepState.Progress(3, "Generating Web App Manifest & Android container...", 70))
        delay(700)

        emit(BuildStepState.Progress(4, "Signing APK package & verifying checksum...", 92))
        delay(600)

        val cleanProjectName = project.name.replace("\\s+".toRegex(), "_").lowercase()
        val standaloneHtml = SandboxBridge.buildStandaloneHtml(files)
        val calculatedSize = (files.sumOf { it.content.length } / 1024L) + 1240L // Base APK size ~1.2MB

        emit(BuildStepState.Success(
            apkName = "${cleanProjectName}-release.apk",
            sizeKb = calculatedSize,
            standaloneHtml = standaloneHtml
        ))
    }

    fun exportProjectZip(context: Context, project: ProjectEntity, files: List<ProjectFileEntity>): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanName = project.name.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val zipFile = File(exportDir, "${cleanName}_source.zip")

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (file in files) {
                    val entry = ZipEntry(file.filePath)
                    zos.putNextEntry(entry)
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
            zipFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareExportedFile(context: Context, file: File, mimeType: String = "application/zip") {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export ${file.name}"))
        } catch (e: Exception) {
            // Fallback generic send
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_TEXT, "Exported project file: ${file.name}")
            }
            context.startActivity(Intent.createChooser(intent, "Share"))
        }
    }
}
