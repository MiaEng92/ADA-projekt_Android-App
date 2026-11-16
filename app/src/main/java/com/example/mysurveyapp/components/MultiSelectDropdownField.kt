package com.example.mysurveyapp.components

import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

// En dropdown som DropdownField, men man kan afkrydse flere muligheder. Menuen bliver åben, mens man klikker.
// Feltet viser de valgte: op til to navne, ellers "3 valgt".
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdownField(
    label: String,
    choiceOptions: List<String>,
    chosen: Set<String>,
    onChosenChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val allChosen = choiceOptions.isNotEmpty() && choiceOptions.all { it in chosen }
    val shownText = when {
        chosen.isEmpty() -> ""
        chosen.size <= 2 -> choiceOptions.filter { it in chosen }.joinToString(", ")
        else -> "${chosen.size} valgt"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = shownText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (choiceOptions.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Ingen muligheder") },
                    onClick = {},
                    enabled = false
                )
            } else {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (allChosen) "Fjern alle" else "Vælg alle",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    onClick = { onChosenChange(if (allChosen) emptySet() else choiceOptions.toSet()) }
                )
                HorizontalDivider()
                choiceOptions.forEach { option ->
                    val isChosen = option in chosen
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onChosenChange(if (isChosen) chosen - option else chosen + option) },
                        leadingIcon = { Checkbox(checked = isChosen, onCheckedChange = null) }
                    )
                }
            }
        }
    }
}