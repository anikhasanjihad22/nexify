package com.ahr.nexify.data.repository

import com.ahr.nexify.data.dao.AppCategoryDao
import com.ahr.nexify.data.model.AppCategory
import kotlinx.coroutines.flow.Flow

class AppCategoryRepository(private val categoryDao: AppCategoryDao) {

    val allCategories: Flow<List<AppCategory>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: Long): AppCategory? = categoryDao.getCategoryById(id)

    suspend fun createCategory(category: AppCategory): Long {
        return categoryDao.insert(category)
    }

    suspend fun updateCategory(category: AppCategory) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: AppCategory) {
        categoryDao.delete(category)
    }

    suspend fun deleteCategoryById(id: Long) {
        categoryDao.deleteById(id)
    }

    suspend fun getCategoryCount(): Int = categoryDao.getCategoryCount()
}
