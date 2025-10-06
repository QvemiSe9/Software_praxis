package com.servletTest;

import com.servlet.Buyercheck_ordermessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/**
 * Buyercheck_ordermessage Servlet 的单元测试类。
 * 使用 JUnit 5 和 Mockito，通过 CSV 文件驱动参数化测试。
 * 通过模拟静态方法 DriverManager.getConnection() 来隔离数据库，无需修改 Servlet 源代码。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Buyercheck_ordermessageTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    // 不再使用 @Spy，直接创建一个真实的 Servlet 实例
    private Buyercheck_ordermessage servlet;

    @BeforeEach
    void setUp() {
        // 在每个测试前创建一个新的 servlet 实例
        servlet = new Buyercheck_ordermessage();
        // 通用模拟设置，对所有测试都有效
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @Test
    @DisplayName("当通过GET方法直接访问时，应重定向回表单")
    void doGet_directAccess_forwardsToForm() throws ServletException, IOException {
        servlet.doGet(request, response);

        verify(request).setAttribute("errorMessage", "请先输入订单号并点击查询按钮。");
        verify(request).getRequestDispatcher("check_order.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    @ParameterizedTest(name = "[{index}] {3}")
    @CsvFileSource(resources = "/post_validation_scenarios.csv", numLinesToSkip = 1, encoding = "UTF-8")
    @DisplayName("参数化测试：处理无效的doPost请求")
    void doPost_whenInputIsInvalid_forwardsToForm(String orderId, String action, String expectedErrorMessage, String description) throws ServletException, IOException {
        when(request.getParameter("order_id")).thenReturn(orderId);
        when(request.getParameter("action")).thenReturn(action);

        servlet.doPost(request, response);

        verify(request).setAttribute("errorMessage", expectedErrorMessage);
        verify(request).getRequestDispatcher("check_order.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    @ParameterizedTest(name = "[{index}] {9}")
    @CsvFileSource(resources = "/query_scenarios.csv", numLinesToSkip = 1, encoding = "UTF-8")
    @DisplayName("参数化测试：处理各种订单查询场景")
    void doPost_whenActionIsQuery_handlesScenariosCorrectly(
            String orderId, boolean orderFound, int workId, boolean workFound, String workStatus,
            boolean inTrade, String expectedResult, String expectedAttribute, boolean showCancel, String description) throws Exception {

        when(request.getParameter("order_id")).thenReturn(orderId);
        when(request.getParameter("action")).thenReturn("query");

        // 使用 try-with-resources 来模拟静态方法调用，确保模拟在测试后被清理
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            // 安排(Arrange): 拦截静态调用并返回我们模拟的 connection 对象
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                           .thenReturn(connection);
            
            // 安排(Arrange): 设置后续的数据库交互模拟
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(resultSet.next()).thenReturn(orderFound, workFound, inTrade);
            if (orderFound) {
                when(resultSet.getInt("work_id")).thenReturn(workId);
            }
            if (workFound) {
                when(resultSet.getString("work_status")).thenReturn(workStatus);
            }
            when(resultSet.getInt(1)).thenReturn(inTrade ? 1 : 0);

            // 执行(Act)
            servlet.doPost(request, response);
        }

        // 断言(Assert)
        verify(request).setAttribute(eq(expectedAttribute), contains(expectedResult));
        if (showCancel) {
            verify(request).setAttribute("showCancelButton", true);
            verify(request).setAttribute("orderIdToCancel", orderId);
        }
        verify(request).getRequestDispatcher("check_order.jsp");
        verify(requestDispatcher).forward(request, response);
    }

    @ParameterizedTest(name = "[{index}] {5}")
    @CsvFileSource(resources = "/cancel_scenarios.csv", numLinesToSkip = 1, encoding = "UTF-8")
    @DisplayName("参数化测试：处理各种订单取消场景")
    void doPost_whenActionIsCancel_handlesScenariosCorrectly(
        String orderId, int rowsAffected, boolean throwException, String expectedMessage, String expectedAttribute, String description) throws Exception {

        when(request.getParameter("order_id")).thenReturn(orderId);
        when(request.getParameter("action")).thenReturn("cancel");
        
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                           .thenReturn(connection);
            
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

            if (throwException) {
                // 修正：先创建异常实例，再在 stubbing 中使用它
                SQLException dbException = new SQLException("数据库连接中断");
                when(preparedStatement.executeUpdate()).thenThrow(dbException);
            } else {
                when(preparedStatement.executeUpdate()).thenReturn(rowsAffected);
            }

            // 执行(Act)
            servlet.doPost(request, response);
        }

        // 断言(Assert)
        verify(request).setAttribute(expectedAttribute, expectedMessage);
        verify(request).getRequestDispatcher("check_order.jsp");
        verify(requestDispatcher).forward(request, response);
    }
}

