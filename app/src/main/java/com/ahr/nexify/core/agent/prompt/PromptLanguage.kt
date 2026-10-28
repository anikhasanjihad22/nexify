package com.ahr.nexify.core.agent.prompt

import com.ahr.nexify.core.i18n.AppLanguage

enum class PromptLang { EN, ZH }

fun AppLanguage.toPromptLang(): PromptLang = when (this) {
    AppLanguage.CHINESE -> PromptLang.ZH
    else -> PromptLang.EN
}
