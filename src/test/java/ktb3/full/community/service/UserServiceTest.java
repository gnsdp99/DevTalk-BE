package ktb3.full.community.service;

import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.common.exception.CannotChangeSameNicknameException;
import ktb3.full.community.common.exception.CannotChangeSamePasswordException;
import ktb3.full.community.common.exception.DuplicatedEmailException;
import ktb3.full.community.common.exception.DuplicatedNicknameException;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.UserAccountUpdateRequest;
import ktb3.full.community.dto.request.UserPasswordUpdateRequest;
import ktb3.full.community.dto.request.UserRegisterRequest;
import ktb3.full.community.dto.response.UserAccountUpdateResponse;
import ktb3.full.community.fixture.MultipartFileFixture;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserService sut;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    class register {

        @Test
        void 회원을_생성한다() {
            // given
            MockMultipartFile profileImage = MultipartFileFixture.createProfileImageWithOriginName("profileImage.png");

            UserRegisterRequest request = UserRegisterRequest.builder()
                    .email("email@example.com")
                    .password("Password123!")
                    .nickname("name")
                    .profileImage(profileImage)
                    .build();

            // when
            long registeredUserID = sut.register(request);

            // then
            User foundUser = userRepository.findById(registeredUserID).orElseThrow();
            assertThat(foundUser.getEmail()).isEqualTo("email@example.com");
            assertThat(passwordEncoder.matches("Password123!", foundUser.getPassword())).isTrue();
            assertThat(foundUser.getNickname()).isEqualTo("name");
            assertThat(foundUser.getProfileImageName()).isEqualTo("/images/profileImage.png");
        }

        @Test
        void 회원_생성_시_이미_존재하는_이메일이면_예외가_발생한다() {
            // given
            userRepository.save(UserFixture.createUser("email@example.com", "other"));

            UserRegisterRequest request = UserRegisterRequest.builder()
                    .email("email@example.com")
                    .password("Password123!")
                    .nickname("me")
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.register(request))
                    .isInstanceOf(DuplicatedEmailException.class);
        }

        @Test
        void 회원_생성_시_이미_존재하는_닉네임이면_예외가_발생한다() {
            // given
            userRepository.save(UserFixture.createUser("other@example.com", "name"));

            UserRegisterRequest request = UserRegisterRequest.builder()
                    .email("me@example.com")
                    .password("Password123!")
                    .nickname("name")
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.register(request))
                    .isInstanceOf(DuplicatedNicknameException.class);
        }
    }

    @Nested
    class updateAccount {

        @Test
        void 회원의_닉네임만_수정한다() {
            // given
            User me = userRepository.save(UserFixture.createUser("me@example.com", "me", "/images/me.png"));

            UserAccountUpdateRequest request = UserAccountUpdateRequest.builder()
                    .nickname("newName")
                    .build();

            // when
            UserAccountUpdateResponse response = sut.updateAccount(me.getId(), request);

            // then
            assertThat(response.getProfileImageName()).isEqualTo("/images/me.png");

            User foundUser = userRepository.findById(me.getId()).orElseThrow();
            assertThat(foundUser.getNickname()).isEqualTo("newName");
        }

        @Test
        void 회원의_프로필이미지만_수정한다() {
            // given
            User me = userRepository.save(UserFixture.createUser("me@example.com", "me", "/images/me.png"));
            MockMultipartFile newProfileImage = MultipartFileFixture.createProfileImageWithOriginName("newProfileImage.png");

            UserAccountUpdateRequest request = UserAccountUpdateRequest.builder()
                    .profileImage(newProfileImage)
                    .build();

            // when
            UserAccountUpdateResponse response = sut.updateAccount(me.getId(), request);

            // then
            assertThat(response.getProfileImageName()).isEqualTo("/images/newProfileImage.png");

            User foundUser = userRepository.findById(me.getId()).orElseThrow();
            assertThat(foundUser.getNickname()).isEqualTo("me");
        }

        @Test
        void 회원의_닉네임_수정_시_이미_존재하는_닉네임이면_예외가_발생한다() {
            // given
            User me = userRepository.save(UserFixture.createUser("me@example.com", "me"));
            userRepository.save(UserFixture.createUser("other@example.com", "other"));

            UserAccountUpdateRequest request = UserAccountUpdateRequest.builder()
                    .nickname("other")
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.updateAccount(me.getId(), request))
                    .isInstanceOf(DuplicatedNicknameException.class);
        }

        @Test
        void 회원의_닉네임_수정_시_이전과_동일한_닉네임이면_예외가_발생한다() {
            // given
            User me = userRepository.save(UserFixture.createUser("me@example.com", "me"));

            UserAccountUpdateRequest request = UserAccountUpdateRequest.builder()
                    .nickname("me")
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.updateAccount(me.getId(), request))
                    .isInstanceOf(CannotChangeSameNicknameException.class);
        }
    }

    @Nested
    class updatePassword {

        @Test
        void 회원의_비밀번호를_수정한다() {
            // given
            User me = userRepository.save(UserFixture.createWithPassword("Password123!"));

            UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder()
                    .password("NewPassword123!")
                    .build();

            // when
            sut.updatePassword(me.getId(), request);

            // then
            User foundUser = userRepository.findById(me.getId()).orElseThrow();
            assertThat(passwordEncoder.matches("NewPassword123!", foundUser.getPassword())).isTrue();
        }

        @Test
        void 회원의_비밀번호_수정_시_이전과_동일한_비밀번호면_예외가_발생한다() {
            // given
            User me = userRepository.save(UserFixture.createWithPassword(passwordEncoder.encode("Password123!")));

            UserPasswordUpdateRequest request = UserPasswordUpdateRequest.builder()
                    .password("Password123!")
                    .build();

            // when & then
            assertThatThrownBy(() -> sut.updatePassword(me.getId(), request))
                    .isInstanceOf(CannotChangeSamePasswordException.class);
        }
    }
}
