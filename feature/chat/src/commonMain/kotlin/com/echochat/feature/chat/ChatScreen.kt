package com.echochat.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
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
import com.echochat.core.designsystem.resources.ic_arrow_down
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
import com.echochat.feature.chat.resources.scroll_to_latest
import com.echochat.feature.chat.resources.send
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
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

    // Landscape is measured on the whole screen, so the keyboard opening doesn't change it.
    BoxWithConstraints(modifier) {
        val bubbleFraction = if (maxWidth > maxHeight) LandscapeBubbleFraction else BubbleWidthFraction
        Scaffold(
            topBar = { ChatTopBar() },
            // The bottom inset is left to the composer and attach panel, so the panel's colour reaches the screen edge.
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
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
                    bubbleFraction = bubbleFraction,
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
    bubbleFraction: Float,
    onOpenImage: (Message) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Only messages that arrive while the chat is on screen animate in, and only once.
    var shownUpTo by rememberSaveable { mutableLongStateOf(messages.lastOrNull()?.id ?: 0L) }
    val newestId = messages.lastOrNull()?.id
    LaunchedEffect(newestId) {
        if (newestId != null) shownUpTo = newestId
    }

    if (messages.isEmpty() && !isReplying) {
        EmptyChat(modifier.fillMaxWidth())
        return
    }

    val lastIndex = messages.size + (if (isReplying) 1 else 0) - 1
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = lastIndex)
    var following by remember { mutableStateOf(false) }
    // These are saved, so rebuilding the screen (a rotation, or back from the photo viewer)
    // neither forgets that I'm reading further up nor mistakes old messages for new ones.
    var atBottom by rememberSaveable { mutableStateOf(true) }
    var followedSize by rememberSaveable { mutableIntStateOf(messages.size) }
    var followedReplying by rememberSaveable { mutableStateOf(isReplying) }

    // Follow new messages only from the bottom, or when I sent them.
    val follow = atBottom || messages.lastOrNull()?.author == Author.Me
    LaunchedEffect(messages.size, isReplying) {
        if (messages.size == followedSize && isReplying == followedReplying) return@LaunchedEffect
        followedSize = messages.size
        followedReplying = isReplying
        if (!follow) return@LaunchedEffect
        following = true
        try {
            listState.scrollToEnd(lastIndex, animated = true)
        } finally {
            following = false
        }
    }

    // Tracks whether I'm at the bottom as I scroll. When the list's height changes (the keyboard,
    // a taller input, a rotation) while I'm at the bottom, it keeps the end in view (CHAT-18).
    LaunchedEffect(listState) {
        var lastHeight = 0
        snapshotFlow { Triple(listState.layoutInfo.viewportSize.height, listState.canScrollForward, following) }
            .filter { (height) -> height > 0 }
            .collect { (height, canScrollDown, isFollowing) ->
                when {
                    height != lastHeight && atBottom && canScrollDown ->
                        listState.scrollToEnd(listState.layoutInfo.totalItemsCount - 1, animated = false)
                    !isFollowing -> atBottom = !canScrollDown
                }
                lastHeight = height
            }
    }

    val scope = rememberCoroutineScope()
    Box(modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(messages, key = Message::id) { message ->
                MessageRow(
                    message = message,
                    isNew = message.id > shownUpTo,
                    widthFraction = bubbleFraction,
                    onOpenImage = onOpenImage,
                    // The entrance below does the fade-in; this keeps the slide of the bubbles above.
                    modifier = Modifier.animateItem(fadeInSpec = null),
                )
            }
            if (isReplying) {
                item(key = TypingRowKey) {
                    TypingRow(Modifier.animateItem(fadeInSpec = null))
                }
            }
        }
        AnimatedVisibility(
            visible = listState.canScrollForward && !following,
            enter = fadeIn() + scaleIn(initialScale = 0.6f),
            exit = fadeOut() + scaleOut(targetScale = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp),
        ) {
            SmallFloatingActionButton(
                onClick = { scope.launch { listState.scrollToEnd(lastIndex, animated = true) } },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Icon(painterResource(DesignRes.drawable.ic_arrow_down), stringResource(Res.string.scroll_to_latest))
            }
        }
    }
}

@Composable
private fun MessageRow(
    message: Message,
    isNew: Boolean,
    widthFraction: Float,
    onOpenImage: (Message) -> Unit,
    modifier: Modifier = Modifier,
) {
    val mine = message.author == Author.Me
    val entrance = rememberEntrance(play = isNew)
    Box(
        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp, if (mine) Alignment.End else Alignment.Start),
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .entrance(entrance, fromEnd = mine),
        ) {
            if (!mine) Avatar(initial = stringResource(Res.string.echo_initial), size = 28.dp)
            // A photo stands on its own, and its caption gets a bubble of its own below (PHOTO-06).
            Column(
                horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                message.image?.let { Photo(message, it, onOpenImage) }
                if (message.text.isNotEmpty()) {
                    Bubble(mine = mine) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Photo(
    message: Message,
    image: Image,
    onOpenImage: (Message) -> Unit,
) {
    val openLabel = stringResource(
        if (message.author == Author.Me) Res.string.open_photo_from_you else Res.string.open_photo_from_echo,
    )
    AsyncImage(
        model = PlatformFile(image.path),
        contentDescription = openLabel,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(PhotoWidth, PhotoHeight)
            .clip(PhotoShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onOpenImage(message) },
    )
}

@Composable
private fun TypingRow(modifier: Modifier = Modifier) {
    val typing = stringResource(Res.string.echo_typing)
    val entrance = rememberEntrance(play = true)
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .entrance(entrance, fromEnd = false),
    ) {
        Avatar(initial = stringResource(Res.string.echo_initial), size = 28.dp)
        Bubble(mine = false, modifier = Modifier.semantics { contentDescription = typing }) {
            TypingDots(Modifier.padding(horizontal = 16.dp, vertical = 17.dp))
        }
    }
}

/** Scrolls to the very end of the list, even when the newest message is taller than the list itself. */
private suspend fun LazyListState.scrollToEnd(lastIndex: Int, animated: Boolean) {
    if (animated) animateScrollToItem(lastIndex) else scrollToItem(lastIndex)
    // That lines up the item's top with the list's top; a tall item still has its end below.
    val lastItemHeight = layoutInfo.visibleItemsInfo.lastOrNull()?.size?.toFloat() ?: return
    if (!canScrollForward) return
    if (animated) animateScrollBy(lastItemHeight) else scrollBy(lastItemHeight)
}

/** How far a bubble's entrance has played: from 0 for a new arrival, otherwise already at 1. */
@Composable
private fun rememberEntrance(play: Boolean): Animatable<Float, AnimationVector1D> {
    val progress = remember { Animatable(if (play) 0f else 1f) }
    LaunchedEffect(progress) {
        progress.animateTo(1f, tween(EntranceMillis, easing = LinearOutSlowInEasing))
    }
    return progress
}

/** Fades in while rising and growing from the bubble's own bottom corner. */
private fun Modifier.entrance(progress: Animatable<Float, AnimationVector1D>, fromEnd: Boolean) = graphicsLayer {
    val played = progress.value
    alpha = played
    translationY = (1f - played) * EntranceRise.toPx()
    scaleX = EntranceStartScale + (1f - EntranceStartScale) * played
    scaleY = scaleX
    transformOrigin = TransformOrigin(pivotFractionX = if (fromEnd) 1f else 0f, pivotFractionY = 1f)
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
        modifier = modifier.testTag(BubbleTestTag),
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

/** Marks message bubbles, so tests can tell what sits inside one. */
internal const val BubbleTestTag = "bubble"
private const val TypingRowKey = "typing"
private const val BubbleWidthFraction = 0.85f
private const val LandscapeBubbleFraction = 2f / 3f
private const val EntranceMillis = 220
private const val EntranceStartScale = 0.92f
private val EntranceRise = 12.dp
private val PhotoWidth = 208.dp
private val PhotoHeight = 156.dp
private val PhotoShape = RoundedCornerShape(20.dp)
private val MyBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp)
private val TheirBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp)
