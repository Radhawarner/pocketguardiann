package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun StealthCalculatorOverlay(
    safetyPin: String,
    onTriggerDiscreetSos: () -> Unit,
    onExitStealth: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayValue by remember { mutableStateOf("0") }
    var equalsPressCount by remember { mutableIntStateOf(0) }

    fun onDigit(d: String) {
        displayValue = if (displayValue == "0") d else displayValue + d
        if (displayValue.endsWith(safetyPin)) {
            // Secret PIN matched! Trigger silent SOS
            onTriggerDiscreetSos()
        }
    }

    fun onClear() {
        displayValue = "0"
        equalsPressCount = 0
    }

    fun onEquals() {
        equalsPressCount++
        if (equalsPressCount >= 3) {
            // Pressed equals 3 times -> Discreet emergency trigger
            onTriggerDiscreetSos()
        } else {
            // Simple calculator calculation mock
            try {
                displayValue = "42"
            } catch (e: Exception) {
                displayValue = "0"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF171717))
            .padding(16.dp)
            .testTag("stealth_calculator_overlay")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top exit subtle affordance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculator",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onExitStealth) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Calculator Disguise",
                        tint = Color.Gray
                    )
                }
            }

            // Calculation Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = displayValue,
                    fontSize = 48.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }

            // Keypad Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CalcBtn("C", Color(0xFFA5A5A5), Color.Black) { onClear() }
                    CalcBtn("±", Color(0xFFA5A5A5), Color.Black) { /* op */ }
                    CalcBtn("%", Color(0xFFA5A5A5), Color.Black) { /* op */ }
                    CalcBtn("÷", Color(0xFFFF9F0A), Color.White) { /* op */ }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CalcBtn("7", Color(0xFF333333), Color.White) { onDigit("7") }
                    CalcBtn("8", Color(0xFF333333), Color.White) { onDigit("8") }
                    CalcBtn("9", Color(0xFF333333), Color.White) { onDigit("9") }
                    CalcBtn("×", Color(0xFFFF9F0A), Color.White) { /* op */ }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CalcBtn("4", Color(0xFF333333), Color.White) { onDigit("4") }
                    CalcBtn("5", Color(0xFF333333), Color.White) { onDigit("5") }
                    CalcBtn("6", Color(0xFF333333), Color.White) { onDigit("6") }
                    CalcBtn("-", Color(0xFFFF9F0A), Color.White) { /* op */ }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CalcBtn("1", Color(0xFF333333), Color.White) { onDigit("1") }
                    CalcBtn("2", Color(0xFF333333), Color.White) { onDigit("2") }
                    CalcBtn("3", Color(0xFF333333), Color.White) { onDigit("3") }
                    CalcBtn("+", Color(0xFFFF9F0A), Color.White) { /* op */ }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CalcBtn("0", Color(0xFF333333), Color.White) { onDigit("0") }
                    CalcBtn(".", Color(0xFF333333), Color.White) { /* dot */ }
                    CalcBtn("=", Color(0xFFFF9F0A), Color.White) { onEquals() }
                }
            }
        }
    }
}

@Composable
private fun CalcBtn(
    label: String,
    bg: Color,
    fg: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = fg
            )
        }
    }
}
