package ktb3.full.community.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.full.community.common.exception.LoginRequiredException;
import ktb3.full.community.dto.response.ApiResponse;
import ktb3.full.community.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.warn("AuthenticationException: {}", authException.getMessage());

        LoginRequiredException exception = new LoginRequiredException();
        ApiResponse<Void> apiResponse = ApiResponse.error(exception.getApiErrorCode());

        ResponseUtil.responseJsonUtf8(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                objectMapper.writeValueAsString(apiResponse));
    }
}
