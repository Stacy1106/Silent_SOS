package com.example.silent_sos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
    PROFILE
}


// =====================================================
// MAIN APPLICATION
// =====================================================

@Composable
fun SilentSOSApp() {

    var currentScreen by remember {
        mutableStateOf(Screen.HOME)
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
                }
            )
        }
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
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
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

        // LARGE CLICKABLE AREA
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
                    // SOS functionality will be added later
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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "Safety Map",
            onBack = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEAF2F8)),
            contentAlignment = Alignment.Center
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "🗺️",
                    fontSize = 70.sp
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = "Safety Map",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Google Maps will be added later.",
                    color = Color.Gray
                )
            }
        }
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
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        BackButton(
            title = "Profile",
            onBack = onBack
        )

        Column(
            modifier = Modifier.padding(20.dp)
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
        }
    }
}