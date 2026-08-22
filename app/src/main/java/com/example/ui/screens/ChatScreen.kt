package com.example.ui.screens

import com.example.engine.ModelRouterEngine
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ModelConfigEntity
import com.example.data.model.QualityPreference
import com.example.ui.components.ComplexityMeter
import com.example.ui.components.FormattedMessageContent
import com.example.ui.components.ModelTierBadge
import com.example.ui.components.ReasoningInspectionCard
import com.example.ui.components.TaskTypeBadge
import com.example.ui.components.copyToClipboard
import com.example.ui.components.getTaskIcon
import com.example.ui.components.getTierColor
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoBorderDark
import com.example.ui.theme.BentoBorderHighlight
import com.example.ui.theme.BentoDarkTile
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BentoSurfaceTinted
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.TierFast
import com.example.ui.theme.TierFlagship
import com.example.ui.viewmodel.OmniViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: OmniViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val intentPreview by viewModel.promptIntentPreview.collectAsStateWithLifecycle()
    val manualOverrideModel by viewModel.manualOverrideModel.collectAsStateWithLifecycle()
    val qualityPreference by viewModel.qualityPreference.collectAsStateWithLifecycle()
    val allModels by viewModel.models.collectAsStateWithLifecycle()
    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val localModels by viewModel.localModels.collectAsStateWithLifecycle()
    val activeLocalModel by viewModel.activeLocalModel.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineModeEnabled.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showOverrideSheet by remember { mutableStateOf(false) }
    var showQualityMenu by remember { mutableStateOf(false) }
    var showQuickKeyDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val listState = rememberLazyListState()

    val hasAnyApiKey = providers.any { it.apiKey.isNotBlank() && it.id != "ollama" }

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) {
        // --- Top Header Bar in Bento Grid Style ---
        Surface(
            color = BentoBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Header Titles
                Column {
                    Text(
                        text = "MULTI-MODEL HUB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = BentoPrimary
                    )
                    Text(
                        text = "Nexus AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }

                // Header Action Pill
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // API Key Quick Button
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (hasAnyApiKey) Color(0xFF10B981).copy(alpha = 0.15f) else BentoPrimaryContainer,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasAnyApiKey) Color(0xFF10B981) else BentoPrimary
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showQuickKeyDialog = true }
                            .testTag("quick_key_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Set API Key",
                                tint = if (hasAnyApiKey) Color(0xFF10B981) else BentoPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasAnyApiKey) "Key Set" else "Set Key",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = if (hasAnyApiKey) Color(0xFF10B981) else BentoPrimary
                            )
                        }
                    }

                    // Router Status / Model Pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isOfflineMode) BentoPrimary.copy(alpha = 0.12f) else BentoSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isOfflineMode) BentoPrimary.copy(alpha = 0.5f) else BentoBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showOverrideSheet = true }
                            .testTag("model_override_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isOfflineMode) BentoPrimary
                                        else if (manualOverrideModel != null) BentoPrimary
                                        else Color(0xFF10B981)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOfflineMode) (activeLocalModel?.displayName?.let { "📱 $it" } ?: "📱 Offline Mode")
                                       else (manualOverrideModel?.displayName ?: "Auto Router"),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = if (isOfflineMode) BentoPrimary else BentoTextPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    // Quality Strategy Menu
                    Box {
                        IconButton(
                            onClick = { showQualityMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("quality_pref_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Quality strategy",
                                tint = BentoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showQualityMenu,
                            onDismissRequest = { showQualityMenu = false }
                        ) {
                            Text(
                                text = "Routing Policy",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BentoPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                            QualityPreference.entries.forEach { pref ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = pref.displayName,
                                                fontWeight = if (pref == qualityPreference) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = pref.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = BentoTextSecondary
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (pref == qualityPreference) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = BentoPrimary
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.setQualityPreference(pref)
                                        showQualityMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("clear_chat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear Chat",
                            tint = BentoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // --- Chat Messages List ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty() && !isGenerating) {
                // Empty State / Bento Suggestion Grid
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Bento Welcome Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = BentoSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(BentoPrimaryContainer)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "OmniRouter AI",
                                                tint = BentoOnPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Intelligent Auto-Router",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoTextPrimary
                                            )
                                            Text(
                                                text = "Live neural routing engine",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = BentoTextSecondary
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = BentoPrimary
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Prompts are classified into technical domains (Coding, Reasoning, Research) and routed automatically to frontier flagships (GPT-5, Claude 3.7) or lightweight low-latency models (Gemini 2.5 Flash).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (!hasAnyApiKey) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BentoPrimaryContainer.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, BentoPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { showQuickKeyDialog = true }
                                    .testTag("empty_state_key_banner")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(BentoPrimary)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Key,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Only 1 API Key Needed",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoTextPrimary
                                            )
                                            Text(
                                                text = "Paste your OpenRouter/OpenAI/Gemini key to activate all models",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = BentoTextSecondary
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { showQuickKeyDialog = true },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                                    ) {
                                        Text("Set Key", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "SUGGESTED WORKLOADS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = BentoTextSecondary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    val samplePrompts = listOf(
                        Triple("💻", "Complex Coding", "Write a thread-safe LRU Cache in Kotlin using Coroutines and Mutex locks"),
                        Triple("⚡", "Fast Search", "What is quantum entanglement in simple, everyday terms?"),
                        Triple("🧠", "Logic & Reasoning", "If 5 machines take 5 minutes to make 5 widgets, how long for 100 machines?"),
                        Triple("📚", "Research Synthesis", "Comprehensive architectural comparison of Transformers vs RNNs")
                    )

                    samplePrompts.forEach { (icon, category, prompt) ->
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = BentoSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        inputText = prompt
                                        viewModel.onPromptTextChanged(inputText)
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = BentoSurfaceVariant,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = icon, fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = category.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            ),
                                            color = BentoPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = prompt,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = BentoTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages, key = { it.id }) { message ->
                        ChatMessageItem(message = message)
                    }

                    if (isGenerating) {
                        item {
                            GenerationLoadingItem(intentPreview = intentPreview)
                        }
                    }
                }
            }
        }

        // --- Real-time Intent Prediction Bento Bar ---
        AnimatedVisibility(
            visible = intentPreview != null && inputText.isNotBlank(),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut()
        ) {
            intentPreview?.let { preview ->
                Surface(
                    color = BentoSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderHighlight),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TaskTypeBadge(taskType = preview.detectedTask)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "→ ${preview.recommendedModelName}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = BentoOnPrimaryContainer
                            )
                        }
                        ComplexityMeter(score = preview.complexityScore)
                    }
                }
            }
        }

        // --- Bottom Input Bar Bento Card ---
        Surface(
            color = BentoBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        viewModel.onPromptTextChanged(it)
                    },
                    placeholder = {
                        Text(
                            text = if (manualOverrideModel != null) "Ask ${manualOverrideModel?.displayName}..." else "Ask anything (Auto-routed by complexity)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoTextMuted
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = BentoSurface,
                        unfocusedContainerColor = BentoSurface,
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary
                    ),
                    shape = RoundedCornerShape(28.dp),
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val textToSend = inputText
                        inputText = ""
                        viewModel.onPromptTextChanged("")
                        viewModel.sendPrompt(textToSend)
                    },
                    enabled = inputText.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isGenerating) BentoPrimary
                            else BentoBorderDark.copy(alpha = 0.5f)
                        )
                        .testTag("send_prompt_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send prompt",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // --- Model Override Bottom Sheet in Bento Style ---
    if (showOverrideSheet) {
        ModalBottomSheet(
            onDismissRequest = { showOverrideSheet = false },
            sheetState = sheetState,
            containerColor = BentoSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "ROUTING OVERRIDE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = BentoPrimary
                        )
                        Text(
                            text = "Select Model / Routing Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                    if (manualOverrideModel != null) {
                        TextButton(
                            onClick = {
                                viewModel.setManualOverrideModel(null)
                                showOverrideSheet = false
                            },
                            modifier = Modifier.testTag("reset_auto_route_btn")
                        ) {
                            Text("Reset to Auto", color = BentoPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "By default, OmniRouter analyzes prompt intent & complexity. Select a model below to manually force all responses to that model.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Auto-Router Option
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (manualOverrideModel == null) BentoPrimaryContainer else BentoSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (manualOverrideModel == null) BentoBorderHighlight else BentoBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.setManualOverrideModel(null)
                                    showOverrideSheet = false
                                }
                                .testTag("select_auto_router_option")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (manualOverrideModel == null) BentoPrimary else BentoBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Intelligent Auto-Router (Recommended)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (manualOverrideModel == null) BentoOnPrimaryContainer else BentoTextPrimary
                                    )
                                    Text(
                                        text = "Dynamic allocation based on Coding, Reasoning, and Research complexity",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BentoTextSecondary
                                    )
                                }
                                if (manualOverrideModel == null) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = BentoOnPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Local GGUF Models Section
                    if (localModels.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📱 ON-DEVICE GGUF MODELS (OFFLINE)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = BentoPrimary,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }

                        items(localModels) { localModel ->
                            val isCurrentLocal = isOfflineMode && (activeLocalModel?.id == localModel.id)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isCurrentLocal) BentoPrimaryContainer else BentoSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrentLocal) BentoBorderHighlight else if (localModel.isDownloaded) Color(0xFF10B981).copy(alpha = 0.4f) else BentoBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        viewModel.toggleOfflineMode(true)
                                        viewModel.loadLocalModelToRam(localModel)
                                        viewModel.setManualOverrideModel(null)
                                        showOverrideSheet = false
                                    }
                                    .testTag("local_model_option_${localModel.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = localModel.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentLocal) BentoOnPrimaryContainer else BentoTextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = BentoPrimary.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = localModel.parameters,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoPrimary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (localModel.isDownloaded) "🟢 On-Device Ready" else "⚪ Download Needed",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = if (localModel.isDownloaded) Color(0xFF10B981) else BentoTextMuted
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${localModel.quantization} • ${localModel.fileSizeFormatted} • 0ms Cloud",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = BentoTextSecondary
                                            )
                                        }
                                    }
                                    if (isCurrentLocal) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = BentoOnPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Cloud Models Section
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "☁️ CLOUD NEURAL MODELS (API)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = BentoTextSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    // Available Models
                    items(allModels) { model ->
                        val isSelected = !isOfflineMode && manualOverrideModel?.id == model.id
                        val isOnline = ModelRouterEngine.isModelOnline(model, providers)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) BentoPrimaryContainer else BentoSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BentoBorderHighlight else if (isOnline) Color(0xFF10B981).copy(alpha = 0.4f) else BentoBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.toggleOfflineMode(false)
                                    viewModel.setManualOverrideModel(model)
                                    showOverrideSheet = false
                                }
                                .testTag("model_option_${model.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = model.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) BentoOnPrimaryContainer else BentoTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ModelTierBadge(tier = model.tier)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isOnline) "🟢 Online" else "⚪ Key Needed",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isOnline) Color(0xFF10B981) else BentoTextMuted
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${model.providerId.uppercase()} • ${model.capabilities}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = BentoTextSecondary
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = BentoOnPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showQuickKeyDialog) {
        QuickApiKeyDialog(
            onDismiss = { showQuickKeyDialog = false },
            onSaveUniversalKey = { key ->
                viewModel.setUniversalApiKey(key)
                showQuickKeyDialog = false
            }
        )
    }
}

@Composable
fun ChatMessageItem(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    if (isUser) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp).copy(bottomEnd = androidx.compose.foundation.shape.CornerSize(4.dp)),
                color = BentoPrimary,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    lineHeight = 22.sp
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp).copy(bottomStart = androidx.compose.foundation.shape.CornerSize(4.dp)),
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with model info and copy action
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(BentoPrimaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BentoOnPrimaryContainer,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.routedModelName.ifBlank { "OmniRouter Assistant" },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        }

                        IconButton(
                            onClick = { copyToClipboard(context, message.content, "Response copied") },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("copy_response_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Response",
                                tint = BentoTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Message Content
                    FormattedMessageContent(text = message.content)

                    // Routing Inspector Card
                    Spacer(modifier = Modifier.height(12.dp))
                    ReasoningInspectionCard(
                        taskType = message.taskTypeDetected,
                        modelName = message.routedModelName,
                        providerId = message.routedProviderId,
                        reasoning = message.routingReason,
                        tokensPrompt = message.tokensPrompt,
                        tokensCompletion = message.tokensCompletion,
                        latencyMs = message.latencyMs,
                        costUsd = message.costUsd
                    )
                }
            }
        }
    }
}

@Composable
fun GenerationLoadingItem(intentPreview: com.example.ui.viewmodel.PromptIntentPreview?) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BentoSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderHighlight),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = BentoPrimary,
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Routing request through neural matrix...",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                if (intentPreview != null) {
                    Text(
                        text = "Intent: ${intentPreview.detectedTask.displayName} • Selected: ${intentPreview.recommendedModelName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun QuickApiKeyDialog(
    onDismiss: () -> Unit,
    onSaveUniversalKey: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var keyInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BentoPrimaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = BentoOnPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Quick API Key Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Paste any API Key (OpenRouter, OpenAI, Google Gemini, Anthropic, DeepSeek). The app will automatically route and configure the models for you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextSecondary,
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("API Key") },
                    placeholder = { Text("sk-or-v1-... or AIza...") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    keyInput = clip.trim()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste from clipboard",
                                tint = BentoPrimary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = BentoSurfaceVariant,
                        unfocusedContainerColor = BentoSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_key_input")
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BentoSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recommended: OpenRouter key accesses all GPT-5, Claude 3.7 & Gemini models simultaneously.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = BentoTextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (keyInput.isNotBlank()) {
                        onSaveUniversalKey(keyInput.trim())
                    }
                },
                enabled = keyInput.isNotBlank(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                modifier = Modifier.testTag("save_quick_key_btn")
            ) {
                Text("Save & Activate", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BentoTextSecondary)
            }
        },
        containerColor = BentoSurface,
        shape = RoundedCornerShape(24.dp)
    )
}
