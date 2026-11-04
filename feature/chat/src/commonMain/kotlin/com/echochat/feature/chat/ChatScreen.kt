package com.echochat.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.echochat.core.designsystem.component.Avatar
import com.echochat.core.designsystem.component.TypingDots
import com.echochat.core.designsystem.resources.ic_send
import com.echochat.core.model.Author
import com.echochat.core.model.Message
import com.echochat.feature.chat.resources.Res
import com.echochat.feature.chat.resources.echo_initial
import com.echochat.feature.chat.resources.echo_name
import com.echochat.feature.chat.resources.echo_typing
import com.echochat.feature.chat.resources.empty_hint
import com.echochat.feature.chat.resources.empty_title
import com.echochat.feature.chat.resources.message_placeholder
import com.echochat.feature.chat.resources.send
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.echochat.core.designsystem.resources.Res as DesignRes

@Composable
fun ChatRoute(viewModel: ChatViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatScreen(
        uiState = uiState,
        draft = viewModel.draft,
        canSend = viewModel.canSend,
        onDraftChange = viewModel::onDraftChange,
        onSend = viewModel::send,
    )
}

@Composable
fun ChatScreen(
    uiState: ChatUiState,
    draft: String,
    canSend: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = { ChatTopBar() },
        modifier = modifier,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
        ) {
            MessageList(
                messages = uiState.messages,
                isReplying = uiState.isReplying,
                modifier = Modifier.weight(1f),
            )
            Composer(
                draft = draft,
                canSend = canSend,
                onDraftChange = onDraftChange,
                onSend = onSend,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatTopBar() {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(initial = stringResource(Res.string.echo_initial))
                Text(stringResource(Res.string.echo_name))
            }
        },
    )
}

@Composable
private fun MessageList(
    messages: List<Message>,
    isReplying: Boolean,
    modifier: Modifier = Modifier,
) {
    if (messages.isEmpty() && !isReplying) {
        EmptyChat(modifier.fillMaxWidth())
        return
    }

    val listState = rememberLazyListState()
    val lastIndex = messages.size + (if (isReplying) 1 else 0) - 1
    LaunchedEffect(messages.size, isReplying) {
        listState.animateScrollToItem(lastIndex)
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
        modifier = modifier.fillMaxSize(),
    ) {
        items(messages, key = Message::id) { message ->
            MessageRow(message, Modifier.animateItem())
        }
        if (isReplying) {
            item(key = TypingRowKey) {
                TypingRow(Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun MessageRow(message: Message, modifier: Modifier = Modifier) {
    val mine = message.author == Author.Me
    Box(
        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp, if (mine) Alignment.End else Alignment.Start),
            modifier = Modifier.fillMaxWidth(BubbleWidthFraction),
        ) {
            if (!mine) Avatar(initial = stringResource(Res.string.echo_initial), size = 28.dp)
            Bubble(mine = mine, modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun TypingRow(modifier: Modifier = Modifier) {
    val typing = stringResource(Res.string.echo_typing)
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Avatar(initial = stringResource(Res.string.echo_initial), size = 28.dp)
        Bubble(mine = false, modifier = Modifier.semantics { contentDescription = typing }) {
            TypingDots(Modifier.padding(horizontal = 16.dp, vertical = 17.dp))
        }
    }
}

@Composable
private fun Bubble(
    mine: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = if (mine) MyBubbleShape else TheirBubbleShape,
        color = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier,
        content = content,
    )
}

@Composable
private fun EmptyChat(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
    ) {
        Avatar(initial = stringResource(Res.string.echo_initial), size = 56.dp)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.empty_title), style = MaterialTheme.typography.titleMedium)
        Text(
            text = stringResource(Res.string.empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Composer(
    draft: String,
    canSend: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
    ) {
        TextField(
            value = draft,
            onValueChange = onDraftChange,
            placeholder = { Text(stringResource(Res.string.message_placeholder)) },
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Send,
            ),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            modifier = Modifier.weight(1f),
        )
        FilledIconButton(
            onClick = onSend,
            enabled = canSend,
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                painter = painterResource(DesignRes.drawable.ic_send),
                contentDescription = stringResource(Res.string.send),
            )
        }
    }
}

private const val TypingRowKey = "typing"
private const val BubbleWidthFraction = 0.85f
private val MyBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp)
private val TheirBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp)
