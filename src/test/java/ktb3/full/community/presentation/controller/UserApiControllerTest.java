package ktb3.full.community.presentation.controller;

import ktb3.full.community.ControllerTestSupport;
import ktb3.full.community.fixture.MultipartFileFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserApiControllerTest extends ControllerTestSupport {

    @Nested
    class signUp {

        @Test
        void 회원을_가입한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, "/users")
                    .file(MultipartFileFixture.createProfileImage())
                    .param("email", "email@example.com")
                    .param("password", "Password123!")
                    .param("nickname", "name"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 회원가입_시_이메일에_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, "/users")
                    .file(MultipartFileFixture.createProfileImage())
                    .param("email", " ")
                    .param("password", "Password123!")
                    .param("nickname", "name"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("이메일은 필수입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 회원가입_시_비밀번호에_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, "/users")
                    .file(MultipartFileFixture.createProfileImage())
                    .param("email", "email@example.com")
                    .param("password", " ")
                    .param("nickname", "name"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("비밀번호는 필수입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        void 회원가입_시_닉네임에_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, "/users")
                    .file(MultipartFileFixture.createProfileImage())
                    .param("email", "email@example.com")
                    .param("password", "Password123!")
                    .param("nickname", " "));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("닉네임은 필수입니다."))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}