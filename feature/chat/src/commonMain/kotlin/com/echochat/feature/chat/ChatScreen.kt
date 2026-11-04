package com.echochat.feature.chat

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.echochat.core.designsystem.component.Avatar
import com.echochat.core.designsystem.component.TypingDots
import com.echochat.core.designsystem.resources.ic_add
import com.echochat.core.designsystem.resources.ic_camera
import com.echochat.core.designsystem.resources.ic_close
import com.echochat.core.designsystem.resources.ic_photo
import com.echochat.core.designsystem.resources.ic_send
import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import com.echochat.feature.chat.resources.Res
import com.echochat.feature.chat.resources.attach
import com.echochat.feature.chat.resources.camera
import com.echochat.feature.chat.resources.echo_initial
import com.echochat.feature.chat.resources.echo_name
import com.echochat.feature.chat.resources.echo_typing
import com.echochat.feature.chat.resources.empty_hint
import com.echochat.feature.chat.resources.empty_title
import com.echochat.feature.chat.resources.gallery
import com.echochat.feature.chat.resources.message_placeholder
import com.echochat.feature.chat.resources.open_photo_from_echo
import com.echochat.feature.chat.resources.open_photo_from_you
import com.echochat.feature.chat.resources.remove_photo
import com.echochat.feature.chat.resources.send
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.echochat.core.designsystem.resources.Res as DesignRes

@Composable
fun ChatRoute(
    onOpenImage: (Message) -> Unit,
    viewModel: ChatViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Android's photo picker, iOS's photo picker, or a file dialog for images on desktop.
    val pickPhoto = rememberFilePickerLauncher(type = FileKitType.Image) { photo ->
        photo?.let(viewModel::onPhotoPicked)
    }
    val takePhoto = rememberTakePhoto(viewModel::onCameraResult)

    ChatScreen(
        uiState = uiState,
        draft = viewModel.draft,
        attachment = viewModel.attachment,
        canSend = viewModel.canSend,
        onDraftChange = viewModel::onDraftChange,
        onSend = viewModel::send,
        onPickPhoto = pickPhoto::launch,
        onTakePhoto = takePhoto?.let { launch -> { launch(viewModel.startCamera()) } },
        onRemoveAttachment = viewModel::removeAttachment,
        onOpenImage = onOpenImage,
    )
}

/** [onTakePhoto] is null where there is no camera, as on desktop. */
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    draft: String,
    attachment: Image?,
    canSend: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: (() -> Unit)?,
    onRemoveAttachment: () -> Unit,
    onOpenImage: (Message) -> Unit,
    modifier: Modifier = Modifier,
) {
    var attachPanelOpen by rememberSaveable { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = { ChatTopBar() },
        // The bottom inset is left to the composer and attach panel, so the panel's colour reaches the screen edge.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
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
                onOpenImage = onOpenImage,
                modifier = Modifier.weight(1f),
            )
            Composer(
                draft = draft,
                attachment = attachment,
                canSend = canSend,
                attachPanelOpen = attachPanelOpen,
                onDraftChange = onDraftChange,
                onSend = onSend,
                onToggleAttachPanel = {
                    attachPanelOpen = !attachPanelOpen
                    if (attachPanelOpen) keyboard?.hide()
                },
                onRemoveAttachment = onRemoveAttachment,
                modifier = if (attachPanelOpen) Modifier else Modifier.navigationBarsPadding(),
            )
            if (attachPanelOpen) {
                AttachPanel(
                    onPickPhoto = {
                        attachPanelOpen = false
                        onPickPhoto()
                    },
                    onTakePhoto = onTakePhoto?.let { takePhoto ->
                        {
                            attachPanelOpen = false
                            takePhoto()
                        }
                    },
                )
            }
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
    onOpenImage: (Message) -> Unit,
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
            MessageRow(message, onOpenImage, Modifier.animateItem())
        }
        if (isReplying) {
            item(key = TypingRowKey) {
                TypingRow(Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun MessageRow(
    message: Message,
    onOpenImage: (Message) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                val image = message.image
                if (image == null) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                } else {
                    PhotoContent(message, image, onOpenImage)
                }
            }
        }
    }
}

@Composable
private fun PhotoContent(
    message: Message,
    image: Image,
    onOpenImage: (Message) -> Unit,
) {
    val openLabel = stringResource(
        if (message.author == Author.Me) Res.string.open_photo_from_you else Res.string.open_photo_from_echo,
    )
    Column(Modifier.width(PhotoWidth + 8.dp).padding(4.dp)) {
        AsyncImage(
            model = PlatformFile(image.path),
            contentDescription = openLabel,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(PhotoWidth, PhotoHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                .clickable { onOpenImage(message) },
        )
        if (message.text.isNotEmpty()) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 4.dp),
            )
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
    attachment: Image?,
    canSend: Boolean,
    attachPanelOpen: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onToggleAttachPanel: () -> Unit,
    onRemoveAttachment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        if (attachment != null) {
            AttachmentPreview(attachment, onRemoveAttachment, Modifier.padding(start = 60.dp, top = 8.dp))
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        ) {
            val plusTurn by animateFloatAsState(if (attachPanelOpen) 45f else 0f, label = "attach")
            IconButton(onClick = onToggleAttachPanel, modifier = Modifier.size(56.dp)) {
                Icon(
                    painter = painterResource(DesignRes.drawable.ic_add),
                    contentDescription = stringResource(Res.string.attach),
                    modifier = Modifier.rotate(plusTurn),
                )
            }
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
}

@Composable
private fun AttachmentPreview(
    attachment: Image,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.size(76.dp)) {
        AsyncImage(
            model = PlatformFile(attachment.path),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        )
        IconButton(
            onClick = onRemove,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.6f), contentColor = Color.White),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(26.dp),
        ) {
            Icon(
                painter = painterResource(DesignRes.drawable.ic_close),
                contentDescription = stringResource(Res.string.remove_photo),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun AttachPanel(
    onPickPhoto: () -> Unit,
    onTakePhoto: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
    ) {
        AttachTile(DesignRes.drawable.ic_photo, stringResource(Res.string.gallery), onPickPhoto)
        if (onTakePhoto != null) {
            AttachTile(DesignRes.drawable.ic_camera, stringResource(Res.string.camera), onTakePhoto)
        }
    }
}

@Composable
private fun AttachTile(
    icon: DrawableResource,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.background, CircleShape),
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

private const val TypingRowKey = "typing"
private const val BubbleWidthFraction = 0.85f
private val PhotoWidth = 208.dp
private val PhotoHeight = 156.dp
private val MyBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp)
private val TheirBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp)
