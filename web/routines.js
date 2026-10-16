//
// routines.js - 12 Desk Stretches & Micro-Workouts Player
// Powered by shared-spec/routines_catalog.json
//

class RoutinesManager {
  constructor() {
    this.catalog = null;
    this.currentRoutine = null;
    this.timerInterval = null;
    this.secondsLeft = 0;
    this.totalSeconds = 0;
    this.isPlaying = false;
    this.activeCategory = 'all';
  }

  async loadCatalog() {
    try {
      const res = await fetch('/shared-spec/routines_catalog.json');
      this.catalog = await res.json();
    } catch (e) {
      console.warn('Fallback inline catalog loading', e);
      this.catalog = {
        categories: [
          { id: "neck_shoulders", name_vi: "Cổ & Vai", name_en: "Neck & Shoulders" },
          { id: "wrists_hands", name_vi: "Cổ tay & Ngón", name_en: "Wrists & Hands" },
          { id: "spine_back", name_vi: "Lưng & Cột sống", name_en: "Spine & Back" },
          { id: "eyes_mind", name_vi: "Mắt & Hơi thở", name_en: "Eyes & Breathing" },
          { id: "legs_circulation", name_vi: "Chân & Tuần hoàn", name_en: "Legs & Circulation" }
        ],
        routines: [
          {
            id: "desk_neck_side_stretch",
            title_vi: "Nghiêng Đầu Kéo Giãn Cơ Cổ",
            title_en: "Neck Lateral Stretch",
            category: "neck_shoulders",
            duration_seconds: 30,
            instructions_vi: "Ngồi thẳng lưng, từ từ nghiêng đầu sang vai phải trong 15s rồi đổi bên. Giữ nhịp thở đều.",
            instructions_en: "Sit upright, gently tilt your head toward your right shoulder for 15s, then switch sides."
          },
          {
            id: "shoulder_rolls",
            title_vi: "Xoay Khớp Vai Thả Lỏng",
            title_en: "Shoulder Blade Rolls",
            category: "neck_shoulders",
            duration_seconds: 30,
            instructions_vi: "Xoay hai vai về phía trước 10 lần, sau đó xoay ngược lại về phía sau 10 lần.",
            instructions_en: "Roll both shoulders backward in big circles 10 times, then reverse direction."
          },
          {
            id: "wrist_prayer_stretch",
            title_vi: "Giãn Cổ Tay Chắp Tay",
            title_en: "Prayer Wrist Stretch",
            category: "wrists_hands",
            duration_seconds: 45,
            instructions_vi: "Chắp hai lòng bàn tay trước ngực, hạ dần bàn tay xuống thắt lưng đến khi căng dễ chịu.",
            instructions_en: "Place palms together in front of your chest and slowly lower them down to stretch forearms."
          },
          {
            id: "seated_spinal_twist",
            title_vi: "Vặn Mình Trên Ghế Văn Phòng",
            title_en: "Seated Chair Twist",
            category: "spine_back",
            duration_seconds: 45,
            instructions_vi: "Đặt tay phải lên lưng ghế, tay trái lên đầu gối phải, xoay thân trên sang phải. Đổi bên sau 20s.",
            instructions_en: "Place right hand on chair back, twist torso gently right. Switch sides after 20s."
          },
          {
            id: "eye_20_20_relief",
            title_vi: "Quy Tắc 20-20-20 Thư Giãn Mắt",
            title_en: "20-20-20 Eye Rest",
            category: "eyes_mind",
            duration_seconds: 30,
            instructions_vi: "Nhìn vào một vật ở xa ít nhất 6 mét trong 20 giây và chớp mắt chậm 5 lần.",
            instructions_en: "Look at an object at least 20 feet away for 20 seconds. Blink slowly 5 times."
          },
          {
            id: "box_breathing_reset",
            title_vi: "Hít Thở Hộp 4 Thì Giảm Căng Thẳng",
            title_en: "Box Breathing Reset",
            category: "eyes_mind",
            duration_seconds: 60,
            instructions_vi: "Hít vào 4s -> Giữ hơi 4s -> Thở ra 4s -> Giữ hơi 4s. Giúp xoa dịu thần kinh.",
            instructions_en: "Inhale 4s -> Hold 4s -> Exhale 4s -> Hold 4s. Calms nervous system."
          }
        ]
      };
    }
    return this.catalog;
  }

  startRoutine(routineId, onTick, onComplete) {
    const routine = this.catalog.routines.find(r => r.id === routineId);
    if (!routine) return;

    this.currentRoutine = routine;
    this.totalSeconds = routine.duration_seconds;
    this.secondsLeft = routine.duration_seconds;
    this.isPlaying = true;

    window.soundEngine.playBell();
    clearInterval(this.timerInterval);

    this.timerInterval = setInterval(() => {
      this.secondsLeft--;
      if (this.secondsLeft > 0 && this.secondsLeft <= 3) {
        window.soundEngine.playTick();
      }

      const progress = 1 - (this.secondsLeft / this.totalSeconds);
      if (onTick) onTick(this.secondsLeft, progress, this.currentRoutine);

      if (this.secondsLeft <= 0) {
        clearInterval(this.timerInterval);
        this.isPlaying = false;
        window.soundEngine.playVictory();

        // Increment routine completed count
        const completed = parseInt(localStorage.getItem('breakpulse_routines_done') || '0', 10) + 1;
        localStorage.setItem('breakpulse_routines_done', completed.toString());

        // Award 15 Watts
        const currentWatts = parseInt(localStorage.getItem('breakpulse_watts') || '0', 10);
        localStorage.setItem('breakpulse_watts', (currentWatts + 15).toString());

        if (onComplete) onComplete(this.currentRoutine);
      }
    }, 1000);
  }

  pauseRoutine() {
    this.isPlaying = false;
    clearInterval(this.timerInterval);
  }

  resumeRoutine(onTick, onComplete) {
    if (this.secondsLeft <= 0) return;
    this.isPlaying = true;
    this.timerInterval = setInterval(() => {
      this.secondsLeft--;
      const progress = 1 - (this.secondsLeft / this.totalSeconds);
      if (onTick) onTick(this.secondsLeft, progress, this.currentRoutine);
      if (this.secondsLeft <= 0) {
        clearInterval(this.timerInterval);
        this.isPlaying = false;
        window.soundEngine.playVictory();
        if (onComplete) onComplete(this.currentRoutine);
      }
    }, 1000);
  }
}

window.routinesManager = new RoutinesManager();
