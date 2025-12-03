package ktb3.full.community.service;

import ktb3.full.community.IntegrationTestSupport;
import ktb3.full.community.config.WithAuthMockUser;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.PostCreateRequest;
import ktb3.full.community.dto.request.PostUpdateRequest;
import ktb3.full.community.dto.response.PostDetailResponse;
import ktb3.full.community.fixture.MultipartFileFixture;
import ktb3.full.community.fixture.PostFixture;
import ktb3.full.community.fixture.UserFixture;
import ktb3.full.community.repository.PostRepository;
import ktb3.full.community.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

public class PostServiceTest extends IntegrationTestSupport {

    @Autowired
    private PostService sut;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    class createPost {

        @Test
        void 이미지가_있는_게시글을_생성한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            MockMultipartFile image = MultipartFileFixture.createWithOriginName("image.png");

            PostCreateRequest request = PostCreateRequest.builder()
                    .title("title")
                    .content("content")
                    .image(image)
                    .build();

            // when
            Long postId = sut.createPost(user.getId(), request);

            // then
            Post foundPost = postRepository.findById(postId).orElseThrow();
            assertThat(foundPost.getTitle()).isEqualTo("title");
            assertThat(foundPost.getContent()).isEqualTo("content");
            assertThat(foundPost.getImageName()).isEqualTo("/images/image.png");
            assertThat(foundPost.getOriginImageName()).isEqualTo("image.png");
        }

        @Test
        void 이미지가_없는_게시글을_생성한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());

            PostCreateRequest request = PostCreateRequest.builder()
                    .title("title")
                    .content("content")
                    .image(null)
                    .build();

            // when
            Long postId = sut.createPost(user.getId(), request);

            // then
            Post foundPost = postRepository.findById(postId).orElseThrow();
            assertThat(foundPost.getTitle()).isEqualTo("title");
            assertThat(foundPost.getContent()).isEqualTo("content");
            assertThat(foundPost.getImageName()).isNull();
            assertThat(foundPost.getOriginImageName()).isNull();
        }
    }

    @Nested
    class updatePost {

        @WithAuthMockUser
        @Test
        void 게시글의_제목_내용_이미지_모두_수정한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createPost(user));
            MockMultipartFile updatedImage = MultipartFileFixture.createWithOriginName("updatedImage.png");

            PostUpdateRequest request = PostUpdateRequest.builder()
                    .title("updated title")
                    .content("updated content")
                    .image(updatedImage)
                    .build();

            // when
            sut.updatePost(post.getId(), request);

            // then
            Post foundPost = postRepository.findById(post.getId()).orElseThrow();
            assertThat(foundPost.getTitle()).isEqualTo("updated title");
            assertThat(foundPost.getContent()).isEqualTo("updated content");
            assertThat(foundPost.getImageName()).isEqualTo("/images/updatedImage.png");
            assertThat(foundPost.getOriginImageName()).isEqualTo("updatedImage.png");
        }

        @WithAuthMockUser
        @Test
        void 게시글의_제목만_입력하면_제목만_수정된다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createForUpdate(user, "title", "content", "originImage.png", "/images/originImage.png"));

            PostUpdateRequest request = PostUpdateRequest.builder()
                    .title("updated title")
                    .content(null)
                    .image(null)
                    .build();

            // when
            sut.updatePost(post.getId(), request);

            // then
            Post foundPost = postRepository.findById(post.getId()).orElseThrow();
            assertThat(foundPost.getTitle()).isEqualTo("updated title");
            assertThat(foundPost.getContent()).isEqualTo("content");
            assertThat(foundPost.getImageName()).isEqualTo("/images/originImage.png");
            assertThat(foundPost.getOriginImageName()).isEqualTo("originImage.png");
        }

        @WithAuthMockUser
        @Test
        void 게시글의_내용만_입력하면_내용만_수정된다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createForUpdate(user, "title", "content", "originImage.png", "/images/originImage.png"));

            PostUpdateRequest request = PostUpdateRequest.builder()
                    .title(null)
                    .content("updated content")
                    .image(null)
                    .build();

            // when
            sut.updatePost(post.getId(), request);

            // then
            Post foundPost = postRepository.findById(post.getId()).orElseThrow();
            assertThat(foundPost.getTitle()).isEqualTo("title");
            assertThat(foundPost.getContent()).isEqualTo("updated content");
            assertThat(foundPost.getImageName()).isEqualTo("/images/originImage.png");
            assertThat(foundPost.getOriginImageName()).isEqualTo("originImage.png");
        }

        @WithAuthMockUser
        @Test
        void 게시글의_이미지만_입력하면_이미지만_수정된다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createForUpdate(user, "title", "content", "originImage.png", "/images/originImage.png"));
            MockMultipartFile updatedImage = MultipartFileFixture.createWithOriginName("updatedImage.png");

            PostUpdateRequest request = PostUpdateRequest.builder()
                    .title(null)
                    .content(null)
                    .image(updatedImage)
                    .build();

            // when
            sut.updatePost(post.getId(), request);

            // then
            Post foundPost = postRepository.findById(post.getId()).orElseThrow();
            assertThat(foundPost.getTitle()).isEqualTo("title");
            assertThat(foundPost.getContent()).isEqualTo("content");
            assertThat(foundPost.getImageName()).isEqualTo("/images/updatedImage.png");
            assertThat(foundPost.getOriginImageName()).isEqualTo("updatedImage.png");
        }
    }

    @Nested
    class getPost {

        @Test
        void 게시글을_조회하면_조회수가_1_증가한다() {
            // given
            User user = userRepository.save(UserFixture.createUser());
            Post post = postRepository.save(PostFixture.createPost(user));

            // when
            PostDetailResponse response = sut.getPost(user.getId(), post.getId());

            // then
            assertThat(response.getViewCount()).isOne();
        }
    }
}