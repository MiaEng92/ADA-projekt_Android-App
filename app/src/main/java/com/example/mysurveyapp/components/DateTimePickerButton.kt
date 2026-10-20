package com.example.mysurveyapp.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerButton(
    label: String,                           // fx "Startdato"
    value: LocalDateTime?,                   // den valgte dato+tid (null = intet valgt endnu)
    onValueChange: (LocalDateTime) -> Unit,  // kaldes når brugeren er færdig
    modifier: Modifier = Modifier
) {
    var visDatoVaelger by remember { mutableStateOf(false) }
    var visTidVaelger by remember { mutableStateOf(false) }
    var valgtDato by remember { mutableStateOf<LocalDate?>(null) }

    val format = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    // Selve knappen
    OutlinedButton(
        onClick = { visDatoVaelger = true },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(if (value == null) label else value.format(format),
    maxLines = 1)
    }

    // Dialog 1: vælg dato
    if (visDatoVaelger) {
        val datoState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { visDatoVaelger = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datoState.selectedDateMillis
                    if (millis != null) {
                        // DatePicker giver tid i UTC, så vi bruger UTC her
                        valgtDato = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        visDatoVaelger = false
                        visTidVaelger = true   // gå videre til tid
                    }
                }) { Text("Næste") }
            },
            dismissButton = {
                TextButton(onClick = { visDatoVaelger = false }) { Text("Annuller") }
            }
        ) {
            DatePicker(state = datoState)
        }
    }

    // Dialog 2: vælg klokkeslæt
    if (visTidVaelger) {
        val tidState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { visTidVaelger = false },
            confirmButton = {
                TextButton(onClick = {
                    val dato = valgtDato
                    if (dato != null) {
                        onValueChange(
                            LocalDateTime.of(dato, LocalTime.of(tidState.hour, tidState.minute))
                        )
                    }
                    visTidVaelger = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { visTidVaelger = false }) { Text("Annuller") }
            },
            text = { TimePicker(state = tidState) }
        )
    }
}