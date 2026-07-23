package uz.tikuvchi.ui.screens.order

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import uz.tikuvchi.R
import uz.tikuvchi.data.model.Measurement
import uz.tikuvchi.ui.components.AppHeader
import uz.tikuvchi.ui.components.LabeledField
import uz.tikuvchi.ui.components.PrimaryButton
import uz.tikuvchi.ui.components.SecondaryButton
import uz.tikuvchi.ui.theme.Cream200
import uz.tikuvchi.ui.theme.Cream300
import uz.tikuvchi.ui.theme.Cream50
import uz.tikuvchi.ui.theme.Ink500
import uz.tikuvchi.ui.theme.Ink700
import uz.tikuvchi.ui.theme.Ink900
import uz.tikuvchi.ui.theme.Red700
import uz.tikuvchi.ui.theme.Terra50
import uz.tikuvchi.ui.theme.Terra600
import uz.tikuvchi.ui.theme.Terra700

@Composable
fun OrderWizardScreen(
    ustaId: String,
    onClose: () -> Unit,
    onSent: (ustaId: String, ustaName: String) -> Unit,
) {
    val vm: OrderWizardViewModel = viewModel(
        key = ustaId,
        factory = viewModelFactory { initializer { OrderWizardViewModel(ustaId) } },
    )
    val s by vm.state.collectAsStateWithLifecycle()

    fun goBack() { if (vm.back()) onClose() }
    BackHandler { goBack() }

    Column(Modifier.fillMaxSize().background(Cream50).statusBarsPadding().imePadding()) {
        AppHeader(title = stringResource(R.string.order_flow_title), onBack = ::goBack)
        Stepper(s.step)

        if (s.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Terra600)
            }
            return@Column
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            when (s.step) {
                0 -> StepDetails(s, vm)
                1 -> StepSize(s, vm)
                else -> StepSummary(s, vm)
            }

            s.error?.let { err ->
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(
                        when (err) {
                            WizardError.SELECT_MEASUREMENT -> R.string.order_flow_select_measurement_first
                            WizardError.GENERIC -> R.string.common_error
                        },
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = Red700,
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SecondaryButton(
                text = stringResource(R.string.common_back),
                onClick = ::goBack,
                modifier = Modifier.weight(1f),
            )
            if (s.step < 2) {
                PrimaryButton(
                    text = stringResource(R.string.common_next),
                    onClick = vm::next,
                    modifier = Modifier.weight(1.4f),
                )
            } else {
                PrimaryButton(
                    text = stringResource(R.string.order_flow_confirm),
                    onClick = { vm.submit { id, name -> onSent(id, name) } },
                    loading = s.submitting,
                    modifier = Modifier.weight(1.4f),
                )
            }
        }
    }
}

@Composable
private fun Stepper(step: Int) {
    val labels = listOf(
        R.string.order_flow_step_type,
        R.string.order_flow_step_size,
        R.string.order_flow_step_finish,
    )
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { i, res ->
            Column(Modifier.weight(1f)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (i <= step) Terra600 else Cream300),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(res),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i <= step) Terra700 else Ink500,
                )
            }
        }
    }
}

@Composable
private fun StepDetails(s: OrderWizardUiState, vm: OrderWizardViewModel) {
    LabeledField(
        label = stringResource(R.string.order_flow_material),
        value = s.material,
        onValueChange = vm::setMaterial,
        placeholder = stringResource(R.string.order_flow_material_placeholder),
    )
    Spacer(Modifier.height(12.dp))
    LabeledField(
        label = stringResource(R.string.order_flow_model_note),
        value = s.modelNote,
        onValueChange = vm::setModelNote,
        placeholder = stringResource(R.string.order_flow_model_note_placeholder),
    )
    Spacer(Modifier.height(12.dp))
    LabeledField(
        label = stringResource(R.string.order_flow_price_suggested),
        value = s.suggestedPrice,
        onValueChange = vm::setSuggestedPrice,
        placeholder = stringResource(R.string.order_flow_price_placeholder),
        keyboardType = KeyboardType.Number,
    )
}

@Composable
private fun StepSize(s: OrderWizardUiState, vm: OrderWizardViewModel) {
    SectionTitle(stringResource(R.string.order_flow_choose_measurement))
    if (s.measurements.isEmpty()) {
        Text(
            stringResource(R.string.order_flow_no_measurements),
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500,
        )
        Spacer(Modifier.height(8.dp))
    }
    s.measurements.forEach { m ->
        MeasurementRow(m, selected = s.measurementId == m.id) { m.id?.let(vm::setMeasurement) }
        Spacer(Modifier.height(8.dp))
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (s.measurementId == NEW_MEASUREMENT) Terra50 else Color.White)
            .clickable { vm.setMeasurement(NEW_MEASUREMENT) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.order_flow_new_measurement),
            style = MaterialTheme.typography.titleSmall,
            color = Ink900,
            modifier = Modifier.weight(1f),
        )
        if (s.measurementId == NEW_MEASUREMENT) {
            Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = Terra600)
        }
    }

    if (s.measurementId == NEW_MEASUREMENT) {
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            LabeledField(
                label = stringResource(R.string.measurements_label),
                value = s.newLabel,
                onValueChange = vm::setNewLabel,
                placeholder = stringResource(R.string.measurements_label_placeholder),
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField(
                    label = stringResource(R.string.measurements_chest),
                    value = s.newChest,
                    onValueChange = vm::setNewChest,
                    modifier = Modifier.weight(1f),
                )
                LabeledField(
                    label = stringResource(R.string.measurements_waist),
                    value = s.newWaist,
                    onValueChange = vm::setNewWaist,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField(
                    label = stringResource(R.string.measurements_hips),
                    value = s.newHips,
                    onValueChange = vm::setNewHips,
                    modifier = Modifier.weight(1f),
                )
                LabeledField(
                    label = stringResource(R.string.measurements_height),
                    value = s.newHeight,
                    onValueChange = vm::setNewHeight,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabeledField(
                    label = stringResource(R.string.measurements_shoulder),
                    value = s.newShoulder,
                    onValueChange = vm::setNewShoulder,
                    modifier = Modifier.weight(1f),
                )
                LabeledField(
                    label = stringResource(R.string.measurements_sleeve_length),
                    value = s.newSleeveLength,
                    onValueChange = vm::setNewSleeveLength,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    Spacer(Modifier.height(12.dp))
    LabeledField(
        label = stringResource(R.string.order_flow_size_note),
        value = s.sizeNote,
        onValueChange = vm::setSizeNote,
        placeholder = stringResource(R.string.order_flow_size_note_placeholder),
    )
}

@Composable
private fun MeasurementRow(m: Measurement, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Terra50 else Color.White)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(m.label, style = MaterialTheme.typography.titleSmall, color = Ink900)
            val parts = listOfNotNull(
                m.chest?.let { "${it.toInt()}" },
                m.waist?.let { "${it.toInt()}" },
                m.hips?.let { "${it.toInt()}" },
            )
            if (parts.isNotEmpty()) {
                Text(
                    parts.joinToString(" / ") + " " + stringResource(R.string.measurements_cm),
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500,
                )
            }
        }
        if (selected) Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = Terra600)
    }
}

@Composable
private fun StepSummary(s: OrderWizardUiState, vm: OrderWizardViewModel) {
    SectionTitle(stringResource(R.string.order_flow_summary))
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        SummaryRow(stringResource(R.string.order_flow_usta), s.ustaName)
        if (s.material.isNotBlank()) {
            SummaryRow(stringResource(R.string.order_flow_material), s.material)
        }
        if (s.modelNote.isNotBlank()) {
            SummaryRow(stringResource(R.string.order_flow_model_note), s.modelNote)
        }
        SummaryRow(
            stringResource(R.string.order_flow_measurement),
            if (s.measurementId == NEW_MEASUREMENT) s.newLabel
            else s.measurements.firstOrNull { it.id == s.measurementId }?.label.orEmpty(),
        )
        if (s.suggestedPrice.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Cream200))
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.order_flow_price),
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink700,
                )
                Text(
                    "${s.suggestedPrice} so'm",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink900,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Cream200)
                .padding(12.dp),
        ) {
            Text(
                stringResource(R.string.order_flow_chat_hint),
                style = MaterialTheme.typography.labelSmall,
                color = Ink700,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink500,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = Ink900,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.2f),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Ink900,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    )
}
