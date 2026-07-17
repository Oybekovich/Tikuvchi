package uz.tikuvchi.ui.screens.measurements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.tikuvchi.R
import uz.tikuvchi.data.model.Measurement
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.EmptyState
import uz.tikuvchi.ui.components.LabeledField
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.components.SecondaryButton
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Red700
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.util.formatDate

@Composable
fun MeasurementsScreen(
    onMenu: () -> Unit,
    onProfile: () -> Unit,
    vm: MeasurementsViewModel = viewModel(),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding()) {
        AppHeader(
            title = stringResource(R.string.measurements_title),
            onMenu = onMenu,
            onProfile = onProfile,
        )

        when {
            s.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 24.dp + WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (s.items.isEmpty()) {
                    item {
                        EmptyState(
                            icon = "📏",
                            title = stringResource(R.string.measurements_empty),
                            hint = stringResource(R.string.measurements_empty_hint),
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                } else {
                    items(s.items, key = { it.id.orEmpty() }) { m ->
                        MeasurementCard(
                            m = m,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onEdit = { vm.openEdit(m) },
                            onDelete = { confirmDelete = m.id },
                        )
                    }
                }

                item {
                    PrimaryButton(
                        text = stringResource(R.string.measurements_add),
                        onClick = vm::openNew,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }

    s.form?.let { form -> FormDialog(form, s.saving, s.error, vm) }

    confirmDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.common_delete)) },
            text = { Text(stringResource(R.string.measurements_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = null; vm.delete(id) }) {
                    Text(stringResource(R.string.common_delete), color = Red700)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            containerColor = Cream50,
        )
    }
}

@Composable
private fun MeasurementCard(
    m: Measurement,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    m.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink900,
                )
                m.updatedAt?.let {
                    Text(
                        stringResource(R.string.measurements_updated_at, formatDate(it)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink500,
                    )
                }
            }
            Icon(
                Icons.Filled.Edit,
                contentDescription = stringResource(R.string.common_edit),
                tint = Ink500,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onEdit).padding(10.dp),
            )
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.common_delete),
                tint = Red700,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDelete).padding(10.dp),
            )
        }

        val values = listOfNotNull(
            m.chest?.let { R.string.measurements_chest to it },
            m.waist?.let { R.string.measurements_waist to it },
            m.hips?.let { R.string.measurements_hips to it },
            m.height?.let { R.string.measurements_height to it },
            m.shoulder?.let { R.string.measurements_shoulder to it },
            m.sleeveLength?.let { R.string.measurements_sleeve_length to it },
        )
        if (values.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Cream200))
            Spacer(Modifier.height(12.dp))
            values.forEach { (labelRes, value) ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        stringResource(labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ink500,
                    )
                    Text(
                        "${value.pretty()} ${stringResource(R.string.measurements_cm)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink900,
                    )
                }
            }
        }

        m.notes?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Ink700)
        }
    }
}

/** 90.0 emas, 90 — o'lchov butun bo'lsa kasr qismi ko'rsatilmaydi. */
private fun Double.pretty(): String =
    if (this % 1.0 == 0.0) toLong().toString() else toString()

@Composable
private fun FormDialog(
    form: MeasurementForm,
    saving: Boolean,
    error: Boolean,
    vm: MeasurementsViewModel,
) {
    AlertDialog(
        onDismissRequest = vm::closeForm,
        containerColor = Cream50,
        title = {
            Text(
                stringResource(
                    if (form.id == null) R.string.measurements_add
                    else R.string.measurements_edit_title
                ),
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()).imePadding(),
            ) {
                LabeledField(
                    label = stringResource(R.string.measurements_label),
                    value = form.label,
                    onValueChange = { v -> vm.editForm { it.copy(label = v) } },
                    placeholder = stringResource(R.string.measurements_label_placeholder),
                )
                NumberField(R.string.measurements_chest, form.chest) { v -> vm.editForm { it.copy(chest = v) } }
                NumberField(R.string.measurements_waist, form.waist) { v -> vm.editForm { it.copy(waist = v) } }
                NumberField(R.string.measurements_hips, form.hips) { v -> vm.editForm { it.copy(hips = v) } }
                NumberField(R.string.measurements_height, form.height) { v -> vm.editForm { it.copy(height = v) } }
                NumberField(R.string.measurements_shoulder, form.shoulder) { v -> vm.editForm { it.copy(shoulder = v) } }
                NumberField(R.string.measurements_sleeve_length, form.sleeveLength) { v -> vm.editForm { it.copy(sleeveLength = v) } }
                LabeledField(
                    label = stringResource(R.string.measurements_notes),
                    value = form.notes,
                    onValueChange = { v -> vm.editForm { it.copy(notes = v) } },
                )
                if (error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.common_error),
                        style = MaterialTheme.typography.labelLarge,
                        color = Red700,
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.common_save),
                onClick = vm::save,
                loading = saving,
                enabled = form.label.isNotBlank(),
                modifier = Modifier.fillMaxWidth(0.5f),
            )
        },
        dismissButton = {
            SecondaryButton(
                text = stringResource(R.string.common_cancel),
                onClick = vm::closeForm,
                modifier = Modifier.fillMaxWidth(0.4f),
            )
        },
    )
}

@Composable
private fun NumberField(labelRes: Int, value: String, onChange: (String) -> Unit) {
    Spacer(Modifier.height(8.dp))
    LabeledField(
        label = "${stringResource(labelRes)} (${stringResource(R.string.measurements_cm)})",
        value = value,
        // Faqat raqam va bitta nuqta — o'lchov kasr bo'lishi mumkin
        onValueChange = { v -> onChange(v.filter { it.isDigit() || it == '.' }) },
        keyboardType = KeyboardType.Decimal,
    )
}
