<%--
    Document   : vnpay_result
    Created on : Dec 28, 2024
    Author     : VNPay Demo Team
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Kết Quả Thanh Toán VNPay - Perfume Shop</title>
        <link rel="icon" href="images/logo1.png"/>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"/>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css"/>
        <style>
            body {
                background: linear-gradient(135deg,
                    <c:choose>
                        <c:when test="${payment_status == 'success'}">#667eea 0%, #764ba2 100%</c:when>
                        <c:otherwise>#ff6b6b 0%, #ffa500 100%</c:otherwise>
                    </c:choose>);
                min-height: 100vh;
                font-family: 'Arial', sans-serif;
            }

            .result-container {
                background: rgba(255, 255, 255, 0.95);
                border-radius: 20px;
                box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
                padding: 40px;
                margin: 50px auto;
                max-width: 700px;
                text-align: center;
            }

            .result-icon {
                font-size: 80px;
                margin-bottom: 20px;
            }

            .success-icon {
                color: #28a745;
            }

            .error-icon {
                color: #dc3545;
            }

            .result-title {
                font-size: 32px;
                font-weight: bold;
                margin-bottom: 10px;
                color: #2c3e50;
            }

            .result-message {
                font-size: 18px;
                color: #7f8c8d;
                margin-bottom: 30px;
            }

            .order-details {
                background: #f8f9fa;
                border-radius: 15px;
                padding: 25px;
                margin: 30px 0;
                border: 2px solid #e9ecef;
            }

            .detail-row {
                display: flex;
                justify-content: space-between;
                padding: 8px 0;
                border-bottom: 1px solid #e9ecef;
            }

            .detail-row:last-child {
                border-bottom: none;
            }

            .detail-label {
                font-weight: 600;
                color: #2c3e50;
            }

            .detail-value {
                color: #495057;
                font-weight: 500;
            }

            .amount-highlight {
                font-size: 24px;
                font-weight: bold;
                color: #28a745;
            }

            .action-buttons {
                margin-top: 40px;
            }

            .btn-primary-custom {
                background: linear-gradient(45deg, #667eea, #764ba2);
                border: none;
                border-radius: 10px;
                padding: 12px 30px;
                font-size: 16px;
                font-weight: 600;
                color: white;
                text-decoration: none;
                display: inline-block;
                margin: 0 10px;
                transition: all 0.3s ease;
            }

            .btn-primary-custom:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
                color: white;
                text-decoration: none;
            }

            .btn-secondary-custom {
                background: #6c757d;
                border: none;
                border-radius: 10px;
                padding: 12px 30px;
                font-size: 16px;
                font-weight: 600;
                color: white;
                text-decoration: none;
                display: inline-block;
                margin: 0 10px;
                transition: all 0.3s ease;
            }

            .btn-secondary-custom:hover {
                background: #5a6268;
                transform: translateY(-2px);
                color: white;
                text-decoration: none;
            }

            .demo-notice {
                background: #fff3cd;
                border: 1px solid #ffeaa7;
                color: #856404;
                padding: 15px;
                border-radius: 10px;
                margin-top: 30px;
                font-size: 14px;
            }

            @media (max-width: 768px) {
                .result-container {
                    margin: 20px;
                    padding: 20px;
                }

                .result-title {
                    font-size: 24px;
                }

                .action-buttons .btn {
                    display: block;
                    margin: 10px 0;
                    width: 100%;
                }
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
            <div class="result-container">
                <c:choose>
                    <c:when test="${payment_status == 'success'}">
                        <!-- Success Result -->
                        <div class="result-icon success-icon">
                            <i class="fa fa-check-circle"></i>
                        </div>
                        <div class="result-title">Thanh Toán Thành Công!</div>
                        <div class="result-message">
                            Đơn hàng của bạn đã được xử lý thành công. Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!
                        </div>

                        <div class="order-details">
                            <h5><i class="fa fa-receipt"></i> Chi Tiết Đơn Hàng</h5>
                            <div class="detail-row">
                                <span class="detail-label">Mã đơn hàng:</span>
                                <span class="detail-value">${order_id}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Mã giao dịch VNPay:</span>
                                <span class="detail-value">${transaction_id}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Phương thức thanh toán:</span>
                                <span class="detail-value">${payment_method}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Số tiền:</span>
                                <span class="detail-value amount-highlight">$${amount}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Thời gian thanh toán:</span>
                                <span class="detail-value"><%= new java.util.Date() %></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Trạng thái:</span>
                                <span class="detail-value" style="color: #28a745; font-weight: bold;">Đã thanh toán</span>
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <!-- Failed Result -->
                        <div class="result-icon error-icon">
                            <i class="fa fa-times-circle"></i>
                        </div>
                        <div class="result-title">Thanh Toán Thất Bại</div>
                        <div class="result-message">
                            ${error_message != null ? error_message : 'Có lỗi xảy ra trong quá trình thanh toán. Vui lòng thử lại.'}
                        </div>

                        <div class="order-details">
                            <h5><i class="fa fa-exclamation-triangle"></i> Thông Tin Lỗi</h5>
                            <div class="detail-row">
                                <span class="detail-label">Mã giao dịch:</span>
                                <span class="detail-value">${transaction_id != null ? transaction_id : 'N/A'}</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Thời gian:</span>
                                <span class="detail-value"><%= new java.util.Date() %></span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Trạng thái:</span>
                                <span class="detail-value" style="color: #dc3545; font-weight: bold;">Thanh toán thất bại</span>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="action-buttons">
                    <a href="home" class="btn-primary-custom">
                        <i class="fa fa-home"></i> Về Trang Chủ
                    </a>
                    <a href="viewcart" class="btn-secondary-custom">
                        <i class="fa fa-shopping-cart"></i> Giỏ Hàng
                    </a>
                    <c:if test="${payment_status != 'success'}">
                        <a href="vnpay_payment" class="btn-primary-custom">
                            <i class="fa fa-refresh"></i> Thử Lại
                        </a>
                    </c:if>
                </div>

                <div class="demo-notice">
                    <i class="fa fa-info-circle"></i>
                    <strong>Lưu ý:</strong> Đây là hệ thống thanh toán VNPay thử nghiệm cho mục đích demo.
                    Không có giao dịch thực tế nào được thực hiện.
                </div>
            </div>
        </div>

        <%@ include file="footer.jsp"%>

        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
        <script>
            $(document).ready(function() {
                // Auto redirect after 10 seconds on success
                <c:if test="${payment_status == 'success'}">
                setTimeout(function() {
                    window.location.href = 'home';
                }, 10000);
                </c:if>

                // Add some animation
                $('.result-icon').addClass('animate__animated animate__bounceIn');
                $('.result-title').addClass('animate__animated animate__fadeInUp');
            });
        </script>
    </body>
</html>
