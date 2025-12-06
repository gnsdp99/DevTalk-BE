package ktb3.full.community.domain.entity;

import ktb3.full.community.fixture.UserFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Nested
    class create {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "email!example.com", "email@example,com", "!!!@!!!.!!!"})
        void 회원_생성_시_이메일_형식이_유효하지_않으면_예외가_발생한다(String email) {
            // given

            // when & then
            assertThatThrownBy(() -> User.create(email, "Password123!", "nickname"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("이메일이 유효하지 않습니다.");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "tooLongNickname", "nick_name", "_nickname", "nickname_", "nick name"})
        void 회원_생성_시_닉네임_형식이_유효하지_않으면_예외가_발생한다(String nickname) {
            // given

            // when & then
            assertThatThrownBy(() -> User.create("email@example.com", "Password123!", nickname))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("닉네임이 유효하지 않습니다.");
        }
    }

    @Nested
    class delete {

        @Test
        void 회원_삭제_시_이미_삭제된_회원이면_예외가_발생한다() {
            // given
            User user = UserFixture.createDeleted();

            // when & then
            assertThatThrownBy(user::delete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 삭제된 회원입니다.");
        }
    }
}