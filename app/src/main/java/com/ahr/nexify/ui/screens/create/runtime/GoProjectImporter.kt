package com.ahr.nexify.ui.screens.create.runtime

import android.content.Context
import com.ahr.nexify.core.golang.GoRuntime
import com.ahr.nexify.ui.screens.create.common.ImportedProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class GoProjectImporter(
    private val context: Context,
    private val analyzer: GoProjectImportAnalyzer = GoProjectImportAnalyzer(),
) {
    suspend fun importProject(projectDir: File): ImportedProject<GoProjectImportAnalysis> {
        val analysis = withContext(Dispatchers.IO) {
            analyzer.analyze(projectDir)
        }
        val projectId = UUID.randomUUID().toString()
        return withContext(Dispatchers.IO) {
            GoRuntime(context).createProject(projectId, analysis.projectDir)
            ImportedProject(analysis = analysis, projectId = projectId)
        }
    }
}
