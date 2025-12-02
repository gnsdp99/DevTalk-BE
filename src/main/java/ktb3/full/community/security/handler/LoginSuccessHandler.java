package ktb3.full.community.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.full.community.dto.response.ApiResponse;
import ktb3.full.community.dto.response.UserLoginResponse;
import ktb3.full.community.security.userdetails.AuthUserDetails;
import ktb3.full.community.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();

        UserLoginResponse userLoginResponse = UserLoginResponse.builder()
                .userId(principal.getUserId())
                .profileImageName(principal.getProfileImageName())
                .build();
        ApiResponse<UserLoginResponse> apiResponse = ApiResponse.success(userLoginResponse);

        ResponseUtil.responseJsonUtf8(
                response,
                HttpServletResponse.SC_OK,
                objectMapper.writeValueAsString(apiResponse));

        /* SecurityContextHolder 및 HttpSession에 인증 정보 저장 */
        AuthUserDetails targetPrincipal = AuthUserDetails.onlyUserId(principal);
        UsernamePasswordAuthenticationToken targetAuthentication = UsernamePasswordAuthenticationToken.authenticated(targetPrincipal, authentication.getCredentials(), authentication.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(targetAuthentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}
