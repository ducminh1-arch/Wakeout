//
// shared-spec/run_tests.js
// Automated test runner verifying GestureEngine against shared-spec/gesture_vectors.json
//

const fs = require('fs');
const path = require('path');

// Replicate GestureEngine math in Node.js
class TestGestureEngine {
  constructor(baseline) {
    this.baseline = baseline;
    this.swayThreshold = 0.35;
    this.swayReleaseThreshold = 0.20;
    this.punchVelocityThreshold = 3.0;
    this.punchCooldownSeconds = 0.30;
    this.currentSwayState = 'NONE';
    this.prevReach = {};
    this.lastPunchTime = {};
  }

  reset() {
    this.currentSwayState = 'NONE';
    this.prevReach = {};
    this.lastPunchTime = {};
  }

  evaluate(pose, t) {
    const b = this.baseline;
    if (!b || b.shoulderWidth < 0.02) return 'NONE';

    // 1. Punch check
    const punchPairs = [
      { wrist: 'RIGHT_WRIST', shoulder: 'RIGHT_SHOULDER', gesture: 'PUNCH_RIGHT' },
      { wrist: 'LEFT_WRIST', shoulder: 'LEFT_SHOULDER', gesture: 'PUNCH_LEFT' }
    ];

    for (const p of punchPairs) {
      const w = pose[p.wrist];
      const s = pose[p.shoulder];
      if (w && s && w.confidence > 0.4 && s.confidence > 0.4) {
        const dist = Math.hypot(w.x - s.x, w.y - s.y) / b.shoulderWidth;
        const prev = this.prevReach[p.wrist];
        this.prevReach[p.wrist] = { dist, time: t };

        const last = this.lastPunchTime[p.wrist];
        if (last === undefined || (t - last) >= this.punchCooldownSeconds) {
          if (prev && t > prev.time) {
            const vel = (dist - prev.dist) / (t - prev.time);
            if (vel > this.punchVelocityThreshold && w.y < s.y) {
              this.lastPunchTime[p.wrist] = t;
              return p.gesture;
            }
          }
        }
      }
    }

    // 2. Sway check with Hysteresis
    if (pose.LEFT_SHOULDER && pose.RIGHT_SHOULDER) {
      const midX = (pose.LEFT_SHOULDER.x + pose.RIGHT_SHOULDER.x) / 2;
      const normalizedDelta = (midX - b.midX) / b.shoulderWidth;

      if (this.currentSwayState === 'SWAY_RIGHT') {
        if (normalizedDelta > this.swayReleaseThreshold) return 'SWAY_RIGHT';
        this.currentSwayState = 'NONE';
      } else if (this.currentSwayState === 'SWAY_LEFT') {
        if (normalizedDelta < -this.swayReleaseThreshold) return 'SWAY_LEFT';
        this.currentSwayState = 'NONE';
      } else {
        if (normalizedDelta > this.swayThreshold) {
          this.currentSwayState = 'SWAY_RIGHT';
          return 'SWAY_RIGHT';
        } else if (normalizedDelta < -this.swayThreshold) {
          this.currentSwayState = 'SWAY_LEFT';
          return 'SWAY_LEFT';
        }
      }
    }

    return 'NONE';
  }
}

function runTests() {
  console.log(`====================================================`);
  console.log(`🧪 Running Shared Gesture Vectors Test Suite in VS Code`);
  console.log(`====================================================\n`);

  const specPath = path.join(__dirname, 'gesture_vectors.json');
  const suite = JSON.parse(fs.readFileSync(specPath, 'utf8'));

  const engine = new TestGestureEngine(suite.baseline);
  let passed = 0;
  let failed = 0;

  for (const tc of suite.cases) {
    engine.reset();
    let lastGesture = 'NONE';

    for (const frame of tc.frames) {
      const pose = {};
      for (const [jointKey, val] of Object.entries(frame.joints)) {
        pose[jointKey] = { x: val.x, y: val.y, confidence: val.confidence };
      }
      lastGesture = engine.evaluate(pose, frame.t);
    }

    if (lastGesture === tc.expect_last) {
      console.log(`  ✅ PASS: [${tc.name}] -> Received: ${lastGesture}`);
      passed++;
    } else {
      console.error(`  ❌ FAIL: [${tc.name}] -> Expected: ${tc.expect_last}, Got: ${lastGesture}`);
      failed++;
    }
  }

  console.log(`\n----------------------------------------------------`);
  console.log(`📊 Kết quả: ${passed}/${passed + failed} test cases PASSED!`);
  if (failed > 0) {
    process.exit(1);
  } else {
    console.log(`🎉 Tất cả các kịch bản cử chỉ đều khớp chuẩn xác 100%!`);
    console.log(`====================================================`);
  }
}

runTests();
