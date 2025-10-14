package com.example.bloghub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.bloghub.ui.AppNavigation
import com.example.bloghub.ui.theme.BlogHubTheme // <-- THIS LINE IS NOW CORRECT
import com.example.bloghub.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
        
        setContent {
            BlogHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Call your AppNavigation here instead of a specific screen
                    AppNavigation()
                }
            }
        }
    }
}
