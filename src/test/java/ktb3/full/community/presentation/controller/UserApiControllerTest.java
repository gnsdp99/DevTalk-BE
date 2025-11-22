package ktb3.full.community.presentation.controller;

import ktb3.full.community.common.exception.DuplicatedEmailException;
import ktb3.full.community.common.exception.DuplicatedNicknameException;
import ktb3.full.community.common.exception.UserNotFoundException;
import ktb3.full.community.dto.request.UserRegisterRequest;
import ktb3.full.community.dto.response.UserProfileResponse;
import ktb3.full.community.dto.response.UserValidationResponse;
import ktb3.full.community.fixture.MultipartFileFixture;
import ktb3.full.community.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserApiController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UserApiControllerTest {

    private static final String BASE_URI = "/users";

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class validateEmailAvailable {

        String TARGET_URI = BASE_URI + "/email-validation";

        @Test
        void 존재하지_않는_이메일이면_200_Ok와_true를_응답한다() throws Exception {
            // given
            String email = "email@example.com";
            UserValidationResponse response = new UserValidationResponse(true);

            when(userService.validateEmailAvailable(email)).thenReturn(response);

            // when
            ResultActions resultActions = mockMvc.perform(get(TARGET_URI)
                    .param("email", email));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(true));
        }

        @Test
        void 이미_존재하는_이메일이면_200_Ok와_false를_응답한다() throws Exception {
            // given
            String email = "email@example.com";
            UserValidationResponse response = new UserValidationResponse(false);

            when(userService.validateEmailAvailable(email)).thenReturn(response);

            // when
            ResultActions resultActions = mockMvc.perform(get(TARGET_URI)
                    .param("email", email));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(false));
        }
    }

    @Nested
    class validateNicknameAvailable {

        String TARGET_URI = BASE_URI + "/nickname-validation";

        @Test
        void 존재하지_않는_닉네임이면_200_Ok와_true를_응답한다() throws Exception {
            // given
            String nickname = "name";
            UserValidationResponse response = new UserValidationResponse(true);

            when(userService.validateNicknameAvailable(nickname)).thenReturn(response);

            // when
            ResultActions resultActions = mockMvc.perform(get(TARGET_URI)
                    .param("nickname", nickname));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(true));
        }

        @Test
        void 이미_존재하는_닉네임이면_200_Ok와_false를_응답한다() throws Exception {
            // given
            String nickname = "name";
            UserValidationResponse response = new UserValidationResponse(false);

            when(userService.validateNicknameAvailable(nickname)).thenReturn(response);

            // when
            ResultActions resultActions = mockMvc.perform(get(TARGET_URI)
                    .param("nickname", nickname));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").value(false));
        }
    }

    @Nested
    class signUp {

        String TARGET_URI = BASE_URI;

        @Test
        void 회원가입에_성공하면_201_Created를_응답한다() throws Exception {
            // given
            when(userService.register(any(UserRegisterRequest.class))).thenReturn(1L);

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, TARGET_URI)
                    .file(MultipartFileFixture.createProfileImage())
                    .param("email", "email@example.com")
                    .param("password", "Password123!")
                    .param("nickname", "name"));

            // then
            resultActions
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "/users/1"));
        }

        @Test
        void 입력값이_유효하지_않으면_400_BadRequest와_4001을_응답한다() throws Exception {
            // given

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, TARGET_URI)
                    .file(MultipartFileFixture.createProfileImage())
                    .param("email", "email.com")
                    .param("password", "password123!")
                    .param("nickname", "longNickname"));

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4001));
        }

        @Test
        void 이미_존재하는_이메일이면_409_Conflict와_4091을_응답한다() throws Exception {
            // given
            when(userService.register(any(UserRegisterRequest.class))).thenThrow(DuplicatedEmailException.class);

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, TARGET_URI)
                    .file(MultipartFileFixture.createProfileImage())
                    .param("email", "email@example.com")
                    .param("password", "Password123!")
                    .param("nickname", "name"));

            // then
            resultActions
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(4091));
        }

        @Test
        void 이미_존재하는_닉네임이면_409_Conflict와_4092를_응답한다() throws Exception {
            // given
            when(userService.register(any(UserRegisterRequest.class))).thenThrow(DuplicatedNicknameException.class);

            // when
            ResultActions resultActions = mockMvc.perform(multipart(HttpMethod.POST, TARGET_URI)
                    .file(MultipartFileFixture.createProfileImage())
                    .param("email", "email@example.com")
                    .param("password", "Password123!")
                    .param("nickname", "name"));

            // then
            resultActions
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value(4092));
        }
    }

    @Nested
    class getUserProfile {

        String TARGET_URI = BASE_URI + "/{userId}";

        @Test
        void 회원이_존재하면_200_Ok와_프로필_정보를_응답한다() throws Exception {
            // given
            long userId = 1L;
            UserProfileResponse response = new UserProfileResponse(userId, "name", "profileImage.png");

            when(userService.getUserProfile(userId)).thenReturn(response);

            // when
            ResultActions resultActions = mockMvc.perform(get(TARGET_URI, userId));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.nickname").value("name"))
                    .andExpect(jsonPath("$.data.profileImageName").value("profileImage.png"));
        }

        @Test
        void 존재하지_않는_회원이면_404_NotFound와_4041을_응답한다() throws Exception {
            // given
            long userId = 1L;
            when(userService.getUserProfile(userId)).thenThrow(UserNotFoundException.class);

            // when
            ResultActions resultActions = mockMvc.perform(get(TARGET_URI, userId));

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(4041));
        }
    }
}