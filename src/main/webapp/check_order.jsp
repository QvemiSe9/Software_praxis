<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>查询订单</title>
    <style>
        body { margin: 0; font-family: sans-serif; background-color: #f8f9fa; }
        /* 统一导航栏样式 */
        .navbar {
            background-color: #f2f2f2;
            padding: 0 20px;
            border-bottom: 1px solid #ccc;
            overflow: hidden; 
        }
        .navbar a {
            float: left; 
            display: block;
            color: #333;
            text-align: center;
            padding: 14px 20px;
            text-decoration: none;
            font-size: 16px;
            border: 1px solid transparent; 
            border-bottom: none;
        }
        .navbar a:hover {
            background-color: #ddd;
        }
        
        .navbar a.active {
            background-color: white;
            color: black;
            border: 1px solid #ccc;
            border-bottom: 1px solid white; 
            position: relative;
            top: 1px;
            border-radius: 5px 5px 0 0;
        }
        
        .container { max-width: 600px; margin: 0 auto; padding: 30px; background-color: #fff; border: 1px solid #ccc; border-top: none;}
        h1 { text-align: center; color: #333; margin-top: 0;}
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; font-weight: bold; color: #555; }
        input[type="text"] { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; }
        .btn { display: inline-block; padding: 10px 20px; border-radius: 4px; text-decoration: none; color: white; cursor: pointer; border: none; }
        .btn-query { background-color: #007bff; }
        .btn-cancel { background-color: #dc3545; }
        .result-box { margin-top: 30px; padding: 20px; border: 1px solid #ddd; border-radius: 4px; background-color: #f9f9f9; }
        .error-message { color: #dc3545; font-weight: bold; text-align: center; }
        .success-message { color: #28a745; font-weight: bold; text-align: center; }
    </style>
</head>
<body>

    <!-- 导航栏 -->
    <div class="navbar">
        <a href="work_message.jsp">查询商品</a>
        <a href="check_order.jsp" class="active">查询订单</a>
        <a href="merchant_login.jsp">商家登录</a>
    </div>

    <div class="container">
        <h1>订单状态查询</h1>

        <!-- 关键修正: action 指向 web.xml 中配置的 url-pattern，不带 .java -->
        <form action="Buyercheck_ordermessage" method="post">
            <input type="hidden" name="action" value="query">
            <div class="form-group">
                <label for="order_id">请输入您的订单编号:</label>
                <input type="text" id="order_id" name="order_id" required>
            </div>
            <button type="submit" class="btn btn-query">查询</button>
        </form>

        <!-- 结果显示区域 -->
        <div class="result-box">
            <%
                String errorMessage = (String) request.getAttribute("errorMessage");
                if (errorMessage != null) {
            %>
                <p class="error-message"><%= errorMessage %></p>
            <%
                }

                String successMessage = (String) request.getAttribute("successMessage");
                if (successMessage != null) {
            %>
                 <p class="success-message"><%= successMessage %></p>
            <%
                }

                String resultMessage = (String) request.getAttribute("resultMessage");
                if (resultMessage != null) {
            %>
                <p><strong>查询结果:</strong> <%= resultMessage %></p>
            <%
                }

                Boolean showCancelButton = (Boolean) request.getAttribute("showCancelButton");
                if (showCancelButton != null && showCancelButton) {
                    String orderIdToCancel = (String) request.getAttribute("orderIdToCancel");
            %>
                    <!-- 关键修正: action 指向 web.xml 中配置的 url-pattern，不带 .java -->
                    <form action="Buyercheck_ordermessage" method="post" style="margin-top: 15px;">
                        <input type="hidden" name="action" value="cancel">
                        <input type="hidden" name="order_id" value="<%= orderIdToCancel %>">
                        <button type="submit" class="btn btn-cancel">取消此订单</button>
                    </form>
            <%
                }
            %>
        </div>
    </div>

</body>
</html>

