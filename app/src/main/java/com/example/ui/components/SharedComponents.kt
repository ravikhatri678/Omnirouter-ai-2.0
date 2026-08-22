package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChangeLogCategory
import com.example.data.model.ModelTier
import com.example.data.model.TaskType
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoBorderHighlight
import com.example.ui.theme.BentoDarkTile
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.TierBalanced
import com.example.ui.theme.TierFast
import com.example.ui.theme.TierFlagship
import com.example.ui.theme.TierLocal

fun getTaskIcon(taskType: TaskType): ImageVector {
    return when (taskType) {
        TaskType.CODING -> Icons.Default.Code
        TaskType.REASONING -> Icons.Default.Psychology
        TaskType.RESEARCH -> Icons.Default.MenuBook
        TaskType.FAST_QUERY -> Icons.Default.Bolt
        TaskType.CASUAL_CHAT -> Icons.Default.Forum
        TaskType.CREATIVE_WRITING -> Icons.Default.EditNote
    }
}

fun getTierColor(tier: ModelTier): Color {
    return when (tier) {
        ModelTier.FLAGSHIP_FRONTIER -> BentoPrimary
        ModelTier.BALANCED -> TierBalanced
        ModelTier.FAST_LIGHTWEIGHT -> BentoSuccess
        ModelTier.LOCAL_OFFLINE -> TierLocal
    }
}

@Composable
fun TaskTypeBadge(taskType: TaskType, modifier: Modifier = Modifier) {
    val icon = getTaskIcon(taskType)
    Surface(
        shape = RoundedCornerShape(50),
        color = BentoPrimaryContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderHighlight),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = taskType.displayName,
                tint = BentoOnPrimaryContainer,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = taskType.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    letterSpacing = 0.3.sp
                ),
                color = BentoOnPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ModelTierBadge(tier: ModelTier, modifier: Modifier = Modifier) {
    val color = getTierColor(tier)
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = tier.displayName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ChangeCategoryChip(category: ChangeLogCategory, modifier: Modifier = Modifier) {
    val color = Color(category.colorHex)
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Text(
            text = category.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ComplexityMeter(score: Int, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "COMPLEXITY: $score/10",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            ),
            color = BentoTextSecondary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..10) {
                val filled = i <= score
                val color = when {
                    score <= 3 -> BentoSuccess
                    score <= 6 -> TierBalanced
                    else -> BentoPrimary
                }
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (filled) color else BentoBorder)
                )
            }
        }
    }
}

@Composable
fun ReasoningInspectionCard(
    taskType: TaskType?,
    modelName: String,
    providerId: String,
    reasoning: String,
    tokensPrompt: Int,
    tokensCompletion: Int,
    latencyMs: Long,
    costUsd: Double,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BentoSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Bento Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BentoPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Routing Info",
                            tint = BentoOnPrimaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ROUTED MODEL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = BentoTextSecondary
                        )
                        Text(
                            text = modelName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = BentoSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                    ) {
                        Text(
                            text = "${latencyMs}ms • ${(tokensPrompt + tokensCompletion)} tok",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = BentoTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = BentoTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    androidx.compose.material3.HorizontalDivider(
                        color = BentoBorder,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Inner Bento Nested Tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (taskType != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(BentoSurface)
                                    .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "CLASSIFIED TASK",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = BentoTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TaskTypeBadge(taskType = taskType)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BentoSurface)
                                .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "EST. COST",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = BentoTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$${String.format("%.5f", costUsd)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSuccess
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reasoning Details Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(BentoSurface)
                            .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "ROUTING LOGIC",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = BentoTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reasoning.ifBlank { "Routed based on prompt task complexity and provider availability." },
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoTextPrimary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Prompt: $tokensPrompt tok | Output: $tokensCompletion tok | Provider: $providerId",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = BentoTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedMessageContent(text: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val parts = remember(text) { splitMarkdownCodeBlocks(text) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEach { part ->
            if (part.isCode) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoDarkTile,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF36343B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2B2930))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = (part.language.ifBlank { "code" }).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = BentoBorderHighlight
                            )
                            IconButton(
                                onClick = {
                                    copyToClipboard(context, part.content, "Code copied")
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("copy_code_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = Color(0xFFCAC4D0),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = part.content,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            color = Color(0xFFF4EFF4),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = part.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextPrimary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

data class ContentChunk(val content: String, val isCode: Boolean, val language: String = "")

fun splitMarkdownCodeBlocks(text: String): List<ContentChunk> {
    val chunks = mutableListOf<ContentChunk>()
    val codeFenceRegex = Regex("```([a-zA-Z0-9_+#-]*)\\n([\\s\\S]*?)```")
    var lastIdx = 0

    codeFenceRegex.findAll(text).forEach { matchResult ->
        val textBefore = text.substring(lastIdx, matchResult.range.first)
        if (textBefore.isNotBlank()) {
            chunks.add(ContentChunk(textBefore.trim(), isCode = false))
        }
        val language = matchResult.groupValues[1].trim()
        val codeBody = matchResult.groupValues[2].trimEnd()
        chunks.add(ContentChunk(codeBody, isCode = true, language = language))
        lastIdx = matchResult.range.last + 1
    }

    if (lastIdx < text.length) {
        val remaining = text.substring(lastIdx).trim()
        if (remaining.isNotBlank()) {
            chunks.add(ContentChunk(remaining, isCode = false))
        }
    }

    if (chunks.isEmpty() && text.isNotBlank()) {
        chunks.add(ContentChunk(text, isCode = false))
    }

    return chunks
}

fun copyToClipboard(context: Context, text: String, toastMsg: String = "Copied to clipboard") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("OmniRouter", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
}
