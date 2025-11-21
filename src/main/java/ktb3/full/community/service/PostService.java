package ktb3.full.community.service;

import ktb3.full.community.common.exception.PostNotFoundException;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.PostCreateRequest;
import ktb3.full.community.dto.request.PostUpdateRequest;
import ktb3.full.community.dto.response.PostDetailResponse;
import ktb3.full.community.dto.response.PostResponse;
import ktb3.full.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserService userService;
    private final PostLikeService postLikeService;
    private final ImageUploadService imageUploadService;

    public PagedModel<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> postPages = postRepository.findAll(pageable);
        return new PagedModel<>(postPages.map(PostResponse::from));
    }

    @Transactional
    public PostDetailResponse getPost(long userId, long postId) {
        postRepository.increaseViewCount(postId);
        Post post = getOrThrow(postId);
        boolean liked = postLikeService.isLiked(userId, postId);
        return PostDetailResponse.from(post, liked);
    }

    @Transactional
    public PostDetailResponse createPost(long userId, PostCreateRequest request) {
        MultipartFile image = request.getImage();
        String imagePath = imageUploadService.saveImageAndGetPath(request.getImage());
        String imageName = image != null ? image.getOriginalFilename() : null;
        User user = userService.getOrThrow(userId);
        Post post = request.toEntity(user, imagePath, imageName);

        postRepository.save(post);

        return PostDetailResponse.from(post, false);
    }

    @PreAuthorize("@postRepository.findById(#postId).get().getUser().getId() == principal.userId")
    @Transactional
    public PostDetailResponse updatePost(long postId, PostUpdateRequest request) {
        Post post = getOrThrow(postId);

        if (request.getTitle() != null) {
            post.updateTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            post.updateContent(request.getContent());
        }

        if (request.getImage() != null) {
            String imagePath = imageUploadService.saveImageAndGetPath(request.getImage());
            post.updateImage(imagePath, request.getImage().getOriginalFilename());
        }

        boolean liked = postLikeService.isLiked(post.getUser().getId(), postId);

        return PostDetailResponse.from(post, liked);
    }

    @Transactional
    public void deleteAllPostByUserId(long userId) {
        postRepository.deleteAllByUserId(userId);
    }

    public Post getOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(PostNotFoundException::new);
    }

    public Post getForUpdateOrThrow(long postId) {
        return postRepository.findByIdForUpdate(postId)
                .orElseThrow(PostNotFoundException::new);
    }
}
