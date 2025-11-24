package ktb3.full.community.service;

import ktb3.full.community.common.exception.PostNotFoundException;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.repository.CommentRepository;
import ktb3.full.community.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostDeleteService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @PreAuthorize("@postRepository.findByIdActive(#postId).get().getUser().getId() == principal.userId")
    @Transactional
    public void deletePost(long postId) {
        // soft delete
        Post post = postRepository.findByIdActive(postId).orElseThrow(PostNotFoundException::new);
        post.delete();
        commentRepository.deleteAllByPostId(postId);
    }
}
