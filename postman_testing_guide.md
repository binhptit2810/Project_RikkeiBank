# Rikkei Bank API - Hướng dẫn Kiểm thử Toàn diện trên Postman (13 Chức năng)

Tài liệu này hướng dẫn chi tiết cách kiểm thử toàn bộ 13 chức năng của hệ thống Rikkei Bank (từ Ngày 1 đến Ngày 3) bằng công cụ Postman.

---

## 1. Xác thực người dùng (Điều kiện tiên quyết)

Tất cả các endpoint (ngoại trừ các API đăng ký/đăng nhập/quên mật khẩu trong `/api/auth/**`) đều yêu cầu mã token JWT Bearer trong header `Authorization`.

### A. Đăng ký tài khoản mới (Register - FR-01)
* **Phương thức**: `POST`
* **Đường dẫn (URL)**: `http://localhost:8080/api/auth/register`
* **Headers**: `Content-Type: application/json`
* **Body** (raw JSON):
  ```json
  {
    "fullName": "Nguyen Van A",
    "username": "customer1",
    "email": "customer1@rikkei.com",
    "phone": "0987654321",
    "password": "Password123"
  }
  ```

### B. Đăng nhập hệ thống (Login - FR-01)
* **Phương thức**: `POST`
* **Đường dẫn (URL)**: `http://localhost:8080/api/auth/login`
* **Headers**: `Content-Type: application/json`
* **Body** (raw JSON):
  ```json
  {
    "username": "customer1",
    "password": "Password123"
  }
  ```
* **Phản hồi trả về (Response)**: Hệ thống trả về `accessToken` và `refreshToken` (Chức năng FR-02).
  Hãy lưu hai chuỗi này để kiểm thử cho các bước tiếp theo. Trong tất cả các yêu cầu tiếp theo cần xác thực, vào tab **Authorization** trên Postman, chọn **Type**: `Bearer Token` và dán `accessToken` vào.

---

## 2. FR-02: Xoay vòng Token (Refresh Token Rotation)

Khi `accessToken` hết hạn, sử dụng `refreshToken` để lấy cặp token mới mà không bắt người dùng đăng nhập lại. Cơ chế rotation sẽ thu hồi/xóa Refresh Token cũ để sinh cái mới.

* **Phương thức**: `POST`
* **Đường dẫn (URL)**: `http://localhost:8080/api/auth/refresh`
* **Headers**: `Content-Type: application/json`
* **Body** (raw JSON):
  ```json
  {
    "refreshToken": "CHUỖI_REFRESH_TOKEN_NHẬN_ĐƯỢC_LÚC_LOGIN"
  }
  ```
* **Kết quả**: Nhận được `accessToken` mới và `refreshToken` mới đã xoay vòng.

---

## 3. FR-03: Đăng xuất (Revoke Token)

* **Phương thức**: `POST`
* **Đường dẫn (URL)**: `http://localhost:8080/api/auth/logout`
* **Headers**:
  * `Authorization`: `Bearer <MÃ_TOKEN_JWT_HIỆN_TẠI>`
* **Kết quả**: Token hiện tại sẽ bị đẩy vào Redis Blacklist (FR-13) với thời gian sống còn lại (TTL). Refresh token của tài khoản cũng bị thu hồi khỏi Database. Mọi yêu cầu gọi API bằng token này sau khi logout sẽ trả về lỗi `403 Forbidden` / `401 Unauthorized`.

---

## 4. FR-04: Đăng ký mở tài khoản (Tải lên eKYC)

* **Phương thức**: `POST`
* **Đường dẫn (URL)**: `http://localhost:8080/api/kyc/submit`
* **Headers**:
  * `Authorization`: `Bearer <MÃ_TOKEN_JWT>`
* **Body**: Chọn **form-data** và nhập:
  
  | Key | Type | Value | Mô tả |
  | :--- | :--- | :--- | :--- |
  | `userId` | `Text` | `1` | ID của người dùng (từ API đăng ký) |
  | `idNumber` | `Text` | `123456789` | Số CCCD / Hộ chiếu |
  | `fullName` | `Text` | `Nguyen Van A` | Họ tên |
  | `dob` | `Text` | `1995-10-15` | Ngày sinh |
  | `sex` | `Text` | `MALE` | Giới tính |
  | `address` | `Text` | `Ha Noi` | Địa chỉ |
  | `frontImage` | `File` | *(Chọn ảnh mặt trước)* | Tải ảnh thật lên |

---

## 5. FR-05: Quản lý Người dùng & Tài khoản (CRUD, Phân trang)

### A. Quản lý Người dùng
* **Lấy danh sách người dùng (Phân trang)**: `GET` `http://localhost:8080/api/users?page=0&size=5` (Header `Authorization`)
* **Lấy chi tiết**: `GET` `http://localhost:8080/api/users/1`
* **Cập nhật thông tin**: `PUT` `http://localhost:8080/api/users/1`
  * Body (JSON):
    ```json
    {
      "fullName": "Nguyen Van B",
      "email": "customer1_new@rikkei.com",
      "phoneNumber": "0987654322",
      "isActive": true
    }
    ```
* **Xóa**: `DELETE` `http://localhost:8080/api/users/1`

### B. Quản lý Tài khoản ngân hàng
* **Mở tài khoản**: `POST` `http://localhost:8080/api/accounts/users/1` (Hệ thống trả về số tài khoản mới dạng `RBxxxxxxxxx`)
* **Lấy danh sách tài khoản (Phân trang)**: `GET` `http://localhost:8080/api/accounts?page=0&size=5`
* **Cập nhật tài khoản (Cài mã PIN giao dịch)**: `PUT` `http://localhost:8080/api/accounts/1`
  * Body (JSON):
    ```json
    {
      "currency": "VND",
      "transactionPin": "123456",
      "active": true
    }
    ```
* **Đóng/Xóa tài khoản**: `DELETE` `http://localhost:8080/api/accounts/1`

---

## 6. FR-06: Vấn tin số dư tài khoản

* **Phương thức**: `GET`
* **Đường dẫn (URL)**: `http://localhost:8080/api/accounts/SỐ_TÀI_KHOẢN/balance`
  * Ví dụ: `http://localhost:8080/api/accounts/RB8563248967/balance`
* **Headers**: `Authorization: Bearer <MÃ_TOKEN_JWT>`

---

## 7. FR-07: Chuyển tiền (Nội bộ / Liên ngân hàng)

### A. Chuyển tiền nội bộ (Trong hệ thống Rikkei Bank)
* **Phương thức**: `POST`
* **Đường dẫn (URL)**: `http://localhost:8080/api/transactions/transfer`
* **Headers**: `Authorization: Bearer <MÃ_TOKEN_JWT>`
* **Body** (raw JSON):
  ```json
  {
    "fromAccountNumber": "SỐ_TÀI_KHOẢN_NGUỒN",
    "toAccountNumber": "SỐ_TÀI_KHOẢN_ĐÍCH",
    "amount": 5000.00,
    "description": "Chuyen khoan noi bo"
  }
  ```

### B. Chuyển tiền liên ngân hàng (Interbank)
* **Phương thức**: `POST`
* **Đường dẫn (URL)**: `http://localhost:8080/api/transactions/transfer-interbank`
* **Headers**: `Authorization: Bearer <MÃ_TOKEN_JWT>`
* **Body** (raw JSON):
  ```json
  {
    "fromAccountNumber": "SỐ_TÀI_KHOẢN_NGUỒN",
    "toAccountNumber": "970403123456789", // Số tài khoản ngoài hệ thống
    "bankName": "Vietcombank",
    "amount": 10000.00,
    "description": "Chuyen tien lien ngan hang",
    "transactionPin": "123456" // Mã PIN giao dịch
  }
  ```

---

## 8. FR-08: Xem sao kê lịch sử giao dịch (Phân trang)

* **Phương thức**: `GET`
* **Đường dẫn (URL)**: `http://localhost:8080/api/transactions/accounts/SỐ_TÀI_KHOẢN?page=0&size=10`
  * Ví dụ: `http://localhost:8080/api/transactions/accounts/RB8563248967?page=0&size=10`
* **Headers**: `Authorization: Bearer <MÃ_TOKEN_JWT>`

---

## 9. FR-09: Phê duyệt hồ sơ định danh (Duyệt eKYC)

* **Phương thức**: `PUT`
* **Đường dẫn (URL)**: `http://localhost:8080/api/kyc/ID_HỒ_SƠ_KYC/verify`
  * Ví dụ: `http://localhost:8080/api/kyc/1/verify`
* **Headers**: `Authorization: Bearer <MÃ_TOKEN_JWT>`
* **Body** (raw JSON):
  ```json
  {
    "status": "CONFIRM" // Hoặc "REJECT" để từ chối
  }
  ```

---

## 10. FR-10: Đổi mã PIN / Quên mật khẩu

### A. Đổi mã PIN
* **Phương thức**: `POST`
* **Đường dẫn (URL)**: `http://localhost:8080/api/accounts/SỐ_TÀI_KHOẢN/change-pin`
* **Headers**: `Authorization: Bearer <MÃ_TOKEN_JWT>`
* **Body** (raw JSON):
  ```json
  {
    "oldPin": "123456",
    "newPin": "654321"
  }
  ```

### B. Quên mật khẩu (2 Bước)
1. **Yêu cầu sinh OTP**:
   * `POST` `http://localhost:8080/api/auth/forgot-password`
   * Body (JSON): `{"username": "customer1"}`
   * **Cách lấy OTP**: Xem trên tab Console Log chạy ứng dụng Spring Boot (có in dòng OTP 6 chữ số).
2. **Đặt lại mật khẩu**:
   * `POST` `http://localhost:8080/api/auth/reset-password`
   * Body (JSON):
     ```json
     {
       "username": "customer1",
       "otp": "MÃ_OTP_LẤY_TỪ_CONSOLE",
       "newPassword": "NewSecurePassword123"
     }
     ```

---

## 11. FR-11: Ghi log thời gian thực hiện cho tất cả các chức năng (Aspect AOP)

Tính năng này hoạt động ngầm (AOP). Khi bạn thực hiện bất kỳ yêu cầu nào ở trên, hãy kiểm tra console log của Spring Boot. Mỗi lần phương thức Service được gọi, hệ thống sẽ in log dạng:
```text
[INFO] Start execution: AccountService.getBalance(...)
[INFO] Success execution: AccountService.getBalance(...) in 15 ms
```

---

## 12. FR-12: Chạy kiểm thử tự động (Unit Test)

Bạn có thể chạy kiểm thử toàn bộ 12 test case (7 Service test và 5 Controller test) bằng lệnh:
```powershell
.\gradlew.bat test
```
Báo cáo kiểm thử chi tiết dạng HTML sẽ được sinh ra tại: `build/reports/tests/test/index.html`.

---

## 13. FR-13: Redis Token Blacklist (Tối ưu hóa tránh nghẽn cổ chai)

Thay vì lưu token bị thu hồi trong bảng MySQL `token_blacklist` gây chậm hệ thống do phải truy vấn DB liên tục ở mỗi request, hệ thống sử dụng Redis.
* Khi gọi API **/logout**, JWT token được ghi nhận vào Redis dưới key `blacklist:MÃ_TOKEN` với thời gian hết hạn tự động (TTL) bằng thời gian sống còn lại của JWT.
* Ở mỗi Request, bộ lọc bảo mật `JwtAuthFilter` kiểm tra Redis siêu tốc bằng lệnh `stringRedisTemplate.hasKey(...)` trước khi xử lý, giải quyết hoàn toàn vấn đề tắc nghẽn cổ chai database.
