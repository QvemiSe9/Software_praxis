package com.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class Buyer_message_servlet extends HttpServlet {

    private static final long serialVersionUID = -1970406637082738876L;

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        String buyerName = request.getParameter("buyer_name");
        String phoneNumber = request.getParameter("buyer_phonenumber");
        String address = request.getParameter("trading_address");
        String time = request.getParameter("trading_time");

        boolean isNameValid = buyerName != null && buyerName.matches("[A-Za-z0-9\\u4e00-\\u9fa5]{1,5}");
        boolean isPhoneValid = phoneNumber != null && phoneNumber.matches("\\d{11}");
        boolean isAddressValid = address != null && !address.isEmpty() && address.length() <= 20;
        boolean isTimeValid = time != null && !time.isEmpty();

        if (!isNameValid || !isPhoneValid || !isAddressValid || !isTimeValid) {
            request.setAttribute("errorMessage", "输入不符合要求，请重新输入");
            
            request.setAttribute("buyer_name", buyerName);
            request.setAttribute("buyer_phonenumber", phoneNumber);
            request.setAttribute("trading_address", address);
            request.setAttribute("trading_time", time);
            request.getRequestDispatcher("buyer_message.jsp").forward(request, response);
            return;
        }
        
        try {
            // 1. 获取最新商品的work_id和信息
            int latestWorkId = getLatestWorkId();
            if (latestWorkId == -1) {
                 throw new ServletException("系统中当前没有任何可预订的商品。");
            }

            // 2. 生成订单ID和订单时间
            String orderId = generateNewOrderId();
            String orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // 3. 将预订信息（包含work_id）保存到 Reservation 表
            saveToReservation(orderId, orderTime, buyerName, phoneNumber, address, time, latestWorkId);

            // 4. 再次获取商品信息用于在成功页面显示
            getWorkDetailsAndForward(request, response, orderId, latestWorkId);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("数据库操作失败", e);
        }
    }
    

    public void saveToReservation(String orderId, String orderTime, String name, String phone, String address, String time, int workId) 
        throws SQLException, ClassNotFoundException {
        
        Connection connection = null;
        PreparedStatement statement = null;
        String sql = "INSERT INTO Reservation (order_id, order_time, buyer_name, buyer_phonenumber, trading_address, trading_time, work_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            
            statement.setString(1, orderId);
            statement.setString(2, orderTime);
            statement.setString(3, name);
            statement.setString(4, phone);
            statement.setString(5, address);
            statement.setString(6, time);
            statement.setInt(7, workId);
            
            statement.executeUpdate();
            System.out.println("预订信息 (订单号: " + orderId + ") 已成功存入 Reservation 表。");
        } finally {
            close(connection, statement, null);
        }
    }


    private String generateNewOrderId() throws SQLException, ClassNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        String newOrderId = "DD00001"; // 默认起始ID

        // 修正：应该从 Reservation 表查询最大订单号
        String sql = "SELECT MAX(order_id) AS maxId FROM Reservation";

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            rs = statement.executeQuery();
            
            if (rs.next()) {
                String maxId = rs.getString("maxId");
                if (maxId != null && maxId.startsWith("DD")) {
                    int numericPart = Integer.parseInt(maxId.substring(2));
                    numericPart++;
                    newOrderId = String.format("DD%05d", numericPart);
                }
            }
        } finally {
            close(connection, statement, rs);
        }
        
        return newOrderId;
    }

    /**
     * 新增方法：从 Works 表获取最新的 work_id。
     */
    private int getLatestWorkId() throws SQLException, ClassNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        int latestId = -1; // -1 表示未找到

        String sql = "SELECT MAX(work_id) AS maxWorkId FROM Works";

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            rs = statement.executeQuery();
            
            if (rs.next()) {
                latestId = rs.getInt("maxWorkId");
            }
        } finally {
            close(connection, statement, rs);
        }
        
        return latestId;
    }

    /**
     * 新增方法：获取商品详情并转发到成功页面。
     */
    private void getWorkDetailsAndForward(HttpServletRequest request, HttpServletResponse response, String orderId, int workId) throws ServletException, IOException, SQLException, ClassNotFoundException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        String sql = "SELECT work_name, work_price FROM Works WHERE work_id = ?";

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setInt(1, workId);
            rs = statement.executeQuery();

            if (rs.next()) {
                request.setAttribute("order_id", orderId);
                request.setAttribute("work_name", rs.getString("work_name"));
                request.setAttribute("work_price", rs.getString("work_price"));
            } else {
                // 如果找不到商品，也设置默认值，避免页面出错
                request.setAttribute("order_id", orderId);
                request.setAttribute("work_name", "商品信息未知");
                request.setAttribute("work_price", "N/A");
            }
        } finally {
            close(connection, statement, rs);
        }
        
        request.getRequestDispatcher("booking_success.jsp").forward(request, response);
    }
    

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        String jdbcURL = "jdbc:sqlserver://localhost:1433;DatabaseName=Web;trustServerCertificate=true"; 
        String dbUser ="sa"; 
        String dbPassword =  "a1228671503";
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(jdbcURL, dbUser, dbPassword);
    }

    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

