//
// app.js - Wakeout Interactive App Controller
// Orchestrates the 5 Authentic Screens from Apple App Store
//

document.addEventListener('DOMContentLoaded', async () => {
  // Navigation Tabs
  const navTabs = document.querySelectorAll('.nav-tab-item');
  const phoneViews = document.querySelectorAll('.phone-view');

  // Device Mode Toggle (iPhone Frame vs Fullscreen)
  const deviceContainer = document.getElementById('device-container');
  const btnDeviceView = document.getElementById('btn-device-view');
  const viewModeLabel = document.getElementById('view-mode-label');

  let isFullscreen = false;
  btnDeviceView.addEventListener('click', () => {
    isFullscreen = !isFullscreen;
    if (isFullscreen) {
      deviceContainer.classList.add('mode-fullscreen');
      viewModeLabel.textContent = 'iPhone Device Frame';
      btnDeviceView.querySelector('span:first-child').textContent = '🖥️';
    } else {
      deviceContainer.classList.remove('mode-fullscreen');
      viewModeLabel.textContent = 'Fullscreen Mode';
      btnDeviceView.querySelector('span:first-child').textContent = '📱';
    }
    resizeCanvases();
  });

  // Sound Toggle
  const btnSoundToggle = document.getElementById('btn-sound-toggle');
  const soundIcon = document.getElementById('sound-icon');
  btnSoundToggle.addEventListener('click', () => {
    window.soundEngine.enabled = !window.soundEngine.enabled;
    soundIcon.textContent = window.soundEngine.enabled ? '🔊' : '🔇';
  });

  // Clock in status bar
  function updateClock() {
    const now = new Date();
    const h = String(now.getHours()).padStart(2, '0');
    const m = String(now.getMinutes()).padStart(2, '0');
    const el = document.getElementById('status-clock');
    if (el) el.textContent = `${h}:${m}`;
  }
  updateClock();
  setInterval(updateClock, 30000);

  // Tab Switching
  navTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      navTabs.forEach(t => t.classList.remove('active'));
      tab.classList.add('active');

      const targetViewId = tab.getAttribute('data-view');
      phoneViews.forEach(v => {
        if (v.id === targetViewId) {
          v.classList.add('active');
        } else {
          v.classList.remove('active');
        }
      });

      if (targetViewId === 'view-game') {
        resizeCanvases();
      }
    });
  });

  // ==========================================================
  // SCREEN 1: 1-Phút Game (Watt Invaders / Wave 6)
  // ==========================================================
  const videoEl = document.getElementById('webcam-video');
  const skeletonCanvas = document.getElementById('skeleton-canvas');
  const gameCanvas = document.getElementById('game-canvas');
  const btnStartGame = document.getElementById('btn-start-game');
  const btnGameLabel = document.getElementById('btn-game-label');
  const btnCamSwitch = document.getElementById('btn-cam-switch');
  const gameHudScore = document.getElementById('game-hud-score');
  const gameHudMultiplier = document.getElementById('game-hud-multiplier');
  const gameHudTime = document.getElementById('game-hud-time');
  const waveBanner = document.getElementById('game-wave-banner');

  let isCameraRunning = false;
  let gameEngine = new window.MeteorGame(gameCanvas);

  function resizeCanvases() {
    const wrapper = document.querySelector('.game-screen-wrapper');
    if (!wrapper) return;
    const w = wrapper.clientWidth;
    const h = wrapper.clientHeight;
    skeletonCanvas.width = w;
    skeletonCanvas.height = h;
    gameCanvas.width = w;
    gameCanvas.height = h;
  }
  window.addEventListener('resize', resizeCanvases);
  setTimeout(resizeCanvases, 100);

  btnCamSwitch.addEventListener('click', async () => {
    if (!isCameraRunning) {
      const ok = await window.motionEngine.startCamera(videoEl, skeletonCanvas);
      isCameraRunning = true;
      btnCamSwitch.style.background = 'var(--wakeout-green)';
      btnCamSwitch.style.color = '#FFF';
    } else {
      window.motionEngine.stopCamera();
      isCameraRunning = false;
      btnCamSwitch.style.background = 'rgba(255, 255, 255, 0.85)';
      btnCamSwitch.style.color = '#000';
    }
  });

  btnStartGame.addEventListener('click', async () => {
    if (!isCameraRunning) {
      await btnCamSwitch.click();
    }
    resizeCanvases();

    if (!gameEngine.isActive) {
      gameEngine.start();
      btnGameLabel.textContent = 'STOP';
      btnStartGame.style.background = '#EF4444';

      const hudInterval = setInterval(() => {
        if (!gameEngine.isActive) {
          clearInterval(hudInterval);
          btnGameLabel.textContent = 'START (1 MIN)';
          btnStartGame.style.background = 'var(--wakeout-green)';
          return;
        }
        gameHudScore.textContent = gameEngine.score;
        gameHudMultiplier.textContent = `x${Math.max(1, gameEngine.combo * 2)}`;
        gameHudTime.textContent = `${gameEngine.timeLeft}s`;

        const waveNum = Math.min(8, Math.floor((60 - gameEngine.timeLeft) / 10) + 1);
        waveBanner.textContent = `WAVE ${waveNum}`;
      }, 200);
    } else {
      gameEngine.end();
      btnGameLabel.textContent = 'START (1 MIN)';
      btnStartGame.style.background = 'var(--wakeout-green)';
    }
  });

  gameEngine.onGameEnd = (results) => {
    window.soundEngine.playVictory();
    btnGameLabel.textContent = 'PLAY AGAIN (1 MIN)';
    btnStartGame.style.background = 'var(--wakeout-green)';

    // Update Watts on screen 5
    const wattsTodayEl = document.getElementById('watts-today-num');
    if (wattsTodayEl) {
      const current = parseInt(wattsTodayEl.textContent, 10) || 318;
      wattsTodayEl.textContent = current + results.score;
    }
  };

  // Keyboard Fallback for game
  window.addEventListener('keydown', (e) => {
    if (!gameEngine || !gameEngine.isActive) return;
    if (e.key === 'ArrowLeft') {
      gameEngine.shipX = Math.max(0.1, gameEngine.shipX - 0.08);
    } else if (e.key === 'ArrowRight') {
      gameEngine.shipX = Math.min(0.9, gameEngine.shipX + 0.08);
    } else if (e.key === ' ' || e.key === 'ArrowUp') {
      gameEngine.fireLaser();
    }
  });

  // ==========================================================
  // SCREEN 2: Daily Challenge (Single-Leg Balance)
  // ==========================================================
  const btnStartChallenge = document.getElementById('btn-start-challenge');
  const challengeTimeDigits = document.getElementById('challenge-time-digits');
  const balanceStatusText = document.getElementById('balance-status-text');
  let challengeSeconds = 0;
  let challengeInterval = null;

  btnStartChallenge.addEventListener('click', () => {
    if (!challengeInterval) {
      challengeSeconds = 0;
      btnStartChallenge.textContent = 'Stop';
      btnStartChallenge.style.background = '#EF4444';
      balanceStatusText.textContent = '✅ Great balance! Keep holding!';
      balanceStatusText.style.color = '#4ADE80';
      window.soundEngine.playBell();

      challengeInterval = setInterval(() => {
        challengeSeconds++;
        challengeTimeDigits.textContent = `${challengeSeconds}s / 45s`;
        if (challengeSeconds >= 45) {
          clearInterval(challengeInterval);
          challengeInterval = null;
          btnStartChallenge.textContent = 'Completed! 🎉';
          btnStartChallenge.style.background = 'var(--wakeout-green)';
          window.soundEngine.playVictory();
        }
      }, 1000);
    } else {
      clearInterval(challengeInterval);
      challengeInterval = null;
      btnStartChallenge.textContent = 'Start';
      btnStartChallenge.style.background = 'var(--wakeout-green)';
      balanceStatusText.textContent = 'Paused';
      balanceStatusText.style.color = '#FFF';
    }
  });

  // ==========================================================
  // SCREEN 3: 3k+ Routines Carousel
  // ==========================================================
  const catalog = await window.routinesManager.loadCatalog();
  const subContainer = document.getElementById('sub-routines-container');

  if (catalog && catalog.routines) {
    catalog.routines.slice(1, 6).forEach((r, idx) => {
      const tile = document.createElement('div');
      tile.className = 'mini-routine-tile';
      const title = r.title_en || r.title || r.title_vi;
      tile.innerHTML = `
        <div>
          <div style="font-size: 11px; font-weight: 800; color: var(--wakeout-green); text-transform: uppercase;">#${idx + 2} Stretch</div>
          <div style="font-size: 14px; font-weight: 900; margin-top: 4px;">${title}</div>
        </div>
        <div style="font-size: 12px; font-weight: 800; color: var(--text-secondary); margin-top: 10px;">⏱️ ${r.duration_seconds}s</div>
      `;
      tile.addEventListener('click', () => {
        document.getElementById('hero-routine-title').textContent = title;
        document.getElementById('hero-routine-desc').textContent = r.instructions_en || r.instructions || r.instructions_vi;
        window.soundEngine.playCoin();
      });
      subContainer.appendChild(tile);
    });
  }

    const btnHeroPlay = document.getElementById('btn-hero-play');
    btnHeroPlay.addEventListener('click', () => {
      window.soundEngine.playBell();
      btnHeroPlay.textContent = 'Playing (30s)...';
      btnHeroPlay.style.background = 'var(--wakeout-orange)';
      setTimeout(() => {
        window.soundEngine.playVictory();
        btnHeroPlay.textContent = 'Completed! +15⚡';
        btnHeroPlay.style.background = 'var(--wakeout-green)';
        const wattsTodayEl = document.getElementById('watts-today-num');
        if (wattsTodayEl) {
          wattsTodayEl.textContent = (parseInt(wattsTodayEl.textContent, 10) || 318) + 15;
        }
      }, 3000);
    });

    // ==========================================================
    // SCREEN 4: Wiggle Reminder (Every 30 min)
    // ==========================================================
    let wiggleRemaining = 29 * 60 + 53; // 29:53 like screenshot
    const giantDigits = document.getElementById('giant-countdown-digits');
    const giantBar = document.getElementById('giant-timer-bar');
    const subtitleRound = document.getElementById('reminder-subtitle-text');

    setInterval(() => {
      if (wiggleRemaining > 0) {
        wiggleRemaining--;
        const m = Math.floor(wiggleRemaining / 60);
        const s = wiggleRemaining % 60;
        giantDigits.textContent = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
        const total = 30 * 60;
        const progress = 1 - (wiggleRemaining / total);
        giantBar.style.strokeDashoffset = `${603 * progress}`;
      }
    }, 1000);

    // Interval pills
    document.querySelectorAll('.interval-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        document.querySelectorAll('.interval-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        const mins = parseInt(pill.getAttribute('data-min'), 10);
        wiggleRemaining = mins * 60;
        subtitleRound.textContent = `Every ${mins} min • 1st round`;
        window.soundEngine.playCoin();
      });
    });

    document.getElementById('btn-trigger-now').addEventListener('click', () => {
      window.soundEngine.playVictory();
      const wattsTodayEl = document.getElementById('watts-today-num');
      if (wattsTodayEl) {
        wattsTodayEl.textContent = (parseInt(wattsTodayEl.textContent, 10) || 318) + 10;
      }
      // Switch directly to Game tab
      document.querySelector('[data-view="view-game"]').click();
      if (!gameEngine.isActive) {
        btnStartGame.click();
      }
    });

    // ==========================================================
    // SCREEN 5: Activity Log & Heatmap Interactivity
    // ==========================================================
    const activityLogBtn = document.querySelector('.screen-header-bar div span');
    if (activityLogBtn) {
      activityLogBtn.style.cursor = 'pointer';
      activityLogBtn.addEventListener('click', () => {
        alert("📋 Activity Log:\n• Watt Invaders (1 Min): +56⚡\n• Single-Leg Balance: +50⚡\n• Neck Lateral Stretch: +15⚡\n• Wiggle Break: +10⚡");
      });
    }

    document.querySelectorAll('.calendar-grid-days .cal-day-cell').forEach(cell => {
      if (!isNaN(parseInt(cell.textContent, 10))) {
        cell.style.cursor = 'pointer';
        cell.addEventListener('click', () => {
          window.soundEngine.playCoin();
          const day = cell.textContent;
          alert(`📅 Oct ${day}, 2026: 318 Watts earned • Keep up the great streak!`);
        });
      }
    });
  });
