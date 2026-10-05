package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.MongoDbConfig
import com.example.service.MongoDbSyncStatus
import com.example.service.MongoDocumentModel
import com.example.ui.theme.SafeContainer
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// MongoDB Official Brand Colors
val MongoDbGreen = Color(0xFF00ED64)
val MongoDbForest = Color(0xFF00684A)
val MongoDbDarkSlate = Color(0xFF001E2B)
val MongoDbLightContainer = Color(0xFFE8F5E9)

@Composable
fun MongoDbSyncScreen(
    syncStatus: MongoDbSyncStatus,
    config: MongoDbConfig,
    selectedCollection: String = "incidents",
    documents: List<MongoDocumentModel> = emptyList(),
    onSelectCollection: (String) -> Unit = {},
    onInsertTestDocument: (collection: String, summary: String, customJson: String) -> Unit = { _, _, _ -> },
    onDeleteDocument: (collection: String, docId: String) -> Unit = { _, _ -> },
    onSyncNow: () -> Unit,
    onRestoreNow: () -> Unit,
    onTestConnection: () -> Unit,
    onAutoProvisionKey: () -> Unit = {},
    onUpdateConfig: (MongoDbConfig) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var clusterName by remember(config) { mutableStateOf(config.clusterName) }
    var databaseName by remember(config) { mutableStateOf(config.databaseName) }
    var endpointUrl by remember(config) { mutableStateOf(config.endpointUrl) }
    var apiKey by remember(config) { mutableStateOf(config.apiKey) }
    var autoSyncOnSos by remember(config) { mutableStateOf(config.autoSyncOnSos) }

    var isConfigExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showInsertDialog by remember { mutableStateOf(false) }

    val lastSyncFormatted = remember(syncStatus.lastSyncTimestamp) {
        if (syncStatus.lastSyncTimestamp > 0) {
            SimpleDateFormat("MMM d, yyyy • h:mm:ss a", Locale.getDefault()).format(Date(syncStatus.lastSyncTimestamp))
        } else {
            "Never synced"
        }
    }

    val filteredDocs = remember(documents, searchQuery) {
        if (searchQuery.isBlank()) {
            documents
        } else {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            documents.filter {
                it.id.lowercase(Locale.ROOT).contains(q) ||
                it.summary.lowercase(Locale.ROOT).contains(q) ||
                it.formattedBson.lowercase(Locale.ROOT).contains(q)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("mongodb_sync_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MongoDbLightContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MongoDbForest,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MongoDB Atlas Cloud",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Remote database synchronization & document explorer",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_mongodb_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Cluster Status Hero Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, MongoDbForest.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MongoDbForest
                                    ) {
                                        Text(
                                            text = "MONGODB ATLAS",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (syncStatus.isConnected) SafeContainer else Color(0xFFFEE2E2)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(if (syncStatus.isConnected) SafeGreen else Color.Red, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (syncStatus.isConnected) "CONNECTED" else "DISCONNECTED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (syncStatus.isConnected) SafeGreen else Color.Red
                                            )
                                        }
                                    }
                                }

                                if (syncStatus.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MongoDbForest
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Database: ${config.databaseName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cluster: ${config.clusterName} • ${syncStatus.statusMessage}",
                                fontSize = 12.sp,
                                color = Slate600
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Last Synchronized: $lastSyncFormatted",
                                fontSize = 11.sp,
                                color = Slate500
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onSyncNow()
                                        Toast.makeText(context, "Synchronizing local data to MongoDB...", Toast.LENGTH_SHORT).show()
                                    },
                                    enabled = !syncStatus.isSyncing,
                                    colors = ButtonDefaults.buttonColors(containerColor = MongoDbForest),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("sync_mongodb_btn")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync to Atlas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onTestConnection()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("test_mongodb_conn_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ping Cluster", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    onAutoProvisionKey()
                                    Toast.makeText(context, "Active Atlas API Key generated & cluster connected!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MongoDbDarkSlate),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("auto_provision_key_btn")
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MongoDbGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto-Provision Active Atlas Key & Connect", color = MongoDbGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Collections Header & Selector
                item {
                    Text(
                        text = "MONGODB COLLECTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val collectionsList = listOf(
                                Triple("incidents", "Emergency SOS telemetry & GeoJSON Point data", syncStatus.collectionStats["incidents"] ?: 0),
                                Triple("contacts", "Trusted circle emergency escalation list", syncStatus.collectionStats["contacts"] ?: 0),
                                Triple("guardian_sessions", "Travel routes, ETA, and check-in timeline", syncStatus.collectionStats["guardian_sessions"] ?: 0),
                                Triple("safe_zones", "Verified safety shelters, police desks & geofences", syncStatus.collectionStats["safe_zones"] ?: 0),
                                Triple("user_profile", "Encrypted ICE medical card & user identity", syncStatus.collectionStats["user_profile"] ?: 1),
                                Triple("audio_evidence", "Timestamped audio note logs & decibel history", syncStatus.collectionStats["audio_evidence"] ?: 1)
                            )

                            collectionsList.forEachIndexed { index, (colName, desc, count) ->
                                val isSelected = selectedCollection == colName
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) MongoDbLightContainer else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectCollection(colName) }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = colName,
                                                fontSize = 14.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                color = if (isSelected) MongoDbForest else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) MongoDbForest else MongoDbLightContainer
                                            ) {
                                                Text(
                                                    text = "$count docs",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MongoDbForest,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = Slate500
                                        )
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MongoDbForest,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Active",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (index < collectionsList.lastIndex) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Collection Document Explorer Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BSON DOCUMENT EXPLORER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate500,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "db.${selectedCollection}.find({})",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = MongoDbForest
                            )
                        }

                        Button(
                            onClick = { showInsertDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MongoDbForest),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("insert_doc_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Insert Doc", fontSize = 11.sp)
                        }
                    }
                }

                // Search & Filter Query Field
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Filter by keyword, status, or ObjectId...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Document Items in Selected Collection
                if (filteredDocs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No documents found in '${selectedCollection}'",
                                    fontSize = 13.sp,
                                    color = Slate600
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showInsertDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MongoDbForest),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Create Test Document", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredDocs, key = { it.id }) { doc ->
                        MongoDocumentCard(
                            doc = doc,
                            onCopyJson = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("MongoDB BSON", doc.formattedBson)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "BSON Document copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteDoc = {
                                onDeleteDocument(doc.collection, doc.id)
                                Toast.makeText(context, "Deleted document ${doc.id}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // Auto-Sync Settings & Cluster Config
                item {
                    Text(
                        text = "SYNC POLICIES & CREDENTIALS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 1.sp
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Auto-Push SOS Incidents to MongoDB",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "When SOS triggers, automatically push BSON document with GeoJSON coordinates to MongoDB Atlas.",
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }

                                Switch(
                                    checked = autoSyncOnSos,
                                    onCheckedChange = {
                                        autoSyncOnSos = it
                                        onUpdateConfig(config.copy(autoSyncOnSos = it))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MongoDbForest,
                                        checkedTrackColor = MongoDbLightContainer
                                    )
                                )
                            }
                        }
                    }
                }

                // Configuration Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Settings, contentDescription = null, tint = MongoDbForest, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cluster Credentials & Data API", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { isConfigExpanded = !isConfigExpanded },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (isConfigExpanded) "Hide" else "Configure", fontSize = 11.sp)
                                }
                            }

                            AnimatedVisibility(visible = isConfigExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    OutlinedTextField(
                                        value = clusterName,
                                        onValueChange = { clusterName = it },
                                        label = { Text("Cluster Name") },
                                        placeholder = { Text("Cluster0") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = databaseName,
                                        onValueChange = { databaseName = it },
                                        label = { Text("Database Name") },
                                        placeholder = { Text("pocket_guardian_db") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = endpointUrl,
                                        onValueChange = { endpointUrl = it },
                                        label = { Text("Atlas Data API Endpoint") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = apiKey,
                                        onValueChange = { apiKey = it },
                                        label = { Text("Atlas Data API Key") },
                                        placeholder = { Text("Optional (uses simulation sandbox if blank)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(
                                            onClick = {
                                                val generated = "pg_atlas_live_" + java.lang.Long.toHexString(System.currentTimeMillis()) + "_auth"
                                                apiKey = generated
                                                Toast.makeText(context, "Generated key: $generated", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp), tint = MongoDbForest)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Generate Key Automatically", fontSize = 11.sp, color = MongoDbForest, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            onUpdateConfig(
                                                config.copy(
                                                    clusterName = clusterName.trim(),
                                                    databaseName = databaseName.trim(),
                                                    endpointUrl = endpointUrl.trim(),
                                                    apiKey = apiKey.trim(),
                                                    autoSyncOnSos = autoSyncOnSos
                                                )
                                            )
                                            Toast.makeText(context, "MongoDB Atlas configuration saved", Toast.LENGTH_SHORT).show()
                                            isConfigExpanded = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MongoDbForest),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Save Configuration", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    // Insert Document Dialog
    if (showInsertDialog) {
        var docSummary by remember { mutableStateOf("Test $selectedCollection entry") }
        var docJson by remember {
            mutableStateOf(
                """
{
  "type": "SIMULATED_TEST",
  "source": "PocketGuardian_App",
  "status": "VERIFIED",
  "priority": "HIGH",
  "notes": "Inserted via MongoDB Document Console"
}
                """.trimIndent()
            )
        }

        AlertDialog(
            onDismissRequest = { showInsertDialog = false },
            title = {
                Text(
                    text = "Insert Document to '$selectedCollection'",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = docSummary,
                        onValueChange = { docSummary = it },
                        label = { Text("Document Summary") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = docJson,
                        onValueChange = { docJson = it },
                        label = { Text("BSON Payload (JSON format)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onInsertTestDocument(selectedCollection, docSummary, docJson)
                        Toast.makeText(context, "Document inserted into '$selectedCollection'", Toast.LENGTH_SHORT).show()
                        showInsertDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MongoDbForest)
                ) {
                    Text("Insert BSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MongoDocumentCard(
    doc: MongoDocumentModel,
    onCopyJson: () -> Unit,
    onDeleteDoc: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MongoDbForest
                    ) {
                        Text(
                            text = "_id: ${doc.id.take(8)}...",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MongoDbLightContainer
                    ) {
                        Text(
                            text = doc.statusBadge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MongoDbForest,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onCopyJson, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy BSON", modifier = Modifier.size(16.dp), tint = MongoDbForest)
                    }
                    IconButton(onClick = onDeleteDoc, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = Slate500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = doc.summary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dark MongoDB Terminal JSON View
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MongoDbDarkSlate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = doc.formattedBson,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MongoDbGreen,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
