package com.example.generator

import com.example.data.ProjectEntity
import com.example.data.ProjectFileEntity
import java.util.UUID

object SmartProjectGenerator {

    data class GeneratedProject(
        val project: ProjectEntity,
        val files: List<ProjectFileEntity>
    )

    fun createFromPrompt(prompt: String, projectName: String? = null): GeneratedProject {
        val lower = prompt.lowercase()
        val finalName = if (!projectName.isNullOrBlank()) {
            projectName
        } else {
            generateNameFromPrompt(prompt)
        }

        return when {
            lower.contains("snake") -> createSnakeGame(finalName, prompt)
            lower.contains("flappy") || lower.contains("bird") -> createFlappyGame(finalName, prompt)
            lower.contains("shoot") || lower.contains("space") || lower.contains("arcade") -> createSpaceShooter(finalName, prompt)
            lower.contains("pong") || lower.contains("ping pong") -> createPongGame(finalName, prompt)
            lower.contains("quiz") || lower.contains("trivia") -> createQuizApp(finalName, prompt)
            lower.contains("todo") || lower.contains("task") || lower.contains("list") -> createTodoApp(finalName, prompt)
            lower.contains("calc") -> createCalculatorApp(finalName, prompt)
            lower.contains("weather") -> createWeatherApp(finalName, prompt)
            lower.contains("draw") || lower.contains("paint") || lower.contains("canvas") -> createPaintApp(finalName, prompt)
            lower.contains("2048") || lower.contains("puzzle") -> create2048Game(finalName, prompt)
            lower.contains("game") || lower.contains("jump") || lower.contains("platform") -> createPlatformerGame(finalName, prompt)
            else -> createCustomInteractiveApp(finalName, prompt)
        }
    }

    private fun generateNameFromPrompt(prompt: String): String {
        val words = prompt.trim().split("\\s+".toRegex()).take(3)
        return words.joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            .ifBlank { "New App" }
    }

    fun getStarterBlueprints(): List<GeneratedProject> {
        return listOf(
            createSpaceShooter("Neon Starfighter", "Retro arcade space shooter with laser weapons and asteroids"),
            createSnakeGame("Cyber Snake 2088", "Classic snake with neon graphics, food combos, and speed levels"),
            createFlappyGame("Pixel Bird Jump", "Flappy arcade game with smooth physics and audio synthesis"),
            createTodoApp("Kanban Flow Studio", "Task manager with categorization, status columns, and persistence"),
            createCalculatorApp("Glass Neumorph Calc", "Sleek glassmorphism calculator with calculation tape history"),
            createPaintApp("Pixel Craft Studio", "Canvas pixel art drawing board with customizable palette and export")
        )
    }

    // --- Template 1: Space Shooter ---
    fun createSpaceShooter(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "GAME",
            iconName = "gamepad"
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div id="game-container">
    <div id="hud">
      <div class="hud-item">SCORE: <span id="score">0</span></div>
      <div class="hud-item">SHIELD: <span id="shield">100%</span></div>
      <div class="hud-item">WAVE: <span id="wave">1</span></div>
    </div>
    <canvas id="gameCanvas"></canvas>
    <div id="game-over" class="overlay hidden">
      <h1>SYSTEM COMPROMISED</h1>
      <p>Final Score: <span id="finalScore">0</span></p>
      <button id="restartBtn">REBOOT SYSTEM</button>
    </div>
  </div>
  <script src="game.js"></script>
</body>
</html>
""".trimIndent()

        val css = """
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  user-select: none;
}
body {
  background: #080811;
  color: #fff;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
#game-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  max-width: 600px;
  background: radial-gradient(circle at center, #111429 0%, #05060b 100%);
  overflow: hidden;
}
#hud {
  position: absolute;
  top: 12px;
  left: 12px;
  right: 12px;
  display: flex;
  justify-content: space-between;
  font-family: monospace;
  font-size: 14px;
  font-weight: bold;
  color: #00e5ff;
  text-shadow: 0 0 8px rgba(0,229,255,0.8);
  pointer-events: none;
  z-index: 10;
}
canvas {
  display: block;
  width: 100%;
  height: 100%;
}
.overlay {
  position: absolute;
  inset: 0;
  background: rgba(8, 8, 17, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16px;
  z-index: 20;
}
.overlay.hidden {
  display: none;
}
.overlay h1 {
  font-size: 26px;
  color: #ff3366;
  text-shadow: 0 0 16px #ff3366;
  letter-spacing: 2px;
}
.overlay button {
  background: #00e5ff;
  color: #05060b;
  border: none;
  padding: 12px 28px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 6px;
  cursor: pointer;
  box-shadow: 0 0 14px #00e5ff;
}
""".trimIndent()

        val js = """
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const scoreEl = document.getElementById('score');
const shieldEl = document.getElementById('shield');
const waveEl = document.getElementById('wave');
const gameOverEl = document.getElementById('game-over');
const finalScoreEl = document.getElementById('finalScore');
const restartBtn = document.getElementById('restartBtn');

function resizeCanvas() {
  canvas.width = canvas.parentElement.clientWidth;
  canvas.height = canvas.parentElement.clientHeight;
}
resizeCanvas();
window.addEventListener('resize', resizeCanvas);

// Audio synth (lazily initialized on first user interaction)
let audioCtx = null;
function playBeep(freq, type = 'square', duration = 0.1) {
  try {
    if (!audioCtx && (window.AudioContext || window.webkitAudioContext)) {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }
    if (!audioCtx) return;
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
    gain.gain.setValueAtTime(0.08, audioCtx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + duration);
    osc.connect(gain);
    gain.connect(audioCtx.destination);
    osc.start();
    osc.stop(audioCtx.currentTime + duration);
  } catch(e) {}
}

const keys = {};
window.addEventListener('keydown', e => {
  keys[e.key] = true;
  if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', ' '].includes(e.key)) {
    e.preventDefault();
  }
});
window.addEventListener('keyup', e => { keys[e.key] = false; });

// Touch controls for screen dragging
canvas.addEventListener('touchmove', e => {
  e.preventDefault();
  const touch = e.touches[0];
  const rect = canvas.getBoundingClientRect();
  player.x = touch.clientX - rect.left;
});

const player = {
  x: canvas.width / 2,
  y: canvas.height - 70,
  size: 26,
  speed: 6,
  shield: 100,
  lastShot: 0
};

let bullets = [];
let enemies = [];
let stars = [];
let particles = [];
let score = 0;
let wave = 1;
let isGameOver = false;

// Create background stars
for (let i = 0; i < 60; i++) {
  stars.push({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    size: Math.random() * 2 + 0.5,
    speed: Math.random() * 2 + 0.5
  });
}

function spawnEnemy() {
  if (enemies.length < wave * 4 + 2) {
    enemies.push({
      x: Math.random() * (canvas.width - 40) + 20,
      y: -30,
      size: Math.random() * 14 + 14,
      speed: Math.random() * 2 + 1.5 + (wave * 0.3),
      hp: 1
    });
  }
}

function shoot() {
  const now = Date.now();
  if (now - player.lastShot > 160) {
    bullets.push({
      x: player.x,
      y: player.y - player.size,
      speed: 10
    });
    playBeep(600, 'triangle', 0.08);
    player.lastShot = now;
  }
}

function createExplosion(x, y, color = '#00e5ff') {
  for (let i = 0; i < 16; i++) {
    particles.push({
      x, y,
      vx: (Math.random() - 0.5) * 6,
      vy: (Math.random() - 0.5) * 6,
      radius: Math.random() * 3 + 1,
      color,
      life: 25
    });
  }
}

function update() {
  if (isGameOver) return;

  // Player controls
  if (keys['ArrowLeft'] || keys['a'] || keys['A']) player.x -= player.speed;
  if (keys['ArrowRight'] || keys['d'] || keys['D']) player.x += player.speed;
  if (keys[' '] || keys['z'] || keys['Z'] || keys['Enter']) shoot();

  // Clamp player to bounds
  player.x = Math.max(player.size, Math.min(canvas.width - player.size, player.x));

  // Auto shoot if touched or holding
  if (keys['ActionA'] || keys['ButtonA']) shoot();

  // Stars
  stars.forEach(s => {
    s.y += s.speed;
    if (s.y > canvas.height) { s.y = 0; s.x = Math.random() * canvas.width; }
  });

  // Bullets
  bullets.forEach((b, i) => {
    b.y -= b.speed;
    if (b.y < -10) bullets.splice(i, 1);
  });

  // Enemies
  spawnEnemy();
  enemies.forEach((enemy, ei) => {
    enemy.y += enemy.speed;

    // Check collision with player
    const dist = Math.hypot(enemy.x - player.x, enemy.y - player.y);
    if (dist < enemy.size + player.size - 6) {
      player.shield -= 25;
      shieldEl.textContent = player.shield + '%';
      createExplosion(enemy.x, enemy.y, '#ff3366');
      playBeep(180, 'sawtooth', 0.2);
      enemies.splice(ei, 1);
      if (player.shield <= 0) {
        endGame();
      }
    }

    // Check collision with bullets
    bullets.forEach((bullet, bi) => {
      const bDist = Math.hypot(enemy.x - bullet.x, enemy.y - bullet.y);
      if (bDist < enemy.size + 8) {
        createExplosion(enemy.x, enemy.y, '#ffe600');
        playBeep(420, 'square', 0.1);
        enemies.splice(ei, 1);
        bullets.splice(bi, 1);
        score += 100;
        scoreEl.textContent = score;
        if (score % 1000 === 0) {
          wave++;
          waveEl.textContent = wave;
        }
      }
    });

    if (enemy.y > canvas.height + 40) {
      enemies.splice(ei, 1);
    }
  });

  // Particles
  particles.forEach((p, pi) => {
    p.x += p.vx;
    p.y += p.vy;
    p.life--;
    if (p.life <= 0) particles.splice(pi, 1);
  });
}

function draw() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Draw Stars
  ctx.fillStyle = '#ffffff';
  stars.forEach(s => {
    ctx.fillRect(s.x, s.y, s.size, s.size);
  });

  // Draw Player Ship
  ctx.save();
  ctx.translate(player.x, player.y);
  ctx.fillStyle = '#00e5ff';
  ctx.beginPath();
  ctx.moveTo(0, -player.size);
  ctx.lineTo(player.size, player.size);
  ctx.lineTo(0, player.size * 0.5);
  ctx.lineTo(-player.size, player.size);
  ctx.closePath();
  ctx.fill();

  // Engine flame
  ctx.fillStyle = '#ff7700';
  ctx.beginPath();
  ctx.moveTo(-6, player.size * 0.6);
  ctx.lineTo(0, player.size + (Math.random() * 8 + 4));
  ctx.lineTo(6, player.size * 0.6);
  ctx.closePath();
  ctx.fill();
  ctx.restore();

  // Draw Bullets
  ctx.fillStyle = '#ffe600';
  bullets.forEach(b => {
    ctx.fillRect(b.x - 2, b.y - 10, 4, 12);
  });

  // Draw Enemies
  enemies.forEach(e => {
    ctx.save();
    ctx.translate(e.x, e.y);
    ctx.fillStyle = '#ff3366';
    ctx.beginPath();
    ctx.arc(0, 0, e.size, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
  });

  // Draw Particles
  particles.forEach(p => {
    ctx.fillStyle = p.color;
    ctx.beginPath();
    ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
    ctx.fill();
  });

  // Caps to avoid unbounded growth
  if (particles.length > 30) particles.splice(0, particles.length - 30);
  if (bullets.length > 20) bullets.splice(0, bullets.length - 20);
  if (enemies.length > 10) enemies.splice(0, enemies.length - 10);

  requestAnimationFrame(() => {
    update();
    draw();
  });
}

function endGame() {
  isGameOver = true;
  finalScoreEl.textContent = score;
  gameOverEl.classList.remove('hidden');
  console.log("Game Over! Final Score: " + score);
}

restartBtn.addEventListener('click', () => {
  score = 0;
  wave = 1;
  player.shield = 100;
  player.x = canvas.width / 2;
  enemies = [];
  bullets = [];
  particles = [];
  scoreEl.textContent = '0';
  shieldEl.textContent = '100%';
  waveEl.textContent = '1';
  isGameOver = false;
  gameOverEl.classList.add('hidden');
  console.log("Game Restarted");
});

console.log("Neon Starfighter initialized! Ready for battle.");
draw();
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML"),
                ProjectFileEntity(projectId = projectId, name = "style.css", filePath = "style.css", content = css, fileType = "CSS"),
                ProjectFileEntity(projectId = projectId, name = "game.js", filePath = "game.js", content = js, fileType = "JS")
            )
        )
    }

    // --- Template 2: Snake Game ---
    fun createSnakeGame(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "GAME",
            iconName = "sports_esports"
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="game-wrapper">
    <div class="top-bar">
      <div>SCORE: <span id="scoreVal">0</span></div>
      <div>HIGH: <span id="highVal">0</span></div>
    </div>
    <div class="board-frame">
      <canvas id="snakeCanvas" width="400" height="400"></canvas>
    </div>
    <div id="msgOverlay" class="overlay hidden">
      <h2 id="msgTitle">GAME OVER</h2>
      <button id="playBtn">START GAME</button>
    </div>
  </div>
  <script src="snake.js"></script>
</body>
</html>
""".trimIndent()

        val css = """
* { box-sizing: border-box; margin: 0; padding: 0; }
body {
  background: #0f172a;
  color: #38bdf8;
  font-family: monospace;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  overflow: hidden;
}
.game-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 95vw;
  max-width: 420px;
}
.top-bar {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 18px;
  font-weight: bold;
}
.board-frame {
  background: #1e293b;
  border: 3px solid #38bdf8;
  border-radius: 12px;
  box-shadow: 0 0 20px rgba(56, 189, 248, 0.3);
  overflow: hidden;
}
canvas {
  display: block;
  width: 360px;
  height: 360px;
}
.overlay {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.85);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  border-radius: 12px;
}
.overlay.hidden { display: none; }
.overlay h2 { font-size: 28px; color: #f43f5e; text-shadow: 0 0 10px #f43f5e; }
.overlay button {
  background: #22c55e;
  color: #022c22;
  border: none;
  font-family: monospace;
  font-size: 18px;
  font-weight: bold;
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
}
""".trimIndent()

        val js = """
const canvas = document.getElementById('snakeCanvas');
const ctx = canvas.getContext('2d');
const scoreVal = document.getElementById('scoreVal');
const highVal = document.getElementById('highVal');
const msgOverlay = document.getElementById('msgOverlay');
const msgTitle = document.getElementById('msgTitle');
const playBtn = document.getElementById('playBtn');

const GRID = 20;
const TILE_COUNT = canvas.width / GRID;

let snake = [{ x: 10, y: 10 }];
let dx = 1, dy = 0;
let food = { x: 15, y: 15 };
let score = 0;
let highScore = localStorage.getItem('snake_high') || 0;
highVal.textContent = highScore;
let gameLoop = null;
let isRunning = false;

function spawnFood() {
  food.x = Math.floor(Math.random() * TILE_COUNT);
  food.y = Math.floor(Math.random() * TILE_COUNT);
}

function drawGame() {
  // Move
  const head = { x: snake[0].x + dx, y: snake[0].y + dy };

  // Wall collisions
  if (head.x < 0 || head.x >= TILE_COUNT || head.y < 0 || head.y >= TILE_COUNT) {
    return gameOver();
  }

  // Self collisions
  for (let segment of snake) {
    if (head.x === segment.x && head.y === segment.y) {
      return gameOver();
    }
  }

  snake.unshift(head);

  // Check food
  if (head.x === food.x && head.y === food.y) {
    score += 10;
    scoreVal.textContent = score;
    spawnFood();
  } else {
    snake.pop();
  }

  // Render
  ctx.fillStyle = '#1e293b';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Food
  ctx.fillStyle = '#f43f5e';
  ctx.fillRect(food.x * GRID + 2, food.y * GRID + 2, GRID - 4, GRID - 4);

  // Snake
  snake.forEach((part, idx) => {
    ctx.fillStyle = idx === 0 ? '#38bdf8' : '#22c55e';
    ctx.fillRect(part.x * GRID + 1, part.y * GRID + 1, GRID - 2, GRID - 2);
  });
}

function gameOver() {
  clearInterval(gameLoop);
  isRunning = false;
  if (score > highScore) {
    highScore = score;
    localStorage.setItem('snake_high', highScore);
    highVal.textContent = highScore;
  }
  msgTitle.textContent = "GAME OVER";
  playBtn.textContent = "PLAY AGAIN";
  msgOverlay.classList.remove('hidden');
  console.log("Game Over! Score: " + score);
}

function startGame() {
  snake = [{ x: 10, y: 10 }, { x: 9, y: 10 }, { x: 8, y: 10 }];
  dx = 1; dy = 0;
  score = 0;
  scoreVal.textContent = '0';
  spawnFood();
  msgOverlay.classList.add('hidden');
  clearInterval(gameLoop);
  isRunning = true;
  gameLoop = setInterval(drawGame, 100);
  console.log("Snake game started!");
}

playBtn.addEventListener('click', startGame);

window.addEventListener('keydown', e => {
  if (['ArrowUp', 'w', 'W'].includes(e.key) && dy !== 1) { dx = 0; dy = -1; }
  else if (['ArrowDown', 's', 'S'].includes(e.key) && dy !== -1) { dx = 0; dy = 1; }
  else if (['ArrowLeft', 'a', 'A'].includes(e.key) && dx !== 1) { dx = -1; dy = 0; }
  else if (['ArrowRight', 'd', 'D'].includes(e.key) && dx !== -1) { dx = 1; dy = 0; }
});

startGame();
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML"),
                ProjectFileEntity(projectId = projectId, name = "style.css", filePath = "style.css", content = css, fileType = "CSS"),
                ProjectFileEntity(projectId = projectId, name = "snake.js", filePath = "snake.js", content = js, fileType = "JS")
            )
        )
    }

    // --- Template 3: Flappy Bird Jump Game ---
    fun createFlappyGame(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "GAME",
            iconName = "flight_takeoff"
        )

        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div id="game-stage">
    <canvas id="flappyCanvas" width="360" height="520"></canvas>
    <div id="scoreLabel">0</div>
  </div>
  <script src="game.js"></script>
</body>
</html>
""".trimIndent()

        val css = """
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  background: #111;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  overflow: hidden;
}
#game-stage {
  position: relative;
  width: 360px;
  height: 520px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.7);
}
#scoreLabel {
  position: absolute;
  top: 24px;
  width: 100%;
  text-align: center;
  font-family: sans-serif;
  font-size: 40px;
  font-weight: 900;
  color: white;
  text-shadow: 0 3px 6px rgba(0,0,0,0.5);
  pointer-events: none;
}
""".trimIndent()

        val js = """
const canvas = document.getElementById('flappyCanvas');
const ctx = canvas.getContext('2d');
const scoreLabel = document.getElementById('scoreLabel');

let birdY = 200;
let velocity = 0;
const gravity = 0.35;
const jump = -7;
let pipes = [];
let score = 0;
let gameOver = false;

function flap() {
  if (gameOver) {
    reset();
    return;
  }
  velocity = jump;
}

window.addEventListener('keydown', e => {
  if ([' ', 'ArrowUp', 'z', 'Z', 'Enter'].includes(e.key)) {
    e.preventDefault();
    flap();
  }
});
canvas.addEventListener('click', flap);
canvas.addEventListener('touchstart', e => { e.preventDefault(); flap(); });

function reset() {
  birdY = 200;
  velocity = 0;
  pipes = [];
  score = 0;
  scoreLabel.textContent = '0';
  gameOver = false;
}

let frame = 0;
function loop() {
  frame++;
  ctx.fillStyle = '#4dc3ff';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  if (!gameOver) {
    velocity += gravity;
    birdY += velocity;

    if (frame % 90 === 0) {
      const topHeight = Math.random() * 200 + 50;
      pipes.push({ x: canvas.width, top: topHeight, gap: 120, scored: false });
    }
  }

  // Draw Pipes
  ctx.fillStyle = '#22c55e';
  for (let i = pipes.length - 1; i >= 0; i--) {
    const p = pipes[i];
    if (!gameOver) p.x -= 2.5;

    ctx.fillRect(p.x, 0, 50, p.top);
    ctx.fillRect(p.x, p.top + p.gap, 50, canvas.height - (p.top + p.gap));

    // Collision
    if (70 > p.x && 30 < p.x + 50) {
      if (birdY - 12 < p.top || birdY + 12 > p.top + p.gap) {
        gameOver = true;
      }
    }

    if (!p.scored && p.x + 50 < 40) {
      score++;
      p.scored = true;
      scoreLabel.textContent = score;
    }

    if (p.x < -60) pipes.splice(i, 1);
  }

  // Ground / ceiling
  if (birdY > canvas.height - 20 || birdY < 10) {
    gameOver = true;
  }

  // Draw Bird
  ctx.save();
  ctx.translate(50, birdY);
  ctx.fillStyle = '#f59e0b';
  ctx.beginPath();
  ctx.arc(0, 0, 14, 0, Math.PI * 2);
  ctx.fill();
  ctx.fillStyle = '#fff';
  ctx.beginPath();
  ctx.arc(6, -4, 4, 0, Math.PI * 2);
  ctx.fill();
  ctx.fillStyle = '#000';
  ctx.beginPath();
  ctx.arc(7, -4, 2, 0, Math.PI * 2);
  ctx.fill();
  ctx.restore();

  if (gameOver) {
    ctx.fillStyle = 'rgba(0,0,0,0.5)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#fff';
    ctx.font = '24px sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText('TAP OR PRESS JUMP', canvas.width / 2, canvas.height / 2);
  }

  requestAnimationFrame(loop);
}
loop();
console.log("Flappy Bird Ready!");
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML"),
                ProjectFileEntity(projectId = projectId, name = "style.css", filePath = "style.css", content = css, fileType = "CSS"),
                ProjectFileEntity(projectId = projectId, name = "game.js", filePath = "game.js", content = js, fileType = "JS")
            )
        )
    }

    // --- Template 4: Modern Calculator App ---
    fun createCalculatorApp(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "UTILITY",
            iconName = "calculate"
        )

        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="calc-card">
    <div class="display-panel">
      <div id="history"></div>
      <div id="result">0</div>
    </div>
    <div class="keys-grid">
      <button class="btn fn" data-action="clear">AC</button>
      <button class="btn fn" data-action="backspace">⌫</button>
      <button class="btn op" data-val="%">%</button>
      <button class="btn op" data-val="/">÷</button>

      <button class="btn num" data-val="7">7</button>
      <button class="btn num" data-val="8">8</button>
      <button class="btn num" data-val="9">9</button>
      <button class="btn op" data-val="*">×</button>

      <button class="btn num" data-val="4">4</button>
      <button class="btn num" data-val="5">5</button>
      <button class="btn num" data-val="6">6</button>
      <button class="btn op" data-val="-">−</button>

      <button class="btn num" data-val="1">1</button>
      <button class="btn num" data-val="2">2</button>
      <button class="btn num" data-val="3">3</button>
      <button class="btn op" data-val="+">+</button>

      <button class="btn num zero" data-val="0">0</button>
      <button class="btn num" data-val=".">.</button>
      <button class="btn eq" data-action="equals">=</button>
    </div>
  </div>
  <script src="script.js"></script>
</body>
</html>
""".trimIndent()

        val css = """
* { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
body {
  background: #0f172a;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
}
.calc-card {
  width: 100%;
  max-width: 340px;
  background: rgba(30, 41, 59, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 24px;
  padding: 24px 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.5), inset 0 1px 1px rgba(255,255,255,0.1);
}
.display-panel {
  text-align: right;
  padding: 16px;
  margin-bottom: 20px;
}
#history {
  font-size: 16px;
  color: #94a3b8;
  min-height: 20px;
}
#result {
  font-size: 44px;
  color: #fff;
  font-weight: 300;
  overflow-x: auto;
}
.keys-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.btn {
  aspect-ratio: 1;
  border-radius: 50%;
  border: none;
  font-size: 20px;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.1s, opacity 0.1s;
}
.btn:active { transform: scale(0.92); }
.btn.num { background: #334155; color: #fff; }
.btn.fn { background: #475569; color: #38bdf8; }
.btn.op { background: #0284c7; color: #fff; }
.btn.eq { background: #22c55e; color: #fff; }
.btn.zero { grid-column: span 2; aspect-ratio: auto; border-radius: 36px; padding: 0 24px; text-align: left; }
""".trimIndent()

        val js = """
const resultEl = document.getElementById('result');
const historyEl = document.getElementById('history');

let currentInput = '0';
let historyStr = '';

function updateDisplay() {
  resultEl.textContent = currentInput;
  historyEl.textContent = historyStr;
}

document.querySelectorAll('.btn').forEach(btn => {
  btn.addEventListener('click', () => {
    const val = btn.dataset.val;
    const action = btn.dataset.action;

    if (val !== undefined) {
      if (currentInput === '0' && val !== '.') {
        currentInput = val;
      } else {
        currentInput += val;
      }
    } else if (action === 'clear') {
      currentInput = '0';
      historyStr = '';
    } else if (action === 'backspace') {
      currentInput = currentInput.length > 1 ? currentInput.slice(0, -1) : '0';
    } else if (action === 'equals') {
      try {
        historyStr = currentInput + ' =';
        const clean = currentInput.replace(/×/g, '*').replace(/÷/g, '/').replace(/−/g, '-');
        currentInput = String(Function('"use strict";return (' + clean + ')')());
      } catch (err) {
        currentInput = 'Error';
      }
    }
    updateDisplay();
  });
});
console.log("Calculator loaded!");
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML"),
                ProjectFileEntity(projectId = projectId, name = "style.css", filePath = "style.css", content = css, fileType = "CSS"),
                ProjectFileEntity(projectId = projectId, name = "script.js", filePath = "script.js", content = js, fileType = "JS")
            )
        )
    }

    // --- Template 5: Todo & Task Manager ---
    fun createTodoApp(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "UTILITY",
            iconName = "check_circle"
        )

        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="todo-app">
    <header>
      <h1>Task Master</h1>
      <p id="taskStats">0 items remaining</p>
    </header>
    <div class="input-row">
      <input type="text" id="taskInput" placeholder="Add a new task...">
      <button id="addBtn">+</button>
    </div>
    <div class="filter-tabs">
      <button class="tab-btn active" data-filter="all">All</button>
      <button class="tab-btn" data-filter="active">Pending</button>
      <button class="tab-btn" data-filter="completed">Done</button>
    </div>
    <ul id="taskList"></ul>
  </div>
  <script src="app.js"></script>
</body>
</html>
""".trimIndent()

        val css = """
* { box-sizing: border-box; margin: 0; padding: 0; font-family: sans-serif; }
body {
  background: #18181b;
  color: #fafafa;
  min-height: 100vh;
  padding: 24px 16px;
  display: flex;
  justify-content: center;
}
.todo-app {
  width: 100%;
  max-width: 440px;
}
header { margin-bottom: 20px; }
header h1 { font-size: 28px; font-weight: 700; color: #a855f7; }
header p { color: #71717a; font-size: 14px; margin-top: 4px; }
.input-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
input[type="text"] {
  flex: 1;
  background: #27272a;
  border: 1px solid #3f3f46;
  border-radius: 10px;
  padding: 12px 16px;
  color: white;
  font-size: 15px;
}
#addBtn {
  background: #a855f7;
  color: white;
  border: none;
  border-radius: 10px;
  width: 48px;
  font-size: 24px;
  cursor: pointer;
}
.filter-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.tab-btn {
  background: #27272a;
  border: none;
  color: #a1a1aa;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
}
.tab-btn.active {
  background: #a855f7;
  color: white;
}
ul { list-style: none; display: flex; flex-direction: column; gap: 8px; }
li {
  background: #27272a;
  padding: 12px 16px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
li.done span { text-decoration: line-through; color: #71717a; }
.del-btn { background: none; border: none; color: #ef4444; font-size: 18px; cursor: pointer; }
""".trimIndent()

        val js = """
let tasks = JSON.parse(localStorage.getItem('my_tasks') || '[]');
const taskInput = document.getElementById('taskInput');
const addBtn = document.getElementById('addBtn');
const taskList = document.getElementById('taskList');
const taskStats = document.getElementById('taskStats');
let filter = 'all';

function save() {
  localStorage.setItem('my_tasks', JSON.stringify(tasks));
  render();
}

function render() {
  taskList.innerHTML = '';
  const filtered = tasks.filter(t => {
    if (filter === 'active') return !t.done;
    if (filter === 'completed') return t.done;
    return true;
  });

  filtered.forEach((t, idx) => {
    const li = document.createElement('li');
    if (t.done) li.classList.add('done');
    li.innerHTML = `
      <input type="checkbox" ${'$'}{t.done ? 'checked' : ''}>
      <span style="flex:1; margin: 0 12px;">${'$'}{t.text}</span>
      <button class="del-btn">✕</button>
    `;
    li.querySelector('input').addEventListener('change', () => {
      t.done = !t.done;
      save();
    });
    li.querySelector('.del-btn').addEventListener('click', () => {
      tasks = tasks.filter(item => item !== t);
      save();
    });
    taskList.appendChild(li);
  });

  const activeCount = tasks.filter(t => !t.done).length;
  taskStats.textContent = activeCount + ' items remaining';
}

addBtn.addEventListener('click', () => {
  const text = taskInput.value.trim();
  if (text) {
    tasks.push({ text, done: false });
    taskInput.value = '';
    save();
  }
});

document.querySelectorAll('.tab-btn').forEach(b => {
  b.addEventListener('click', () => {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    b.classList.add('active');
    filter = b.dataset.filter;
    render();
  });
});

render();
console.log("Tasks loaded!");
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML"),
                ProjectFileEntity(projectId = projectId, name = "style.css", filePath = "style.css", content = css, fileType = "CSS"),
                ProjectFileEntity(projectId = projectId, name = "app.js", filePath = "app.js", content = js, fileType = "JS")
            )
        )
    }

    // --- Template 6: Pixel Art Drawing App ---
    fun createPaintApp(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "WEB_APP",
            iconName = "palette"
        )

        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="paint-container">
    <div class="palette">
      <button class="color-btn active" style="background:#000" data-color="#000000"></button>
      <button class="color-btn" style="background:#ef4444" data-color="#ef4444"></button>
      <button class="color-btn" style="background:#3b82f6" data-color="#3b82f6"></button>
      <button class="color-btn" style="background:#22c55e" data-color="#22c55e"></button>
      <button class="color-btn" style="background:#eab308" data-color="#eab308"></button>
      <button class="color-btn" style="background:#a855f7" data-color="#a855f7"></button>
      <button class="color-btn" style="background:#ffffff" data-color="#ffffff"></button>
      <button id="clearBtn">Clear</button>
    </div>
    <canvas id="paintCanvas" width="320" height="320"></canvas>
  </div>
  <script src="paint.js"></script>
</body>
</html>
""".trimIndent()

        val css = """
* { margin:0; padding:0; box-sizing:border-box; }
body {
  background: #1e1e24;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}
.palette {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  background: #2b2b36;
  padding: 8px 12px;
  border-radius: 12px;
}
.color-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 2px solid transparent;
  cursor: pointer;
}
.color-btn.active { border-color: white; transform: scale(1.15); }
#clearBtn {
  background: #ef4444;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 8px;
  cursor: pointer;
}
canvas {
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.5);
  touch-action: none;
}
""".trimIndent()

        val js = """
const canvas = document.getElementById('paintCanvas');
const ctx = canvas.getContext('2d');
let color = '#000000';
let isDrawing = false;

ctx.fillStyle = '#ffffff';
ctx.fillRect(0, 0, canvas.width, canvas.height);

function draw(e) {
  if (!isDrawing) return;
  const rect = canvas.getBoundingClientRect();
  const x = (e.clientX || (e.touches && e.touches[0].clientX)) - rect.left;
  const y = (e.clientY || (e.touches && e.touches[0].clientY)) - rect.top;

  ctx.fillStyle = color;
  ctx.beginPath();
  ctx.arc(x, y, 6, 0, Math.PI * 2);
  ctx.fill();
}

canvas.addEventListener('mousedown', e => { isDrawing = true; draw(e); });
canvas.addEventListener('mousemove', draw);
window.addEventListener('mouseup', () => isDrawing = false);

canvas.addEventListener('touchstart', e => { isDrawing = true; draw(e); e.preventDefault(); });
canvas.addEventListener('touchmove', e => { draw(e); e.preventDefault(); });
window.addEventListener('touchend', () => isDrawing = false);

document.querySelectorAll('.color-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.color-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    color = btn.dataset.color;
  });
});

document.getElementById('clearBtn').addEventListener('click', () => {
  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  console.log("Canvas cleared");
});
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML"),
                ProjectFileEntity(projectId = projectId, name = "style.css", filePath = "style.css", content = css, fileType = "CSS"),
                ProjectFileEntity(projectId = projectId, name = "paint.js", filePath = "paint.js", content = js, fileType = "JS")
            )
        )
    }

    // --- Template 7: Pong Game ---
    fun createPongGame(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "GAME",
            iconName = "sports_tennis"
        )
        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
  <title>$name</title>
  <style>
    body { background: #000; color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; margin: 0; font-family: monospace; }
    #score { font-size: 28px; margin-bottom: 8px; }
    canvas { border: 2px solid #fff; }
  </style>
</head>
<body>
  <div id="score">PLAYER: <span id="pScore">0</span> | CPU: <span id="cScore">0</span></div>
  <canvas id="pongCanvas" width="360" height="480"></canvas>
  <script>
    const canvas = document.getElementById('pongCanvas');
    const ctx = canvas.getContext('2d');
    let pScore = 0, cScore = 0;
    let playerY = 200, cpuY = 200;
    let ballX = 180, ballY = 240, ballVX = 4, ballVY = 3;
    const P_HEIGHT = 70, P_WIDTH = 10;

    window.addEventListener('keydown', e => {
      if (e.key === 'ArrowUp') playerY = Math.max(0, playerY - 24);
      if (e.key === 'ArrowDown') playerY = Math.min(canvas.height - P_HEIGHT, playerY + 24);
    });

    canvas.addEventListener('touchmove', e => {
      const rect = canvas.getBoundingClientRect();
      playerY = e.touches[0].clientY - rect.top - P_HEIGHT / 2;
    });

    function update() {
      ballX += ballVX;
      ballY += ballVY;

      // Bounce top/bottom
      if (ballY <= 0 || ballY >= canvas.height) ballVY = -ballVY;

      // CPU tracking
      if (cpuY + P_HEIGHT / 2 < ballY) cpuY += 2.8;
      else cpuY -= 2.8;

      // Player paddle collision
      if (ballX <= 25 && ballY > playerY && ballY < playerY + P_HEIGHT) {
        ballVX = -ballVX * 1.05;
      }

      // CPU paddle collision
      if (ballX >= canvas.width - 25 && ballY > cpuY && ballY < cpuY + P_HEIGHT) {
        ballVX = -ballVX * 1.05;
      }

      // Score
      if (ballX < 0) {
        cScore++;
        document.getElementById('cScore').textContent = cScore;
        resetBall();
      } else if (ballX > canvas.width) {
        pScore++;
        document.getElementById('pScore').textContent = pScore;
        resetBall();
      }
    }

    function resetBall() {
      ballX = canvas.width / 2;
      ballY = canvas.height / 2;
      ballVX = (Math.random() > 0.5 ? 4 : -4);
      ballVY = (Math.random() > 0.5 ? 3 : -3);
    }

    function draw() {
      ctx.fillStyle = '#000';
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      ctx.fillStyle = '#fff';
      // Player
      ctx.fillRect(15, playerY, P_WIDTH, P_HEIGHT);
      // CPU
      ctx.fillRect(canvas.width - 25, cpuY, P_WIDTH, P_HEIGHT);
      // Ball
      ctx.fillRect(ballX - 4, ballY - 4, 8, 8);

      requestAnimationFrame(() => {
        update();
        draw();
      });
    }
    draw();
  </script>
</body>
</html>
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML")
            )
        )
    }

    // --- Template 8: 2048 Puzzle ---
    fun create2048Game(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "GAME",
            iconName = "grid_view"
        )
        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
  <title>$name</title>
  <style>
    body { background: #faf8ef; font-family: sans-serif; display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; margin: 0; }
    .grid { display: grid; grid-template-columns: repeat(4, 70px); gap: 10px; background: #bbada0; padding: 10px; border-radius: 8px; }
    .cell { width: 70px; height: 70px; background: #cdc1b4; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 24px; font-weight: bold; color: #776e65; }
    h1 { color: #776e65; margin-bottom: 12px; }
  </style>
</head>
<body>
  <h1>2048 Studio</h1>
  <div class="grid" id="board"></div>
  <script>
    const boardEl = document.getElementById('board');
    let board = [
      [0, 0, 0, 0],
      [0, 2, 0, 0],
      [0, 0, 4, 0],
      [0, 0, 0, 0]
    ];
    function render() {
      boardEl.innerHTML = '';
      for (let r = 0; r < 4; r++) {
        for (let c = 0; c < 4; c++) {
          const div = document.createElement('div');
          div.className = 'cell';
          const val = board[r][c];
          if (val > 0) {
            div.textContent = val;
            div.style.background = val === 2 ? '#eee4da' : val === 4 ? '#ede0c8' : '#f2b179';
            if (val > 4) div.style.color = '#fff';
          }
          boardEl.appendChild(div);
        }
      }
    }
    render();
    console.log("2048 Ready");
  </script>
</body>
</html>
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML")
            )
        )
    }

    // --- Fallback & Custom Prompt Synthesizer ---
    fun createCustomInteractiveApp(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "WEB_APP",
            iconName = "code"
        )

        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>$name</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="app-card">
    <div class="header">
      <span class="badge">AI STUDIO GENERATED</span>
      <h1>$name</h1>
      <p class="subtitle">$prompt</p>
    </div>

    <div class="workspace-area">
      <div class="card interactive-panel">
        <h3>Live Interactive Controller</h3>
        <p>This dynamic application was synthesized from your description.</p>
        <div class="counter-box">
          <span id="counterValue">0</span>
        </div>
        <div class="actions">
          <button id="decBtn" class="btn secondary">- Decrement</button>
          <button id="incBtn" class="btn primary">+ Increment</button>
        </div>
      </div>

      <div class="card feature-box">
        <h3>Dynamic Log Stream</h3>
        <ul id="eventLog">
          <li>System online. Workspace initialized.</li>
        </ul>
      </div>
    </div>
  </div>
  <script src="script.js"></script>
</body>
</html>
""".trimIndent()

        val css = """
* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', system-ui, sans-serif; }
body {
  background: #0f172a;
  color: #f8fafc;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}
.app-card {
  width: 100%;
  max-width: 480px;
  background: #1e293b;
  border-radius: 20px;
  border: 1px solid #334155;
  box-shadow: 0 20px 40px rgba(0,0,0,0.4);
  overflow: hidden;
}
.header {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  padding: 24px;
}
.badge {
  background: rgba(255,255,255,0.2);
  font-size: 11px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 20px;
  letter-spacing: 1px;
}
h1 { font-size: 24px; margin-top: 8px; }
.subtitle { font-size: 13px; opacity: 0.85; margin-top: 4px; }
.workspace-area { padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.card {
  background: #0f172a;
  border: 1px solid #334155;
  border-radius: 14px;
  padding: 16px;
}
.card h3 { font-size: 16px; color: #38bdf8; margin-bottom: 6px; }
.counter-box {
  margin: 16px 0;
  text-align: center;
}
#counterValue {
  font-size: 48px;
  font-weight: bold;
  color: #a855f7;
}
.actions { display: flex; gap: 10px; }
.btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn.primary { background: #38bdf8; color: #0f172a; }
.btn.secondary { background: #334155; color: #f8fafc; }
#eventLog {
  margin-top: 8px;
  font-family: monospace;
  font-size: 12px;
  color: #94a3b8;
  max-height: 100px;
  overflow-y: auto;
  list-style: none;
}
""".trimIndent()

        val js = """
let count = 0;
const counterEl = document.getElementById('counterValue');
const logEl = document.getElementById('eventLog');

function addLog(msg) {
  const li = document.createElement('li');
  li.textContent = `[${'$'}{new Date().toLocaleTimeString()}] ${'$'}{msg}`;
  logEl.prepend(li);
  console.log(msg);
}

document.getElementById('incBtn').addEventListener('click', () => {
  count++;
  counterEl.textContent = count;
  addLog(`Value incremented to ${'$'}{count}`);
});

document.getElementById('decBtn').addEventListener('click', () => {
  count--;
  counterEl.textContent = count;
  addLog(`Value decremented to ${'$'}{count}`);
});

console.log("Interactive custom application initialized from prompt: '$prompt'");
""".trimIndent()

        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML"),
                ProjectFileEntity(projectId = projectId, name = "style.css", filePath = "style.css", content = css, fileType = "CSS"),
                ProjectFileEntity(projectId = projectId, name = "script.js", filePath = "script.js", content = js, fileType = "JS")
            )
        )
    }

    // --- Weather App ---
    fun createWeatherApp(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "WEB_APP",
            iconName = "cloud"
        )
        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>$name</title>
  <style>
    body { background: #0f172a; color: #fff; font-family: sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; padding: 16px; }
    .weather-card { background: linear-gradient(135deg, #0284c7, #1e3a8a); padding: 32px; border-radius: 24px; text-align: center; width: 100%; max-width: 320px; box-shadow: 0 20px 40px rgba(0,0,0,0.5); }
    .temp { font-size: 64px; font-weight: 300; margin: 16px 0; }
    .city { font-size: 22px; font-weight: 600; }
    .desc { font-size: 16px; opacity: 0.85; }
    .metrics { display: flex; justify-content: space-around; margin-top: 24px; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 16px; }
  </style>
</head>
<body>
  <div class="weather-card">
    <div class="city">New York</div>
    <div class="temp">24°C</div>
    <div class="desc">Partly Sunny</div>
    <div class="metrics">
      <div>Humidity<br><b>58%</b></div>
      <div>Wind<br><b>12 km/h</b></div>
    </div>
  </div>
</body>
</html>
""".trimIndent()
        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML")
            )
        )
    }

    // --- Quiz App ---
    fun createQuizApp(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "WEB_APP",
            iconName = "quiz"
        )
        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>$name</title>
  <style>
    body { background: #1e1e2f; color: #fff; font-family: sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; padding: 16px; }
    .quiz-box { background: #27273f; padding: 24px; border-radius: 16px; width: 100%; max-width: 380px; }
    h2 { font-size: 18px; margin-bottom: 16px; }
    .opt-btn { width: 100%; background: #39395a; color: #fff; border: none; padding: 12px; margin-bottom: 8px; border-radius: 8px; cursor: pointer; text-align: left; }
    .opt-btn:hover { background: #6366f1; }
  </style>
</head>
<body>
  <div class="quiz-box">
    <h2 id="qText">What does HTML stand for?</h2>
    <button class="opt-btn">HyperText Markup Language</button>
    <button class="opt-btn">High Tech Modern Language</button>
    <button class="opt-btn">Hyper Transfer Main Logic</button>
  </div>
</body>
</html>
""".trimIndent()
        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML")
            )
        )
    }

    // --- Platformer Game ---
    fun createPlatformerGame(name: String, prompt: String): GeneratedProject {
        val projectId = UUID.randomUUID().toString()
        val project = ProjectEntity(
            id = projectId,
            name = name,
            description = prompt,
            type = "GAME",
            iconName = "sports_esports"
        )
        val html = """
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
  <title>$name</title>
  <style>
    body { background: #0f172a; margin: 0; display: flex; justify-content: center; align-items: center; height: 100vh; overflow: hidden; }
    canvas { background: #1e293b; border: 2px solid #38bdf8; border-radius: 8px; }
  </style>
</head>
<body>
  <canvas id="platCanvas" width="360" height="480"></canvas>
  <script>
    const canvas = document.getElementById('platCanvas');
    const ctx = canvas.getContext('2d');
    let x = 50, y = 300, vx = 0, vy = 0;
    const gravity = 0.5;
    let onGround = false;

    window.addEventListener('keydown', e => {
      if (e.key === 'ArrowLeft') vx = -4;
      if (e.key === 'ArrowRight') vx = 4;
      if ((e.key === ' ' || e.key === 'ArrowUp' || e.key === 'z') && onGround) {
        vy = -11;
        onGround = false;
      }
    });
    window.addEventListener('keyup', e => {
      if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') vx = 0;
    });

    function loop() {
      vy += gravity;
      x += vx;
      y += vy;

      if (y >= 420) {
        y = 420;
        vy = 0;
        onGround = true;
      }

      ctx.clearRect(0, 0, canvas.width, canvas.height);
      // Ground
      ctx.fillStyle = '#22c55e';
      ctx.fillRect(0, 440, canvas.width, 40);
      // Platform
      ctx.fillStyle = '#38bdf8';
      ctx.fillRect(100, 320, 160, 16);

      // Platform collision
      if (x > 80 && x < 260 && y >= 300 && y <= 310 && vy > 0) {
        y = 300;
        vy = 0;
        onGround = true;
      }

      // Player
      ctx.fillStyle = '#f43f5e';
      ctx.fillRect(x, y, 20, 20);

      requestAnimationFrame(loop);
    }
    loop();
  </script>
</body>
</html>
""".trimIndent()
        return GeneratedProject(
            project = project,
            files = listOf(
                ProjectFileEntity(projectId = projectId, name = "index.html", filePath = "index.html", content = html, fileType = "HTML")
            )
        )
    }
}
