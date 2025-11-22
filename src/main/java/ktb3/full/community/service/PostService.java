package ktb3.full.community.service;

import ktb3.full.community.common.exception.PostNotFoundException;
import ktb3.full.community.common.exception.UserNotFoundException;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.PostCreateRequest;
import ktb3.full.community.dto.request.PostUpdateRequest;
import ktb3.full.community.dto.response.PostDetailResponse;
import ktb3.full.community.dto.response.PostResponse;
import ktb3.full.community.repository.PostRepository;
import ktb3.full.community.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final PostLikeService postLikeService;
    private final ImageUploadService imageUploadService;

    public PagedModel<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> postPages = postRepository.findAll(pageable);
        return new PagedModel<>(postPages.map(PostResponse::from));
    }

    @Transactional
    public PostDetailResponse getPost(long userId, long postId) {
        postRepository.increaseViewCount(postId);
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);
        boolean liked = postLikeService.isLiked(userId, postId);
        return PostDetailResponse.from(post, liked);
    }

    @Transactional
    public long createPost(long userId, PostCreateRequest request) {
        MultipartFile image = request.getImage();
        String imagePath = imageUploadService.saveImageAndGetPath(request.getImage());
        String imageName = image != null ? image.getOriginalFilename() : null;
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = request.toEntity(user, imagePath, imageName);
        postRepository.save(post);
        return PostDetailResponse.from(post, false).getPostId();
    }

    @PreAuthorize("@postRepository.findById(#postId).get().getUser().getId() == principal.userId")
    @Transactional
    public void updatePost(long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

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
    }

    @Transactional
    public void deleteAllPostByUserId(long userId) {
        postRepository.deleteAllByUserId(userId);
    }
}
