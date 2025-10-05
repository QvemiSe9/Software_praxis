<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- 导入所有必需的Java SQL库 --%>
<%@ page import="java.sql.Connection, java.sql.DriverManager, java.sql.PreparedStatement, java.sql.ResultSet, java.sql.SQLException" %>

<html>
<head>
    <title>作品详情</title>
    <style>
        body { margin: 0; font-family: sans-serif; }
        .navbar { background-color: #f2f2f2; padding: 0 20px; border-bottom: 1px solid #ccc; overflow: hidden; }
        .navbar a { float: left; display: block; color: #333; text-align: center; padding: 14px 20px; text-decoration: none; font-size: 16px; border: 1px solid transparent; border-bottom: none; }
        .navbar a:hover { background-color: #ddd; }
        .navbar a.active { background-color: white; color: black; border: 1px solid #ccc; border-bottom: 1px solid white; position: relative; top: 1px; border-radius: 5px 5px 0 0; }
    </style>
</head>
<body>
    
    <div class="navbar">
        <%-- 注意：现在可以直接链接到JSP文件 --%>
        <a href="Work_message.jsp" class="active">查询商品</a>
        <a href="check_order.jsp">查询订单</a>
        <a href="merchant_login.jsp">商家登录</a>
    </div>

    <%
        // --- Java数据库查询逻辑 ---
        // 1. 定义变量并设置默认值
        String work_name = "示例作品名称";
        String work_description = "作品的详细描述，介绍作品的材质、尺寸、创作背景等信息。";
        String work_image = "https://placehold.co/300x400?text=作品图片";
        String work_price = "¥9,999.00";
        String work_status = "available";

        // 2. 声明数据库连接对象
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // 3. 连接数据库
            String jdbcURL = "jdbc:sqlserver://localhost:1433;DatabaseName=Web;trustServerCertificate=true";
            String dbUser = "sa";
            String dbPassword = "a1228671503";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);

            // 4. 创建并执行SQL查询
            String sql = "SELECT TOP 1 work_name, work_description, work_image, work_price, work_status FROM Works ORDER BY work_id DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // 5. 如果查询到数据，则覆盖默认值
            if (rs.next()) {
                work_name = rs.getString("work_name");
                work_description = rs.getString("work_description");
                work_image = rs.getString("work_image");
                work_price = rs.getString("work_price");
                work_status = rs.getString("work_status");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // 发生错误时，页面将显示上面的默认值
        } finally {
            // 6. 关闭所有数据库资源
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    %>


    <table style="width:100%; max-width: 800px; border-spacing: 20px; margin: 20px auto; border-top: 1px solid #ccc; padding-top: 20px;">
        <tr>
            <!-- 左侧：作品图片 -->
            <td style="width: 300px; vertical-align: top;">
                <img src="<%= work_image %>" alt="作品图片" style="width: 100%; height: auto; display: block; border-radius: 8px;">
            </td>

            <!-- 右侧：作品信息和按钮 -->
            <td style="vertical-align: top;">
                <div style="font-weight: bold; font-family: 'SimHei', '黑体', sans-serif; font-size: 24px; color: black; margin-bottom: 10px;">
                    <%= work_name %>
                </div>
                <div style="font-weight: bold; color: red; font-size: 20px; margin-bottom: 15px;">
                    <%= work_price %>
                </div>
                <div style="color: grey; margin-bottom: 25px; line-height: 1.6;">
                    <%= work_description %>
                </div>
                <div>
                    <%
                        if ("available".equals(work_status)) {
                    %>
                            <a href="buyer_message.jsp" style="text-decoration: none; background-color: #E53935; color: white; padding: 10px 25px; border-radius: 5px; font-size: 16px; display: inline-block;">
                                可预订
                            </a>
                    <%
                        } else if ("frozen".equals(work_status)) {
                    %>
                            <button style="background-color: #BDBDBD; color: white; padding: 10px 25px; border: none; border-radius: 5px; font-size: 16px; cursor: not-allowed;" disabled>
                                不可预订
                            </button>
                    <%
                        } else if ("sold".equals(work_status)) {
                    %>
                             <button style="background-color: #616161; color: white; padding: 10px 25px; border: none; border-radius: 5px; font-size: 16px; cursor: not-allowed;" disabled>
                                售罄
                            </button>
                    <%
                        }
                    %>
                </div>
            </td>
        </tr>
    </table>

</body>
</html>

