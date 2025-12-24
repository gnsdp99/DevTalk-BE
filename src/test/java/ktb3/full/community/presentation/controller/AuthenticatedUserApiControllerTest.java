package ktb3.full.community.presentation.controller;

import ktb3.full.community.ControllerTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.dto.request.UserAccountUpdateRequest;
import ktb3.full.community.dto.request.UserPasswordUpdateRequest;
import ktb3.full.community.dto.response.UserAccountResponse;
import ktb3.full.community.dto.response.UserAccountUpdateResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithAuthMockUser
class AuthenticatedUserApiControllerTest extends ControllerTestSupport {

    @Nested
    class getUserAccount {

        @Test
        void 회원정보를_조회한다() throws Exception {
            // given
            UserAccountResponse response = new UserAccountResponse(1L, "email@example.com", "name", "profileImageName", LocalDateTime.of(2025, 11, 23, 0, 0, 0));

            given(userService.getUserAccount(1L)).willReturn(response);

            // when
            ResultActions resultActions = mockMvc.perform(get("/api/user"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(1L))
                    .andExpect(jsonPath("$.data.email").value("email@example.com"))
                    .andExpect(jsonPath("$.data.nickname").value("name"))
                    .andExpect(jsonPath("$.data.profileImageName").value("profileImageName"))
                    .andExpect(jsonPath("$.data.createdDate").value("2025-11-23 00:00:00"));
        }
    }

    @Nested
    class updateUserAccount {

        @Test
        void 회원정보를_수정한다() throws Exception {
            // given
            UserAccountUpdateRequest request = UserAccountUpdateRequest.builder()
                    .nickname("newName")
                    .profileImageName("newProfileImageName")
                    .build();

            UserAccountUpdateResponse result = UserAccountUpdateResponse.builder()
                    .profileImageName("newProfileImageName")
                    .build();

            given(userService.updateAccount(any(Long.class), any(UserAccountUpdateRequest.class))).willReturn(result);

            // when
            ResultActions resultActions = mockMvc.perform(patch("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data.profileImageName").value("newProfileImageName"));
        }

        @Test
        void 회원정보수정_시_닉네임이_입력됐다면_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given
            UserAccountUpdateRequest request = UserAccountUpdateRequest.builder()
                    .nickname(" ")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(patch("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("닉네임은 공백일 수 없습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());;
        }
    }

    @Nested
    class updatePassword {

        @Test
        void 비밀번호를_변경한다() throws Exception {
            // given
            UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder()
                    .password("NewPassword123!")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(patch("/api/user/password")
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());;
        }

        @Test
        void 비밀번호변경_시_공백이_아닌_문자가_1개_이상_있어야_한다() throws Exception {
            // given
            UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder()
                    .password(" ")
                    .build();

            // when
            ResultActions resultActions = mockMvc.perform(patch("/api/user/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("비밀번호는 공백일 수 없습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());;
        }
    }

    @Nested
    class deleteUserAccount {

        @Test
        void 회원을_탈퇴한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(delete("/api/user"));

            // then
            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").isEmpty())
                    .andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
                    .andExpect(jsonPath("$.data").isEmpty());;
        }
    }
}