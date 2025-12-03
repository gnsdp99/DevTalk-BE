package ktb3.full.community.fixture;

import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PostFixture {

    public static Post createPost(User user, String title, String content, String originImageName, String imageName, int viewCount, int commentCount, int likeCount, boolean isDeleted) {
        return Post.builder()
                .user(user)
                .title(title == null ? "Post Title" : title)
                .content(content == null ? "this is post content" : content)
                .originImageName(originImageName)
                .imageName(imageName)
                .viewCount(viewCount)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .isDeleted(isDeleted)
                .build();
    }

    public static Post createPost() {
        return createPost(null, null, null, null, null, 0, 0, 0, false);
    }

    public static Post createPost(User user) {
        return createPost(user, null, null, null, null, 0, 0, 0, false);
    }

    public static List<Post> createPosts(int count, Supplier<Post> supplier) {
        List<Post> posts =  new ArrayList<>();
        for (int i = 0; i < count; i++) {
            posts.add(supplier.get());
        }
        return posts;
    }

    public static List<Post> createPosts(User user, int count) {
        return createPosts(count, () -> PostFixture.createPost(user));
    }

    public static List<Post> createDeletedPosts(User user, int count) {
        return createPosts(count, () -> PostFixture.createDeleted(user));
    }

    public static List<Post> createWithoutUserPosts(int count) {
        return createPosts(count, PostFixture::createWithoutUser);
    }

    public static Post createWithLikeCount(int likeCount) {
        return createPost(null, null, null, null, null, 0, 0, likeCount, false);
    }

    public static Post createWithLikeCount(User user, int likeCount) {
        return createPost(user, null, null, null, null, 0, 0, likeCount, false);
    }

    public static Post createWithCommentCount(User user, int commentCount) {
        return createPost(user, null, null, null, null, 0, commentCount, 0, false);
    }

    public static Post createWithViewCount(User user, int viewCount) {
        return createPost(user, null, null, null, null, viewCount, 0, 0, false);
    }

    public static Post createDeleted(User user) {
        return createPost(user, null, null, null, null, 0, 0, 0, true);
    }

    public static Post createWithoutUser() {
        return createPost(null, null, null, null, null, 0, 0, 0, false);
    }

    public static Post createForUpdate(User user, String title, String content, String originImageName, String imageName) {
        return createPost(user, title, content, originImageName, imageName, 0, 0, 0, false);
    }
}
