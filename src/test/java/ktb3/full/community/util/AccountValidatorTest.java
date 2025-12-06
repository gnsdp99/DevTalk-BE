package ktb3.full.community.util;

import ktb3.full.community.domain.entity.User;
import ktb3.full.community.fixture.UserFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountValidatorTest {

    @Nested
    class getUserId {

        @Test
        void 활성화된_회원이면_회원ID를_반환한다() {
            // given
            User user = UserFixture.createWithId(1L);

            // when
            Long result = AccountValidator.getUserId(user);

            // then
            assertThat(result).isEqualTo(1L);
        }

        @Test
        void 탈퇴한_회원이면_null을_반환한다() {
            // given
            User user = UserFixture.createDeleted();

            // when
            Long result = AccountValidator.getUserId(user);

            // then
            assertThat(result).isNull();
        }
    }

    @Nested
    class getAuthorName {

        @Test
        void 활성화된_회원이면_닉네임을_반환한다() {
            // given
            User user = UserFixture.createWithNickname("nickname");

            // when
            String result = AccountValidator.getAuthorName(user);

            // then
            assertThat(result).isEqualTo("nickname");
        }

        @Test
        void 탈퇴한_회원이면_특정_문자열을_반환한다() {
            // given
            User user = UserFixture.createDeleted();

            // when
            String result = AccountValidator.getAuthorName(user);

            // then
            assertThat(result).isEqualTo("(탈퇴한 회원)");
        }
    }

    @Nested
    class getAuthorProfileImageName {

        @Test
        void 활성화된_회원이면_프로필_이미지_파일명을_반환한다() {
            // given
            User user = UserFixture.createWithProfileImageName("profileImageName");

            // when
            String result = AccountValidator.getAuthorProfileImageName(user);

            // then
            assertThat(result).isEqualTo("profileImageName");
        }

        @Test
        void 탈퇴한_회원이면_null을_반환한다() {
            // given
            User user = UserFixture.createDeleted();

            // when
            String result = AccountValidator.getAuthorProfileImageName(user);

            // then
            assertThat(result).isNull();
        }
    }
}