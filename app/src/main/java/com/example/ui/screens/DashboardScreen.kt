package com.example.ui.screens

import com.example.engine.ModelRouterEngine
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ModelConfigEntity
import com.example.data.model.ModelTier
import com.example.data.model.ProviderEntity
import com.example.data.model.QualityPreference
import com.example.data.model.RoutingRuleEntity
import com.example.data.model.TaskType
import com.example.ui.components.ModelTierBadge
import com.example.ui.components.TaskTypeBadge
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: OmniViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Providers & Keys", "Model Catalog", "Routing Matrix")

    val providers by viewModel.providers.collectAsStateWithLifecycle()
    val models by viewModel.models.collectAsStateWithLifecycle()
    val routingRules by viewModel.routingRules.collectAsStateWithLifecycle()
    val qualityPreference by viewModel.qualityPreference.collectAsStateWithLifecycle()
    val testingProviderId by viewModel.testingProviderId.collectAsStateWithLifecycle()

    var showAddModelDialog by remember { mutableStateOf(false) }
    var showAddProviderDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) {
        // Bento Header Title
        Surface(
            color = BentoBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "CONFIGURATION & MATRIX",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = BentoPrimary
                )
                Text(
                    text = "Neural Control Center",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
            }
        }

        // Bento Pill Segmented Tab Bar
        Surface(
            color = BentoSurfaceVariant,
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) BentoPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedTabIndex = index }
                            .testTag("dashboard_tab_$index")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else BentoTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> ProvidersTabContent(
                    providers = providers,
                    testingProviderId = testingProviderId,
                    onUpdateCredentials = { id, key, url -> viewModel.updateProviderCredentials(id, key, url) },
                    onQuickSaveKey = { id, key -> viewModel.saveApiKey(id, key) },
                    onUniversalKeySet = { key -> viewModel.setUniversalApiKey(key) },
                    onToggleEnabled = { id, enabled -> viewModel.toggleProviderEnabled(id, enabled) },
                    onTestConnection = { provider -> viewModel.testProviderConnection(provider) },
                    onOpenAddProvider = { showAddProviderDialog = true }
                )
                1 -> ModelsTabContent(
                    models = models,
                    providers = providers,
                    onToggleEnabled = { id, enabled -> viewModel.toggleModelEnabled(id, enabled) },
                    onDeleteModel = { id -> viewModel.deleteModel(id) },
                    onOpenAddModel = { showAddModelDialog = true }
                )
                2 -> RoutingMatrixTabContent(
                    routingRules = routingRules,
                    models = models,
                    qualityPreference = qualityPreference,
                    onSetQuality = { viewModel.setQualityPreference(it) },
                    onUpdateRule = { viewModel.updateRoutingRule(it) }
                )
            }
        }
    }

    if (showAddModelDialog) {
        AddCustomModelDialog(
            providers = providers,
            onDismiss = { showAddModelDialog = false },
            onAdd = { id, name, providerId, wireName, tier, caps, context, inCost, outCost ->
                viewModel.addCustomModel(id, name, providerId, wireName, tier, caps, context, inCost, outCost)
                showAddModelDialog = false
            }
        )
    }

    if (showAddProviderDialog) {
        AddCustomProviderDialog(
            onDismiss = { showAddProviderDialog = false },
            onAdd = { name, url, key ->
                viewModel.addCustomProvider(name, url, key)
                showAddProviderDialog = false
            }
        )
    }
}

@Composable
fun ProvidersTabContent(
    providers: List<ProviderEntity>,
    testingProviderId: String?,
    onUpdateCredentials: (String, String, String) -> Unit,
    onQuickSaveKey: (String, String) -> Unit,
    onUniversalKeySet: (String) -> Unit,
    onToggleEnabled: (String, Boolean) -> Unit,
    onTestConnection: (ProviderEntity) -> Unit,
    onOpenAddProvider: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var universalKeyInput by remember { mutableStateOf("") }
    var universalKeySaved by remember { mutableStateOf(false) }

    val hasAnyKey = providers.any { it.apiKey.isNotBlank() && it.id != "ollama" }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. Universal One-Key Bento Hero Card ---
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoPrimaryContainer.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BentoPrimary.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BentoPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Single API Key Mode",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextPrimary
                                )
                                Text(
                                    text = "Only 1 key needed to power all AI models",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BentoTextSecondary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (hasAnyKey) Color(0xFF10B981).copy(alpha = 0.15f) else BentoSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (hasAnyKey) Color(0xFF10B981) else BentoBorder
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (hasAnyKey) Color(0xFF10B981) else BentoTextMuted)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (hasAnyKey) "Active & Ready" else "No Details Needed",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = if (hasAnyKey) Color(0xFF10B981) else BentoTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Paste your OpenRouter (sk-or-...), OpenAI (sk-...), Gemini (AIza...), or Anthropic key below. The app handles all routing, URLs, and models automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Universal Key Input
                    OutlinedTextField(
                        value = universalKeyInput,
                        onValueChange = {
                            universalKeyInput = it
                            universalKeySaved = false
                        },
                        label = { Text("Enter Your API Key") },
                        placeholder = { Text("sk-or-... or AIza... or sk-...") },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val clipText = clipboardManager.getText()?.text ?: ""
                                        if (clipText.isNotBlank()) {
                                            universalKeyInput = clipText.trim()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste Key",
                                        tint = BentoPrimary
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder,
                            focusedContainerColor = BentoSurface,
                            unfocusedContainerColor = BentoSurface
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("universal_api_key_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (universalKeyInput.isNotBlank()) {
                                    onUniversalKeySet(universalKeyInput)
                                    universalKeySaved = true
                                }
                            },
                            enabled = universalKeyInput.isNotBlank(),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            modifier = Modifier.testTag("save_universal_key_btn")
                        ) {
                            Icon(
                                imageVector = if (universalKeySaved) Icons.Default.Check else Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (universalKeySaved) "Key Saved & Activated!" else "Activate Models",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Section Header ---
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            ) {
                Column {
                    Text(
                        text = "Individual Providers (Keys Only)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "No base URLs or configurations required",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                }
                OutlinedButton(
                    onClick = onOpenAddProvider,
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderHighlight),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary),
                    modifier = Modifier.testTag("add_custom_provider_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Custom", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // --- 3. Streamlined Provider Cards ---
        items(providers, key = { it.id }) { provider ->
            ProviderConfigCard(
                provider = provider,
                isTesting = testingProviderId == provider.id,
                onSave = { key, url -> onUpdateCredentials(provider.id, key, url) },
                onQuickSave = { key -> onQuickSaveKey(provider.id, key) },
                onToggleEnabled = { onToggleEnabled(provider.id, it) },
                onTestConnection = { onTestConnection(provider) }
            )
        }
    }
}

@Composable
fun ProviderConfigCard(
    provider: ProviderEntity,
    isTesting: Boolean,
    onSave: (String, String) -> Unit,
    onQuickSave: (String) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onTestConnection: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var apiKey by remember(provider.apiKey) { mutableStateOf(provider.apiKey) }
    var baseUrl by remember(provider.baseUrl) { mutableStateOf(provider.baseUrl) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var showAdvancedUrl by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (provider.apiKey.isNotBlank() || provider.id == "ollama") BentoPrimaryContainer
                                else BentoSurfaceVariant
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = if (provider.apiKey.isNotBlank() || provider.id == "ollama") BentoOnPrimaryContainer else BentoTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (provider.statusMessage.startsWith("Error")) MaterialTheme.colorScheme.error
                                        else if (provider.apiKey.isNotBlank() || provider.id == "ollama") Color(0xFF10B981)
                                        else BentoTextMuted
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (provider.apiKey.isBlank() && provider.id != "ollama") "Needs API Key" else provider.statusMessage,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (provider.statusMessage.startsWith("Error")) MaterialTheme.colorScheme.error else BentoTextSecondary
                            )
                        }
                    }
                }
                Switch(
                    checked = provider.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BentoPrimary
                    ),
                    modifier = Modifier.testTag("switch_provider_${provider.id}")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // API Key Input (Primary & Only Required Detail)
            OutlinedTextField(
                value = apiKey,
                onValueChange = {
                    apiKey = it
                    hasUnsavedChanges = (it != provider.apiKey || baseUrl != provider.baseUrl)
                },
                label = { Text("API Key") },
                placeholder = { Text(if (provider.id == "ollama") "ollama-local" else "Paste API Key here...") },
                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text ?: ""
                                if (clip.isNotBlank()) {
                                    apiKey = clip.trim()
                                    hasUnsavedChanges = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste from Clipboard",
                                tint = BentoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                            Icon(
                                imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Key Visibility",
                                tint = BentoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BentoPrimary,
                    unfocusedBorderColor = BentoBorder,
                    focusedContainerColor = BentoSurfaceVariant,
                    unfocusedContainerColor = BentoSurfaceVariant
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_key_${provider.id}")
            )

            // Optional Advanced Collapsible Endpoint URL (Hidden by default)
            AnimatedVisibility(visible = showAdvancedUrl || provider.isCustom) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = {
                            baseUrl = it
                            hasUnsavedChanges = (apiKey != provider.apiKey || it != provider.baseUrl)
                        },
                        label = { Text("Custom Endpoint URL (Optional)") },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder,
                            focusedContainerColor = BentoSurfaceVariant,
                            unfocusedContainerColor = BentoSurfaceVariant
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_url_${provider.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Advanced Toggle
                TextButton(
                    onClick = { showAdvancedUrl = !showAdvancedUrl },
                    modifier = Modifier.testTag("toggle_advanced_${provider.id}")
                ) {
                    Icon(
                        imageVector = if (showAdvancedUrl) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = BentoTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showAdvancedUrl) "Hide URL" else "Custom URL",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = BentoTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onTestConnection,
                        enabled = !isTesting,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderHighlight),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary),
                        modifier = Modifier.testTag("test_provider_${provider.id}")
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = BentoPrimary, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing...", style = MaterialTheme.typography.labelSmall)
                        } else {
                            Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Ping", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    if (hasUnsavedChanges || apiKey != provider.apiKey) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (showAdvancedUrl) {
                                    onSave(apiKey, baseUrl)
                                } else {
                                    onQuickSave(apiKey)
                                }
                                hasUnsavedChanges = false
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                            modifier = Modifier.testTag("save_provider_${provider.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Key", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelsTabContent(
    models: List<ModelConfigEntity>,
    providers: List<ProviderEntity>,
    onToggleEnabled: (String, Boolean) -> Unit,
    onDeleteModel: (String) -> Unit,
    onOpenAddModel: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf<ModelTier?>(null) }
    var showOnlyOnline by remember { mutableStateOf(false) }

    val onlineModelsCount = models.count { ModelRouterEngine.isModelOnline(it, providers) }

    val filteredModels = models.filter { model ->
        val isOnline = ModelRouterEngine.isModelOnline(model, providers)
        val matchesOnline = !showOnlyOnline || isOnline
        val matchesSearch = model.displayName.contains(searchQuery, ignoreCase = true) ||
                model.capabilities.contains(searchQuery, ignoreCase = true) ||
                model.providerId.contains(searchQuery, ignoreCase = true)
        val matchesTier = selectedTierFilter == null || model.tier == selectedTierFilter
        matchesOnline && matchesSearch && matchesTier
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "AI Model Catalog ($onlineModelsCount/${models.size} Online)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "Only models with live API keys are routed during chats",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                }
                Button(
                    onClick = onOpenAddModel,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                    modifier = Modifier.testTag("add_model_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Model", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        }

        // Bento Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search models, providers, capabilities...", color = BentoTextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = BentoTextSecondary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BentoPrimary,
                    unfocusedBorderColor = BentoBorder,
                    focusedContainerColor = BentoSurface,
                    unfocusedContainerColor = BentoSurface
                ),
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tier & Online Filter Chips in Bento Style
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // All Chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (selectedTierFilter == null && !showOnlyOnline) BentoPrimary else BentoSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selectedTierFilter == null && !showOnlyOnline) BentoPrimary else BentoBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            selectedTierFilter = null
                            showOnlyOnline = false
                        }
                ) {
                    Text(
                        text = "All (${models.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (selectedTierFilter == null && !showOnlyOnline) Color.White else BentoTextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Online Only Filter Chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (showOnlyOnline) Color(0xFF10B981) else BentoSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (showOnlyOnline) Color(0xFF10B981) else BentoBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            showOnlyOnline = !showOnlyOnline
                            selectedTierFilter = null
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (showOnlyOnline) Color.White else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Online ($onlineModelsCount)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (showOnlyOnline) Color.White else Color(0xFF10B981)
                        )
                    }
                }

                ModelTier.entries.forEach { tier ->
                    val isSelected = selectedTierFilter == tier && !showOnlyOnline
                    val color = getTierColor(tier)
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) color else BentoSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) color else BentoBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                showOnlyOnline = false
                                selectedTierFilter = if (isSelected) null else tier
                            }
                    ) {
                        Text(
                            text = tier.displayName.substringBefore(" "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isSelected) Color.White else color,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Bento Model Cards List
        items(filteredModels, key = { it.id }) { model ->
            val isOnline = ModelRouterEngine.isModelOnline(model, providers)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isOnline) Color(0xFF10B981).copy(alpha = 0.4f) else BentoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = model.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ModelTierBadge(tier = model.tier)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Online/Offline status pill
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isOnline) Color(0xFF10B981).copy(alpha = 0.15f) else BentoSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (isOnline) Color(0xFF10B981) else BentoTextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOnline) "🟢 Online" else "⚪ Key Required",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = if (isOnline) Color(0xFF10B981) else BentoTextMuted
                                    )
                                }
                            }

                            Text(
                                text = "${model.providerId.uppercase()} • ${model.modelIdentifier}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = BentoTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Capabilities: ${model.capabilities}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = BentoPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = BentoSurfaceVariant,
                                modifier = Modifier.padding(1.dp)
                            ) {
                                Text(
                                    text = "Context: ${(model.contextWindow / 1000)}k",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = BentoTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.padding(1.dp)
                            ) {
                                Text(
                                    text = "In: $${model.costPer1MInput} • Out: $${model.costPer1MOutput}/1M",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Switch(
                        checked = model.isEnabled,
                        onCheckedChange = { onToggleEnabled(model.id, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BentoPrimary
                        ),
                        modifier = Modifier.testTag("switch_model_${model.id}")
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutingMatrixTabContent(
    routingRules: List<RoutingRuleEntity>,
    models: List<ModelConfigEntity>,
    qualityPreference: QualityPreference,
    onSetQuality: (QualityPreference) -> Unit,
    onUpdateRule: (RoutingRuleEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Column {
                Text(
                    text = "Intelligent Routing Rules Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
                Text(
                    text = "Configure default primary and fallback models per task category",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextSecondary
                )
            }
        }

        // Global Quality Strategy Bento Selector Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderHighlight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BentoPrimaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = BentoOnPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Global Routing Optimization Policy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Controls whether the system balances performance and cost or forces maximum frontier quality.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    QualityPreference.entries.forEach { pref ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (pref == qualityPreference) BentoSurface else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (pref == qualityPreference) BentoPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSetQuality(pref) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = pref == qualityPreference,
                                    onClick = { onSetQuality(pref) },
                                    colors = androidx.compose.material3.RadioButtonDefaults.colors(
                                        selectedColor = BentoPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = pref.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (pref == qualityPreference) FontWeight.Bold else FontWeight.Normal,
                                        color = BentoTextPrimary
                                    )
                                    Text(
                                        text = pref.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = BentoTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Task Specific Rules Bento Cards
        items(routingRules, key = { it.taskType.name }) { rule ->
            TaskRuleCard(
                rule = rule,
                availableModels = models,
                onSaveRule = onUpdateRule
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRuleCard(
    rule: RoutingRuleEntity,
    availableModels: List<ModelConfigEntity>,
    onSaveRule: (RoutingRuleEntity) -> Unit
) {
    var primaryExpanded by remember { mutableStateOf(false) }
    var fallbackExpanded by remember { mutableStateOf(false) }

    val currentPrimaryModel = availableModels.find { it.id == rule.primaryModelId }
    val currentFallbackModel = availableModels.find { it.id == rule.fallbackModelId }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BentoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BentoPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = getTaskIcon(rule.taskType),
                            contentDescription = null,
                            tint = BentoOnPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = rule.taskType.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }
                TaskTypeBadge(taskType = rule.taskType)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = rule.customNotes.ifBlank { rule.taskType.description },
                style = MaterialTheme.typography.bodySmall,
                color = BentoTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Model Dropdown
            Text(
                text = "Primary Route Model:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = BentoTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = primaryExpanded,
                onExpandedChange = { primaryExpanded = !primaryExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentPrimaryModel?.displayName ?: rule.primaryModelId,
                    onValueChange = {},
                    readOnly = true,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = primaryExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = BentoSurfaceVariant,
                        unfocusedContainerColor = BentoSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = primaryExpanded,
                    onDismissRequest = { primaryExpanded = false }
                ) {
                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = model.displayName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${model.providerId.uppercase()} • ${model.tier.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BentoTextSecondary
                                    )
                                }
                            },
                            onClick = {
                                onSaveRule(rule.copy(primaryModelId = model.id))
                                primaryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fallback Model Dropdown
            Text(
                text = "Secondary Fallback Model:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = BentoTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = fallbackExpanded,
                onExpandedChange = { fallbackExpanded = !fallbackExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentFallbackModel?.displayName ?: if (rule.fallbackModelId.isNotBlank()) rule.fallbackModelId else "None (Auto)",
                    onValueChange = {},
                    readOnly = true,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fallbackExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedContainerColor = BentoSurfaceVariant,
                        unfocusedContainerColor = BentoSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = fallbackExpanded,
                    onDismissRequest = { fallbackExpanded = false }
                ) {
                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Text(text = "${model.displayName} (${model.providerId})")
                            },
                            onClick = {
                                onSaveRule(rule.copy(fallbackModelId = model.id))
                                fallbackExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomModelDialog(
    providers: List<ProviderEntity>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, ModelTier, String, Int, Double, Double) -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var modelIdentifier by remember { mutableStateOf("") }
    var selectedProviderId by remember { mutableStateOf(providers.firstOrNull()?.id ?: "openrouter") }
    var selectedTier by remember { mutableStateOf(ModelTier.FLAGSHIP_FRONTIER) }
    var capabilities by remember { mutableStateOf("Coding, Reasoning, Chat") }
    var contextWindowStr by remember { mutableStateOf("128000") }
    var inCostStr by remember { mutableStateOf("2.5") }
    var outCostStr by remember { mutableStateOf("10.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Custom AI Model",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. Qwen 2.5 Coder 32B") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = modelIdentifier,
                        onValueChange = { modelIdentifier = it },
                        label = { Text("Model API Identifier") },
                        placeholder = { Text("e.g. qwen/qwen-2.5-coder-32b-instruct") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Provider:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        providers.take(4).forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (selectedProviderId == p.id) BentoPrimary else BentoSurfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { selectedProviderId = p.id }
                            ) {
                                Text(
                                    text = p.name.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selectedProviderId == p.id) Color.White else BentoTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = capabilities,
                        onValueChange = { capabilities = it },
                        label = { Text("Capabilities (Comma separated)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inCostStr,
                            onValueChange = { inCostStr = it },
                            label = { Text("Input $/1M") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = outCostStr,
                            onValueChange = { outCostStr = it },
                            label = { Text("Output $/1M") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (displayName.isNotBlank() && modelIdentifier.isNotBlank()) {
                        val id = "$selectedProviderId/${modelIdentifier.replace('/', '_')}"
                        val context = contextWindowStr.toIntOrNull() ?: 128000
                        val inCost = inCostStr.toDoubleOrNull() ?: 1.0
                        val outCost = outCostStr.toDoubleOrNull() ?: 3.0
                        onAdd(id, displayName, selectedProviderId, modelIdentifier, selectedTier, capabilities, context, inCost, outCost)
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text("Add Model", color = Color.White)
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

@Composable
fun AddCustomProviderDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("http://localhost:8000/v1/") }
    var apiKey by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Add Custom Provider",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Provider Name") },
                    placeholder = { Text("e.g. Local vLLM Server") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    placeholder = { Text("http://192.168.1.50:8000/v1/") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && baseUrl.isNotBlank()) {
                        onAdd(name, baseUrl, apiKey)
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text("Add Provider", color = Color.White)
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
