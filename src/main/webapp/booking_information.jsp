<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>预订成功</title>
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
        h2 {
            color: #4CAF50; /* 绿色 */
        }
        p {
            color: #555;
            font-size: 16px;
        }
        a {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background-color: #007BFF; /* 蓝色 */
            color: white;
            text-decoration: none;
            border-radius: 5px;
        }
        a:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>

    <div class="container">
        <h2>预订信息提交成功！</h2>
        <p>已收到您的预订信息，将会尽快与您确认。</p>
        <a href="work_message.jsp">返回商品页</a>
    </div>

</body>
</html>
