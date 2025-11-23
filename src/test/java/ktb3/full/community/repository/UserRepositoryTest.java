package ktb3.full.community.repository;

import ktb3.full.community.config.JpaTest;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.fixture.UserFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@JpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Nested
    class findByEmail {

        @Test
        void 탈퇴하지_않은_회원이면_조회된다() {
            // given
            userRepository.save(UserFixture.createWithEmail("email@example.com"));

            // when
            User user = userRepository.findByEmail("email@example.com").orElse(null);

            // then
            assertThat(user).isNotNull();
        }

        @Test
        void 탈퇴한_회원이면_조회되지_않는다() {
            // given
            userRepository.save(UserFixture.createDeletedWithEmail("email@example.com"));

            // when
            User user = userRepository.findByEmail("email@example.com").orElse(null);

            // then
            assertThat(user).isNull();
        }
    }
}