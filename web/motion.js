//
// motion.js - Motion Tracking & Gesture Classification Engine
// Conforms to shared-spec/README.md & gesture_vectors.json
//

class OneEuroFilter {
  constructor(minCutoff = 1.2, beta = 0.02, dCutoff = 1.0) {
    this.minCutoff = minCutoff;
    this.beta = beta;
    this.dCutoff = dCutoff;
    this.xPrev = null;
    this.dxPrev = 0;
    this.tPrev = null;
  }

  alpha(cutoff, dt) {
    const tau = 1.0 / (2.0 * Math.PI * cutoff);
    return 1.0 / (1.0 + tau / dt);
  }

  filter(x, t) {
    if (this.xPrev === null || this.tPrev === null || t <= this.tPrev) {
      this.xPrev = x;
      this.tPrev = t;
      this.dxPrev = 0;
      return x;
    }
    const dt = t - this.tPrev;
    const dx = (x - this.xPrev) / dt;
    const dxHat = this.dxPrev + this.alpha(this.dCutoff, dt) * (dx - this.dxPrev);
    const cutoff = this.minCutoff + this.beta * Math.abs(dxHat);
    const xHat = this.xPrev + this.alpha(cutoff, dt) * (x - this.xPrev);

    this.xPrev = xHat;
    this.dxPrev = dxHat;
    this.tPrev = t;
    return xHat;
  }

  reset() {
    this.xPrev = null;
    this.dxPrev = 0;
    this.tPrev = null;
  }
}

class MotionEngine {
  constructor() {
    this.video = null;
    this.canvas = null;
    this.ctx = null;
    this.isRunning = false;
    this.poseDetector = null;

    // Filters for smoothing
    this.filters = {
      midX: new OneEuroFilter(1.2, 0.02),
      width: new OneEuroFilter(1.0, 0.01),
      leftWristX: new OneEuroFilter(1.5, 0.03),
      leftWristY: new OneEuroFilter(1.5, 0.03),
      rightWristX: new OneEuroFilter(1.5, 0.03),
      rightWristY: new OneEuroFilter(1.5, 0.03),
    };

    // Baseline & thresholds matching shared-spec
    this.baseline = { midX: 0.5, shoulderWidth: 0.30 };
    this.isCalibrated = false;
    this.calibrationSamples = [];

    this.swayThreshold = 0.35;
    this.swayReleaseThreshold = 0.20;
    this.punchVelocityThreshold = 3.0;
    this.punchCooldownSeconds = 0.30;

    this.currentSwayState = 'NONE';
    this.prevReach = {};
    this.lastPunchTime = {};

    // Current smoothed frame
    this.currentPose = null;
    this.onGestureDetected = null;
    this.onPoseUpdate = null;

    // Optical flow fallback canvas
    this.prevFrameData = null;
    this.simulatedMode = false;
  }

  async startCamera(videoElement, canvasElement) {
    this.video = videoElement;
    this.canvas = canvasElement;
    this.ctx = canvasElement.getContext('2d');

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          width: { ideal: 640 },
          height: { ideal: 480 },
          facingMode: 'user'
        },
        audio: false
      });
      this.video.srcObject = stream;
      await new Promise(resolve => {
        this.video.onloadedmetadata = () => {
          this.video.play();
          resolve();
        };
      });

      this.isRunning = true;
      this.initPoseTracker();
      this.startLoop();
      return true;
    } catch (err) {
      console.warn('Camera permission denied or unavailable. Enabling interactive simulation mode.', err);
      this.simulatedMode = true;
      this.isRunning = true;
      this.startSimulatedLoop();
      return false;
    }
  }

  stopCamera() {
    this.isRunning = false;
    if (this.video && this.video.srcObject) {
      const tracks = this.video.srcObject.getTracks();
      tracks.forEach(track => track.stop());
      this.video.srcObject = null;
    }
  }

  initPoseTracker() {
    // Check if MediaPipe Pose is available globally from CDN
    if (window.Pose) {
      try {
        this.poseDetector = new window.Pose({
          locateFile: (file) => `https://cdn.jsdelivr.net/npm/@mediapipe/pose/${file}`
        });
        this.poseDetector.setOptions({
          modelComplexity: 1,
          smoothLandmarks: true,
          minDetectionConfidence: 0.5,
          minTrackingConfidence: 0.5
        });
        this.poseDetector.onResults(results => this.onMediaPipeResults(results));
        console.log('✅ MediaPipe Pose loaded successfully!');
        return;
      } catch (e) {
        console.warn('MediaPipe initialization fallback to optical motion tracking', e);
      }
    }
    console.log('⚡ Using Fast Native Computer Vision tracker (Zero external latency)');
  }

  onMediaPipeResults(results) {
    if (!results.poseLandmarks) return;
    const lm = results.poseLandmarks;
    const now = performance.now() / 1000;

    // 13 Joints mapping with mirroring (x -> 1 - x)
    const pose = {
      NOSE: { x: 1 - lm[0].x, y: lm[0].y, confidence: lm[0].visibility || 0.9 },
      LEFT_SHOULDER: { x: 1 - lm[11].x, y: lm[11].y, confidence: lm[11].visibility || 0.9 },
      RIGHT_SHOULDER: { x: 1 - lm[12].x, y: lm[12].y, confidence: lm[12].visibility || 0.9 },
      LEFT_ELBOW: { x: 1 - lm[13].x, y: lm[13].y, confidence: lm[13].visibility || 0.9 },
      RIGHT_ELBOW: { x: 1 - lm[14].x, y: lm[14].y, confidence: lm[14].visibility || 0.9 },
      LEFT_WRIST: { x: 1 - lm[15].x, y: lm[15].y, confidence: lm[15].visibility || 0.9 },
      RIGHT_WRIST: { x: 1 - lm[16].x, y: lm[16].y, confidence: lm[16].visibility || 0.9 },
      LEFT_HIP: { x: 1 - lm[23].x, y: lm[23].y, confidence: lm[23].visibility || 0.9 },
      RIGHT_HIP: { x: 1 - lm[24].x, y: lm[24].y, confidence: lm[24].visibility || 0.9 },
    };

    this.processPose(pose, now);
  }

  // Native Vision fallback: frame difference & optical centroid tracking
  processFrameDifferencing(now) {
    if (!this.video || this.video.readyState < 2) return;
    const w = 160;
    const h = 120;
    if (!this.diffCanvas) {
      this.diffCanvas = document.createElement('canvas');
      this.diffCanvas.width = w;
      this.diffCanvas.height = h;
      this.diffCtx = this.diffCanvas.getContext('2d', { willReadFrequently: true });
    }

    this.diffCtx.save();
    this.diffCtx.scale(-1, 1);
    this.diffCtx.drawImage(this.video, -w, 0, w, h);
    this.diffCtx.restore();

    const imgData = this.diffCtx.getImageData(0, 0, w, h);
    const data = imgData.data;

    if (!this.prevFrameData) {
      this.prevFrameData = new Uint8Array(data);
      return;
    }

    let leftMotionX = 0, leftMotionY = 0, leftCount = 0;
    let rightMotionX = 0, rightMotionY = 0, rightCount = 0;
    let centerMotionX = 0, centerCount = 0;

    for (let y = 0; y < h; y += 4) {
      for (let x = 0; x < w; x += 4) {
        const i = (y * w + x) * 4;
        const diff = Math.abs(data[i] - this.prevFrameData[i]) +
                     Math.abs(data[i + 1] - this.prevFrameData[i + 1]) +
                     Math.abs(data[i + 2] - this.prevFrameData[i + 2]);

        if (diff > 55) {
          const normX = x / w;
          const normY = y / h;

          if (normX < 0.4) {
            leftMotionX += normX;
            leftMotionY += normY;
            leftCount++;
          } else if (normX > 0.6) {
            rightMotionX += normX;
            rightMotionY += normY;
            rightCount++;
          }
          centerMotionX += normX;
          centerCount++;
        }
      }
    }
    this.prevFrameData.set(data);

    // Reconstruct pose landmarks from visual dynamics
    const headX = centerCount > 10 ? (centerMotionX / centerCount) : 0.5;
    const shoulderSpan = 0.32;
    const leftShoulderX = headX - shoulderSpan / 2;
    const rightShoulderX = headX + shoulderSpan / 2;

    const leftWristX = leftCount > 6 ? (leftMotionX / leftCount) : leftShoulderX - 0.05;
    const leftWristY = leftCount > 6 ? (leftMotionY / leftCount) : 0.65;
    const rightWristX = rightCount > 6 ? (rightMotionX / rightCount) : rightShoulderX + 0.05;
    const rightWristY = rightCount > 6 ? (rightMotionY / rightCount) : 0.65;

    const pose = {
      NOSE: { x: headX, y: 0.30, confidence: 0.9 },
      LEFT_SHOULDER: { x: leftShoulderX, y: 0.48, confidence: 0.9 },
      RIGHT_SHOULDER: { x: rightShoulderX, y: 0.48, confidence: 0.9 },
      LEFT_WRIST: { x: leftWristX, y: leftWristY, confidence: leftCount > 6 ? 0.85 : 0.4 },
      RIGHT_WRIST: { x: rightWristX, y: rightWristY, confidence: rightCount > 6 ? 0.85 : 0.4 },
    };

    this.processPose(pose, now);
  }

  processPose(rawPose, now) {
    if (!rawPose.LEFT_SHOULDER || !rawPose.RIGHT_SHOULDER) return;

    // Filter midX and shoulderWidth
    const rawMidX = (rawPose.LEFT_SHOULDER.x + rawPose.RIGHT_SHOULDER.x) / 2;
    const rawWidth = Math.hypot(
      rawPose.RIGHT_SHOULDER.x - rawPose.LEFT_SHOULDER.x,
      rawPose.RIGHT_SHOULDER.y - rawPose.LEFT_SHOULDER.y
    );

    const smoothMidX = this.filters.midX.filter(rawMidX, now);
    const smoothWidth = this.filters.width.filter(rawWidth, now);

    this.currentPose = {
      ...rawPose,
      midX: smoothMidX,
      shoulderWidth: smoothWidth,
      timestamp: now
    };

    // Handle Calibration
    if (!this.isCalibrated) {
      this.calibrationSamples.push({ midX: smoothMidX, width: smoothWidth });
      if (this.calibrationSamples.length >= 40) {
        const sortedMids = this.calibrationSamples.map(s => s.midX).sort();
        const sortedWidths = this.calibrationSamples.map(s => s.width).sort();
        this.baseline = {
          midX: sortedMids[Math.floor(sortedMids.length / 2)],
          shoulderWidth: Math.max(0.15, sortedWidths[Math.floor(sortedWidths.length / 2)])
        };
        this.isCalibrated = true;
        console.log('🎯 Calibrated Baseline:', this.baseline);
      }
    }

    // Evaluate gestures
    this.evaluateGestures(this.currentPose, now);

    if (this.onPoseUpdate) {
      this.onPoseUpdate(this.currentPose, this.baseline);
    }
  }

  evaluateGestures(pose, now) {
    const b = this.baseline;
    if (!b || b.shoulderWidth < 0.02) return;

    // 1. Check Punch (priority)
    const punchCheckList = [
      { wrist: 'RIGHT_WRIST', shoulder: 'RIGHT_SHOULDER', gesture: 'PUNCH_RIGHT' },
      { wrist: 'LEFT_WRIST', shoulder: 'LEFT_SHOULDER', gesture: 'PUNCH_LEFT' }
    ];

    for (const p of punchCheckList) {
      const w = pose[p.wrist];
      const s = pose[p.shoulder];
      if (w && s && w.confidence > 0.4 && s.confidence > 0.4) {
        const dist = Math.hypot(w.x - s.x, w.y - s.y) / b.shoulderWidth;
        const prev = this.prevReach[p.wrist];
        this.prevReach[p.wrist] = { dist, time: now };

        const lastPunch = this.lastPunchTime[p.wrist];
        if (lastPunch === undefined || (now - lastPunch) >= this.punchCooldownSeconds) {
          if (prev && now > prev.time) {
            const vel = (dist - prev.dist) / (now - prev.time);
            if (vel > this.punchVelocityThreshold && w.y < s.y) {
              this.lastPunchTime[p.wrist] = now;
              this.triggerGesture(p.gesture);
              return;
            }
          }
        }
      }
    }

    // 2. Check Sway with Hysteresis
    const normalizedDelta = (pose.midX - b.midX) / b.shoulderWidth;
    if (this.currentSwayState === 'SWAY_RIGHT') {
      if (normalizedDelta > this.swayReleaseThreshold) {
        this.triggerGesture('SWAY_RIGHT');
      } else {
        this.currentSwayState = 'NONE';
      }
    } else if (this.currentSwayState === 'SWAY_LEFT') {
      if (normalizedDelta < -this.swayReleaseThreshold) {
        this.triggerGesture('SWAY_LEFT');
      } else {
        this.currentSwayState = 'NONE';
      }
    } else {
      if (normalizedDelta > this.swayThreshold) {
        this.currentSwayState = 'SWAY_RIGHT';
        this.triggerGesture('SWAY_RIGHT');
      } else if (normalizedDelta < -this.swayThreshold) {
        this.currentSwayState = 'SWAY_LEFT';
        this.triggerGesture('SWAY_LEFT');
      }
    }
  }

  triggerGesture(gesture) {
    if (this.onGestureDetected) {
      this.onGestureDetected(gesture);
    }
  }

  startLoop() {
    const loop = async () => {
      if (!this.isRunning) return;
      const now = performance.now() / 1000;

      if (this.poseDetector && this.video && this.video.readyState >= 2) {
        try {
          await this.poseDetector.send({ image: this.video });
        } catch (e) {
          this.processFrameDifferencing(now);
        }
      } else {
        this.processFrameDifferencing(now);
      }

      this.renderDebugSkeleton();
      requestAnimationFrame(loop);
    };
    requestAnimationFrame(loop);
  }

  startSimulatedLoop() {
    let t = 0;
    const loop = () => {
      if (!this.isRunning) return;
      t += 0.03;
      const now = performance.now() / 1000;
      const sway = Math.sin(t * 1.5) * 0.12;

      const simPose = {
        NOSE: { x: 0.5 + sway, y: 0.28, confidence: 0.95 },
        LEFT_SHOULDER: { x: 0.35 + sway, y: 0.45, confidence: 0.95 },
        RIGHT_SHOULDER: { x: 0.65 + sway, y: 0.45, confidence: 0.95 },
        LEFT_WRIST: { x: 0.32 + sway, y: 0.60 + Math.sin(t * 3) * 0.15, confidence: 0.9 },
        RIGHT_WRIST: { x: 0.68 + sway, y: 0.60 + Math.cos(t * 3) * 0.15, confidence: 0.9 },
      };
      this.processPose(simPose, now);
      this.renderDebugSkeleton();
      requestAnimationFrame(loop);
    };
    requestAnimationFrame(loop);
  }

  renderDebugSkeleton() {
    if (!this.canvas || !this.ctx || !this.currentPose) return;
    const w = this.canvas.width;
    const h = this.canvas.height;
    const ctx = this.ctx;

    ctx.clearRect(0, 0, w, h);

    const pose = this.currentPose;
    const points = [
      pose.NOSE, pose.LEFT_SHOULDER, pose.RIGHT_SHOULDER,
      pose.LEFT_ELBOW, pose.RIGHT_ELBOW, pose.LEFT_WRIST, pose.RIGHT_WRIST
    ].filter(Boolean);

    // Draw lines
    ctx.strokeStyle = '#22C55E';
    ctx.lineWidth = 3;
    ctx.shadowColor = '#22C55E';
    ctx.shadowBlur = 8;

    if (pose.LEFT_SHOULDER && pose.RIGHT_SHOULDER) {
      ctx.beginPath();
      ctx.moveTo(pose.LEFT_SHOULDER.x * w, pose.LEFT_SHOULDER.y * h);
      ctx.lineTo(pose.RIGHT_SHOULDER.x * w, pose.RIGHT_SHOULDER.y * h);
      ctx.stroke();
    }
    if (pose.LEFT_SHOULDER && pose.LEFT_WRIST) {
      ctx.beginPath();
      ctx.moveTo(pose.LEFT_SHOULDER.x * w, pose.LEFT_SHOULDER.y * h);
      ctx.lineTo(pose.LEFT_WRIST.x * w, pose.LEFT_WRIST.y * h);
      ctx.stroke();
    }
    if (pose.RIGHT_SHOULDER && pose.RIGHT_WRIST) {
      ctx.beginPath();
      ctx.moveTo(pose.RIGHT_SHOULDER.x * w, pose.RIGHT_SHOULDER.y * h);
      ctx.lineTo(pose.RIGHT_WRIST.x * w, pose.RIGHT_WRIST.y * h);
      ctx.stroke();
    }

    // Draw joints
    points.forEach(p => {
      ctx.fillStyle = '#A3E635';
      ctx.beginPath();
      ctx.arc(p.x * w, p.y * h, 6, 0, Math.PI * 2);
      ctx.fill();
    });

    ctx.shadowBlur = 0;
  }
}

window.motionEngine = new MotionEngine();
