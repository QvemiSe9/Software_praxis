package com.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Buyercheck_ordermessage extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * 新增方法：处理 GET 请求。
     * 当用户直接通过浏览器地址栏访问此 Servlet 时，
     * 会触发此方法，我们将他们重定向回表单页面，并给出提示。
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("errorMessage", "请先输入订单号并点击查询按钮。");
        request.getRequestDispatcher("check_order.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String orderId = request.getParameter("order_id");

        if (orderId == null || orderId.trim().isEmpty()) {
            request.setAttribute("errorMessage", "订单编号不能为空！");
            request.getRequestDispatcher("check_order.jsp").forward(request, response);
            return;
        }

        if ("query".equals(action)) {
            handleQuery(request, response, orderId);
        } else if ("cancel".equals(action)) {
            handleCancel(request, response, orderId);
        } else {
            request.setAttribute("errorMessage", "无效的操作请求！");
            request.getRequestDispatcher("check_order.jsp").forward(request, response);
        }
    }

    private void handleQuery(HttpServletRequest request, HttpServletResponse response, String orderId)
            throws ServletException, IOException {
        
        int workId = -1; 
        String workStatus = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            
            String sqlReservation = "SELECT work_id FROM Reservation WHERE order_id = ?";
            pstmt = conn.prepareStatement(sqlReservation);
            pstmt.setString(1, orderId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                workId = rs.getInt("work_id");
            } else {
                request.setAttribute("errorMessage", "未找到订单 " + orderId);
                request.getRequestDispatcher("check_order.jsp").forward(request, response);
                return;
            }
            rs.close();
            pstmt.close();

            String sqlWorks = "SELECT work_status FROM Works WHERE work_id = ?";
            pstmt = conn.prepareStatement(sqlWorks);
            pstmt.setInt(1, workId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                workStatus = rs.getString("work_status");
            } else {
                 request.setAttribute("errorMessage", "关联的商品信息不存在 (work_id: " + workId +")！");
                 request.getRequestDispatcher("check_order.jsp").forward(request, response);
                 return;
            }
             rs.close();
             pstmt.close();

            if ("available".equalsIgnoreCase(workStatus)) {
                request.setAttribute("resultMessage", "商品状态正常，您的预订有效。");
                request.setAttribute("showCancelButton", true);
                request.setAttribute("orderIdToCancel", orderId);
            } else if ("frozen".equalsIgnoreCase(workStatus)) {
                boolean isInTrade = false;
                String sqlTrade = "SELECT COUNT(*) FROM Trade WHERE order_id = ?";
                pstmt = conn.prepareStatement(sqlTrade);
                pstmt.setString(1, orderId);
                rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    isInTrade = true;
                }
                
                if (isInTrade) {
                    request.setAttribute("resultMessage", "订单正在等待交易，请耐心等候。");
                } else {
                    request.setAttribute("resultMessage", "商品已被冻结并可能出售给其他买家，您的订单已失效。");
                }

            } else if ("sold".equalsIgnoreCase(workStatus)) {
                request.setAttribute("resultMessage", "此商品已成功售罄。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "数据库查询出错！");
        } finally {
            close(conn, pstmt, rs);
        }
        
        request.getRequestDispatcher("check_order.jsp").forward(request, response);
    }

    private void handleCancel(HttpServletRequest request, HttpServletResponse response, String orderId)
            throws ServletException, IOException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            String sql = "DELETE FROM Reservation WHERE order_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, orderId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                request.setAttribute("successMessage", "订单 " + orderId + " 已成功取消！");
            } else {
                request.setAttribute("errorMessage", "取消失败，可能订单已被处理或不存在。");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "数据库操作失败！");
        } finally {
            close(conn, pstmt, null);
        }

        request.getRequestDispatcher("check_order.jsp").forward(request, response);
    }
    
    private Connection getConnection() throws SQLException, ClassNotFoundException {
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

