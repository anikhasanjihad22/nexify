package com.ahr.nexify.di

import com.ahr.nexify.core.activation.ActivationManager
import com.ahr.nexify.core.adblock.AdBlocker
import com.ahr.nexify.core.announcement.AnnouncementManager
import com.ahr.nexify.core.crypto.KeyManager
import com.ahr.nexify.core.shell.ShellModeManager
import com.ahr.nexify.core.stats.AppStatsRepository
import com.ahr.nexify.core.stats.AppUsageTracker
import com.ahr.nexify.core.stats.AppHealthMonitor
import com.ahr.nexify.core.stats.WebsiteScreenshotService
import com.ahr.nexify.core.stats.BatchImportService
import com.ahr.nexify.data.database.AppDatabase
import com.ahr.nexify.data.repository.AppCategoryRepository
import com.ahr.nexify.data.repository.WebAppRepository
import com.ahr.nexify.core.usecase.SaveAppUseCase
import com.ahr.nexify.ui.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().webAppDao() }
    single { get<AppDatabase>().appCategoryDao() }
    single { get<AppDatabase>().appUsageStatsDao() }
}

val repositoryModule = module {
    single { WebAppRepository(get()) }
    single { AppCategoryRepository(get()) }
    single { AppStatsRepository(get()) }
}

val managerModule = module {
    single { ActivationManager(androidContext()) }
    single { AnnouncementManager(androidContext()) }
    single { AdBlocker() }
    single { ShellModeManager(androidContext()) }
    single { KeyManager.getInstance(androidContext()) }
    single { AppUsageTracker(get()) }
    single { AppHealthMonitor.getInstance(androidContext(), get()) }
    single { WebsiteScreenshotService.getInstance(androidContext()) }
    single { BatchImportService(androidContext(), get()) }
}

val useCaseModule = module {
    factory { SaveAppUseCase(get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
}

val appModules = listOf(
    databaseModule,
    repositoryModule,
    managerModule,
    useCaseModule,
    viewModelModule
)
