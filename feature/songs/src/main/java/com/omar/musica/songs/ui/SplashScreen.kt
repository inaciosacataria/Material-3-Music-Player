package com.omar.musica.songs.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.omar.musica.songs.R
import com.omar.musica.songs.navigation.SONGS_NAVIGATION_GRAPH
import com.omar.musica.songs.navigation.SONGS_ROUTE
import com.omar.musica.songs.navigation.SPLASH_ROUTE
import kotlinx.coroutines.delay


@Preview
@Composable
fun SplashScreenScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        contentAlignment = Alignment.Center // Centraliza a coluna
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = CircleShape)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.splash_pen),
                contentDescription = "splash",
                modifier = Modifier
                    .height(100.dp)
                    .width(100.dp)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text("powered by\nLibertas", color = Color.White, textAlign = TextAlign.Center, lineHeight = 20.sp)
    }

    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(SONGS_NAVIGATION_GRAPH) {
            popUpTo(SPLASH_ROUTE) { inclusive = true }
        }
    }
}
