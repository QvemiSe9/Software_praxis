<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>商家密码修改</title>
    <style>
        body {
            font-family: sans-serif;
            text-align: center;
            padding-top: 50px;
            background-color: #f4f4f4;
        }
        .container {
            width: 100%;
            max-width: 500px;
            margin: 40px auto;
            padding: 30px;
            border: 1px solid #ddd;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            background-color: #fff;
        }
        .form-group {
            margin-bottom: 20px;
            text-align: left;
        }
        label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
        }
        input[type="text"], input[type="password"] {
            width: calc(100% - 22px); /* 减去 padding 和 border */
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }
        button {
            width: 100%;
            padding: 12px;
            background-color: #007BFF;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
        }
        button:hover {
            background-color: #0056b3;
        }
        .message {
            padding: 10px;
            margin-bottom: 15px;
            border-radius: 4px;
            font-weight: bold;
        }
        .error {
            color: #D8000C;
            background-color: #FFD2D2;
        }
        .success {
            color: #4F8A10;
            background-color: #DFF2BF;
        }
    </style>
</head>
<body>

    <div class="container">
        <h2>修改商家密码</h2>

        <%-- 用于显示来自 Servlet 的成功或失败消息 --%>
        <% 
            String errorMessage = (String) request.getAttribute("errorMessage");
            String successMessage = (String) request.getAttribute("successMessage");
            if (errorMessage != null) {
        %>
            <div class="message error"><%= errorMessage %></div>
        <%
            }
            if (successMessage != null) {
        %>
            <div class="message success"><%= successMessage %></div>
        <%
            }
        %>

        <form action="password_change_servlet.java" method="post">
            <div class="form-group">
                <label for="username">商家用户名:</label>
                <input type="text" id="username" name="username" required>
            </div>
            <div class="form-group">
                <label for="old_password">原密码:</label>
                <input type="password" id="old_password" name="old_password" required>
            </div>
            <div class="form-group">
                <label for="new_password">新密码:</label>
                <input type="password" id="new_password" name="new_password" required>
            </div>
            <button type="submit">确认修改</button>
        </form>
    </div>

</body>
</html>
