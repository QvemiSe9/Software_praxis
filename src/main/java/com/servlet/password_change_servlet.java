package com.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class password_change_servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        // 1. 获取当前的会话 (HttpSession)，如果不存在则不创建新的
        HttpSession session = request.getSession(false);

        // 2. 检查会话是否存在以及是否包含商家信息
        if (session == null || session.getAttribute("merchant_name") == null) {
            request.setAttribute("errorMessage", "您尚未登录，请先登录后再操作！");
            request.getRequestDispatcher("merchantpassword_change.jsp").forward(request, response);
            return; // 终止执行
        }

        // 3. 从表单获取输入
        String usernameInput = request.getParameter("username");
        String oldPasswordInput = request.getParameter("old_password");
        String newPasswordInput = request.getParameter("new_password");

        // 4. 从 Session 获取已登录的商家信息
        String loggedInUsername = (String) session.getAttribute("merchant_name");
        String loggedInPassword = (String) session.getAttribute("merchant_password");
        
        // 5. 验证输入
        if (usernameInput == null || oldPasswordInput == null || newPasswordInput == null ||
            usernameInput.isEmpty() || oldPasswordInput.isEmpty() || newPasswordInput.isEmpty()) {
            
            request.setAttribute("errorMessage", "所有字段都不能为空！");

        } else if (usernameInput.equals(loggedInUsername) && oldPasswordInput.equals(loggedInPassword)) {
            
            try {
                // 验证通过，先更新数据库
                updatePasswordInDatabase(loggedInUsername, newPasswordInput);
                
                // 数据库更新成功后，再更新 Session 中的密码
                session.setAttribute("merchant_password", newPasswordInput);
                request.setAttribute("successMessage", "密码修改成功！");
                System.out.println("商家 '" + loggedInUsername + "' 的密码已成功在数据库和Session中修改。");

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "数据库操作失败，密码未修改！");
                // 抛出异常以便调试
                // throw new ServletException("数据库更新失败", e);
            }

        } else {
            
            // 验证失败
            request.setAttribute("errorMessage", "用户名或原密码不正确！");
        }

        // 6. 将请求转发回 JSP 页面以显示结果
        request.getRequestDispatcher("merchantpassword_change.jsp").forward(request, response);
    }


    private void updatePasswordInDatabase(String username, String newPassword) 
        throws SQLException, ClassNotFoundException {
        
        String jdbcURL = "jdbc:sqlserver://localhost:1433;DatabaseName=Web;trustServerCertificate=true"; 
        String dbUser ="sa"; 
        String dbPassword =  "a1228671503";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
            
            String sql = "UPDATE Merchant SET merchant_password = ? WHERE merchant_name = ?";
            statement = connection.prepareStatement(sql);
            
            statement.setString(1, newPassword);
            statement.setString(2, username);
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("数据库中商家 '" + username + "' 的密码已更新。");
            } else {
                System.out.println("警告: 数据库中未找到商家 '" + username + "'，密码未更新。");
                // 根据业务需求，您也可以在这里抛出一个异常
                // throw new SQLException("更新失败，数据库中未找到该用户。");
            }

        } finally {
            // 确保资源被关闭
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        }
    }
}
