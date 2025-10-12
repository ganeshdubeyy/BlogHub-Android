package com.example.bloghub.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloghub.auth.GoogleSignInHelper
import com.example.bloghub.ui.components.RoundedButton
import com.example.bloghub.viewmodel.AuthViewModel

// 1. UPDATE THE FUNCTION SIGNATURE to accept the new navigation functions
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    // Your existing state variables are kept the same
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val emailError = remember { mutableStateOf("") }
    val passwordError = remember { mutableStateOf("") }
    val isLoading by authViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.errorMessage.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Your Google Sign-In launcher is kept exactly the same
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.result
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                return@rememberLauncherForActivityResult
            }
            // We just call the viewmodel function like before
            authViewModel.loginWithGoogle(idToken) { res ->
                res.onSuccess {
                    // 2. ON SUCCESS, call the new navigation function
                    onLoginSuccess()
                }
                res.onFailure { Toast.makeText(context, it.message ?: "Google sign-in failed", Toast.LENGTH_SHORT).show() }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Your entire UI layout is kept the same
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome to BlogHub", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        if (emailError.value.isNotEmpty()) {
            Text(emailError.value, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
        } else {
            Spacer(Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        if (passwordError.value.isNotEmpty()) {
            Text(passwordError.value, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
        } else {
            Spacer(Modifier.height(4.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        RoundedButton(
            text = if (isLoading) "Signing in..." else "Login",
            onClick = {
                // We keep your validation and viewmodel call exactly as it was
                authViewModel.loginWithEmail(email, password) { result ->
                    result.onSuccess {
                        // 3. ON SUCCESS, call the new navigation function
                        onLoginSuccess()
                    }
                }
            },
            enabled = !isLoading, // Button is disabled while loading
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        RoundedButton(
            text = "Create account",
            onClick = {
                // 4. The "Create account" button now calls the correct navigation function
                onNavigateToSignUp()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        RoundedButton(
            text = "Continue with Google",
            onClick = {
                // Your Google Sign-In logic remains unchanged
                val client = GoogleSignInHelper.getClient(context)
                googleLauncher.launch(client.signInIntent)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
