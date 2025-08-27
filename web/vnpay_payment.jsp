<%--
    Document   : vnpay_payment
    Created on : Dec 28, 2024
    Author     : VNPay Demo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>VNPay Payment - Perfume Shop</title>
        <link rel="icon" href="images/logo1.png"/>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"/>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"/>
        <style>
            body {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                font-family: 'Arial', sans-serif;
            }

            .payment-container {
                background: rgba(255, 255, 255, 0.95);
                border-radius: 20px;
                box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
                padding: 40px;
                margin: 50px auto;
                max-width: 600px;
            }

            .payment-header {
                text-align: center;
                margin-bottom: 40px;
            }

            .payment-header h2 {
                color: #2c3e50;
                font-weight: 700;
                margin-bottom: 10px;
            }

            .payment-header .subtitle {
                color: #7f8c8d;
                font-size: 16px;
            }

            .payment-card {
                background: #f8f9fa;
                border-radius: 15px;
                padding: 25px;
                margin-bottom: 30px;
                border: 2px solid #e9ecef;
            }

            .payment-amount {
                background: linear-gradient(45deg, #ff6b6b, #ffa500);
                color: white;
                padding: 20px;
                border-radius: 15px;
                text-align: center;
                margin-bottom: 25px;
            }

            .amount-display {
                font-size: 36px;
                font-weight: bold;
                margin-bottom: 5px;
            }

            .amount-label {
                font-size: 14px;
                opacity: 0.9;
            }

            .form-group {
                margin-bottom: 20px;
            }

            .form-group label {
                font-weight: 600;
                color: #2c3e50;
                margin-bottom: 8px;
            }

            .form-control {
                border: 2px solid #e1e8ed;
                border-radius: 10px;
                padding: 12px 20px;
                font-size: 16px;
                transition: all 0.3s ease;
            }

            .form-control:focus {
                border-color: #667eea;
                box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
            }

            .payment-methods {
                display: flex;
                justify-content: space-around;
                margin: 30px 0;
            }

            .payment-method {
                text-align: center;
                padding: 15px;
                border: 2px solid #e1e8ed;
                border-radius: 10px;
                cursor: pointer;
                transition: all 0.3s ease;
                background: white;
            }

            .payment-method:hover {
                border-color: #667eea;
                background: #f8f9ff;
            }

            .payment-method.selected {
                border-color: #667eea;
                background: #667eea;
                color: white;
            }

            .payment-method i {
                font-size: 24px;
                margin-bottom: 8px;
            }

            .btn-payment {
                background: linear-gradient(45deg, #667eea, #764ba2);
                border: none;
                border-radius: 12px;
                padding: 15px 40px;
                font-size: 18px;
                font-weight: 600;
                color: white;
                width: 100%;
                cursor: pointer;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
            }

            .btn-payment:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
            }

            .security-badge {
                text-align: center;
                margin-top: 20px;
                color: #7f8c8d;
                font-size: 14px;
            }

            .demo-notice {
                background: #fff3cd;
                border: 1px solid #ffeaa7;
                color: #856404;
                padding: 15px;
                border-radius: 10px;
                margin-bottom: 25px;
                text-align: center;
            }

            .demo-notice strong {
                color: #d69e2e;
            }
        </style>
    </head>
    <body>
        <header style="background-color: black; padding: 15px 40px; display: flex; justify-content: space-between; align-items: center">
            <div class="col-lg-2">
                <div class="logo">
                    <a href="home"><img src="images/logo.png" alt=""></a>
                </div>
            </div>
            <div class="col-lg-10">
                <jsp:include page="header_right.jsp"></jsp:include>
            </div>
        </header>

        <div class="container">
            <div class="payment-container">
                <div class="demo-notice">
                    <i class="fa fa-info-circle"></i>
                    <strong>Demo Mode:</strong> Đây là hệ thống thanh toán VNPay thử nghiệm cho mục đích demo. Không có giao dịch thực tế nào được thực hiện.
                </div>

                <div class="payment-header">
                    <h2><i class="fa fa-credit-card"></i> Thanh Toán VNPay</h2>
                    <div class="subtitle">Hoàn tất đơn hàng của bạn</div>
                </div>

                <div class="payment-amount">
                    <div class="amount-display">
                        $${sessionScope.cart.getTotalMoney() + 3}
                    </div>
                    <div class="amount-label">Tổng tiền (bao gồm phí vận chuyển)</div>
                </div>

                <div class="payment-card">
                    <h5><i class="fa fa-shopping-cart"></i> Chi tiết đơn hàng</h5>
                    <div class="row">
                        <div class="col-sm-6">
                            <p><strong>Số lượng sản phẩm:</strong> ${sessionScope.cartSize}</p>
                        </div>
                        <div class="col-sm-6">
                            <p><strong>Phí vận chuyển:</strong> $3</p>
                        </div>
                    </div>
                </div>

                <form action="vnpay_payment" method="post" id="paymentForm">
                    <div class="payment-card">
                        <h5><i class="fa fa-user"></i> Thông tin thanh toán</h5>

                        <div class="form-group">
                            <label for="customerName">Họ tên:</label>
                            <input type="text" class="form-control" id="customerName" name="customerName"
                                   value="${sessionScope.account.fullName}" required>
                        </div>

                        <div class="form-group">
                            <label for="customerEmail">Email:</label>
                            <input type="email" class="form-control" id="customerEmail" name="customerEmail"
                                   value="${sessionScope.account.email}" required>
                        </div>

                        <div class="form-group">
                            <label for="customerPhone">Số điện thoại:</label>
                            <input type="tel" class="form-control" id="customerPhone" name="customerPhone"
                                   value="${sessionScope.account.phone}" required>
                        </div>

                        <div class="form-group">
                            <label for="customerAddress">Địa chỉ giao hàng:</label>
                            <textarea class="form-control" id="customerAddress" name="customerAddress"
                                      rows="3" required>${sessionScope.account.address}</textarea>
                        </div>

                        <div class="form-group">
                            <label for="orderDescription">Ghi chú đơn hàng:</label>
                            <textarea class="form-control" id="orderDescription" name="orderDescription"
                                      rows="2" placeholder="Nhập ghi chú (tùy chọn)"></textarea>
                        </div>
                    </div>

                    <div class="payment-methods">
                        <div class="payment-method selected" data-method="vnpay">
                            <i class="fa fa-mobile"></i>
                            <div>VNPay QR</div>
                        </div>
                        <div class="payment-method" data-method="atm">
                            <i class="fa fa-credit-card"></i>
                            <div>Thẻ ATM</div>
                        </div>
                        <div class="payment-method" data-method="credit">
                            <i class="fa fa-credit-card-alt"></i>
                            <div>Thẻ tín dụng</div>
                        </div>
                    </div>

                    <button type="submit" class="btn-payment">
                        <i class="fa fa-lock"></i> Thanh Toán Bằng VNPay
                    </button>
                </form>

                <div class="security-badge">
                    <i class="fa fa-shield"></i>
                    Bảo mật bởi VNPay - SSL 256-bit encryption
                </div>
            </div>
        </div>

        <%@ include file="footer.jsp"%>

        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
        <script>
            $(document).ready(function() {
                $('.payment-method').click(function() {
                    $('.payment-method').removeClass('selected');
                    $(this).addClass('selected');
                });

                $('#paymentForm').submit(function(e) {
                    var selectedMethod = $('.payment-method.selected').data('method');
                    if (!selectedMethod) {
                        alert('Vui lòng chọn phương thức thanh toán!');
                        e.preventDefault();
                        return false;
                    }

                    // Hiển thị loading
                    $('button[type="submit"]').html('<i class="fa fa-spinner fa-spin"></i> Đang xử lý...');
                    $('button[type="submit"]').prop('disabled', true);
                });
            });
        </script>
    </body>
</html>
