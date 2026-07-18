package uz.tikuvchi.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import uz.tikuvchi.data.model.Measurement

object MeasurementsRepository {

    suspend fun list(): List<Measurement> = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: return@withContext emptyList()
        supabase.from("measurements")
            .select {
                filter { eq("client_id", clientId) }
                order("updated_at", Order.DESCENDING)
            }
            .decodeList()
    }

    suspend fun insert(m: Measurement): Measurement = withContext(Dispatchers.IO) {
        val clientId = ProfileRepository.currentUserId() ?: error("Sessiya yo'q")
        supabase.from("measurements")
            .insert(InsertPayload(clientId, m.fields())) { select() }
            .decodeSingle()
    }

    suspend fun update(m: Measurement): Measurement = withContext(Dispatchers.IO) {
        val id = m.id ?: error("id yo'q")
        supabase.from("measurements")
            .update(m.fields()) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle()
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        supabase.from("measurements").delete { filter { eq("id", id) } }
    }

    /**
     * Yoziladigan maydonlar. Modelning o'zini yuborib bo'lmaydi: id ham null
     * qiymat bilan ketadi. Bo'sh qoldirilgan o'lchov null bo'lib yozilishi
     * kerak — bu ataylab.
     *
     * updated_at ni DB o'zi yangilamaydi (trigger yo'q), shuning uchun web
     * ilovadagi kabi shu yerda qo'lda qo'yiladi — aks holda "Yangilangan"
     * sanasi eski qiymatda qotib qoladi.
     */
    private fun Measurement.fields() = Fields(
        label = label,
        chest = chest,
        waist = waist,
        hips = hips,
        height = height,
        shoulder = shoulder,
        sleeveLength = sleeveLength,
        notes = notes,
        updatedAt = Instant.now().toString(),
    )

    @Serializable
    private data class Fields(
        val label: String,
        val chest: Double?,
        val waist: Double?,
        val hips: Double?,
        val height: Double?,
        val shoulder: Double?,
        @SerialName("sleeve_length") val sleeveLength: Double?,
        val notes: String?,
        @SerialName("updated_at") val updatedAt: String,
    )

    /** Insert'da client_id ham kerak — RLS shuni talab qiladi. */
    @Serializable
    private data class InsertPayload(
        @SerialName("client_id") val clientId: String,
        val label: String,
        val chest: Double?,
        val waist: Double?,
        val hips: Double?,
        val height: Double?,
        val shoulder: Double?,
        @SerialName("sleeve_length") val sleeveLength: Double?,
        val notes: String?,
        @SerialName("updated_at") val updatedAt: String,
    ) {
        constructor(clientId: String, f: Fields) : this(
            clientId, f.label, f.chest, f.waist, f.hips, f.height, f.shoulder,
            f.sleeveLength, f.notes, f.updatedAt,
        )
    }
}
