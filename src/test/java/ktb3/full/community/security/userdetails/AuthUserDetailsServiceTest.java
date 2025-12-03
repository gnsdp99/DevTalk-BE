package ktb3.full.community.security.userdetails;

import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthUserDetailsServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUserDetailsService sut;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @Nested
    class loadUserByUsername {

        @Test
        void 이메일이_존재하면_회원이_조회된다() {
            // given
            userRepository.save(UserFixture.createUser("email@example.com", "Password123!", "name", "profileImageName", false));

            // when
            AuthUserDetails userDetails = (AuthUserDetails) sut.loadUserByUsername("email@example.com");

            // then
            assertThat(userDetails.getUsername()).isEqualTo("email@example.com");
            assertThat(userDetails.getPassword()).isEqualTo("Password123!");
            assertThat(userDetails.getProfileImageName()).isEqualTo("profileImageName");
        }

        @Test
        void 이메일이_존재하지_않으면_예외가_발생한다() {
            // given

            // when & then
            assertThatThrownBy(() -> sut.loadUserByUsername("email@example.com"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        void 이메일이_존재해도_탈퇴한_회원이면_예외가_발생한다() {
            // given
            userRepository.save(UserFixture.createDeletedWithEmail("email@example.com"));

            // when & then
            assertThatThrownBy(() -> sut.loadUserByUsername("email@example.com"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }
    }
}