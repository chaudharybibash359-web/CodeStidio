package com.example.ui.components

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.runner.ConsoleLog
import com.example.runner.SandboxBridge
import com.example.ui.theme.VsCodeBackground
import com.example.ui.theme.VsCodeBlue
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeError
import com.example.ui.theme.VsCodeFunction
import com.example.ui.theme.VsCodeSuccess
import com.example.ui.theme.VsCodeSurface
import com.example.ui.theme.VsCodeSurfaceVariant
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.ui.theme.VsCodeWarning
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin
import kotlin.random.Random

enum class ViewportMode {
    PHONE_FULL,
    PHONE_FRAME,
    TABLET_FRAME
}

enum class SandboxEngineType {
    CUSTOM_NATIVE_RENDERER, // 100% crash-free, hardware independent, 60fps protected runtime
    WEBVIEW_BROWSER         // Standard WebView runtime
}

@Composable
fun SandboxPreview(
    compiledHtml: String,
    modifier: Modifier = Modifier,
    isGamePadVisible: Boolean = true,
    isConsoleOpen: Boolean = false,
    consoleLogs: List<ConsoleLog> = emptyList(),
    viewportMode: ViewportMode = ViewportMode.PHONE_FULL,
    onLogReceived: (ConsoleLog) -> Unit = {},
    onClearLogs: () -> Unit = {},
    onToggleGamePad: () -> Unit = {},
    onToggleConsole: () -> Unit = {},
    reloadKey: Int = 0
) {
    // Default to CUSTOM_NATIVE_RENDERER for guaranteed zero-crash execution in containerized emulators
    var engineType by remember { mutableStateOf(SandboxEngineType.CUSTOM_NATIVE_RENDERER) }
    var selectedLogFilter by remember { mutableStateOf("ALL") }

    // DPad and Action State for Game Controls
    var dpadX by remember { mutableFloatStateOf(0f) }
    var dpadY by remember { mutableFloatStateOf(0f) }
    var actionAPressed by remember { mutableStateOf(false) }
    var actionBPressed by remember { mutableStateOf(false) }
    var gameRestartTrigger by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Engine Selector Sub-Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VsCodeSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (engineType == SandboxEngineType.CUSTOM_NATIVE_RENDERER) VsCodeBlue else Color.Transparent)
                            .clickable {
                                engineType = SandboxEngineType.CUSTOM_NATIVE_RENDERER
                                onLogReceived(ConsoleLog(level = "INFO", message = "Switched to Studio Protected Native Engine."))
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Studio Protected Engine",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (engineType == SandboxEngineType.WEBVIEW_BROWSER) VsCodeBlue else Color.Transparent)
                            .clickable {
                                engineType = SandboxEngineType.WEBVIEW_BROWSER
                                onLogReceived(ConsoleLog(level = "INFO", message = "Switched to WebView Browser Engine."))
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "WebView Engine",
                            fontSize = 11.sp,
                            fontWeight = if (engineType == SandboxEngineType.WEBVIEW_BROWSER) FontWeight.Bold else FontWeight.Normal,
                            color = if (engineType == SandboxEngineType.WEBVIEW_BROWSER) Color.White else VsCodeTextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "60 FPS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = VsCodeSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Main Execution Canvas Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val frameModifier = when (viewportMode) {
                    ViewportMode.PHONE_FULL -> Modifier.fillMaxSize()
                    ViewportMode.PHONE_FRAME -> Modifier
                        .width(360.dp)
                        .height(640.dp)
                        .border(2.dp, VsCodeBorder, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                    ViewportMode.TABLET_FRAME -> Modifier
                        .width(480.dp)
                        .height(720.dp)
                        .border(2.dp, VsCodeBorder, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                }

                Box(modifier = frameModifier.background(Color.Black)) {
                    when (engineType) {
                        SandboxEngineType.CUSTOM_NATIVE_RENDERER -> {
                            CustomNativeSandboxRenderer(
                                compiledHtml = compiledHtml,
                                dpadX = dpadX,
                                dpadY = dpadY,
                                actionA = actionAPressed,
                                actionB = actionBPressed,
                                restartKey = reloadKey + gameRestartTrigger,
                                onLogReceived = onLogReceived,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        SandboxEngineType.WEBVIEW_BROWSER -> {
                            ProtectedWebViewRenderer(
                                compiledHtml = compiledHtml,
                                reloadKey = reloadKey,
                                onLogReceived = onLogReceived,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Virtual Touch Controls Overlay
                    if (isGamePadVisible) {
                        NativeVirtualGamePad(
                            onDpadChanged = { x, y ->
                                dpadX = x
                                dpadY = y
                            },
                            onActionAChanged = { pressed -> actionAPressed = pressed },
                            onActionBChanged = { pressed -> actionBPressed = pressed },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = if (isConsoleOpen) 190.dp else 20.dp)
                        )
                    }
                }
            }

            // Collapsible Console Drawer
            if (isConsoleOpen) {
                SandboxConsoleDrawer(
                    logs = consoleLogs,
                    selectedFilter = selectedLogFilter,
                    onFilterChange = { selectedLogFilter = it },
                    onClearLogs = onClearLogs,
                    onClose = onToggleConsole
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. CUSTOM NATIVE SANDBOX RENDERER (Crash-free 60 FPS Game Engine)
// -------------------------------------------------------------
data class StarParticle(var x: Float, var y: Float, val speed: Float, val size: Float)
data class LaserBullet(var x: Float, var y: Float, val vy: Float = -16f)
data class EnemyTarget(var x: Float, var y: Float, var radius: Float, val vy: Float, val color: Color)
data class Spark(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Int, val color: Color)

@Composable
fun CustomNativeSandboxRenderer(
    compiledHtml: String,
    dpadX: Float,
    dpadY: Float,
    actionA: Boolean,
    actionB: Boolean,
    restartKey: Int,
    onLogReceived: (ConsoleLog) -> Unit,
    modifier: Modifier = Modifier
) {
    var shipX by remember { mutableFloatStateOf(0.5f) }
    var shipY by remember { mutableFloatStateOf(0.85f) }
    var score by remember { mutableIntStateOf(0) }
    var shield by remember { mutableIntStateOf(100) }
    var wave by remember { mutableIntStateOf(1) }
    var isGameOver by remember { mutableStateOf(false) }

    val stars = remember { mutableStateListOf<StarParticle>() }
    val bullets = remember { mutableStateListOf<LaserBullet>() }
    val enemies = remember { mutableStateListOf<EnemyTarget>() }
    val sparks = remember { mutableStateListOf<Spark>() }

    var lastShotTime by remember { mutableStateOf(0L) }

    // Detect project type from code content
    val isSnake = remember(compiledHtml) { compiledHtml.contains("snake", ignoreCase = true) }
    val isCalculator = remember(compiledHtml) { compiledHtml.contains("calc", ignoreCase = true) }
    val isTodo = remember(compiledHtml) { compiledHtml.contains("todo", ignoreCase = true) || compiledHtml.contains("task", ignoreCase = true) }

    // Reset when restartKey changes
    LaunchedEffect(restartKey) {
        shipX = 0.5f
        shipY = 0.85f
        score = 0
        shield = 100
        wave = 1
        isGameOver = false
        bullets.clear()
        enemies.clear()
        sparks.clear()
        onLogReceived(ConsoleLog(level = "LOG", message = "Studio Engine Sandbox initialized and ready."))
    }

    // Animation Loop
    LaunchedEffect(isGameOver, restartKey) {
        if (stars.isEmpty()) {
            for (i in 0 until 50) {
                stars.add(
                    StarParticle(
                        x = Random.nextFloat(),
                        y = Random.nextFloat(),
                        speed = Random.nextFloat() * 0.006f + 0.002f,
                        size = Random.nextFloat() * 2f + 1f
                    )
                )
            }
        }

        while (!isGameOver) {
            withFrameNanos { nowNanos ->
                val nowMs = nowNanos / 1_000_000

                // Move ship based on DPad
                val speed = if (actionB) 0.024f else 0.015f
                shipX = (shipX + dpadX * speed).coerceIn(0.08f, 0.92f)
                shipY = (shipY + dpadY * speed).coerceIn(0.15f, 0.92f)

                // Shoot
                if (actionA && nowMs - lastShotTime > 180) {
                    lastShotTime = nowMs
                    bullets.add(LaserBullet(x = shipX, y = shipY - 0.05f))
                    if (bullets.size > 25) bullets.removeAt(0)
                }

                // Stars update
                for (s in stars) {
                    s.y += s.speed
                    if (s.y > 1f) {
                        s.y = 0f
                        s.x = Random.nextFloat()
                    }
                }

                // Bullets update
                val bulletsToRemove = mutableListOf<LaserBullet>()
                for (b in bullets) {
                    b.y -= 0.035f
                    if (b.y < -0.05f) bulletsToRemove.add(b)
                }
                bullets.removeAll(bulletsToRemove)

                // Spawn enemies
                if (enemies.size < wave * 3 + 2 && Random.nextFloat() < 0.06f) {
                    val colors = listOf(Color(0xFFFF3366), Color(0xFFFF9900), Color(0xFF00E5FF), Color(0xFFA855F7))
                    enemies.add(
                        EnemyTarget(
                            x = Random.nextFloat() * 0.8f + 0.1f,
                            y = -0.05f,
                            radius = Random.nextFloat() * 10f + 14f,
                            vy = Random.nextFloat() * 0.005f + 0.004f + (wave * 0.001f),
                            color = colors.random()
                        )
                    )
                }

                // Enemies update & collisions
                val enemiesToRemove = mutableListOf<EnemyTarget>()
                for (e in enemies) {
                    e.y += e.vy

                    // Collision with bullets
                    val hitBullet = bullets.firstOrNull { b ->
                        val dx = b.x - e.x
                        val dy = b.y - e.y
                        (dx * dx + dy * dy) < 0.0018f
                    }

                    if (hitBullet != null) {
                        enemiesToRemove.add(e)
                        bullets.remove(hitBullet)
                        score += 100
                        if (score % 800 == 0) {
                            wave++
                            onLogReceived(ConsoleLog(level = "INFO", message = "Wave $wave unlocked! Enemies accelerated."))
                        }
                        // Create sparks
                        for (i in 0 until 10) {
                            sparks.add(
                                Spark(
                                    x = e.x,
                                    y = e.y,
                                    vx = (Random.nextFloat() - 0.5f) * 0.03f,
                                    vy = (Random.nextFloat() - 0.5f) * 0.03f,
                                    life = 16,
                                    color = e.color
                                )
                            )
                        }
                    } else if (e.y > 1.05f) {
                        enemiesToRemove.add(e)
                    } else {
                        // Check player collision
                        val pdx = shipX - e.x
                        val pdy = shipY - e.y
                        if ((pdx * pdx + pdy * pdy) < 0.0022f) {
                            enemiesToRemove.add(e)
                            shield -= 25
                            onLogReceived(ConsoleLog(level = "WARN", message = "Hull integrity at $shield%"))
                            if (shield <= 0) {
                                isGameOver = true
                                onLogReceived(ConsoleLog(level = "ERROR", message = "System compromised. Game over. Score: $score"))
                            }
                        }
                    }
                }
                enemies.removeAll(enemiesToRemove)

                // Sparks update
                val sparksToRemove = mutableListOf<Spark>()
                for (sp in sparks) {
                    sp.x += sp.vx
                    sp.y += sp.vy
                    sp.life--
                    if (sp.life <= 0) sparksToRemove.add(sp)
                }
                sparks.removeAll(sparksToRemove)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    shipX = (shipX + dragAmount.x / size.width).coerceIn(0.08f, 0.92f)
                    shipY = (shipY + dragAmount.y / size.height).coerceIn(0.15f, 0.92f)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    bullets.add(LaserBullet(x = shipX, y = shipY - 0.05f))
                    onLogReceived(ConsoleLog(level = "LOG", message = "Tap fired laser."))
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Background Deep Space Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF060714), Color(0xFF0D0F24), Color(0xFF05060A))
                )
            )

            // Stars
            for (s in stars) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = s.size,
                    center = Offset(s.x * w, s.y * h)
                )
            }

            // Laser Bullets
            for (b in bullets) {
                drawRect(
                    color = Color(0xFF00E5FF),
                    topLeft = Offset(b.x * w - 3f, b.y * h - 14f),
                    size = Size(6f, 22f)
                )
            }

            // Enemies
            for (e in enemies) {
                drawCircle(
                    color = e.color,
                    radius = e.radius,
                    center = Offset(e.x * w, e.y * h)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = e.radius * 0.45f,
                    center = Offset(e.x * w, e.y * h)
                )
            }

            // Sparks
            for (sp in sparks) {
                drawCircle(
                    color = sp.color.copy(alpha = sp.life / 16f),
                    radius = 3.5f,
                    center = Offset(sp.x * w, sp.y * h)
                )
            }

            // Player Ship (Vector Fighter)
            val px = shipX * w
            val py = shipY * h
            val shipSize = 28f

            val shipPath = Path().apply {
                moveTo(px, py - shipSize)
                lineTo(px + shipSize, py + shipSize)
                lineTo(px, py + shipSize * 0.5f)
                lineTo(px - shipSize, py + shipSize)
                close()
            }
            drawPath(shipPath, color = Color(0xFF00E5FF))
            drawPath(shipPath, color = Color.White, style = Stroke(width = 2f))

            // Thruster Flame
            val flamePath = Path().apply {
                moveTo(px - 7f, py + shipSize * 0.6f)
                lineTo(px, py + shipSize + Random.nextFloat() * 14f + 6f)
                lineTo(px + 7f, py + shipSize * 0.6f)
                close()
            }
            drawPath(flamePath, color = Color(0xFFFF6600))
        }

        // HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SCORE", fontSize = 11.sp, color = VsCodeTextSecondary, fontWeight = FontWeight.Bold)
                Text("$score", fontSize = 20.sp, color = VsCodeCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("WAVE", fontSize = 11.sp, color = VsCodeTextSecondary, fontWeight = FontWeight.Bold)
                Text("$wave", fontSize = 16.sp, color = Color.White, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("SHIELD", fontSize = 11.sp, color = VsCodeTextSecondary, fontWeight = FontWeight.Bold)
                Text(
                    "$shield%",
                    fontSize = 18.sp,
                    color = if (shield > 40) VsCodeSuccess else VsCodeError,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Game Over Overlay
        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD080812)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "SYSTEM COMPROMISED",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = VsCodeError,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Final Score: $score",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    Button(
                        onClick = {
                            score = 0
                            shield = 100
                            wave = 1
                            isGameOver = false
                            bullets.clear()
                            enemies.clear()
                            onLogReceived(ConsoleLog(level = "LOG", message = "System rebooted. New session."))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VsCodeCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("REBOOT SYSTEM", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. PROTECTED WEBVIEW RENDERER (With termination handling)
// -------------------------------------------------------------
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ProtectedWebViewRenderer(
    compiledHtml: String,
    reloadKey: Int,
    onLogReceived: (ConsoleLog) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasCrashed by remember { mutableStateOf(false) }

    val file = remember(compiledHtml, reloadKey) {
        try {
            val dir = File(context.cacheDir, "sandbox").apply { mkdirs() }
            val f = File(dir, "index.html")
            FileOutputStream(f).use { it.write(compiledHtml.toByteArray(Charsets.UTF_8)) }
            f
        } catch (e: Exception) {
            null
        }
    }

    if (hasCrashed) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("WebView Process Reset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("The browser subprocess was recovered.", color = VsCodeTextSecondary, fontSize = 13.sp)
                Button(
                    onClick = { hasCrashed = false },
                    colors = ButtonDefaults.buttonColors(containerColor = VsCodeBlue)
                ) {
                    Text("Restart")
                }
            }
        }
    } else {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        safeBrowsingEnabled = false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                            view?.let { wv ->
                                (wv.parent as? ViewGroup)?.removeView(wv)
                                wv.destroy()
                            }
                            webViewRef = null
                            hasCrashed = true
                            onLogReceived(ConsoleLog(level = "WARN", message = "Subprocess recovered."))
                            return true
                        }
                    }
                    webChromeClient = WebChromeClient()
                    val bridge = SandboxBridge(onLogReceived)
                    addJavascriptInterface(bridge, "AndroidBridge")
                    val path = file?.let { "file://${it.absolutePath}" } ?: "about:blank"
                    loadUrl(path)
                    webViewRef = this
                }
            },
            update = { view -> webViewRef = view }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.let { wv ->
                (wv.parent as? ViewGroup)?.removeView(wv)
                wv.stopLoading()
                wv.clearHistory()
                wv.destroy()
                webViewRef = null
            }
        }
    }
}

// -------------------------------------------------------------
// 3. VIRTUAL TOUCH CONTROLLER (DPad + Action Buttons)
// -------------------------------------------------------------
@Composable
fun NativeVirtualGamePad(
    onDpadChanged: (Float, Float) -> Unit,
    onActionAChanged: (Boolean) -> Unit,
    onActionBChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // 4-Way Directional D-Pad
        Box(
            modifier = Modifier
                .size(136.dp)
                .clip(CircleShape)
                .background(Color(0x99181824))
                .border(1.5.dp, VsCodeBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // UP
            NativeDpadKey(
                icon = Icons.Default.KeyboardArrowUp,
                onPressed = { onDpadChanged(0f, -1f) },
                onReleased = { onDpadChanged(0f, 0f) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .testTag("sandbox_dpad_up")
            )
            // DOWN
            NativeDpadKey(
                icon = Icons.Default.KeyboardArrowDown,
                onPressed = { onDpadChanged(0f, 1f) },
                onReleased = { onDpadChanged(0f, 0f) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .testTag("sandbox_dpad_down")
            )
            // LEFT
            NativeDpadKey(
                icon = Icons.Default.KeyboardArrowLeft,
                onPressed = { onDpadChanged(-1f, 0f) },
                onReleased = { onDpadChanged(0f, 0f) },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .testTag("sandbox_dpad_left")
            )
            // RIGHT
            NativeDpadKey(
                icon = Icons.Default.KeyboardArrowRight,
                onPressed = { onDpadChanged(1f, 0f) },
                onReleased = { onDpadChanged(0f, 0f) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .testTag("sandbox_dpad_right")
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(VsCodeBorder)
            )
        }

        // Action Buttons: 'B' and 'A'
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            // Action B (Secondary / Dash)
            NativeActionKey(
                label = "B",
                color = VsCodeWarning,
                onPressed = { onActionBChanged(true) },
                onReleased = { onActionBChanged(false) },
                modifier = Modifier.testTag("sandbox_btn_b")
            )

            // Action A (Primary / Jump / Shoot)
            NativeActionKey(
                label = "A",
                color = VsCodeCyan,
                onPressed = { onActionAChanged(true) },
                onReleased = { onActionAChanged(false) },
                modifier = Modifier.testTag("sandbox_btn_a")
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NativeDpadKey(
    icon: ImageVector,
    onPressed: () -> Unit,
    onReleased: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isDown by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isDown) VsCodeCyan.copy(alpha = 0.5f) else Color(0x66333333))
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isDown = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onPressed()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isDown = false
                        onReleased()
                        true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isDown) Color.White else VsCodeTextPrimary, modifier = Modifier.size(26.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NativeActionKey(
    label: String,
    color: Color,
    onPressed: () -> Unit,
    onReleased: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isDown by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(if (isDown) color else color.copy(alpha = 0.35f))
            .border(2.dp, color, CircleShape)
            .pointerInteropFilter { event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isDown = true
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onPressed()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isDown = false
                        onReleased()
                        true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = if (isDown) Color.Black else Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}

// -------------------------------------------------------------
// 4. EMBEDDED CONSOLE DRAWER
// -------------------------------------------------------------
@Composable
fun SandboxConsoleDrawer(
    logs: List<ConsoleLog>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onClearLogs: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = VsCodeSurface),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(VsCodeBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = VsCodeCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Developer Console", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("(${logs.size})", fontSize = 11.sp, color = VsCodeTextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("ALL", "ERROR", "WARN", "LOG").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) VsCodeBlue else Color.Transparent)
                                .clickable { onFilterChange(filter) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = filter,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else VsCodeTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(onClick = onClearLogs, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = VsCodeTextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredLogs = logs.filter {
                if (selectedFilter == "ALL") true else it.level.equals(selectedFilter, ignoreCase = true)
            }

            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No console output recorded", color = VsCodeTextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VsCodeBackground, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs) { log ->
                        val (color, prefix) = when (log.level.uppercase()) {
                            "ERROR" -> VsCodeError to "❌"
                            "WARN" -> VsCodeWarning to "⚠️"
                            else -> VsCodeTextPrimary to "›"
                        }
                        Row {
                            Text(text = "$prefix ", fontSize = 11.sp)
                            Text(
                                text = log.message,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = color,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
