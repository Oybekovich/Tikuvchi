package uz.tikuvchi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import uz.tikuvchi.ui.TikuvchiRoot
import uz.tikuvchi.ui.theme.TikuvchiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TikuvchiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TikuvchiRoot()
                }
            }
        }
    }
}
