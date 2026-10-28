package com.ahr.nexify.core.agent.tool

import com.ahr.nexify.core.agent.imagery.ImageGeneratorRegistry
import com.ahr.nexify.core.agent.plan.PlanManager
import com.ahr.nexify.core.agent.tool.builtin.AskUserTool
import com.ahr.nexify.core.agent.tool.builtin.BatchImportAppsTool
import com.ahr.nexify.core.agent.tool.builtin.BuildApkTool
import com.ahr.nexify.core.agent.tool.builtin.CheckPlayPolicyTool
import com.ahr.nexify.core.agent.tool.builtin.CheckAppHealthTool
import com.ahr.nexify.core.agent.tool.builtin.ClearAppCacheTool
import com.ahr.nexify.core.agent.tool.builtin.ClearRuntimeCacheTool
import com.ahr.nexify.core.agent.tool.builtin.CreateAppTool
import com.ahr.nexify.core.agent.tool.builtin.CreateModuleTool
import com.ahr.nexify.core.agent.tool.builtin.CreateShortcutTool
import com.ahr.nexify.core.agent.tool.builtin.DeleteAppTool
import com.ahr.nexify.core.agent.tool.builtin.DeleteEngineTool
import com.ahr.nexify.core.agent.tool.builtin.DeleteFileTool
import com.ahr.nexify.core.agent.tool.builtin.CloneAppTool
import com.ahr.nexify.core.agent.tool.builtin.DuplicateAppTool
import com.ahr.nexify.core.agent.tool.builtin.EditFileTool
import com.ahr.nexify.core.agent.tool.builtin.EnterPlanModeTool
import com.ahr.nexify.core.agent.tool.builtin.ExitPlanModeTool
import com.ahr.nexify.core.agent.tool.builtin.ExportAabTool
import com.ahr.nexify.core.agent.tool.builtin.ExportAppTemplateTool
import com.ahr.nexify.core.agent.tool.builtin.ExportAppTool
import com.ahr.nexify.core.agent.tool.builtin.GetUsageStatsTool
import com.ahr.nexify.core.agent.tool.builtin.GetAdBlockStatusTool
import com.ahr.nexify.core.agent.tool.builtin.GetBuildEnvStatusTool
import com.ahr.nexify.core.agent.tool.builtin.GetAppTool
import com.ahr.nexify.core.agent.tool.builtin.InitializeBuildEnvTool
import com.ahr.nexify.core.agent.tool.builtin.GetEngineStatusTool
import com.ahr.nexify.core.agent.tool.builtin.GetModuleTool
import com.ahr.nexify.core.agent.tool.builtin.GetRuntimeStatusTool
import com.ahr.nexify.core.agent.tool.builtin.GlobTool
import com.ahr.nexify.core.agent.tool.builtin.GrepTool
import com.ahr.nexify.core.agent.tool.builtin.InstallRuntimeTool
import com.ahr.nexify.core.agent.tool.builtin.KillAllPortsTool
import com.ahr.nexify.core.agent.tool.builtin.KillPortTool
import com.ahr.nexify.core.agent.tool.builtin.ListInstalledAppsTool
import com.ahr.nexify.core.agent.tool.builtin.ListAppsTool
import com.ahr.nexify.core.agent.tool.builtin.ListFilesTool
import com.ahr.nexify.core.agent.tool.builtin.ListModulesTool
import com.ahr.nexify.core.agent.tool.builtin.ManageHostsRulesTool
import com.ahr.nexify.core.agent.tool.builtin.PreviewTool
import com.ahr.nexify.core.agent.tool.builtin.MoveToCategoryTool
import com.ahr.nexify.core.agent.tool.builtin.ReadFileTool
import com.ahr.nexify.core.agent.tool.builtin.ReadAppFileTool
import com.ahr.nexify.core.agent.tool.builtin.ScanPortsTool
import com.ahr.nexify.core.agent.tool.builtin.SelectEngineTool
import com.ahr.nexify.core.agent.tool.builtin.ShareApkTool
import com.ahr.nexify.core.agent.tool.builtin.TodoUpdateTool
import com.ahr.nexify.core.agent.tool.builtin.TodoWriteTool
import com.ahr.nexify.core.agent.tool.builtin.UpdateAppTool
import com.ahr.nexify.core.agent.tool.builtin.ListConfigTemplatesTool
import com.ahr.nexify.core.agent.tool.builtin.SaveConfigTemplateTool
import com.ahr.nexify.core.agent.tool.builtin.ApplyConfigTemplateTool
import com.ahr.nexify.core.agent.tool.builtin.ExportConfigTemplateTool
import com.ahr.nexify.core.agent.tool.builtin.ImportConfigTemplateTool
import com.ahr.nexify.core.agent.tool.builtin.DeleteConfigTemplateTool
import com.ahr.nexify.core.agent.tool.builtin.UpdateModuleTool
import com.ahr.nexify.core.agent.tool.builtin.WriteFileTool
import com.ahr.nexify.core.agent.tool.builtin.imagery.GenerateImageTool
import com.ahr.nexify.core.agent.tool.builtin.imagery.ListImagesTool
import com.ahr.nexify.core.agent.tool.builtin.imagery.ViewImageTool

class ToolRegistryFactory(
    private val planManager: PlanManager,
    private val imageRegistry: ImageGeneratorRegistry?
) {

    fun build(hasImageModel: Boolean): ToolRegistry {
        val base = baseTools()
        val plan = planTools()
        val imagery = if (hasImageModel && imageRegistry != null) imageryTools(imageRegistry) else emptyList()
        val all = (base + plan + imagery).distinctBy { it.name }
        return ToolRegistry(all)
    }

    /** In-app tools minus plan mode and image tools. Served by the local MCP. */
    fun buildForExternalHost(): ToolRegistry = ToolRegistry(baseTools())

    private fun baseTools(): List<Tool> = listOf(
        ReadFileTool(),
        ReadAppFileTool(),
        WriteFileTool(),
        EditFileTool(),
        DeleteFileTool(),
        ListFilesTool(),
        GlobTool(),
        GrepTool(),
        AskUserTool(),
        TodoWriteTool(),
        TodoUpdateTool(),
        ListAppsTool(),
        GetAppTool(),
        CreateAppTool(),
        UpdateAppTool(),
        // Common-config templates
        ListConfigTemplatesTool(),
        SaveConfigTemplateTool(),
        ApplyConfigTemplateTool(),
        ExportConfigTemplateTool(),
        ImportConfigTemplateTool(),
        DeleteConfigTemplateTool(),
        // App lifecycle
        BuildApkTool(),
        ShareApkTool(),
        ExportAppTool(),
        CreateShortcutTool(),
        MoveToCategoryTool(),
        ClearAppCacheTool(),
        DeleteAppTool(),
        DuplicateAppTool(),
        ExportAabTool(),
        // Ports & engine
        ScanPortsTool(),
        KillPortTool(),
        KillAllPortsTool(),
        GetEngineStatusTool(),
        SelectEngineTool(),
        DeleteEngineTool(),
        // Hosts ad-block & runtime management
        GetAdBlockStatusTool(),
        ManageHostsRulesTool(),
        GetRuntimeStatusTool(),
        InstallRuntimeTool(),
        ClearRuntimeCacheTool(),
        // Stats, modifier & import
        GetUsageStatsTool(),
        CheckAppHealthTool(),
        ListInstalledAppsTool(),
        CloneAppTool(),
        BatchImportAppsTool(),
        ExportAppTemplateTool(),
        // Build env & Play policy
        GetBuildEnvStatusTool(),
        InitializeBuildEnvTool(),
        CheckPlayPolicyTool(),
        // Modules
        ListModulesTool(),
        GetModuleTool(),
        CreateModuleTool(),
        UpdateModuleTool(),
        PreviewTool(),
    )

    private fun planTools(): List<Tool> = listOf(
        EnterPlanModeTool(planManager),
        ExitPlanModeTool(planManager),
    )

    private fun imageryTools(reg: ImageGeneratorRegistry): List<Tool> = listOf(
        GenerateImageTool(reg),
        ViewImageTool(),
        ListImagesTool(),
    )
}
