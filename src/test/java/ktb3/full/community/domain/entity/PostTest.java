package ktb3.full.community.domain.entity;

import ktb3.full.community.fixture.PostFixture;
import ktb3.full.community.fixture.UserFixture;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostTest {

    @Nested
    class create {

        @Test
        void 게시글_생성_시_좋아요수_댓글수_조회수는_0이다() {
            // given
            User user = UserFixture.createUser();

            // when
            Post post = Post.create(user, "title", "content");

            // then
            assertThat(post.getViewCount()).isZero();
            assertThat(post.getCommentCount()).isZero();
            assertThat(post.getLikeCount()).isZero();
        }
    }

    @Nested
    class decreaseLikeCount {

        @Test
        void 좋아요수가_1_감소한다() {
            // given
            User user = UserFixture.createUser();
            Post post = PostFixture.createWithLikeCount(user, 1);

            // when
            post.decreaseLikeCount();

            // then
            assertThat(post.getLikeCount()).isZero();
        }

        @Test
        void 좋아요수가_0일_때_감소시키면_예외가_발생한다() {
            // given
            User user = UserFixture.createUser();
            Post post = PostFixture.createWithLikeCount(user, 0);

            // when & then
            assertThatThrownBy(post::decreaseLikeCount)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("좋아요수는 음수가 될 수 없습니다.");

            assertThat(post.getLikeCount()).isZero();
        }
    }

    @Nested
    class decreaseCommentCount {

        @Test
        void 댓글수가_1_감소한다() {
            // given
            User user = UserFixture.createUser();
            Post post = PostFixture.createWithCommentCount(user, 1);

            // when
            post.decreaseCommentCount();

            // then
            assertThat(post.getCommentCount()).isZero();
        }

        @Test
        void 댓글수가_0일_때_감소시키면_예외가_발생한다() {
            // given
            User user = UserFixture.createUser();
            Post post = PostFixture.createWithCommentCount(user, 0);

            // when & then
            assertThatThrownBy(post::decreaseCommentCount)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("댓글수는 음수가 될 수 없습니다.");;

            assertThat(post.getCommentCount()).isZero();
        }
    }

    @Nested
    class delete {

        @Test
        void 게시글_삭제_시_이미_삭제된_게시글이면_예외가_발생한다() {
            // given
            Post post = PostFixture.createDeleted(null);

            // when & then
            assertThatThrownBy(post::delete)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("이미 삭제된 게시글입니다.");
        }
    }
}