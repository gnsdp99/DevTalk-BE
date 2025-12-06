package ktb3.full.community.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NamedQuery(
        name = "Post.findByIdActive",
        query = "select p from Post p where p.id = :id and p.isDeleted = false"
)
@Entity
public class Post extends AuditTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", nullable = false, length = 26)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "origin_image_name", length = 255)
    private String originImageName;

    @Column(name = "image_name", unique = true, length = 255)
    private String imageName;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "comment_count", nullable = false)
    private int commentCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    public static Post create(User user, String title, String content) {
        return Post.create(user, title, content, null, null);
    }

    public static Post create(User user, String title, String content, String originImageName, String imageName) {
        return Post.builder()
                .user(user)
                .title(title)
                .content(content)
                .originImageName(originImageName)
                .imageName(imageName)
                .viewCount(0)
                .commentCount(0)
                .likeCount(0)
                .isDeleted(false)
                .build();
    }

    @Builder
    private Post(User user, String title, String content, String originImageName, String imageName, int viewCount, int commentCount, int likeCount, boolean isDeleted) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.originImageName = originImageName;
        this.imageName = imageName;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
        this.likeCount = likeCount;
        this.isDeleted = isDeleted;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateImage(String originImageName, String imageName) {
        this.originImageName = originImageName;
        this.imageName = imageName;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount <= 0) {
            throw new IllegalStateException("좋아요수는 음수가 될 수 없습니다.");
        }

        this.likeCount--;
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount <= 0) {
            throw new IllegalStateException("댓글수는 음수가 될 수 없습니다.");
        }

        this.commentCount--;
    }

    public void delete() {
        if (isDeleted) {
            throw new IllegalStateException("이미 삭제된 게시글입니다.");
        }

        this.isDeleted = true;
        this.auditDeletedAt();
    }
}
