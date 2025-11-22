package ktb3.full.community.service;

import ktb3.full.community.common.exception.CannotChangeSamePasswordException;
import ktb3.full.community.common.exception.DuplicatedEmailException;
import ktb3.full.community.common.exception.DuplicatedNicknameException;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.UserAccountUpdateRequest;
import ktb3.full.community.dto.request.UserPasswordUpdateRequest;
import ktb3.full.community.dto.request.UserRegisterRequest;
import ktb3.full.community.dto.response.UserValidationResponse;
import ktb3.full.community.fixture.MultipartFileFixture;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private UserService userService;

    @Nested
    class validateEmailAvailable {

        @Test
        void 이메일이_존재하지_않으면_true를_반환한다() {
            // given
            given(userRepository.existsByEmail("email@example.com")).willReturn(false);

            // when
            UserValidationResponse response = userService.validateEmailAvailable("email@example.com");

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        void 이메일이_이미_존재하면_false를_반환한다() {
            // given
            given(userRepository.existsByEmail("email@example.com")).willReturn(true);

            // when
            UserValidationResponse response = userService.validateEmailAvailable("email@example.com");

            // then
            assertThat(response.isAvailable()).isFalse();
        }
    }

    @Nested
    class validateNicknameAvailable {

        @Test
        void 닉네임이_존재하지_않으면_true를_반환한다() {
            // given
            given(userRepository.existsByNickname("name")).willReturn(false);

            // when
            UserValidationResponse response = userService.validateNicknameAvailable("name");

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        void 닉네임이_이미_존재하면_false를_반환한다() {
            // given
            given(userRepository.existsByNickname("name")).willReturn(true);

            // when
            UserValidationResponse response = userService.validateNicknameAvailable("name");

            // then
            assertThat(response.isAvailable()).isFalse();
        }
    }

    @Nested
    class register {

        @Test
        void 회원가입에_성공한다() {
            // given
            MultipartFile profileImage = MultipartFileFixture.createProfileImage();

            given(userRepository.existsByEmail("email@example.com")).willReturn(false);
            given(userRepository.existsByNickname("name")).willReturn(false);
            given(passwordEncoder.encode("Password123!")).willReturn("EncodedPassword");
            given(imageUploadService.saveImageAndGetName(profileImage)).willReturn("profileImageName");
            given(userRepository.save(any(User.class))).willReturn(UserFixture.createWithId(1L));

            // when
            UserRegisterRequest request = new UserRegisterRequest("email@example.com", "Password123!", "name", profileImage);

            Long registeredId = userService.register(request);

            // then
            assertThat(registeredId).isEqualTo(1L);
        }

        @Test
        void 이메일이_이미_존재하면_예외가_발생한다() {
            // given
            given(userRepository.existsByEmail("email@example.com")).willReturn(true);

            // when & then
            UserRegisterRequest request = new UserRegisterRequest("email@example.com", "Password123!", "name", MultipartFileFixture.createProfileImage());

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicatedEmailException.class);
        }

        @Test
        void 닉네임이_이미_존재하면_예외가_발생한다() {
            // given
            given(userRepository.existsByNickname("name")).willReturn(true);

            // when & then
            UserRegisterRequest request = new UserRegisterRequest("email@example.com", "Password123!", "name", MultipartFileFixture.createProfileImage());

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicatedNicknameException.class);
        }
    }

    @Nested
    class updateAccount {

        @Test
        void 닉네임과_프로필_이미지를_변경한다() {
            // given
            MultipartFile newProfileImage = MultipartFileFixture.createProfileImage();
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "oldProfileImageName");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByNickname("newName")).willReturn(false);
            given(imageUploadService.saveImageAndGetName(newProfileImage)).willReturn("newProfileImageName");

            // given
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("newName", newProfileImage);

            userService.updateAccount(1L, request);

            // then
            assertThat(user.getNickname()).isEqualTo("newName");
            assertThat(user.getProfileImageName()).isEqualTo("newProfileImageName");
        }

        @Test
        void 닉네임만_변경한다() {
            // given
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "oldProfileImageName");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByNickname("newName")).willReturn(false);

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("newName", null);

            userService.updateAccount(1L, request);

            // then
            assertThat(user.getNickname()).isEqualTo("newName");
            assertThat(user.getProfileImageName()).isEqualTo("oldProfileImageName");
        }

        @Test
        void 프로필_이미지만_변경한다() {
            // given
            MultipartFile newProfileImage = MultipartFileFixture.createProfileImage();
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "oldProfileImageName");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(imageUploadService.saveImageAndGetName(newProfileImage)).willReturn("newProfileImageName");

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest(null, newProfileImage);

            userService.updateAccount(1L, request);

            // then
            assertThat(user.getNickname()).isEqualTo("oldName");
            assertThat(user.getProfileImageName()).isEqualTo("newProfileImageName");
        }

        @Test
        void 닉네임이_이미_존재하면_예외가_발생한다() {
            // given
            User user = UserFixture.createWithNickname("oldName");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(userRepository.existsByNickname("dupName")).willReturn(true);

            // when & then
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("dupName", null);

            assertThatThrownBy(() -> userService.updateAccount(1L, request))
                    .isInstanceOf(DuplicatedNicknameException.class);
        }
    }

    @Nested
    class updatePassword {

        @Test
        void 비밀번호를_변경한다() {
            // given
            User user = UserFixture.createWithPassword("OldEncodedPassword");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("NewRawPassword123!", "OldEncodedPassword")).willReturn(false);
            given(passwordEncoder.encode("NewRawPassword123!")).willReturn("NewEncodedPassword");

            // when
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("NewRawPassword123!");

            userService.updatePassword(1L, request);

            // then
            assertThat(user.getPassword()).isEqualTo("NewEncodedPassword");
        }

        @Test
        void 비밀번호가_이전과_동일하면_예외가_발생한다() {
            // given
            User user = UserFixture.createWithPassword("OldEncodedPassword");

            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("NewRawPassword123!", "OldEncodedPassword")).willReturn(true);

            // when & then
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("NewRawPassword123!");

            assertThatThrownBy(() -> userService.updatePassword(1L, request))
                    .isInstanceOf(CannotChangeSamePasswordException.class);
        }
    }
}