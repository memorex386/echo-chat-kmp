package com.echochat.feature.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.echochat.core.designsystem.resources.ic_arrow_back
import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.feature.chat.resources.Res
import com.echochat.feature.chat.resources.back
import com.echochat.feature.chat.resources.echo_name
import com.echochat.feature.chat.resources.photo
import com.echochat.feature.chat.resources.photo_from_echo
import com.echochat.feature.chat.resources.photo_from_you
import com.echochat.feature.chat.resources.you
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.echochat.core.designsystem.resources.Res as DesignRes

/** One photo, fitted to the screen on black, as in a gallery app. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoViewerScreen(
    image: Image,
    from: Author,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    val fromMe = from == Author.Me
    Scaffold(
        containerColor = Color.Black,
        contentColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(if (fromMe) Res.string.you else Res.string.echo_name))
                        Text(stringResource(Res.string.photo), style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(DesignRes.drawable.ic_arrow_back), stringResource(Res.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        AsyncImage(
            model = image,
            contentDescription = stringResource(if (fromMe) Res.string.photo_from_you else Res.string.photo_from_echo),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}
