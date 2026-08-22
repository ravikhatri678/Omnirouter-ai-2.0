package com.example.engine

import com.example.data.model.ChangeLogCategory
import com.example.data.model.ChangeLogEntryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ChangeLogManager {

    fun generateMarkdown(logs: List<ChangeLogEntryEntity>): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return buildString {
            appendLine("# OmniRouter AI — System Change Log")
            appendLine()
            appendLine("> Automatically maintained ledger of AI model integrations, API configurations, routing rules, and architectural upgrades.")
            appendLine()
            appendLine("---")
            appendLine()

            val groupedByVersion = logs.groupBy { it.versionTag }

            groupedByVersion.forEach { (version, versionLogs) ->
                appendLine("## [$version] — Updates & Audit Trail")
                appendLine()

                val groupedByCategory = versionLogs.groupBy { it.category }
                ChangeLogCategory.entries.forEach { category ->
                    val catLogs = groupedByCategory[category]
                    if (!catLogs.isNullOrEmpty()) {
                        appendLine("### ${category.displayName}")
                        catLogs.forEach { log ->
                            val dateStr = sdf.format(Date(log.timestamp))
                            appendLine("- **${log.title}** *($dateStr)*")
                            appendLine("  ${log.description}")
                        }
                        appendLine()
                    }
                }
                appendLine("---")
                appendLine()
            }
        }
    }
}
