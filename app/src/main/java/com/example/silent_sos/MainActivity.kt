package com.example.silent_sos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.messaging.FirebaseMessaging
import android.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker


class SilentSOSApplication : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        Configuration.getInstance().userAgentValue =
            "SilentSOS/1.0 (Android; com.example.silent_sos)"
    }
}
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                SilentSOSApp()
            }
        }
    }
}


// =====================================================
// SCREEN LIST
// =====================================================

enum class Screen {
    HOME,
    SOS,
    SAFE_WALK,
    MAP,
    AI,
    EVIDENCE,
    ALERTS,
    PROFILE,
    EMERGENCY_CONTACTS
}


// =====================================================
// MAIN APPLICATION
// =====================================================

@Composable
fun SilentSOSApp() {

    val auth = remember {
        FirebaseAuth.getInstance()
    }
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->

            if (task.isSuccessful) {

                val token = task.result

                val user = auth.currentUser

                if (user != null) {

                    val db = FirebaseFirestore.getInstance()

                    db.collection("users")
                        .document(user.uid)
                        .set(
                            mapOf(
                                "fcmToken" to token
                            ),
                            SetOptions.merge()
                        )
                        .addOnSuccessListener {
                            println("FCM token saved to Firestore")
                        }
                        .addOnFailureListener { e ->
                            println(
                                "FCM token save error: ${e.message}"
                            )
                        }
                }

            } else {

                println(
                    "FCM TOKEN ERROR: ${task.exception?.message}"
                )
            }
        }

    // Check if the user is already logged in
    var isLoggedIn by remember {
        mutableStateOf(auth.currentUser != null)
    }

    var currentScreen by remember {
        mutableStateOf(Screen.HOME)
    }

    // Show login screen if user is not logged in
    if (!isLoggedIn) {

        AuthScreen(
            onAuthSuccess = {
                isLoggedIn = true
                currentScreen = Screen.HOME
            }
        )

        return
    }

    // Android physical back button
    BackHandler(
        enabled = currentScreen != Screen.HOME
    ) {
        currentScreen = Screen.HOME
    }

    when (currentScreen) {

        Screen.HOME -> {
            HomeScreen(
                onNavigate = { screen ->
                    currentScreen = screen
                }
            )
        }

        Screen.SOS -> {
            SOSScreen(
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.SAFE_WALK -> {
            SafeWalkScreen(
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.MAP -> {
            MapScreen(
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.AI -> {
            AIScreen(
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.EVIDENCE -> {
            EvidenceScreen(
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.ALERTS -> {
            AlertsScreen(
                onBack = {
                    currentScreen = Screen.HOME
                }
            )
        }

        Screen.PROFILE -> {
            ProfileScreen(
                onBack = {
                    currentScreen = Screen.HOME
                },
                userEmail = auth.currentUser?.email ?: "No email",
                onLogout = {
                    auth.signOut()
                    isLoggedIn = false
                    currentScreen = Screen.HOME
                }
            )
        }
        Screen.EMERGENCY_CONTACTS -> EmergencyContactsScreen(
            onBack = {
                currentScreen = Screen.HOME
            }
        )
    }
}



// =====================================================
// FIREBASE AUTHENTICATION SCREEN
// =====================================================

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {

    val auth = remember {
        FirebaseAuth.getInstance()
    }
    val db = remember {
        FirebaseFirestore.getInstance()
    }

    var isLoginMode by remember {
        mutableStateOf(true)
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(55.dp)
        )

        Text(
            text = "🛡️",
            fontSize = 65.sp
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(
            text = "SilentSOS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Text(
            text = "Your personal safety companion",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(35.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = if (isLoginMode) {
                        "Welcome Back"
                    } else {
                        "Create Account"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Email")
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Password")
                    },
                    singleLine = true
                )

                if (!isLoginMode) {

                    Spacer(
                        modifier = Modifier.height(15.dp)
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Confirm Password")
                        },
                        singleLine = true
                    )
                }

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                if (errorMessage.isNotEmpty()) {

                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }

                Button(
                    onClick = {

                        if (email.isBlank()) {

                            errorMessage = "Please enter your email."

                        } else if (password.isBlank()) {

                            errorMessage = "Please enter your password."

                        } else if (!isLoginMode && password != confirmPassword) {

                            errorMessage = "Passwords do not match."

                        } else {

                            isLoading = true
                            errorMessage = ""

                            if (isLoginMode) {

                                // Firebase LOGIN
                                auth.signInWithEmailAndPassword(
                                    email.trim(),
                                    password
                                ).addOnCompleteListener { task ->

                                    isLoading = false

                                    if (task.isSuccessful) {

                                        val user = auth.currentUser

                                        if (user != null) {

                                            // Open Home immediately after successful Firebase login.
                                            onAuthSuccess()

                                            // Save/update the user's profile in Firestore.
                                            val userData = hashMapOf(
                                                "email" to (user.email ?: "")
                                            )

                                            db.collection("users")
                                                .document(user.uid)
                                                .set(userData, SetOptions.merge())
                                                .addOnFailureListener { e ->
                                                    println("Firestore user save error: ${e.message}")
                                                }

                                        }

                                    } else {

                                        errorMessage =
                                            task.exception?.message
                                                ?: "Login failed. Please try again."
                                    }
                                }

                            } else {

                                // Firebase CREATE ACCOUNT
                                auth.createUserWithEmailAndPassword(
                                    email.trim(),
                                    password
                                ).addOnCompleteListener { task ->

                                    isLoading = false

                                    if (task.isSuccessful) {

                                        val user = auth.currentUser

                                        if (user != null) {

                                            // Open Home immediately after successful account creation.
                                            onAuthSuccess()

                                            // Save the user's profile in Firestore.
                                            val userData = hashMapOf(
                                                "email" to (user.email ?: "")
                                            )

                                            db.collection("users")
                                                .document(user.uid)
                                                .set(userData, SetOptions.merge())
                                                .addOnFailureListener { e ->
                                                    println("Firestore user save error: ${e.message}")
                                                }

                                        }

                                    } else {

                                        errorMessage =
                                            task.exception?.message
                                                ?: "Account creation failed."
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {

                    Text(
                        text = when {
                            isLoading -> "Please wait..."
                            isLoginMode -> "LOGIN"
                            else -> "CREATE ACCOUNT"
                        }
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                OutlinedButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                        errorMessage = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = if (isLoginMode) {
                            "Create a new account"
                        } else {
                            "Already have an account? Login"
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Your safety. Your control.",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}


// =====================================================
// HOME SCREEN
// =====================================================

@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // TOP BAR

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "☰",
                fontSize = 28.sp
            )

            Text(
                text = "SilentSOS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "●",
                fontSize = 24.sp,
                modifier = Modifier.clickable {
                    onNavigate(Screen.PROFILE)
                }
            )
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )


        // GREETING

        Text(
            text = "Hello 👋",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your personal safety companion",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // SAFETY STATUS CARD

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "YOU ARE SAFE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(
                    modifier = Modifier.height(15.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(
                            text = "Current Area",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "Low Risk",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {

                        Text(
                            text = "Battery",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "84%",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )


        // SOS BUTTON

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Button(
                onClick = {
                    onNavigate(Screen.SOS)
                },
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "SOS",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Emergency",
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )


        // QUICK ACTIONS

        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            FeatureCard(
                emoji = "🚶",
                title = "Safe Walk",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate(Screen.SAFE_WALK)
                }
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            FeatureCard(
                emoji = "🗺️",
                title = "Safety Map",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate(Screen.MAP)
                }
            )
        }
        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            FeatureCard(
                emoji = "📞",
                title = "Emergency Contacts",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate(Screen.EMERGENCY_CONTACTS)
                }
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            FeatureCard(
                emoji = "🤖",
                title = "AI Assistant",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate(Screen.AI)
                }
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            FeatureCard(
                emoji = "🔐",
                title = "Evidence",
                modifier = Modifier.weight(1f),
                onClick = {
                    onNavigate(Screen.EVIDENCE)
                }
            )
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )


        // SAFETY SCORE

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    text = "Today's Safety Score",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "85%",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Low Risk - You're doing great!",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(
            modifier = Modifier.height(25.dp)
        )


        // BOTTOM NAVIGATION

        BottomNavigationBar(
            onNavigate = onNavigate
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )
    }
}


// =====================================================
// FEATURE CARD
// =====================================================

@Composable
fun FeatureCard(
    emoji: String,
    title: String,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Card(
        modifier = modifier
            .height(115.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = emoji,
                fontSize = 30.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// =====================================================
// BOTTOM NAVIGATION
// =====================================================

@Composable
fun BottomNavigationBar(
    onNavigate: (Screen) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            NavigationItem(
                emoji = "🏠",
                title = "Home",
                onClick = {
                    onNavigate(Screen.HOME)
                }
            )

            NavigationItem(
                emoji = "🗺️",
                title = "Map",
                onClick = {
                    onNavigate(Screen.MAP)
                }
            )

            NavigationItem(
                emoji = "🔔",
                title = "Alerts",
                onClick = {
                    onNavigate(Screen.ALERTS)
                }
            )

            NavigationItem(
                emoji = "👤",
                title = "Profile",
                onClick = {
                    onNavigate(Screen.PROFILE)
                }
            )
        }
    }
}


@Composable
fun NavigationItem(
    emoji: String,
    title: String,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = emoji,
            fontSize = 22.sp
        )

        Text(
            text = title,
            fontSize = 11.sp
        )
    }
}


// =====================================================
// BACK BUTTON
// =====================================================

@Composable
fun BackButton(
    title: String,
    onBack: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(52.dp)
                .clickable {
                    onBack()
                },
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "←",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// =====================================================
// SOS SCREEN
// =====================================================

@Composable
fun SOSScreen(
    onBack: () -> Unit
) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    val context = LocalContext.current

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val localDb = remember {
        DataBaseHelper(context)
    }

    var sosStatus by remember {
        mutableStateOf("Ready to send SOS")
    }

    fun getLocationAndSaveSOS() {

        sosStatus = "Getting your location..."

        try {

            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            fusedLocationClient.getCurrentLocation(
                request,
                null
            ).addOnSuccessListener { location ->

                if (location != null) {

                    val latitude = location.latitude
                    val longitude = location.longitude

                    println("Latitude: $latitude")
                    println("Longitude: $longitude")

                    sosStatus = "Location found. Saving SOS..."

                    // ============================
                    // SAVE TO SQLITE
                    // ============================

                    val timestamp =
                        System.currentTimeMillis().toString()

                    val localSaved = localDb.addSosEvent(
                        timestamp = timestamp,
                        status = "SOS_TRIGGERED",
                        latitude = latitude,
                        longitude = longitude
                    )

                    println("SQLite SOS saved: $localSaved")

                    // ============================
                    // SAVE TO FIRESTORE
                    // ============================

                    val user = auth.currentUser

                    if (user != null) {

                        val sosData = hashMapOf(
                            "timestamp" to com.google.firebase.Timestamp.now(),
                            "status" to "SOS_TRIGGERED",
                            "latitude" to latitude,
                            "longitude" to longitude
                        )

                        db.collection("users")
                            .document(user.uid)
                            .collection("sos_events")
                            .add(sosData)
                            .addOnSuccessListener {

                                println("SOS event saved successfully")

                                sosStatus =
                                    "🚨 SOS SENT SUCCESSFULLY\nLocation saved"
                            }
                            .addOnFailureListener { e ->

                                println("SOS ERROR: ${e.message}")

                                sosStatus =
                                    "Location saved, but Firebase failed"
                            }

                    } else {

                        sosStatus =
                            "SOS saved locally, but user is not logged in"
                    }

                } else {

                    println("Could not get current location")

                    sosStatus =
                        "Could not get location.\nPlease enable emulator location."
                }

            }.addOnFailureListener { e ->

                println("Location error: ${e.message}")

                sosStatus =
                    "Location error: ${e.message}"
            }

        } catch (e: SecurityException) {

            println("Location permission error: ${e.message}")

            sosStatus =
                "Location permission is required."
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineLocationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineLocationGranted || coarseLocationGranted) {

                sosStatus = "Location permission granted..."

                getLocationAndSaveSOS()

            } else {

                sosStatus =
                    "Location permission denied."
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF7F7))
    ) {

        BackButton(
            title = "Emergency SOS",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier = Modifier.height(50.dp)
            )

            Text(
                text = "Emergency Assistance",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Button(
                onClick = {

                    val fineGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    val coarseGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (fineGranted || coarseGranted) {

                        // Permission already granted
                        getLocationAndSaveSOS()

                    } else {

                        // Ask for permission
                        sosStatus = "Requesting location permission..."

                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },

                modifier = Modifier.size(180.dp),

                shape = CircleShape,

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {

                Text(
                    text = "SEND SOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Text(
                text = sosStatus,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Text(
                text = "Your emergency contacts will be notified.",
                color = Color.Gray
            )
        }
    }
}
// =====================================================
// SAFE WALK SCREEN
// =====================================================

@Composable
fun SafeWalkScreen(
    onBack: () -> Unit
) {

    var destination by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "Safe Walk",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "🚶",
                fontSize = 50.sp
            )

            Text(
                text = "Plan Your Safe Walk",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedTextField(
                value = destination,
                onValueChange = {
                    destination = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Enter destination")
                }
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = {
                    // Safe walk functionality later
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "START SAFE WALK"
                )
            }
        }
    }
}


// =====================================================
// MAP SCREEN
// =====================================================

@Composable
fun MapScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var currentLocation by remember {
        mutableStateOf<GeoPoint?>(null)
    }
    val hasLocationPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    Configuration.getInstance().load(
        context,
        PreferenceManager.getDefaultSharedPreferences(context)

    )

    Configuration.getInstance().userAgentValue =
        "SilentSOS/1.0 (Android; com.example.silent_sos)"
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "Safety Map",
            onBack = onBack
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),

            factory = { ctx ->

                MapView(ctx).apply {

                    // OpenStreetMap tiles
                    setTileSource(TileSourceFactory.OpenTopo)

                    setMultiTouchControls(true)

// OpenStreetMap attribution
                    val copyrightOverlay = CopyrightOverlay(ctx)
                    copyrightOverlay.setCopyrightNotice(
                        "© OpenStreetMap contributors"
                    )
                    overlays.add(copyrightOverlay)
                    val mumbai = GeoPoint(
                        19.0760,
                        72.8777
                    )

                    controller.setZoom(12.0)
                    controller.setCenter(mumbai)
                    val marker = Marker(this)
                    marker.position = mumbai
                    marker.title = "Your Location"
                    overlays.add(marker)
                }
            }
        )
    }
}

// =====================================================
// AI ASSISTANT SCREEN
// =====================================================

@Composable
fun AIScreen(
    onBack: () -> Unit
) {

    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "AI Assistant",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "🤖",
                fontSize = 55.sp
            )

            Text(
                text = "How can I help you?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Describe your situation")
                }
            )

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Button(
                onClick = {
                    // AI functionality later
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "GET SAFETY ADVICE"
                )
            }
        }
    }
}


// =====================================================
// EVIDENCE SCREEN
// =====================================================

@Composable
fun EvidenceScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "Evidence Locker",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "🔐",
                fontSize = 55.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Evidence Locker",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Text(
                text = "Your recordings and photos will appear here."
            )
        }
    }
}
@Composable
fun EmergencyContactsScreen(
    onBack: () -> Unit
) {
    val db = remember {
        FirebaseFirestore.getInstance()
    }
    val context = LocalContext.current

    val localDb = remember {
        DataBaseHelper(context)
    }

    val auth = remember {
        FirebaseAuth.getInstance()
    }

    var showAddContact by remember {
        mutableStateOf(false)
    }

    var contactName by remember {
        mutableStateOf("")
    }

    var contactPhone by remember {
        mutableStateOf("")
    }

    var contactRelationship by remember {
        mutableStateOf("")
    }
    var contacts by remember {
        mutableStateOf<List<Map<String, String>>>(emptyList())
    }

    val userId = auth.currentUser?.uid

    DisposableEffect(userId) {

        var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

        if (userId != null) {

            listenerRegistration = db.collection("users")
                .document(userId)
                .collection("emergency_contacts")
                .addSnapshotListener { snapshot, error ->

                    if (error == null && snapshot != null) {

                        contacts = snapshot.documents.map { document ->

                            mapOf(
                                "name" to (document.getString("name") ?: ""),
                                "phone" to (document.getString("phone") ?: ""),
                                "relationship" to (document.getString("relationship") ?: "")
                            )
                        }
                    }
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "Emergency Contacts",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "📞",
                fontSize = 55.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Emergency Contacts",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Add trusted people who can be contacted during an emergency."
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Button(
                onClick = {
                    showAddContact = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("＋ Add Emergency Contact")
            }

            if (showAddContact) {

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                OutlinedTextField(
                    value = contactName,
                    onValueChange = {
                        contactName = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Contact Name")
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = {
                        contactPhone = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Phone Number")
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedTextField(
                    value = contactRelationship,
                    onValueChange = {
                        contactRelationship = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Relationship")
                    },
                    singleLine = true
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Button(
                    onClick = {

                        val user = auth.currentUser

                        if (user != null &&
                            contactName.isNotBlank() &&
                            contactPhone.isNotBlank()
                        ) {

                            // LOCAL DATABASE
                            localDb.addContact(
                                name = contactName.trim(),
                                phone = contactPhone.trim(),
                                relationship = contactRelationship.trim()
                            )

                            // FIREBASE DATABASE
                            val contactData = hashMapOf(
                                "name" to contactName.trim(),
                                "phone" to contactPhone.trim(),
                                "relationship" to contactRelationship.trim()
                            )

                            db.collection("users")
                                .document(user.uid)
                                .collection("emergency_contacts")
                                .add(contactData)
                                .addOnSuccessListener {

                                    contactName = ""
                                    contactPhone = ""
                                    contactRelationship = ""
                                    showAddContact = false
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SAVE CONTACT")
                }
                Spacer(
                    modifier = Modifier.height(25.dp)
                )


            }

            if (contacts.isNotEmpty()) {

                Text(
                    text = "Saved Contacts",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                contacts.forEach { contact ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = contact["name"] ?: "",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(5.dp)
                            )

                            Text(
                                text = "📞 ${contact["phone"] ?: ""}"
                            )

                            Text(
                                text = "Relationship: ${contact["relationship"] ?: ""}"
                            )
                        }
                    }
                }
            }
        }
    }
}



// =====================================================
// ALERTS SCREEN
// =====================================================

@Composable
fun AlertsScreen(
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "Alerts",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "🔔",
                fontSize = 55.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "No Active Alerts",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Your safety notifications will appear here.",
                color = Color.Gray
            )
        }
    }
}


// =====================================================
// PROFILE SCREEN
// =====================================================

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    userEmail: String,
    onLogout: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "Profile",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Text(
                text = "👤",
                fontSize = 65.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "My Profile",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Text(
                text = "Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = userEmail,
                fontSize = 15.sp,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(25.dp)
            )

            Text(
                text = "Emergency Contacts",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Button(
                onClick = {
                    // Contact management later
                }
            ) {

                Text(
                    text = "Manage Contacts"
                )
            }

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            OutlinedButton(
                onClick = {
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = "LOG OUT"
                )
            }
        }
    }
}
