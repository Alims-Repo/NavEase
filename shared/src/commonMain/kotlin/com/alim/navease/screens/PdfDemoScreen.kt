package com.alim.navease.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.alimsrepo.navease.runtime.navigation.NavController
import io.github.alimsrepo.navease.runtime.presentation.ActivityScreen

class PdfDemoScreen : ActivityScreen<AppScreens.PdfDemo>() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: AppScreens.PdfDemo, navController: NavController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Pdf Generator", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Kotlin DSL document builder",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        NavBackButton(onClick = { navController.back() })
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── DSL code example ───────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "KOTLIN DSL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "val file = File(context.cacheDir, \"report.pdf\")\n\npdf {\n" +
                                       "    pageSize(PageSize.A4)\n" +
                                       "    orientation(PageOrientation.PORTRAIT)\n" +
                                       "    margins(PageMargins.NORMAL)\n\n" +
                                       "    header { text(\"Confidential — Q1 2026\") }\n" +
                                       "    footer { pageNumber() }\n" +
                                       "    watermark(\"DRAFT\", alpha = 0.08f)\n\n" +
                                       "    title(\"Sales Report Q1 2026\")\n" +
                                       "    spacer(16f)\n" +
                                       "    divider()\n\n" +
                                       "    heading(\"Summary\")\n" +
                                       "    text(\"Strong growth driven by the Widget product line.\")\n\n" +
                                       "    table {\n" +
                                       "        header(\"Product\", \"Units\", \"Revenue\")\n" +
                                       "        row(\"Widget A\", \"1,200\", \"\$24,000\")\n" +
                                       "        row(\"Widget B\", \"850\",   \"\$17,000\")\n" +
                                       "        row(\"Widget C\", \"430\",   \"\$8,600\")\n" +
                                       "    }\n\n" +
                                       "    heading(\"Highlights\")\n" +
                                       "    bulletList(\n" +
                                       "        \"Exceeded target by 12%\",\n" +
                                       "        \"3 new markets opened\",\n" +
                                       "        \"NPS score: 72\"\n" +
                                       "    )\n\n" +
                                       "    infoBox(\"Next review: April 1, 2026\")\n" +
                                       "    qrCode(QRData.Url(\"https://company.com/q1-report\"))\n\n" +
                                       "}.saveToFile(file)",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
                            )
                        }
                    }
                }

                // ── Document preview simulation ────────────────────────────────
                item {
                    Text(
                        "DOCUMENT PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    // A4 page simulation
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Header bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Confidential — Q1 2026",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.30f))
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            // Title
                            Text(
                                "Sales Report Q1 2026",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Strong growth driven by the Widget product line.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                            Spacer(Modifier.height(12.dp))

                            // Table preview
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column {
                                    // Header row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        listOf("Product", "Units", "Revenue").forEach { h ->
                                            Text(h, style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    // Data rows
                                    listOf(
                                        Triple("Widget A", "1,200", "\$24,000"),
                                        Triple("Widget B", "850",   "\$17,000"),
                                        Triple("Widget C", "430",   "\$8,600"),
                                    ).forEach { (p, u, r) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(p, style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface)
                                            Text(u, style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurface)
                                            Text(r, style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Bullet list
                            Text("Highlights", style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            listOf("Exceeded target by 12%", "3 new markets opened", "NPS score: 72")
                                .forEach { point ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("•", color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall)
                                        Text(point, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f))
                                    }
                                }

                            Spacer(Modifier.height(10.dp))

                            // Info box mock
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("ℹ️", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "Next review: April 1, 2026",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Page 1 of 1",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // ── Element reference ──────────────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "DSL ELEMENTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            listOf(
                                "title(\"...\")            " to "Bold page title",
                                "heading(\"...\")          " to "Section heading",
                                "text(\"...\")             " to "Body paragraph",
                                "bulletList(...)           " to "Bullet list — auto paginates",
                                "numberedList(...)         " to "Numbered list",
                                "table { header(); row() }" to "Full table with auto page-split",
                                "image(bitmap, ...)        " to "Image with sizing + alignment",
                                "qrCode(QRData.Url(...))   " to "QR code generation",
                                "checkbox(label, checked)  " to "Form checkbox",
                                "infoBox / warningBox      " to "Highlighted content boxes",
                                "spacer(dp) / divider()    " to "Spacing helpers",
                                "watermark(text, alpha)    " to "Diagonal text watermark",
                            ).forEach { (fn, desc) ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(
                                        text = fn,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1.4f)
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── QR code element reference ──────────────────────────────────
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "QR CODE TYPES",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            listOf(
                                "QRData.Url(url)",
                                "QRData.Email(address, subject, body)",
                                "QRData.Phone(number)",
                                "QRData.Sms(number, message)",
                                "QRData.WiFi(ssid, password, type)",
                                "QRData.Contact(name, phone, email)",
                                "QRData.GeoLocation(lat, lon)",
                                "QRData.Text(raw)",
                            ).forEach { code ->
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

