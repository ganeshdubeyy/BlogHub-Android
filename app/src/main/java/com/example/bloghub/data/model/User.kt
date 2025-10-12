package com.example.bloghub.data.model

data class User(
    val userId: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val bio: String? = null
)


