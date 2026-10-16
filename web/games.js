//
// games.js - Authentic Wakeout 1-Minute Motion Game Loop
// Watt Invaders / Wave 6 with Flying UFOs, Aliens & Meteors
//

class MeteorGame {
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.isActive = false;

    this.shipX = 0.5;
    this.shipY = 0.88;

    this.targets = [];
    this.lasers = [];
    this.particles = [];
    this.floatingTexts = [];

    this.score = 56;
    this.combo = 28;
    this.maxCombo = 28;
    this.timeLeft = 60;
    this.timerInterval = null;
    this.lastSpawn = 0;

    this.onGameEnd = null;
  }

  start() {
    this.isActive = true;
    this.score = 56;
    this.combo = 28;
    this.timeLeft = 60;
    this.targets = [];
    this.lasers = [];
    this.particles = [];
    this.floatingTexts = [];
    this.lastSpawn = performance.now();

    window.soundEngine.playBell();

    clearInterval(this.timerInterval);
    this.timerInterval = setInterval(() => {
      this.timeLeft--;
      if (this.timeLeft <= 5 && this.timeLeft > 0) {
        window.soundEngine.playTick();
      }
      if (this.timeLeft <= 0) {
        this.end();
      }
    }, 1000);

    // Gestures listener
    window.motionEngine.onGestureDetected = (gesture) => {
      if (!this.isActive) return;
      if (gesture === 'PUNCH_LEFT' || gesture === 'PUNCH_RIGHT') {
        this.fireLaser();
      }
    };

    window.motionEngine.onPoseUpdate = (pose) => {
      if (!this.isActive || !pose) return;
      const targetX = pose.midX || 0.5;
      this.shipX += (targetX - this.shipX) * 0.35;
    };

    this.loop();
  }

  fireLaser() {
    if (!this.isActive) return;
    const w = this.canvas.width;
    const h = this.canvas.height;
    const px = this.shipX * w;
    const py = this.shipY * h;

    this.lasers.push({ x: px - 14, y: py - 20, vy: -15, r: 4 });
    this.lasers.push({ x: px + 14, y: py - 20, vy: -15, r: 4 });

    window.soundEngine.playLaser();
    this.spawnParticles(px, py - 20, '#58CC02', 6);
  }

  spawnTarget() {
    const w = this.canvas.width;
    const typeRoll = Math.random();
    let type = 'ufo'; // 'ufo', 'alien', 'watt'
    if (typeRoll < 0.4) type = 'ufo';
    else if (typeRoll < 0.75) type = 'alien';
    else type = 'watt';

    this.targets.push({
      x: (0.15 + Math.random() * 0.7) * w,
      y: -40,
      type,
      radius: type === 'watt' ? 22 : 28,
      vy: 2.0 + Math.random() * 2.2,
      angle: 0,
      vRot: (Math.random() - 0.5) * 0.04
    });
  }

  spawnParticles(x, y, color, count = 14) {
    for (let i = 0; i < count; i++) {
      const angle = Math.random() * Math.PI * 2;
      const speed = 2 + Math.random() * 5;
      this.particles.push({
        x, y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        life: 1.0,
        decay: 0.035,
        color
      });
    }
  }

  addFloatingText(text, x, y, color = '#FFC800') {
    this.floatingTexts.push({
      text, x, y,
      vy: -2,
      life: 1.0,
      decay: 0.025,
      color
    });
  }

  update() {
    const now = performance.now();
    const w = this.canvas.width;
    const h = this.canvas.height;

    // Spawn targets
    if (now - this.lastSpawn > 850) {
      this.spawnTarget();
      this.lastSpawn = now;
    }

    // Lasers
    for (let i = this.lasers.length - 1; i >= 0; i--) {
      const laser = this.lasers[i];
      laser.y += laser.vy;
      if (laser.y < -20) {
        this.lasers.splice(i, 1);
      }
    }

    const shipPx = this.shipX * w;
    const shipPy = this.shipY * h;

    // Targets collision & update
    for (let i = this.targets.length - 1; i >= 0; i--) {
      const t = this.targets[i];
      t.y += t.vy;
      t.angle += t.vRot;

      let hit = false;
      for (let j = this.lasers.length - 1; j >= 0; j--) {
        const laser = this.lasers[j];
        const dist = Math.hypot(t.x - laser.x, t.y - laser.y);
        if (dist < t.radius + laser.r + 6) {
          hit = true;
          this.lasers.splice(j, 1);
          break;
        }
      }

      if (hit) {
        this.combo++;
        const pts = t.type === 'watt' ? 25 : 10;
        this.score += pts;

        const color = t.type === 'ufo' ? '#CE82FF' : (t.type === 'watt' ? '#58CC02' : '#FF9600');
        this.spawnParticles(t.x, t.y, color, 16);
        this.addFloatingText(t.type === 'watt' ? '+25 WATTS!' : '+8 KICK!', t.x, t.y, '#FFC800');
        window.soundEngine.playHit();
        if (this.combo > 1) window.soundEngine.playCombo(this.combo);

        this.targets.splice(i, 1);
        continue;
      }

      // Collect Watt core or hit by ship
      const shipDist = Math.hypot(t.x - shipPx, t.y - shipPy);
      if (shipDist < t.radius + 24) {
        if (t.type === 'watt') {
          this.score += 25;
          window.soundEngine.playCoin();
          this.spawnParticles(t.x, t.y, '#58CC02', 20);
          this.addFloatingText('+25 WATTS!', shipPx, shipPy - 30, '#58CC02');
        } else {
          this.combo = Math.max(1, Math.floor(this.combo / 2));
          window.soundEngine.playHit();
          this.spawnParticles(shipPx, shipPy, '#FF4B4B', 16);
        }
        this.targets.splice(i, 1);
        continue;
      }

      if (t.y > h + 50) {
        this.targets.splice(i, 1);
      }
    }

    // Particles
    for (let i = this.particles.length - 1; i >= 0; i--) {
      const p = this.particles[i];
      p.x += p.vx;
      p.y += p.vy;
      p.life -= p.decay;
      if (p.life <= 0) this.particles.splice(i, 1);
    }

    // Floating text
    for (let i = this.floatingTexts.length - 1; i >= 0; i--) {
      const ft = this.floatingTexts[i];
      ft.y += ft.vy;
      ft.life -= ft.decay;
      if (ft.life <= 0) this.floatingTexts.splice(i, 1);
    }
  }

  draw() {
    const w = this.canvas.width;
    const h = this.canvas.height;
    const ctx = this.ctx;

    ctx.clearRect(0, 0, w, h);

    // Lasers
    ctx.fillStyle = '#58CC02';
    ctx.shadowColor = '#7BE11A';
    ctx.shadowBlur = 10;
    this.lasers.forEach(l => {
      ctx.beginPath();
      ctx.roundRect(l.x - 3, l.y - 12, 6, 24, 3);
      ctx.fill();
    });

    // Targets (UFO, Alien, Watt)
    this.targets.forEach(t => {
      ctx.save();
      ctx.translate(t.x, t.y);

      if (t.type === 'ufo') {
        // Flying Saucer (Like in Screenshot 1)
        // Glass dome
        ctx.fillStyle = '#67E8F9';
        ctx.beginPath();
        ctx.arc(0, -6, 14, Math.PI, 0);
        ctx.fill();

        // Saucer disk
        ctx.fillStyle = '#CE82FF';
        ctx.beginPath();
        ctx.ellipse(0, 0, 28, 10, 0, 0, Math.PI * 2);
        ctx.fill();

        // Little alien head inside
        ctx.fillStyle = '#22C55E';
        ctx.beginPath();
        ctx.arc(0, -7, 6, 0, Math.PI * 2);
        ctx.fill();

        // Blinking lights
        ctx.fillStyle = '#FFC800';
        ctx.beginPath();
        ctx.arc(-16, 2, 3, 0, Math.PI * 2);
        ctx.arc(0, 4, 3, 0, Math.PI * 2);
        ctx.arc(16, 2, 3, 0, Math.PI * 2);
        ctx.fill();
      } else if (t.type === 'alien') {
        // Purple Alien Monster with tentacles
        ctx.fillStyle = '#A855F7';
        ctx.beginPath();
        ctx.arc(0, -4, 18, 0, Math.PI * 2);
        ctx.fill();

        // Cute Big Eyes
        ctx.fillStyle = '#FFFFFF';
        ctx.beginPath();
        ctx.arc(-7, -6, 5, 0, Math.PI * 2);
        ctx.arc(7, -6, 5, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#000000';
        ctx.beginPath();
        ctx.arc(-7, -6, 2.5, 0, Math.PI * 2);
        ctx.arc(7, -6, 2.5, 0, Math.PI * 2);
        ctx.fill();

        // Antennas
        ctx.strokeStyle = '#A855F7';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(-10, -18);
        ctx.lineTo(-14, -26);
        ctx.moveTo(10, -18);
        ctx.lineTo(14, -26);
        ctx.stroke();
      } else {
        // Glowing Energy Watt Core
        ctx.fillStyle = '#58CC02';
        ctx.shadowColor = '#58CC02';
        ctx.shadowBlur = 16;
        ctx.beginPath();
        ctx.arc(0, 0, t.radius, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#FFFFFF';
        ctx.font = 'bold 18px Nunito, sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('⚡', 0, 0);
      }
      ctx.restore();
    });

    // Ship / Mascot at Bottom
    const px = this.shipX * w;
    const py = this.shipY * h;

    ctx.save();
    ctx.translate(px, py);

    // Thruster glow
    ctx.fillStyle = '#FF9600';
    ctx.beginPath();
    ctx.moveTo(-10, 16);
    ctx.lineTo(0, 28 + Math.random() * 6);
    ctx.lineTo(10, 16);
    ctx.fill();

    // Spaceship Shell
    ctx.fillStyle = '#1E293B';
    ctx.strokeStyle = '#58CC02';
    ctx.lineWidth = 3;
    ctx.beginPath();
    ctx.moveTo(0, -26);
    ctx.lineTo(22, 16);
    ctx.lineTo(0, 10);
    ctx.lineTo(-22, 16);
    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    // Mascot Headband Emblem
    ctx.fillStyle = '#FFC800';
    ctx.beginPath();
    ctx.arc(0, -4, 6, 0, Math.PI * 2);
    ctx.fill();

    ctx.restore();

    // Particles
    this.particles.forEach(p => {
      ctx.fillStyle = p.color;
      ctx.globalAlpha = p.life;
      ctx.beginPath();
      ctx.arc(p.x, p.y, 3 * p.life, 0, Math.PI * 2);
      ctx.fill();
    });
    ctx.globalAlpha = 1.0;

    // Floating text
    this.floatingTexts.forEach(ft => {
      ctx.save();
      ctx.globalAlpha = ft.life;
      ctx.fillStyle = ft.color;
      ctx.font = 'bold 20px Nunito, sans-serif';
      ctx.textAlign = 'center';
      ctx.shadowColor = ft.color;
      ctx.shadowBlur = 8;
      ctx.fillText(ft.text, ft.x, ft.y);
      ctx.restore();
    });
    ctx.shadowBlur = 0;
  }

  loop() {
    if (!this.isActive) return;
    this.update();
    this.draw();
    requestAnimationFrame(() => this.loop());
  }

  end() {
    this.isActive = false;
    clearInterval(this.timerInterval);
    if (this.onGameEnd) {
      this.onGameEnd({
        score: this.score,
        maxCombo: this.combo
      });
    }
  }
}

window.MeteorGame = MeteorGame;
