package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.repository.AiRepository
import com.example.ui.theme.PrivoCyanAccent
import com.example.ui.theme.PrivoVioletPrimary
import kotlinx.coroutines.launch

@Composable
fun AiImageGenScreen(
    aiRepository: AiRepository,
    onNavigateBack: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("futuristic") }
    var generatedImageUrl by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val styles = listOf(
        Pair("آینده‌نگرانه", "futuristic"),
        Pair("واقع‌گرایانه", "realistic"),
        Pair("انیمه", "anime"),
        Pair("هنر دیجیتال", "digital_art")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.testTag("ai_image_back_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "تولید تصویر با هوش مصنوعی",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("توصیف تصویر مورد نظر (پرامپت)...") },
            modifier = Modifier.fillMaxWidth().testTag("ai_image_prompt_input"),
            shape = RoundedCornerShape(14.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "سبک تصویر:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            styles.forEach { (label, key) ->
                val isSelected = selectedStyle == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedStyle = key },
                    label = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (prompt.isNotBlank()) {
                    isGenerating = true
                    scope.launch {
                        val res = aiRepository.generateImage(prompt, selectedStyle)
                        generatedImageUrl = res.getOrNull()
                        isGenerating = false
                    }
                }
            },
            enabled = prompt.isNotBlank() && !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("ai_image_generate_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrivoVioletPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("شروع تولید تصویر")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (generatedImageUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(PrivoCyanAccent, PrivoVioletPrimary)),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                AsyncImage(
                    model = generatedImageUrl,
                    contentDescription = "تصویر تولید شده با AI",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { /* Save */ },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ذخیره در گالری")
                }
                Button(
                    onClick = { /* Share */ },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اشتراک‌گذاری در چت")
                }
            }
        }
    }
}
