package com.example.data

import com.example.generator.SmartProjectGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class ProjectRepository(private val dao: ProjectDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()

    fun getProject(id: String): Flow<ProjectEntity?> = dao.getProjectById(id)

    fun getProjectFiles(projectId: String): Flow<List<ProjectFileEntity>> = dao.getFilesForProject(projectId)

    suspend fun getFile(id: String): ProjectFileEntity? = dao.getFileById(id)

    suspend fun getFileByPath(projectId: String, path: String): ProjectFileEntity? = dao.getFileByPath(projectId, path)

    suspend fun ensureDefaultProjects() {
        val current = dao.getAllProjects().firstOrNull()
        if (current.isNullOrEmpty()) {
            val defaults = SmartProjectGenerator.getStarterBlueprints().take(3)
            for (p in defaults) {
                dao.insertProject(p.project)
                dao.insertFiles(p.files)
            }
        } else {
            val legacyFiles = dao.getFilesNeedingPatch()
            for (file in legacyFiles) {
                var patched = file.content
                    .replace(
                        "const audioCtx = new (window.AudioContext || window.webkitAudioContext)();",
                        "let audioCtx = null;"
                    )
                    .replace(
                        "const osc = audioCtx.createOscillator();",
                        "if (!audioCtx && (window.AudioContext || window.webkitAudioContext)) { try { audioCtx = new (window.AudioContext || window.webkitAudioContext)(); } catch(e) {} }\n    if (!audioCtx) return;\n    const osc = audioCtx.createOscillator();"
                    )
                    .replace("ctx.shadowBlur = 12;", "")
                    .replace("ctx.shadowBlur = 10;", "")
                    .replace("ctx.shadowBlur = 8;", "")
                    .replace("ctx.shadowColor = '#00e5ff';", "")
                    .replace("ctx.shadowColor = '#ffe600';", "")
                    .replace("ctx.shadowColor = '#ff3366';", "")
                    .replace("ctx.shadowColor = '#f43f5e';", "")
                dao.updateFile(file.copy(content = patched))
            }
        }
    }

    suspend fun createProject(name: String, description: String, type: String, icon: String = "code"): ProjectEntity {
        val project = ProjectEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            type = type,
            iconName = icon
        )
        dao.insertProject(project)

        // Seed with starter index.html
        val defaultHtml = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <h1>Hello from $name!</h1>
  <p>Edit index.html and style.css to start creating.</p>
  <script src="script.js"></script>
</body>
</html>
""".trimIndent()

        val defaultCss = """
body {
  background: #121218;
  color: #e0e0e0;
  font-family: sans-serif;
  padding: 24px;
}
h1 { color: #38bdf8; }
""".trimIndent()

        val defaultJs = """
console.log("$name loaded successfully!");
""".trimIndent()

        dao.insertFiles(listOf(
            ProjectFileEntity(projectId = project.id, name = "index.html", filePath = "index.html", content = defaultHtml, fileType = "HTML"),
            ProjectFileEntity(projectId = project.id, name = "style.css", filePath = "style.css", content = defaultCss, fileType = "CSS"),
            ProjectFileEntity(projectId = project.id, name = "script.js", filePath = "script.js", content = defaultJs, fileType = "JS")
        ))
        return project
    }

    suspend fun saveGeneratedProject(generated: SmartProjectGenerator.GeneratedProject) {
        dao.insertProject(generated.project)
        dao.insertFiles(generated.files)
    }

    suspend fun updateFileContent(fileId: String, newContent: String) {
        val file = dao.getFileById(fileId) ?: return
        dao.updateFile(file.copy(content = newContent, updatedAt = System.currentTimeMillis()))
        // update project timestamp
        val project = dao.getProjectById(file.projectId).firstOrNull()
        if (project != null) {
            dao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun createFile(projectId: String, fileName: String): ProjectFileEntity {
        val ext = fileName.substringAfterLast('.', "").uppercase()
        val type = when (ext) {
            "HTML", "HTM" -> "HTML"
            "CSS" -> "CSS"
            "JS" -> "JS"
            "JSON" -> "JSON"
            else -> "TXT"
        }
        val file = ProjectFileEntity(
            projectId = projectId,
            name = fileName,
            filePath = fileName,
            content = if (type == "HTML") "<!DOCTYPE html>\n<html>\n<body>\n</body>\n</html>" else "",
            fileType = type
        )
        dao.insertFile(file)
        return file
    }

    suspend fun deleteFile(fileId: String) {
        dao.deleteFileById(fileId)
    }

    suspend fun deleteProject(projectId: String) {
        dao.deleteFilesByProjectId(projectId)
        dao.deleteProjectById(projectId)
    }
}
