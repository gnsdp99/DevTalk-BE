package ktb3.full.community.service;

import ktb3.full.community.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostDeleteService {

    private final PostService postService;
    private final CommentService commentService;

    @PreAuthorize("@postRepository.findById(#postId).get().getUser().getId() == principal.userId")
    @Transactional
    public void deletePost(long postId) {
        // soft delete
        Post post = postService.getOrThrow(postId);
        post.delete();
        commentService.deleteAllCommentByPostId(postId);
    }
}
