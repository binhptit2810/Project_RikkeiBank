# Báo Cáo Kế Hoạch & Kết Quả Kiểm Thử (Day 1 & Day 2)

Tài liệu này báo cáo chi tiết kịch bản kiểm thử (Test Cases) và cách thức thực hiện kiểm thử trên Postman cho toàn bộ các chức năng thuộc giai đoạn **Day 1 và Day 2** của dự án Rikkei Bank.

---

## I. THÔNG TIN CHUNG
* **Dự án**: Rikkei Bank API
* **Môi trường chạy thử**: `http://localhost:8080`
* **Công cụ kiểm thử**: Postman Client
* **Cơ chế xác thực**: JWT Bearer Token (gửi kèm trong header `Authorization: Bearer <token>`)

---

## II. DANH SÁCH KỊCH BẢN KIỂM THỬ (TEST CASES)

### 1. FR-04: Đăng ký mở tài khoản & Tải lên eKYC

| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR04-01** | Đăng ký tài khoản người dùng mới thành công | 1. Gọi `POST /api/auth/register`<br>2. Gửi body JSON chứa thông tin người dùng chưa tồn tại (username, email, phone). | Trả về thông tin người dùng đã tạo thành công và đồng thời tự động khởi tạo Hồ sơ eKYC mặc định (trạng thái PENDING). | **PASS** |
| **TC-FR04-02** | Đăng ký thất bại do trùng tên đăng nhập hoặc email | 1. Gọi `POST /api/auth/register`<br>2. Gửi body JSON chứa username hoặc email đã đăng ký trước đó. | Trả về mã lỗi 400 hoặc 500 kèm thông điệp báo trùng (Ví dụ: "Username already exists"). | **PASS** |
| **TC-FR04-03** | Tải lên tài liệu eKYC thành công (Lưu cục bộ) | 1. Đăng nhập để lấy Token JWT.<br>2. Gọi `POST /api/kyc/submit` dưới dạng **form-data**.<br>3. Truyền các tham số: `userId=1`, `idNumber=123456`, `fullName=Nguyen Van A`, `dob=1995-10-15`, `sex=MALE`, `address=Hanoi` và chọn tệp ảnh thực tế tải lên cho trường `frontImage`. | Hệ thống lưu ảnh thành công vào thư mục `uploads` của dự án và trả về thông tin KycProfile cập nhật kèm đường dẫn `/uploads/<tên_ảnh>.jpg`. | **PASS** |

---

### 2. FR-05: Quản lý Người dùng & Tài khoản (CRUD, Phân trang)

#### A. Quản lý Người dùng
| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR05-U01** | Lấy danh sách Người dùng có phân trang | 1. Đăng nhập lấy Token.<br>2. Gọi `GET /api/users?page=0&size=5`. | Trả về danh sách người dùng được chia trang dưới định dạng của Spring Data Page (chứa tổng số bản ghi, thông tin trang). | **PASS** |
| **TC-FR05-U02** | Cập nhật thông tin Người dùng | 1. Gọi `PUT /api/users/1` với body JSON chứa thông tin email mới, số điện thoại mới, họ tên mới. | Trả về thông tin người dùng đã được cập nhật thành công (họ tên mới được cập nhật đồng bộ sang KycProfile). | **PASS** |
| **TC-FR05-U03** | Xóa Người dùng | 1. Gọi `DELETE /api/users/1`. | Trả về thông điệp xóa người dùng thành công khỏi database. | **PASS** |

#### B. Quản lý Tài khoản Ngân hàng
| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR05-A01** | Tạo tài khoản mới cho Người dùng | 1. Gọi `POST /api/accounts/users/1`. | Tạo tài khoản mới thành công với số tài khoản tự động sinh có tiền tố `RB...`, số dư mặc định `0 VND`, trạng thái hoạt động `active = true`. | **PASS** |
| **TC-FR05-A02** | Lấy danh sách tài khoản (Phân trang) | 1. Gọi `GET /api/accounts?page=0&size=5`. | Trả về danh sách phân trang tất cả tài khoản ngân hàng trong hệ thống. | **PASS** |
| **TC-FR05-A03** | Cập nhật thông tin Tài khoản | 1. Gọi `PUT /api/accounts/1` với body chứa đơn vị tiền tệ (`currency=USD`), mã PIN mới (`transactionPin=123456`). | Cập nhật thành công thông tin tài khoản và trả về bản ghi mới. | **PASS** |

---

### 3. FR-06: Vấn tin số dư tài khoản

| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR06-01** | Vấn tin số dư tài khoản hợp lệ | 1. Gọi `GET /api/accounts/<SỐ_TÀI_KHOẢN>/balance` (Ví dụ: `RB1234567890/balance`). | Trả về thông tin số dư chính xác, số tài khoản và loại tiền tệ đang sử dụng. | **PASS** |
| **TC-FR06-02** | Vấn tin thất bại do tài khoản không tồn tại | 1. Gọi `GET /api/accounts/RB9999999999/balance` (Số tài khoản không tồn tại). | Trả về lỗi 404 kèm thông điệp "Account not found". | **PASS** |

---

### 4. FR-07: Chuyển tiền (Nội bộ / Liên ngân hàng)

#### A. Chuyển tiền Nội bộ
| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR07-I01** | Chuyển tiền nội bộ thành công | 1. Gọi `POST /api/transactions/transfer`. <br>2. Truyền tài khoản nguồn, tài khoản đích và số tiền hợp lệ. | Tài khoản nguồn bị trừ tiền, tài khoản đích được cộng tiền. Hệ thống tạo bản ghi giao dịch thành công. | **PASS** |
| **TC-FR07-I02** | Chuyển tiền thất bại do không đủ số dư | 1. Gọi `POST /api/transactions/transfer` với số tiền lớn hơn số dư tài khoản nguồn. | Trả về lỗi 500 kèm thông điệp "Insufficient balance". | **PASS** |

#### B. Chuyển tiền Liên ngân hàng
| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR07-E01** | Chuyển tiền liên ngân hàng thành công | 1. Gọi `POST /api/transactions/transfer-interbank`. <br>2. Gửi body JSON chứa tài khoản nguồn, số tài khoản nhận ngoại mạng, tên ngân hàng đích, số tiền và mã PIN giao dịch chính xác. | Tài khoản nguồn bị trừ tiền. Hệ thống lưu trữ thông tin ngân hàng nhận ngoại mạng và trả về giao dịch thành công. | **PASS** |
| **TC-FR07-E02** | Chuyển tiền liên ngân hàng thất bại do sai mã PIN | 1. Thực hiện chuyển khoản liên ngân hàng nhưng nhập sai mã PIN giao dịch. | Hệ thống từ chối giao dịch, trả về thông điệp báo sai mã PIN. | **PASS** |

---

### 5. FR-08: Xem sao kê lịch sử giao dịch

| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR08-01** | Xem sao kê lịch sử giao dịch | 1. Gọi `GET /api/transactions/accounts/<SỐ_TÀI_KHOẢN>` | Trả về toàn bộ danh sách lịch sử giao dịch (nội bộ, liên ngân hàng) liên quan đến tài khoản này, sắp xếp giảm dần theo thời gian. | **PASS** |

---

### 6. FR-09: Phê duyệt hồ sơ định danh (Duyệt eKYC)

| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR09-01** | Duyệt hồ sơ eKYC thành công (CONFIRM) | 1. Gọi `PUT /api/kyc/<KYC_PROFILE_ID>/verify` với body JSON `{"status": "CONFIRM"}`. | Trạng thái KYC Profile chuyển sang `CONFIRM`. Hệ thống cập nhật trường `isKyc` của User liên kết thành `true`. | **PASS** |
| **TC-FR09-02** | Từ chối hồ sơ eKYC (REJECT) | 1. Gọi `PUT /api/kyc/<KYC_PROFILE_ID>/verify` với body JSON `{"status": "REJECT"}`. | Trạng thái KYC Profile chuyển sang `REJECT`. Hệ thống giữ trường `isKyc` của User là `false`. | **PASS** |

---

### 7. FR-10: Đổi mã PIN / Quên mật khẩu

#### A. Đổi mã PIN giao dịch
| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR10-P01** | Đổi mã PIN giao dịch thành công | 1. Gọi `POST /api/accounts/<SỐ_TÀI_KHOẢN>/change-pin`. <br>2. Nhập `oldPin` chính xác và `newPin` mới. | Mã PIN giao dịch của tài khoản được cập nhật mới thành công. | **PASS** |

#### B. Quên mật khẩu & Cấp lại bằng OTP
| Mã kịch bản | Tên kịch bản | Các bước thực hiện | Kết quả mong đợi | Trạng thái |
| :--- | :--- | :--- | :--- | :---: |
| **TC-FR10-F01** | Gửi yêu cầu quên mật khẩu để nhận OTP | 1. Gọi `POST /api/auth/forgot-password` với username của người dùng. | Hệ thống sinh ngẫu nhiên mã OTP 6 chữ số và ghi nhận mã này ra màn hình Console/Log của server Spring Boot. | **PASS** |
| **TC-FR10-F02** | Reset mật khẩu mới bằng OTP thành công | 1. Lấy mã OTP từ màn hình console log.<br>2. Gọi `POST /api/auth/reset-password` truyền username, mã OTP và mật khẩu mới hợp lệ. | Mật khẩu tài khoản được cập nhật mới thành công. Người dùng có thể đăng nhập bằng mật khẩu mới này. | **PASS** |
