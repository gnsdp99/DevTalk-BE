package ktb3.full.community.service;

import ktb3.full.community.common.config.JpaConfig;
import ktb3.full.community.common.exception.CannotChangeSameNicknameException;
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
import ktb3.full.community.security.config.PasswordEncoderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({ JpaConfig.class, PasswordEncoderConfig.class })
@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserService userService;
    
    @BeforeEach
    void setUp() {
        userService =  new UserService(userRepository, new ImageUploadServiceStub(), passwordEncoder);
    }

    @Nested
    class validateEmailAvailable {

        @Test
        void 이메일이_존재하지_않으면_true를_반환한다() {
            // given

            // when
            UserValidationResponse response = userService.validateEmailAvailable("email@example.com");

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        void 이메일이_이미_존재하면_false를_반환한다() {
            // given
            userRepository.save(UserFixture.createWithEmail("email@example.com"));

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

            // when
            UserValidationResponse response = userService.validateNicknameAvailable("name");

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        void 닉네임이_이미_존재하면_false를_반환한다() {
            // given
            userRepository.save(UserFixture.createWithNickname("name"));

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
            MultipartFile profileImage = MultipartFileFixture.createProfileImage("profileImageName");

            // when
            UserRegisterRequest request = new UserRegisterRequest("email@example.com", "Password123!", "name", profileImage);

            Long registeredId = userService.register(request);

            // then
            User foundUser = userRepository.findById(registeredId).orElseThrow();

            assertThat(foundUser.getEmail()).isEqualTo("email@example.com");
            assertThat(passwordEncoder.matches("Password123!", foundUser.getPassword())).isTrue();
            assertThat(foundUser.getNickname()).isEqualTo("name");
            assertThat(foundUser.getProfileImageName()).isEqualTo("profileImageName");
        }

        @Test
        void 이메일이_이미_존재하면_예외가_발생한다() {
            // given
            User user = UserFixture.createWithEmailAndNickname("email@example.com", "name1");

            userRepository.save(user);

            // when & then
            UserRegisterRequest request = new UserRegisterRequest("email@example.com", user.getPassword(), "name2", MultipartFileFixture.createProfileImage());

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicatedEmailException.class);
        }

        @Test
        void 닉네임이_이미_존재하면_예외가_발생한다() {
            // given
            User user = UserFixture.createWithEmailAndNickname("email1@example.com", "name");

            userRepository.save(user);

            // when & then
            UserRegisterRequest request = new UserRegisterRequest("email2@example.com", user.getPassword(), "name", MultipartFileFixture.createProfileImage());

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicatedNicknameException.class);
        }
    }

    @Nested
    class updateAccount {

        @Test
        void 닉네임과_프로필_이미지를_변경한다() {
            // given
            MultipartFile profileImage = MultipartFileFixture.createProfileImage("newProfileImageName");
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "oldProfileImageName");

            userRepository.save(user);

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("newName", profileImage);

            userService.updateAccount(user.getId(), request);

            // then
            User foundUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(foundUser.getNickname()).isEqualTo("newName");
            assertThat(foundUser.getProfileImageName()).isEqualTo("newProfileImageName");
        }

        @Test
        void 닉네임만_변경한다() {
            // given
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "oldProfileImageName");

            userRepository.save(user);

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("newName", null);

            userService.updateAccount(user.getId(), request);

            // then
            User foundUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(foundUser.getNickname()).isEqualTo("newName");
            assertThat(foundUser.getProfileImageName()).isEqualTo("oldProfileImageName");
        }

        @Test
        void 프로필_이미지만_변경한다() {
            // given
            MultipartFile profileImage = MultipartFileFixture.createProfileImage("newProfileImageName");
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "oldProfileImageName");

            userRepository.save(user);

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest(null, profileImage);

            userService.updateAccount(user.getId(), request);

            // then
            User foundUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(foundUser.getNickname()).isEqualTo("oldName");
            assertThat(foundUser.getProfileImageName()).isEqualTo("newProfileImageName");
        }

        @Test
        void 닉네임이_이미_존재하면_예외가_발생한다() {
            // given
            User user = UserFixture.createUser("email1@example.com", "Password123!", "name1", "profileImageName1", false);

            userRepository.save(user);
            userRepository.save(UserFixture.createUser("email2@example.com", "Password123!", "name2", "profileImageName2", false));

            // when & then
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("name2", null);

            assertThatThrownBy(() -> userService.updateAccount(user.getId(), request))
                    .isInstanceOf(DuplicatedNicknameException.class);
        }

        @Test
        void 닉네임이_이전과_동일하면_예외가_발생한다() {
            // given
            User user = UserFixture.createWithNickname("name");

            userRepository.save(user);

            // when & then
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("name", null);

            assertThatThrownBy(() -> userService.updateAccount(user.getId(), request))
                    .isInstanceOf(CannotChangeSameNicknameException.class);
        }
    }

    @Nested
    class updatePassword {

        @Test
        void 비밀번호를_변경한다() {
            // given
            User user = UserFixture.createWithPassword("OldEncodedPassword");

            userRepository.save(user);

            // when
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("NewPassword123!");

            userService.updatePassword(user.getId(), request);

            // then
            User foundUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(passwordEncoder.matches("NewPassword123!", foundUser.getPassword())).isTrue();
        }
    }

    @Test
    void 비밀번호가_이전과_동일하면_예외가_발생한다() {
        // given
        User user = UserFixture.createWithPassword(passwordEncoder.encode("NewPassword123!"));

        userRepository.save(user);

        // when & then
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("NewPassword123!");

        assertThatThrownBy(() -> userService.updatePassword(user.getId(), request))
                .isInstanceOf(CannotChangeSamePasswordException.class);
    }

    static class ImageUploadServiceStub extends ImageUploadService {

        @Override
        public String saveImageAndGetName(MultipartFile image) {
            return image.getName();
        }
    }
}