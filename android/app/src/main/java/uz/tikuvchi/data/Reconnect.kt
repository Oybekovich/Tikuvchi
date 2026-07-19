package uz.tikuvchi.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Butun ilova uchun yagona "qaytadan yuklash" signali.
 *
 * Internet uzilganda har bir ekran o'z xato holatiga tushadi. Ilgari ularning
 * har birida "Qayta urinish" tugmasini alohida bosish kerak edi — bosh sahifada
 * bosgan bilan buyurtmalar tabi xato holatida qolaverardi.
 *
 * Endi signal bitta va u ikki manbadan keladi:
 *  - tizim tarmoq qaytganini aytadi (tugmani bosish umuman shart emas);
 *  - foydalanuvchi istalgan ekranda "Qayta urinish" bosadi.
 *
 * Signalni faqat xato holatidagi ekranlar oladi, shuning uchun ishlab turgan
 * ekranlar behuda so'rov yubormaydi.
 */
object Reconnect {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    /** "Qayta urinish" bosilganda yoki tarmoq qaytganda. */
    fun request() {
        _events.tryEmit(Unit)
    }

    /**
     * Application'da bir marta chaqiriladi. NET_CAPABILITY_VALIDATED shart:
     * usiz onAvailable Wi-Fi'ga ulangan zahoti chaqiriladi, internet hali
     * o'tmagan bo'lsa ham — natijada yangilash yana xato bilan tugaydi.
     */
    fun observe(context: Context) {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        manager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = request()
            },
        )
    }
}

/**
 * Xato holatidagi ekran tarmoq qaytganda o'zini yangilaydi.
 *
 * Faqat o'qish ekranlarida ishlatiladi. Ochiq forma bo'lgan ekranlarga
 * (profil, o'lchamlar, buyurtma sehrgari) qo'llanmaydi: u yerda avtomatik
 * qayta yuklash foydalanuvchi yozgan, hali saqlanmagan matnni o'chirib yuboradi.
 */
fun ViewModel.reloadOnReconnect(isError: () -> Boolean, load: () -> Unit) {
    viewModelScope.launch {
        Reconnect.events.collect { if (isError()) load() }
    }
}
