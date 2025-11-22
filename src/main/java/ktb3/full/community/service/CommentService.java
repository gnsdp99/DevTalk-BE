package ktb3.full.community.service;

import ktb3.full.community.common.exception.CommentNotFoundException;
import ktb3.full.community.common.exception.PostNotFoundException;
import ktb3.full.community.common.exception.UserNotFoundException;
import ktb3.full.community.domain.entity.Comment;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.CommentCreateRequest;
import ktb3.full.community.dto.request.CommentUpdateRequest;
import ktb3.full.community.dto.response.CommentResponse;
import ktb3.full.community.repository.CommentRepository;
import ktb3.full.community.repository.PostRepository;
import ktb3.full.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PagedModel<CommentResponse> getAllComments(long postId, Pageable pageable) {
        Page<Comment> commentsPageResponse = commentRepository.findAllLatestByPostId(postId, pageable);
        return new PagedModel<>(commentsPageResponse.map(CommentResponse::from));
    }

    public CommentResponse getComment(long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        return CommentResponse.from(comment);
    }

    @Transactional
    public long createComment(long userId, long postId, CommentCreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Post post = postRepository.findByIdForUpdate(postId).orElseThrow(PostNotFoundException::new);
        Comment comment = request.toEntity(user, post);
        post.increaseCommentCount();
        commentRepository.save(comment);
        return CommentResponse.from(comment).getCommentId();
    }

    @PreAuthorize("@commentRepository.findById(#commentId).get().getUser().getId() == principal.userId")
    @Transactional
    public void updateComment(long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);

        if (request.getContent() != null) {
            comment.updateContent(request.getContent());
        }
    }

    @PreAuthorize("@commentRepository.findById(#commentId).get().getUser().getId() == principal.userId")
    @Transactional
    public void deleteComment(long commentId) {
        // soft delete
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFoundException::new);
        comment.delete();
        Post post = postRepository.findByIdForUpdate(comment.getPost().getId()).orElseThrow(PostNotFoundException::new);
        post.decreaseCommentCount();
    }

    @Transactional
    public void deleteAllCommentByPostId(long postId) {
        commentRepository.deleteAllByPostId(postId);
    }

    @Transactional
    public void deleteAllCommentByUserId(long userId) {
        commentRepository.deleteAllByUserId(userId);
    }
}
