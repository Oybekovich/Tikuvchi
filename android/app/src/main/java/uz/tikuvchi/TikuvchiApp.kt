package uz.tikuvchi

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import coil3.util.DebugLogger
import uz.tikuvchi.data.Reconnect

/**
 * Coil'ni bir marta sozlaymiz: seed rasmlari SVG formatida, dekodersiz ular
 * umuman chizilmaydi.
 */
class TikuvchiApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        // Tarmoq qaytganda xato holatidagi ekranlar o'zi yangilanadi
        Reconnect.observe(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
                add(OkHttpNetworkFetcherFactory())
            }
            .apply { if (BuildConfig.DEBUG) logger(DebugLogger()) }
            .build()
}
