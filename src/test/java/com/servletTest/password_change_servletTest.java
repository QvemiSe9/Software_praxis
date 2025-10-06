package com.servletTest;

import com.servlet.password_change_servlet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * password_change_servlet 的单元测试类。
 * 通过模拟静态方法 DriverManager.getConnection() 来隔离数据库，无需修改 Servlet 源代码。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 允许存在未被使用的 stubbing，以适应多场景的参数化测试
class password_change_servletTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher requestDispatcher;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;

    private password_change_servlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new password_change_servlet();
        // 通用模拟设置：所有测试最终都会转发请求
        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @ParameterizedTest(name = "[{index}] {11}")
    @CsvFileSource(resources = "/password_change_scenarios.csv", numLinesToSkip = 1, encoding = "UTF-8")
    @DisplayName("参数化测试：处理所有密码修改场景")
    void doPost_handlesAllScenarios(
            boolean sessionExists, String merchantNameInSession, String merchantPasswordInSession,
            String usernameInput, String oldPasswordInput, String newPasswordInput,
            int dbUpdateResult, boolean throwDbException,
            String expectedAttributeKey, String expectedAttributeValue,
            boolean verifySessionUpdate, String description) throws ServletException, IOException, SQLException {

        // --- Arrange (准备阶段) ---

        // 1. 模拟会话 (Session)
        when(request.getSession(false)).thenReturn(sessionExists ? session : null);
        if (sessionExists) {
            // 使用 "null" 字符串来代表 session 中不存在该属性
            when(session.getAttribute("merchant_name")).thenReturn("null".equals(merchantNameInSession) ? null : merchantNameInSession);
            when(session.getAttribute("merchant_password")).thenReturn(merchantPasswordInSession);
        }

        // 2. 模拟表单输入
        when(request.getParameter("username")).thenReturn(usernameInput);
        when(request.getParameter("old_password")).thenReturn(oldPasswordInput);
        when(request.getParameter("new_password")).thenReturn(newPasswordInput);

        // 3. 使用 try-with-resources 模拟数据库静态调用
        try (MockedStatic<DriverManager> mockedDriverManager = Mockito.mockStatic(DriverManager.class)) {
            // 只有在需要数据库操作时才设置这部分模拟
            if (dbUpdateResult != -1) { // -1 作为跳过数据库模拟的标记
                mockedDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                        .thenReturn(connection);
                when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
                
                // 修正：先创建异常实例，再在 stubbing 中使用它
                if (throwDbException) {
                    SQLException dbException = new SQLException("模拟数据库连接失败");
                    when(preparedStatement.executeUpdate()).thenThrow(dbException);
                } else {
                    when(preparedStatement.executeUpdate()).thenReturn(dbUpdateResult);
                }
            }

            // --- Act (执行阶段) ---
            servlet.doPost(request, response);
        }

        // --- Assert (断言阶段) ---

        // 验证是否设置了正确的提示信息
        verify(request).setAttribute(expectedAttributeKey, expectedAttributeValue);

        // 如果预期会更新 session，验证 session 更新操作是否被调用
        if (verifySessionUpdate) {
            verify(session).setAttribute("merchant_password", newPasswordInput);
        } else {
            verify(session, never()).setAttribute(eq("merchant_password"), anyString());
        }

        // 验证是否总是转发到正确的JSP页面
        verify(request).getRequestDispatcher("merchantpassword_change.jsp");
        verify(requestDispatcher).forward(request, response);
    }
}

