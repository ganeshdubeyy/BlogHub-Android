package com.example.bloghub

import android.app.Application
import com.example.bloghub.util.CloudinaryManager

class BlogHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Cloudinary when the application starts
        CloudinaryManager.init(this)
    }
}
