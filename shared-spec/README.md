# Shared Specification: Computer Vision Motion & Gesture Engine

Tài liệu đặc tả kỹ thuật dùng chung giữa **iOS (Swift)** và **Android (Kotlin)** để đảm bảo tính nhất quán tuyệt đối về tọa độ, logic cử chỉ và độ nhạy của game.

---

## 1. Hệ Tọa Độ Chuẩn Hóa (Normalized Coordinate System)

- **Gốc tọa độ (0, 0):** Góc trên-trái màn hình (Top-Left).
- **Trục X:** Tăng dần từ trái sang phải, giá trị trong khoảng $[0.0, 1.0]$.
- **Trục Y:** Tăng dần từ trên xuống dưới, giá trị trong khoảng $[0.0, 1.0]$.
- **Hướng ảnh:** Đã xoay thẳng đứng theo chiều dọc thiết bị (Portrait).
- **Chế độ gương (Mirrored):** Luôn bật mirror cho camera trước.
  - Khi người dùng nghiêng sang bên phải cơ thể họ $\rightarrow$ hình ảnh trên màn hình nghiêng sang bên phải $\rightarrow$ giá trị $x$ tăng.
  - Tọa độ sau khi mirror phản ánh chính xác vị trí như khi người dùng soi gương.

---

## 2. Tập Khớp Chung (13 Joint Intersection)

Bộ 13 khớp giao nhau giữa Apple Vision Framework (`VNHumanBodyPoseObservation.JointName`) và Google ML Kit (`PoseLandmark`):

| Joint Key | Mô tả | Ứng dụng |
| :--- | :--- | :--- |
| `NOSE` | Mũi / Trung tâm đầu | Xác định vị trí đầu, độ cao cơ thể |
| `LEFT_SHOULDER` | Khớp vai trái | Tính độ rộng vai, trung điểm thân trên |
| `RIGHT_SHOULDER` | Khớp vai phải | Tính độ rộng vai, trung điểm thân trên |
| `LEFT_ELBOW` | Khuỷu tay trái | Vận tốc gập duỗi tay |
| `RIGHT_ELBOW` | Khuỷu tay phải | Vận tốc gập duỗi tay |
| `LEFT_WRIST` | Cổ tay trái | Nhận diện đấm, vươn tay chạm mục tiêu |
| `RIGHT_WRIST` | Cổ tay phải | Nhận diện đấm, vươn tay chạm mục tiêu |
| `LEFT_HIP` | Khớp háng / hông trái | Trục cột sống, tư thế đứng/ngồi |
| `RIGHT_HIP` | Khớp háng / hông phải | Trục cột sống, tư thế đứng/ngồi |
| `LEFT_KNEE` | Đầu gối trái | Nhận diện nhấc chân, squat |
| `RIGHT_KNEE` | Đầu gối phải | Nhận diện nhấc chân, squat |
| `LEFT_ANKLE` | Mắt cá chân trái | Thăng bằng 1 chân, đá chân |
| `RIGHT_ANKLE` | Mắt cá chân phải | Thăng bằng 1 chân, đá chân |

---

## 3. Công Thức Chuẩn Hóa & Ngưỡng Cử Chỉ (Normalization & Thresholds)

Mọi tính toán khoảng cách và vận tốc đều được chuẩn hóa theo **độ rộng vai (shoulderWidth)** của người dùng để loại trừ sai số khoảng cách xa/gần camera:

$$P_{mid} = \frac{P_{left\_shoulder} + P_{right\_shoulder}}{2}$$
$$shoulderWidth = \|P_{right\_shoulder} - P_{left\_shoulder}\|$$

### 3.1. Cử chỉ Nghiêng Thân (Sway Left / Sway Right)
- **Độ lệch chuẩn hóa:**
  $$s = \frac{P_{mid}.x - baseline.midX}{shoulderWidth}$$
- **Ngưỡng kích hoạt (Trigger Threshold):**
  - Nghiêng sang phải: $s > +0.35$
  - Nghiêng sang trái: $s < -0.35$
- **Hysteresis (Chống rung nhấp nháy):** Sau khi vào trạng thái nghiêng, chỉ thoát trạng thái khi $|s| < 0.20$ ($0.6 \times T$).

### 3.2. Cử chỉ Đấm / Vươn Tay Cao (Punch / Reach)
- **Khoảng cách mở rộng chuẩn hóa:**
  $$d_{reach} = \frac{\|P_{wrist} - P_{shoulder}\|}{shoulderWidth}$$
- **Vận tốc mở rộng:**
  $$v = \frac{d_{reach}(t) - d_{reach}(t - \Delta t)}{\Delta t}$$
- **Điều kiện kích hoạt:**
  - $v > 3.0 \text{ shoulderWidth/giây}$
  - $P_{wrist}.y < P_{shoulder}.y$ (Cổ tay phải nằm cao hơn vai trên màn hình)
  - Cooldown: $300\text{ ms}$ giữa hai cú đấm cùng bên.

### 3.3. Thăng Bằng Một Chân (Single-Leg Balance - Game Standing)
- **Kiểm tra toàn thân (Full Body Check):** Cả `NOSE`, 2 vai, 2 gối và 2 mắt cá đều có confidence $> 0.4$.
- **Độ cao nâng chân:** Chân nhấc lên khi $|P_{left\_ankle}.y - P_{right\_ankle}.y| > 0.08 \times bodyHeight$.
- **Độ thẳng cột sống:** Góc giữa vector $(P_{mid\_shoulder} \to P_{mid\_hip})$ với trục tung $< 10^\circ$.

---

## 4. Bộ Lọc Làm Mượt One-Euro Filter

Tần số cắt tối thiểu $\min_{cutoff} = 1.2$, hệ số tăng tốc $\beta = 0.02$, đạo hàm $d_{cutoff} = 1.0$.
Đảm bảo khi đứng yên tọa độ không bị rung jitter, và khi chuyển động nhanh không bị trễ lag.
