package ktb3.full.community.domain.entity;

import ktb3.full.community.fixture.CommentFixture;
import ktb3.full.community.fixture.PostFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentTest {

    @Nested
    class delete {

        @Test
        void 댓글_삭제_시_이미_삭제된_댓글이면_예외가_발생한다() {
            // given
            Comment comment = CommentFixture.createDeleted();

            // when & then
            assertThatThrownBy(comment::delete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 삭제된 댓글입니다.");
        }
    }
}