package com.servletTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.servlet.Buyer_message_servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Buyer_message_servlet 的单元测试类。
 * 本测试类使用 Mockito 框架将 Servlet 与数据库和 Servlet 容器完全隔离。
 * 所有测试用例均由外部 CSV 文件驱动，实现了测试数据与逻辑的分离。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // <<--- 添加此行来解决 UnnecessaryStubbingException
class Buyer_message_servletTest {

    // 模拟 Servlet 环境对象
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher requestDispatcher;

    // 模拟 JDBC 数据库交互对象
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    // 使用 @Spy注解可以让我们调用servlet的真实方法，
    // 同时又能存根（stub）其某些特定方法（如此处的 getConnection）。
    // @InjectMocks 会将其他 mock 对象注入到 servlet 实例中。
    @Spy
    @InjectMocks
    private Buyer_message_servlet servlet;

    @BeforeEach
    void setUp() throws Exception {
        // 为所有测试进行通用设置：
        // 1. 当 Servlet 请求 RequestDispatcher 时，返回我们模拟的 dispatcher。
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        // 2. 我们必须存根 getConnection 方法以防止真实的数据库调用。
        doReturn(connection).when(servlet).getConnection();
        
        // 3. 通用的数据库 prepareStatement 模拟
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @ParameterizedTest(name = "[{index}] {11}") // 使用CSV文件第12列（description）作为测试名
    @CsvFileSource(resources = "/success_buyer_data.csv", numLinesToSkip = 1, encoding = "UTF-8")
    @DisplayName("参数化测试：处理合法输入并成功预订")
    void doPost_whenInputIsValid_forwardsToSuccess(
            String buyerName, String phoneNumber, String address, String time,
            int mockLatestWorkId, String mockMaxOrderId, String mockWorkName, String mockWorkPrice,
            String expectedOrderId, String expectedWorkName, String expectedWorkPrice, String description)
            throws ServletException, IOException, SQLException {
        
        // Arrange (准备阶段): 设置来自 CSV 的合法请求参数
        when(request.getParameter("buyer_name")).thenReturn(buyerName);
        when(request.getParameter("buyer_phonenumber")).thenReturn(phoneNumber);
        when(request.getParameter("trading_address")).thenReturn(address);
        when(request.getParameter("trading_time")).thenReturn(time);

        // Arrange (准备阶段): 根据 CSV 数据为成功的业务流程模拟数据库交互
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        
        // 修正: 确保前三次调用 rs.next() 都返回 true，以覆盖 getLatestWorkId, generateNewOrderId, 和 getWorkDetailsAndForward 的调用。
        when(resultSet.next()).thenReturn(true, true, true);
        
        when(resultSet.getInt("maxWorkId")).thenReturn(mockLatestWorkId);
        // 使用 "null" 字符串来代表数据库返回 null
        when(resultSet.getString("maxId")).thenReturn("null".equalsIgnoreCase(mockMaxOrderId) ? null : mockMaxOrderId);
        when(resultSet.getString("work_name")).thenReturn(mockWorkName);
        when(resultSet.getString("work_price")).thenReturn(mockWorkPrice);

        // Act (执行阶段): 调用被测试的方法
        servlet.doPost(request, response);

        // Assert (断言阶段): 验证是否执行了正确的操作
        verify(preparedStatement).executeUpdate();
        verify(request).getRequestDispatcher("booking_success.jsp");
        verify(request).setAttribute("order_id", expectedOrderId);
        verify(request).setAttribute("work_name", expectedWorkName);
        verify(request).setAttribute("work_price", expectedWorkPrice);
        verify(requestDispatcher).forward(request, response);
    }
    
    @ParameterizedTest(name = "[{index}] {4}") // 使用CSV文件第5列（description）作为测试名
    @CsvFileSource(resources = "/invalid_buyer_data.csv", numLinesToSkip = 1, encoding = "UTF-8")
    @DisplayName("参数化测试：当输入无效时，请求会转发到输入页面")
    void doPost_whenInputIsInvalid_forwardsToInputPage(String buyerName, String phoneNumber, String address, String time, String description) throws ServletException, IOException, ClassNotFoundException, SQLException {
        // Arrange: 设置来自 CSV 文件的无效参数
        when(request.getParameter("buyer_name")).thenReturn(buyerName);
        when(request.getParameter("buyer_phonenumber")).thenReturn(phoneNumber);
        when(request.getParameter("trading_address")).thenReturn(address);
        when(request.getParameter("trading_time")).thenReturn(time);
        
        // Act
        servlet.doPost(request, response);
        
        // Assert
        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(request).getRequestDispatcher("buyer_message.jsp");
        verify(requestDispatcher).forward(request, response);
        // 确保没有调用任何数据库保存方法
        verify(servlet, never()).saveToReservation(any(), any(), any(), any(), any(), any(), anyInt());
    }

    @ParameterizedTest(name = "[{index}] {8}") // 使用CSV文件第9列（description）作为测试名
    @CsvFileSource(resources = "/exception_scenarios.csv", numLinesToSkip = 1, encoding = "UTF-8")
    @DisplayName("参数化测试：当发生业务或数据库错误时，应抛出ServletException")
    void doPost_whenErrorsOccur_throwsServletException(
            String buyerName, String phoneNumber, String address, String time,
            String scenarioType, int mockLatestWorkId, String mockMaxOrderId,
            String expectedCauseClass, String expectedCauseMessage, String description) throws Exception {

        // Arrange: 设置合法的输入以通过验证
        when(request.getParameter("buyer_name")).thenReturn(buyerName);
        when(request.getParameter("buyer_phonenumber")).thenReturn(phoneNumber);
        when(request.getParameter("trading_address")).thenReturn(address);
        when(request.getParameter("trading_time")).thenReturn(time);
        
        // Arrange: 根据场景类型配置不同的数据库模拟行为
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true);

        if ("NO_WORK_ID".equals(scenarioType)) {
            when(resultSet.getInt("maxWorkId")).thenReturn(mockLatestWorkId); // 模拟返回0或-1
        } else if ("DB_SAVE_FAIL".equals(scenarioType)) {
            when(resultSet.getInt("maxWorkId")).thenReturn(mockLatestWorkId);
            when(resultSet.getString("maxId")).thenReturn("null".equalsIgnoreCase(mockMaxOrderId) ? null : mockMaxOrderId);
            // 关键：模拟 executeUpdate 时抛出异常
            when(preparedStatement.executeUpdate()).thenThrow(new SQLException("Test DB connection failed"));
        }

        // Act & Assert
        ServletException exception = assertThrows(ServletException.class, () -> servlet.doPost(request, response));
        
        // 断言1: 验证由于servlet中的宽泛catch块，最外层的消息总是“数据库操作失败”
        assertEquals("数据库操作失败", exception.getMessage());

        // 断言2: 深入检查异常的根本原因(Cause)，以确保底层错误符合预期
        Throwable cause = exception.getCause();
        assertNotNull(cause, "Exception cause should not be null");
        assertEquals(Class.forName(expectedCauseClass), cause.getClass(), "The cause exception type is not correct.");
        assertEquals(expectedCauseMessage, cause.getMessage(), "The cause exception message is not correct.");
    }
}

