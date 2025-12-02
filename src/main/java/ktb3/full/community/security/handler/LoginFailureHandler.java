package ktb3.full.community.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.full.community.common.exception.ApiErrorCode;
import ktb3.full.community.dto.response.ApiResponse;
import ktb3.full.community.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        ApiResponse<Void> apiResponse = ApiResponse.error(ApiErrorCode.INVALID_CREDENTIALS);

        ResponseUtil.responseJsonUtf8(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                objectMapper.writeValueAsString(apiResponse));
    }
}
