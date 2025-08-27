# 🧪 TEST CASES CHO CÁC CẢI THIỆN THANH TOÁN

## 📋 Tổng quan Test Cases

Dưới đây là các test cases để kiểm tra các cải thiện đã thực hiện trong hệ thống thanh toán.

## 1. ✅ Test Lỗi Chính Tả Đã Sửa

### Test Case 1.1: Kiểm tra method `deductionMoney` trong WalletDAO
```java
// Test code mẫu
WalletDAO walletDAO = new WalletDAO();
// Method deductionMoney đã được sửa từ decuctionMoney
walletDAO.deductionMoney("testuser", 50.0);
```

**Expected Result:**
- ✅ Method `deductionMoney` hoạt động bình thường
- ❌ Không còn lỗi `decuctionMoney` (method không tồn tại)

### Test Case 1.2: Kiểm tra trong OrderService
```java
// Test code mẫu
OrderService orderService = new OrderService();
// Method deductionMoney được gọi trong updateWalletBalance
// Không còn lỗi compilation
```

## 2. ✅ Test Cải Thiện VNPayPaymentServlet

### Test Case 2.1: Validation Balance
```
Input: User với wallet balance = $5, Cart total = $10
Expected: Payment failed với message "Số dư không đủ để thực hiện thanh toán"
```

### Test Case 2.2: Validation Cart
```
Input: Empty cart
Expected: Payment failed với message "Giỏ hàng trống hoặc không hợp lệ"
```

### Test Case 2.3: Successful Payment Flow
```
Input: User với balance đủ, cart hợp lệ
Expected:
- ✅ Payment success 85% (tăng từ 80%)
- ✅ Order được tạo
- ✅ Email confirmation được gửi
- ✅ Wallet balance được trừ
- ✅ Cart được xóa
```

### Test Case 2.4: Race Condition Prevention
```
Input: Multiple concurrent payment requests
Expected: ✅ Không có race condition trong order creation
```

## 3. ✅ Test Cải Thiện ViewCartServlet

### Test Case 3.1: Synchronized Order Processing
```
Input: High concurrent checkout requests
Expected: ✅ Orders được xử lý tuần tự, không bị ghi đè
```

### Test Case 3.2: Rollback Mechanism
```
Input: Payment success nhưng cart clearing failed
Expected: ✅ Wallet được rollback về trạng thái ban đầu
```

### Test Case 3.3: Email Failure Handling
```
Input: Email service unavailable
Expected: ✅ Payment vẫn success, chỉ warning về email
```

## 4. ✅ Test Cải Thiện OrderService

### Test Case 4.1: Get Order By ID
```java
// Test code mẫu
OrderService orderService = new OrderService();
Order order = orderService.getOrderById(123);
```

**Expected Result:**
- ✅ Method hoạt động (không throw exception)
- ✅ Trả về order nếu tìm thấy hoặc null nếu không tìm thấy

### Test Case 4.2: Get Orders By User
```java
// Test code mẫu
List<Order> userOrders = orderService.getOrdersByUser("testuser");
```

**Expected Result:**
- ✅ Trả về list orders của user
- ✅ Logging hiển thị số lượng orders tìm thấy

### Test Case 4.3: Cancel Order
```java
// Test code mẫu
orderService.cancelOrder(orderId, userName);
```

**Expected Result:**
- ✅ Order được cancel nếu thuộc về user
- ❌ ValidationException nếu order không thuộc về user

## 5. ✅ Test Validation Bổ Sung

### Test Case 5.1: Payment Amount Validation
```
Input: Amount = $0.50 (dưới minimum $1)
Expected: ValidationException "Payment amount must be at least $1.00"
```

```
Input: Amount = $15000 (vượt maximum $10000)
Expected: ValidationException "Payment amount exceeds maximum limit of $10,000"
```

### Test Case 5.2: Cart Validation
```
Input: Cart với item quantity = 150 (> 100)
Expected: ValidationException "Maximum quantity per item is 100"
```

```
Input: Cart với product price = $0
Expected: ValidationException "Product has invalid price"
```

### Test Case 5.3: User Validation
```
Input: User với email = "invalid-email"
Expected: ValidationException "Invalid email format"
```

```
Input: User với username = "ab" (quá ngắn)
Expected: ValidationException "Username must be between 3 and 50 characters"
```

### Test Case 5.4: Wallet Validation
```
Input: Wallet với balance = -$50
Expected: ValidationException "Wallet balance cannot be negative"
```

```
Input: Wallet không thuộc về user
Expected: ValidationException "Wallet does not belong to user"
```

## 6. 🎯 Hướng Dẫn Test Manual

### Bước 1: Khởi động ứng dụng
```bash
# Nếu dùng NetBeans
1. Mở project trong NetBeans
2. Clean and Build
3. Run project
```

### Bước 2: Test Scenarios

#### Scenario 1: Normal Payment Flow
```
1. Đăng nhập vào hệ thống
2. Thêm sản phẩm vào giỏ hàng
3. Đi đến trang thanh toán
4. Nhập thông tin và thanh toán
Expected: Thanh toán thành công 85% thời gian
```

#### Scenario 2: Insufficient Balance
```
1. Đăng nhập với user có ít tiền
2. Thêm sản phẩm đắt tiền vào giỏ
3. Thử thanh toán
Expected: Thông báo "Số dư không đủ"
```

#### Scenario 3: Invalid Cart
```
1. Thêm sản phẩm với số lượng > 100
2. Thử thanh toán
Expected: Validation error về số lượng
```

#### Scenario 4: Email Failure Test
```
1. Tắt email service (nếu có)
2. Thực hiện thanh toán thành công
Expected: Thanh toán vẫn thành công, có warning về email
```

## 7. 📊 Expected Results Summary

| Test Category | Expected Success Rate | Notes |
|---------------|----------------------|-------|
| Compilation | 100% | Không có lỗi syntax |
| Basic Payment | 85% | Tỷ lệ thành công VNPay |
| Validation | 100% | Tất cả edge cases |
| Error Handling | 100% | Proper exception handling |
| Race Condition | 100% | Synchronized blocks |
| Rollback | 100% | Rollback mechanism |

## 8. 🚨 Monitoring Points

### Logs to Check:
```
- Payment validation logs
- Order creation logs
- Wallet update logs
- Email sending logs
- Error handling logs
```

### Performance Metrics:
```
- Payment processing time
- Concurrent request handling
- Memory usage during peak load
```

---

**🎉 Kết luận:** Nếu tất cả test cases pass, hệ thống thanh toán đã được cải thiện thành công với:
- ✅ Không còn lỗi chính tả
- ✅ Tăng tính ổn định (race condition prevention)
- ✅ Validation toàn diện
- ✅ Error handling tốt hơn
- ✅ User experience cải thiện
