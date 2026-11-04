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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.echochat.core.designsystem.component.Avatar
import com.echochat.core.designsystem.component.TypingDots
import com.echochat.core.designsystem.resources.ic_add
import com.echochat.core.designsystem.resources.ic_arrow_down
import com.echochat.core.designsystem.resources.ic_camera
import com.echochat.core.designsystem.resources.ic_close
import com.echochat.core.designsystem.resources.ic_copy
import com.echochat.core.designsystem.resources.ic_delete
import com.echochat.core.designsystem.resources.ic_photo
import com.echochat.core.designsystem.resources.ic_save
import com.echochat.core.designsystem.resources.ic_send
import com.echochat.core.model.Author
import com.echochat.core.model.Image
import com.echochat.core.model.Message
import com.echochat.feature.chat.resources.Res
import com.echochat.feature.chat.resources.attach
import com.echochat.feature.chat.resources.camera
import com.echochat.feature.chat.resources.clip_label
import com.echochat.feature.chat.resources.copied
import com.echochat.feature.chat.resources.copy
import com.echochat.feature.chat.resources.delete
import com.echochat.feature.chat.resources.echo_initial
import com.echochat.feature.chat.resources.echo_name
import com.echochat.feature.chat.resources.echo_typing
import com.echochat.feature.chat.resources.empty_hint
import com.echochat.feature.chat.resources.empty_title
import com.echochat.feature.chat.resources.gallery
import com.echochat.feature.chat.resources.message_actions
import com.echochat.feature.chat.resources.message_deleted
import com.echochat.feature.chat.resources.message_placeholder
import com.echochat.feature.chat.resources.open_photo_from_echo
import com.echochat.feature.chat.resources.open_photo_from_you
import com.echochat.feature.chat.resources.photo_not_saved
import com.echochat.feature.chat.resources.photo_saved
import com.echochat.feature.chat.resources.remove_photo
import com.echochat.feature.chat.resources.save
import com.echochat.feature.chat.resources.scroll_to_latest
import com.echochat.feature.chat.resources.send
import com.echochat.feature.chat.resources.undo
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
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
    // Resumed means the chat is on screen. Paused or gone means it isn't: the photo viewer is
    // open, the app is in the background, or a desktop window has lost focus.
    LifecycleResumeEffect(viewModel) {
        viewModel.onChatShown()
        onPauseOrDispose { viewModel.onChatHidden() }
    }
    val askToNotify = rememberNotificationPermissionRequest()

    val snackbarHostState = remember { SnackbarHostState() }
    val deleted = stringResource(Res.string.message_deleted)
    val undo = stringResource(Res.string.undo)
    val saved = stringResource(Res.string.photo_saved)
    val notSaved = stringResource(Res.string.photo_not_saved)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                // Short, not Material's default for an action (shown until tapped): the offer goes
                // on its own. Android lengthens it for people who ask for more time to act.
                is ChatEvent.Deleted -> {
                    val result = snackbarHostState.showSnackbar(deleted, actionLabel = undo, duration = SnackbarDuration.Short)
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(event.message)
                }
                ChatEvent.PhotoSaved -> snackbarHostState.showSnackbar(saved)
                ChatEvent.PhotoNotSaved -> snackbarHostState.showSnackbar(notSaved)
            }
        }
    }
    val copy = rememberCopy(snackbarHostState)
    val savePhoto = rememberSavePhoto(viewModel)

    ChatScreen(
        uiState = uiState,
        draft = viewModel.draft,
        attachment = viewModel.attachment,
        canSend = viewModel.canSend,
        onDraftChange = viewModel::onDraftChange,
        onSend = {
            viewModel.send()
            askToNotify()
        },
        onPickPhoto = pickPhoto::launch,
        onTakePhoto = takePhoto?.let { launch -> { launch(viewModel.startCamera()) } },
        onRemoveAttachment = viewModel::removeAttachment,
        onOpenImage = onOpenImage,
        onAction = { action ->
            when (action) {
                is MessageAction.CopyText -> copy { label -> copyText(action.message.text, label) }
                is MessageAction.CopyPhoto -> action.message.image?.let { image -> copy { label -> copyPhoto(image.path, label) } }
                is MessageAction.SavePhoto -> savePhoto?.invoke(action.message)
                is MessageAction.Delete -> viewModel.delete(action.message)
            }
        },
        canSavePhotos = savePhoto != null,
        snackbarHostState = snackbarHostState,
    )
}

/** Copies something to the clipboard, and says "Copied" where the system doesn't. */
@Composable
private fun rememberCopy(snackbarHostState: SnackbarHostState): (suspend Clipboard.(label: String) -> Unit) -> Unit {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val label = stringResource(Res.string.clip_label)
    val copied = stringResource(Res.string.copied)
    return { copyTo ->
        scope.launch {
            clipboard.copyTo(label)
            if (!systemConfirmsCopy) snackbarHostState.showSnackbar(copied)
        }
    }
}

/**
 * [onTakePhoto] is null where there is no camera, as on desktop. [canSavePhotos] is false where
 * a photo can't be saved, so the photo menu leaves Save out. [keyboardUp] is whether the
 * on-screen keyboard is showing; desktop has none.
 */
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
    onAction: (MessageAction) -> Unit = {},
    canSavePhotos: Boolean = true,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    keyboardUp: Boolean = isKeyboardUp(),
) {
    var attachPanelOpen by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    // Back closes the panel before it leaves the chat: Android's Back, Esc on desktop, the edge swipe on iOS.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = attachPanelOpen,
        onBackCompleted = { attachPanelOpen = false },
    )

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
                Box(Modifier.weight(1f)) {
                    MessageList(
                        messages = uiState.messages,
                        isReplying = uiState.isReplying,
                        bubbleFraction = bubbleFraction,
                        onOpenImage = onOpenImage,
                        onAction = onAction,
                        canSavePhotos = canSavePhotos,
                        modifier = Modifier.fillMaxSize(),
                    )
                    SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
                }
                Composer(
                    draft = draft,
                    attachment = attachment,
                    canSend = canSend,
                    attachPanelOpen = attachPanelOpen,
                    onDraftChange = onDraftChange,
                    onSend = onSend,
                    // The panel and the keyboard take turns: taking the cursor out of the input closes
                    // the keyboard, and putting it back closes the panel.
                    onToggleAttachPanel = {
                        attachPanelOpen = !attachPanelOpen
                        if (attachPanelOpen) focusManager.clearFocus()
                    },
                    onInputFocused = { attachPanelOpen = false },
                    keyboardUp = keyboardUp,
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
    onAction: (MessageAction) -> Unit,
    canSavePhotos: Boolean,
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
                    onAction = onAction,
                    canSavePhotos = canSavePhotos,
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
    onAction: (MessageAction) -> Unit,
    canSavePhotos: Boolean,
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
                message.image?.let { image ->
                    MessageMenu(message, forPhoto = true, canSave = canSavePhotos, onAction) { openMenu ->
                        Photo(message, image, onOpenImage, openMenu)
                    }
                }
                // A caption is text, so its bubble gets the text menu.
                if (message.text.isNotEmpty()) {
                    MessageMenu(message, forPhoto = false, canSave = false, onAction) { openMenu ->
                        val actionsLabel = stringResource(Res.string.message_actions)
                        Bubble(mine = mine, modifier = Modifier.opensMenuOnLongPress(actionsLabel, openMenu)) {
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
}

@Composable
private fun Photo(
    message: Message,
    image: Image,
    onOpenImage: (Message) -> Unit,
    openMenu: () -> Unit,
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
            .opensMenuOnRightClick(openMenu)
            .combinedClickable(
                onLongClickLabel = stringResource(Res.string.message_actions),
                onLongClick = openMenu,
                onClick = { onOpenImage(message) },
            ),
    )
}

/** A bubble's menu, opened right at the bubble: Copy and Delete, plus Save for a photo where it can be saved. */
@Composable
private fun MessageMenu(
    message: Message,
    forPhoto: Boolean,
    canSave: Boolean,
    onAction: (MessageAction) -> Unit,
    content: @Composable (openMenu: () -> Unit) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val pick = { action: MessageAction ->
        open = false
        onAction(action)
    }
    Box {
        content { open = true }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            if (forPhoto && canSave) {
                MenuItem(Res.string.save, DesignRes.drawable.ic_save) { pick(MessageAction.SavePhoto(message)) }
            }
            MenuItem(Res.string.copy, DesignRes.drawable.ic_copy) {
                pick(if (forPhoto) MessageAction.CopyPhoto(message) else MessageAction.CopyText(message))
            }
            MenuItem(Res.string.delete, DesignRes.drawable.ic_delete) { pick(MessageAction.Delete(message)) }
        }
    }
}

@Composable
private fun MenuItem(
    label: StringResource,
    icon: DrawableResource,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(label)) },
        leadingIcon = { Icon(painterResource(icon), contentDescription = null) },
        onClick = onClick,
    )
}

/**
 * A text bubble has nothing to do on a tap, so its menu opens on a long-press, or a right-click
 * with a mouse. Screen readers get the menu as a named action.
 */
@Composable
private fun Modifier.opensMenuOnLongPress(label: String, openMenu: () -> Unit): Modifier {
    val haptics = LocalHapticFeedback.current
    return opensMenuOnRightClick(openMenu)
        .pointerInput(openMenu) {
            detectTapGestures(
                onLongPress = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    openMenu()
                },
            )
        }
        .semantics {
            onLongClick(label) {
                openMenu()
                true
            }
        }
}

/** Right-click opens the menu too, as desktop users expect. */
private fun Modifier.opensMenuOnRightClick(openMenu: () -> Unit) = pointerInput(openMenu) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                event.changes.forEach { it.consume() }
                openMenu()
            }
        }
    }
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
    onInputFocused: () -> Unit,
    keyboardUp: Boolean,
    onRemoveAttachment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Whether I'm typing, meaning the keyboard is up, saved with the screen: after Android rebuilds the
    // screen for a rotation, the cursor, and with it the keyboard, comes back only if it was. It changes
    // only while the screen is resumed, because a rotation takes the keyboard down as it rebuilds.
    var typing by rememberSaveable { mutableStateOf(false) }
    val wasTyping = remember { typing }
    val focusRequester = remember { FocusRequester() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(keyboardUp) {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) typing = keyboardUp
    }
    LaunchedEffect(Unit) {
        if (wasTyping) focusRequester.requestFocus()
    }
    // The view model keeps the draft as plain text. The cursor, and the word the keyboard is still
    // composing, live here. When the draft changes from outside, by sending or by being restored,
    // the cursor goes to its end.
    var field by remember { mutableStateOf(TextFieldValue(draft, TextRange(draft.length))) }
    val shown = if (field.text == draft) field else TextFieldValue(draft, TextRange(draft.length))

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
            AllowFullScreenKeyboard {
                TextField(
                    value = shown,
                    onValueChange = {
                        field = it
                        onDraftChange(it.text)
                    },
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
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { if (it.isFocused) onInputFocused() }
                        .enterSends(canSend, onSend),
                )
            }
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
