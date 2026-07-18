package uz.tikuvchi.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Nomlar DB'dagi enum qiymatlari bilan aynan bir xil bo'lishi shart —
// @SerialName shuning uchun har birida ochiq yozilgan.

@Serializable
enum class UserRole {
    @SerialName("client") CLIENT,
    @SerialName("usta") USTA,
}

@Serializable
enum class GenderSegment {
    @SerialName("women") WOMEN,
    @SerialName("men") MEN,
    @SerialName("unisex") UNISEX,
}

@Serializable
enum class OrderStatus {
    @SerialName("pending") PENDING,
    @SerialName("accepted") ACCEPTED,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("ready") READY,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED,
}

@Serializable
enum class PaymentStatus {
    @SerialName("pending") PENDING,
    @SerialName("partial") PARTIAL,
    @SerialName("paid") PAID,
}

@Serializable
enum class OrderSource {
    @SerialName("catalog") CATALOG,
    @SerialName("chat_negotiation") CHAT_NEGOTIATION,
}

@Serializable
enum class MessageType {
    @SerialName("text") TEXT,
    @SerialName("price_offer") PRICE_OFFER,
    @SerialName("image") IMAGE,
}

@Serializable
enum class PriceOfferStatus {
    @SerialName("pending") PENDING,
    @SerialName("accepted") ACCEPTED,
    @SerialName("declined") DECLINED,
}

@Serializable
data class Profile(
    val id: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val role: UserRole = UserRole.CLIENT,
    val phone: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class UstaProfile(
    @SerialName("user_id") val userId: String,
    val bio: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double = 0.0,
    @SerialName("rating_count") val ratingCount: Int = 0,
    @SerialName("location_text") val locationText: String? = null,
    val district: String? = null,
    @SerialName("work_hours_start") val workHoursStart: String? = null,
    @SerialName("work_hours_end") val workHoursEnd: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("gender_segment") val genderSegment: GenderSegment = GenderSegment.WOMEN,
    // Postgrest'da profiles!inner(...) bilan birga tortiladi
    val profiles: Profile? = null,
)

// So'rovda faqat kerakli ustunlar tanlanadi, shuning uchun kartochka uchun alohida
// modellar — web'dagi UstaCardData bilan bir xil. To'liq Profile/UstaService bu
// yerda ishlamaydi: tanlanmagan majburiy maydonlar deserializatsiyani buzadi.

@Serializable
data class ProfileBrief(
    @SerialName("full_name") val fullName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)

@Serializable
data class ServicePrice(
    @SerialName("base_price") val basePrice: Long,
)

/** Bosh sahifa va qidiruvdagi usta kartochkasi. */
@Serializable
data class UstaCard(
    @SerialName("user_id") val userId: String,
    val district: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double = 0.0,
    @SerialName("rating_count") val ratingCount: Int = 0,
    val tags: List<String> = emptyList(),
    val profiles: ProfileBrief,
    @SerialName("usta_services") val services: List<ServicePrice> = emptyList(),
) {
    /** Kartochkada "dan boshlab" narxi; xizmat qo'shilmagan bo'lsa — null */
    val minPrice: Long? get() = services.minOfOrNull { it.basePrice }
}

/** Suhbatlar ro'yxatidagi qator — oxirgi xabar bilan. */
@Serializable
data class ConversationRow(
    val id: String,
    @SerialName("usta_id") val ustaId: String,
    @SerialName("last_message_at") val lastMessageAt: String,
    @SerialName("usta_profiles") val usta: OrderUsta,
    // So'rovda limit(1) bilan faqat oxirgi xabar olinadi
    val messages: List<MessagePreview> = emptyList(),
) {
    val last: MessagePreview? get() = messages.firstOrNull()
}

@Serializable
data class MessagePreview(
    val content: String? = null,
    @SerialName("message_type") val messageType: MessageType = MessageType.TEXT,
    @SerialName("created_at") val createdAt: String,
)

/** Buyurtmalar ro'yxatidagi qator — web'dagi orders/page.tsx so'rovi bilan bir xil. */
@Serializable
data class OrderRow(
    val id: String,
    val status: OrderStatus = OrderStatus.PENDING,
    @SerialName("payment_status") val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    @SerialName("total_price") val totalPrice: Long = 0,
    @SerialName("estimated_ready_at") val estimatedReadyAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    val source: OrderSource = OrderSource.CATALOG,
    @SerialName("usta_profiles") val usta: OrderUsta,
    @SerialName("order_items") val items: List<OrderItemTitle> = emptyList(),
) {
    /** Kartochkada birinchi mahsulot nomi ko'rsatiladi. */
    val title: String get() = items.firstOrNull()?.title ?: "—"
}

@Serializable
data class OrderUsta(
    val district: String? = null,
    val profiles: ProfileBrief,
)

/** Buyurtma tafsiloti — web'dagi orders/[id] so'rovi bilan bir xil. */
@Serializable
data class OrderDetail(
    val id: String,
    val status: OrderStatus = OrderStatus.PENDING,
    @SerialName("payment_status") val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val source: OrderSource = OrderSource.CATALOG,
    @SerialName("total_price") val totalPrice: Long = 0,
    @SerialName("estimated_ready_at") val estimatedReadyAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("usta_id") val ustaId: String,
    @SerialName("usta_profiles") val usta: OrderUsta,
    @SerialName("order_items") val items: List<OrderItemDetail> = emptyList(),
)

@Serializable
data class OrderItemDetail(
    val id: Long,
    val title: String,
    val material: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("size_note") val sizeNote: String? = null,
    @SerialName("model_note") val modelNote: String? = null,
    val price: Long = 0,
)

@Serializable
data class OrderItemTitle(val title: String)

@Serializable
data class ServicePriceCategory(
    @SerialName("base_price") val basePrice: Long,
    @SerialName("category_id") val categoryId: Long? = null,
)

/**
 * Qidiruv natijasi. Web'dagi kabi: tuman va reyting server tomonda filtrlanadi,
 * matn/kategoriya/narx esa shu qatorlar ustida — shuning uchun bio va xizmat
 * kategoriyalari ham tortiladi.
 */
@Serializable
data class UstaSearchRow(
    @SerialName("user_id") val userId: String,
    val district: String? = null,
    val bio: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double = 0.0,
    @SerialName("rating_count") val ratingCount: Int = 0,
    val tags: List<String> = emptyList(),
    val profiles: ProfileBrief,
    @SerialName("usta_services") val services: List<ServicePriceCategory> = emptyList(),
) {
    fun toCard() = UstaCard(
        userId = userId,
        district = district,
        ratingAvg = ratingAvg,
        ratingCount = ratingCount,
        tags = tags,
        profiles = profiles,
        services = services.map { ServicePrice(it.basePrice) },
    )
}

/** Usta sahifasidagi xizmat. */
@Serializable
data class ServiceItem(
    val id: Long,
    val title: String,
    val description: String? = null,
    @SerialName("base_price") val basePrice: Long,
)

/** Usta sahifasidagi portfolio rasmi. */
@Serializable
data class PortfolioImage(
    val id: Long,
    @SerialName("image_url") val imageUrl: String,
    val caption: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

/** Usta sahifasidagi sharh. */
@Serializable
data class ReviewItem(
    val id: Long,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String,
    val profiles: ProfileBrief,
)

/** Usta sahifasi — web'dagi usta/[id]/page.tsx so'rovi bilan bir xil. */
@Serializable
data class UstaDetail(
    @SerialName("user_id") val userId: String,
    val bio: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("rating_avg") val ratingAvg: Double = 0.0,
    @SerialName("rating_count") val ratingCount: Int = 0,
    @SerialName("location_text") val locationText: String? = null,
    val district: String? = null,
    @SerialName("work_hours_start") val workHoursStart: String? = null,
    @SerialName("work_hours_end") val workHoursEnd: String? = null,
    val tags: List<String> = emptyList(),
    val profiles: ProfileBrief,
    @SerialName("usta_services") val services: List<ServiceItem> = emptyList(),
    @SerialName("portfolio_items") val portfolio: List<PortfolioImage> = emptyList(),
)

@Serializable
data class ServiceCategory(
    val id: Long,
    val name: String,
    val icon: String? = null,
    @SerialName("gender_segment") val genderSegment: GenderSegment = GenderSegment.WOMEN,
)

@Serializable
data class UstaService(
    val id: Long,
    @SerialName("usta_id") val ustaId: String,
    @SerialName("category_id") val categoryId: Long? = null,
    val title: String,
    val description: String? = null,
    @SerialName("base_price") val basePrice: Long,
)

@Serializable
data class PortfolioItem(
    val id: Long,
    @SerialName("usta_id") val ustaId: String,
    @SerialName("image_url") val imageUrl: String,
    val caption: String? = null,
    @SerialName("sort_order") val sortOrder: Int = 0,
)

@Serializable
data class Review(
    val id: Long,
    @SerialName("usta_id") val ustaId: String,
    @SerialName("client_id") val clientId: String,
    val rating: Int,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String,
    val profiles: Profile? = null,
)

@Serializable
data class Order(
    val id: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("usta_id") val ustaId: String,
    val source: OrderSource = OrderSource.CATALOG,
    val status: OrderStatus = OrderStatus.PENDING,
    @SerialName("total_price") val totalPrice: Long = 0,
    @SerialName("payment_status") val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    @SerialName("estimated_ready_at") val estimatedReadyAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("usta_profiles") val ustaProfile: UstaProfile? = null,
    @SerialName("order_items") val items: List<OrderItem> = emptyList(),
)

@Serializable
data class OrderItem(
    val id: Long? = null,
    @SerialName("order_id") val orderId: String? = null,
    val title: String,
    val material: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("size_note") val sizeNote: String? = null,
    @SerialName("model_note") val modelNote: String? = null,
    val price: Long = 0,
)

@Serializable
data class Measurement(
    val id: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    val label: String,
    val chest: Double? = null,
    val waist: Double? = null,
    val hips: Double? = null,
    val height: Double? = null,
    val shoulder: Double? = null,
    @SerialName("sleeve_length") val sleeveLength: Double? = null,
    val notes: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class Conversation(
    val id: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("usta_id") val ustaId: String,
    @SerialName("last_message_at") val lastMessageAt: String,
    @SerialName("usta_profiles") val ustaProfile: UstaProfile? = null,
)

@Serializable
data class Message(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val content: String? = null,
    @SerialName("message_type") val messageType: MessageType = MessageType.TEXT,
    @SerialName("price_offer_amount") val priceOfferAmount: Long? = null,
    @SerialName("price_offer_duration_days") val priceOfferDurationDays: Int? = null,
    @SerialName("price_offer_note") val priceOfferNote: String? = null,
    @SerialName("price_offer_status") val priceOfferStatus: PriceOfferStatus? = null,
    @SerialName("created_at") val createdAt: String,
)
