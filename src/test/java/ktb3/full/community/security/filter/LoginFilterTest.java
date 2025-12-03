package ktb3.full.community.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.UserLoginRequest;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginFilterTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class login {

        @Test
        void 이메일과_비밀번호가_일치하면_로그인된다() throws Exception {
            // given
            User user = userRepository.save(UserFixture.createWithEmailAndPassword("email@example.com", passwordEncoder.encode("Password123!")));

            UserLoginRequest request = UserLoginRequest.builder()
                    .email("email@example.com")
                    .password("Password123!")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(post("/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(user.getId()))
                    .andExpect(jsonPath("$.data.profileImageName").value(user.getProfileImageName()));
        }

        @Test
        void 이메일이_틀리면_예외가_발생한다() throws Exception {
            // given
            userRepository.save(UserFixture.createWithEmailAndPassword("email@example.com", passwordEncoder.encode("Password123!")));

            UserLoginRequest request = UserLoginRequest.builder()
                    .email("wrongEmail@example.com")
                    .password("Password123!")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(post("/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(4011))
                    .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 틀렸습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 비밀번호가_틀리면_예외가_발생한다() throws Exception {
            // given
            userRepository.save(UserFixture.createWithEmailAndPassword("email@example.com", passwordEncoder.encode("Password123!")));

            UserLoginRequest request = UserLoginRequest.builder()
                    .email("email@example.com")
                    .password("WrongPassword123!")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(post("/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(4011))
                    .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 틀렸습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}
