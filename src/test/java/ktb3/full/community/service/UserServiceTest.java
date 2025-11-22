package ktb3.full.community.service;

import ktb3.full.community.common.config.JpaConfig;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@Import({ JpaConfig.class })
@ExtendWith(MockitoExtension.class)
@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Mock
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
            String email = "email@example.com";

            // when
            UserValidationResponse response = userService.validateEmailAvailable(email);

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        void 이메일이_이미_존재하면_false를_반환한다() {
            // given
            String email = "email@example.com";

            userRepository.save(UserFixture.createWithEmail(email));

            // when
            UserValidationResponse response = userService.validateEmailAvailable(email);

            // then
            assertThat(response.isAvailable()).isFalse();
        }
    }

    @Nested
    class validateNicknameAvailable {

        @Test
        void 닉네임이_존재하지_않으면_true를_반환한다() {
            // given
            String nickname = "name";

            // when
            UserValidationResponse response = userService.validateNicknameAvailable(nickname);

            // then
            assertThat(response.isAvailable()).isTrue();
        }

        @Test
        void 닉네임이_이미_존재하면_false를_반환한다() {
            // given
            String nickname = "name";

            userRepository.save(UserFixture.createWithNickname(nickname));

            // when
            UserValidationResponse response = userService.validateNicknameAvailable(nickname);

            // then
            assertThat(response.isAvailable()).isFalse();
        }
    }

    @Nested
    class register {

        @Test
        void 회원가입에_성공한다() {
            // given
            String rawPassword = "Password123!";
            String profileImageName = UUID.randomUUID().toString();
            MultipartFile profileImage = MultipartFileFixture.createImage(profileImageName);

            when(passwordEncoder.encode(rawPassword)).thenReturn("EncodedPassword");

            // when
            UserRegisterRequest request = new UserRegisterRequest("email@example.com", rawPassword, "name", profileImage);

            Long registeredId = userService.register(request);

            // then
            User foundUser = userRepository.findById(registeredId).orElseThrow();

            assertThat(foundUser.getEmail()).isEqualTo("email@example.com");
            assertThat(foundUser.getPassword()).isEqualTo("EncodedPassword");
            assertThat(foundUser.getNickname()).isEqualTo("name");
            assertThat(foundUser.getProfileImageName()).isEqualTo(profileImageName);
        }

        @Test
        void 이메일이_이미_존재하면_예외가_발생한다() {
            // given
            String sameEmail = "email@example.com";
            User user = UserFixture.createWithEmailAndNickname(sameEmail, "name1");

            userRepository.save(user);

            // when & then
            UserRegisterRequest request = new UserRegisterRequest(sameEmail, user.getPassword(), "name2", MultipartFileFixture.createImage());

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicatedEmailException.class);
        }

        @Test
        void 닉네임이_이미_존재하면_예외가_발생한다() {
            // given
            String sameNickname = "name";
            User user = UserFixture.createWithEmailAndNickname("email1@example.com", sameNickname);

            userRepository.save(user);

            // when & then
            UserRegisterRequest request = new UserRegisterRequest("email2@example.com", user.getPassword(), sameNickname, MultipartFileFixture.createImage());

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicatedNicknameException.class);
        }
    }

    @Nested
    class updateAccount {

        @Test
        void 닉네임과_프로필_이미지를_변경한다() {
            // given
            String newProfileImageName = UUID.randomUUID().toString();
            MultipartFile profileImage = MultipartFileFixture.createImage(newProfileImageName);
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "/images/oldProfile.jpg");

            userRepository.save(user);

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("newName", profileImage);

            userService.updateAccount(user.getId(), request);

            // then
            User foundUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(foundUser.getNickname()).isEqualTo("newName");
            assertThat(foundUser.getProfileImageName()).isEqualTo(newProfileImageName);
        }

        @Test
        void 닉네임만_변경한다() {
            // given
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "/images/oldProfile.jpg");

            userRepository.save(user);

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("newName", null);

            userService.updateAccount(user.getId(), request);

            // then
            User foundUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(foundUser.getNickname()).isEqualTo("newName");
            assertThat(foundUser.getProfileImageName()).isEqualTo("/images/oldProfile.jpg");
        }

        @Test
        void 프로필_이미지만_변경한다() {
            // given
            String newProfileImageName = UUID.randomUUID().toString();
            MultipartFile profileImage = MultipartFileFixture.createImage(newProfileImageName);
            User user = UserFixture.createWithNicknameAndProfileImageName("oldName", "/images/oldProfile.jpg");

            userRepository.save(user);

            // when
            UserAccountUpdateRequest request = new UserAccountUpdateRequest(null, profileImage);

            userService.updateAccount(user.getId(), request);

            // then
            User foundUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(foundUser.getNickname()).isEqualTo("oldName");
            assertThat(foundUser.getProfileImageName()).isEqualTo(newProfileImageName);
        }

        @Test
        void 닉네임이_이미_존재하면_예외가_발생한다() {
            // given
            User user = UserFixture.createUser("email1@example.com", "Password123!", "name1", "/images/oldProfile.jpg", false);

            userRepository.save(user);
            userRepository.save(UserFixture.createWithEmailAndNickname("email2@example.com", "name2"));

            // when & then
            UserAccountUpdateRequest request = new UserAccountUpdateRequest("name2", null);

            assertThatThrownBy(() -> userService.updateAccount(user.getId(), request))
                    .isInstanceOf(DuplicatedNicknameException.class);
        }
    }

    @Nested
    class updatePassword {

        @Test
        void 비밀번호를_변경한다() {
            // given
            String newRawPassword = "NewPassword123!";
            User user = UserFixture.createWithPassword("OldEncodedPassword");

            when(passwordEncoder.matches(newRawPassword, user.getPassword())).thenReturn(false);
            when(passwordEncoder.encode(newRawPassword)).thenReturn("NewEncodedPassword");
            userRepository.save(user);

            // when
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(newRawPassword);

            userService.updatePassword(user.getId(), request);

            // then
            User foundUser = userRepository.findById(user.getId()).orElseThrow();

            assertThat(foundUser.getPassword()).isEqualTo("NewEncodedPassword");
        }
    }

    @Test
    void 비밀번호가_이전과_동일하면_예외가_발생한다() {
        // given
        String newRawPassword = "NewPassword123!";
        User user = UserFixture.createWithPassword("OldEncodedPassword");

        when(passwordEncoder.matches(newRawPassword, user.getPassword())).thenReturn(true);
        userRepository.save(user);

        // when & then
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(newRawPassword);

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