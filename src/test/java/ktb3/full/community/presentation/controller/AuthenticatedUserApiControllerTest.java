package ktb3.full.community.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ktb3.full.community.common.exception.CannotChangeSamePasswordException;
import ktb3.full.community.common.exception.DuplicatedNicknameException;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.dto.request.UserAccountUpdateRequest;
import ktb3.full.community.dto.request.UserPasswordUpdateRequest;
import ktb3.full.community.dto.response.UserAccountResponse;
import ktb3.full.community.fixture.MultipartFileFixture;
import ktb3.full.community.service.UserDeleteService;
import ktb3.full.community.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithAuthMockUser
@WebMvcTest(
        controllers = AuthenticatedUserApiController.class)
class AuthenticatedUserApiControllerTest {

    private static final String BASE_URI = "/user";

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDeleteService userDeleteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class getUserAccount {

        String TARGET_URI = BASE_URI;

        @WithAuthMockUser(userId = 1L)
        @Test
        void 회원정보조회에_성공하면_200_Ok와_회원정보를_응답한다() throws Exception {
            // given
            UserAccountResponse response = new UserAccountResponse(1L, "email@example.com", "name", "profileImageName", LocalDateTime.of(2025, 11, 23, 0, 0, 0));

            given(userService.getUserAccount(1L)).willReturn(response);

            // when
            ResultActions resultActions = mockMvc.perform(get(TARGET_URI)
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(1L))
                    .andExpect(jsonPath("$.data.email").value("email@example.com"))
                    .andExpect(jsonPath("$.data.nickname").value("name"))
                    .andExpect(jsonPath("$.data.profileImageName").value("profileImageName"))
                    .andExpect(jsonPath("$.data.createdAt").value("2025-11-23 00:00:00"));
        }
    }

    @Nested
    class updateUserAccount {

        String TARGET_URI = BASE_URI;

        @Test
        void 회원정보수정에_성공하면_200_Ok를_응답한다() throws Exception {
            // given
            willDoNothing().given(userService).updateAccount(any(Long.class), any(UserAccountUpdateRequest.class));

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, TARGET_URI)
                    .file(MultipartFileFixture.createProfileImage())
                    .param("nickname", "newName")
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isOk());
        }

        @Test
        void 이미_존재하는_닉네임이면_409_Conflict와_4092을_응답한다() throws Exception {
            // given
            willThrow(DuplicatedNicknameException.class).given(userService).updateAccount(any(Long.class), any(UserAccountUpdateRequest.class));

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, TARGET_URI)
                    .file(MultipartFileFixture.createProfileImage())
                    .param("nickname", "dupName")
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(4092));
        }

        @Test
        void 닉네임이_유효하지_않으면_400_BadRequest와_4001을_응답한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.PATCH, TARGET_URI)
                    .file(MultipartFileFixture.createProfileImage())
                    .param("nickname", "longNickname")
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4001));
        }
    }

    @Nested
    class updatePassword {

        String TARGET_URI = BASE_URI + "/password";

        @Test
        void 비밀번호수정에_성공하면_200_Ok를_응답한다() throws Exception {
            // given
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("NewPassword123!");

            willDoNothing().given(userService).updatePassword(any(Long.class), any(UserPasswordUpdateRequest.class));

            // when
            ResultActions resultActions = mockMvc.perform(patch(TARGET_URI)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isOk());
        }

        @Test
        void 이전과_동일한_비밀번호면_400_BadRequest와_4007을_응답한다() throws Exception {
            // given
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("SamePassword123!");

            willThrow(CannotChangeSamePasswordException.class).given(userService).updatePassword(any(Long.class), any(UserPasswordUpdateRequest.class));

            // when
            ResultActions resultActions = mockMvc.perform(patch(TARGET_URI)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4007));
        }
    }

    @Nested
    class deleteUserAccount {

        String TARGET_URI = BASE_URI;

        @Test
        void 회원탈퇴에_성공하면_200_Ok를_응답한다() throws Exception {
            // given
            willDoNothing().given(userDeleteService).deleteAccount(any(Long.class));

            // when
            ResultActions resultActions = mockMvc.perform(delete(TARGET_URI)
                    .with(csrf()));

            // then
            resultActions
                    .andExpect(status().isOk());
        }
    }
}