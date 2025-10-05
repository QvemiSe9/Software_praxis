<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>用户信息填写</title>
</head>
<body style="font-family: sans-serif;">

    <div style="width:100%; max-width: 500px; margin: 40px auto; padding: 20px; border: 1px solid #ccc; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
        <h2 style="text-align: center; margin-bottom: 25px;">填写预订信息</h2>
        
        <%-- 
          新增部分:
          1. 检查 request 中是否存在名为 "errorMessage" 的属性。
          2. 如果存在，就在页面上用红色字体显示该错误信息。
        --%>
        <% 
            String errorMessage = (String) request.getAttribute("errorMessage");
            if (errorMessage != null) {
        %>
            <p style="color: red; text-align: center; font-weight: bold;"><%= errorMessage %></p>
        <%
            }
        %>

        
        <form action="buyer_message_servlet.java" method="post">
            <div style="margin-bottom: 15px;">
                <label for="buyer_name" style="display: block; margin-bottom: 5px;">姓名:</label>
                <input type="text" id="buyer_name" name="buyer_name" 
                       value="<%= request.getAttribute("buyer_name") != null ? request.getAttribute("buyer_name") : "" %>"
                       required 
                       pattern="[A-Za-z0-9\u4e00-\u9fa5]{1,5}" 
                       title="请输入1-5位的中文、英文或数字"
                       style="width: calc(100% - 20px); padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
            </div>

            <div style="margin-bottom: 15px;">
                <label for="buyer_phonenumber" style="display: block; margin-bottom: 5px;">手机号码:</label>
                <input type="text" id="buyer_phonenumber" name="buyer_phonenumber" 
                       value="<%= request.getAttribute("buyer_phonenumber") != null ? request.getAttribute("buyer_phonenumber") : "" %>"
                       required 
                       pattern="\d{11}" 
                       title="请输入11位数字的手机号码"
                       style="width: calc(100% - 20px); padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
            </div>

            <div style="margin-bottom: 15px;">
                <label for="trading_address" style="display: block; margin-bottom: 5px;">交易地址:</label>
                <input type="text" id="trading_address" name="trading_address" 
                       value="<%= request.getAttribute("trading_address") != null ? request.getAttribute("trading_address") : "" %>"
                       required 
                       maxlength="20" 
                       title="地址不超过20个字符"
                       style="width: calc(100% - 20px); padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
            </div>

            <div style="margin-bottom: 25px;">
                <label for="trading_time" style="display: block; margin-bottom: 5px;">交易时间:</label>
                <input type="datetime-local" id="trading_time" name="trading_time" 
                       value="<%= request.getAttribute("trading_time") != null ? request.getAttribute("trading_time") : "" %>"
                       required
                       title="请选择交易日期和时间"
                       style="width: calc(100% - 20px); padding: 8px; border: 1px solid #ccc; border-radius: 4px;">
            </div>

            <div>
                <button type="submit" style="width: 100%; padding: 12px; background-color: #E53935; color: white; border: none; border-radius: 5px; font-size: 16px; cursor: pointer;">
                    提交预订
                </button>
            </div>
        </form>
    </div>

</body>
</html>

