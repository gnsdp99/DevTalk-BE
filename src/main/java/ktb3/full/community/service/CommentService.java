package ktb3.full.community.service;

import ktb3.full.community.common.exception.CommentNotFoundException;
import ktb3.full.community.common.exception.base.NotFoundException;
import ktb3.full.community.domain.entity.Comment;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.CommentCreateRequest;
import ktb3.full.community.dto.request.CommentUpdateRequest;
import ktb3.full.community.dto.response.CommentResponse;
import ktb3.full.community.repository.CommentRepository;
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
    private final UserService userService;
    private final PostService postService;

    public PagedModel<CommentResponse> getAllComments(long postId, Pageable pageable) {
        Page<Comment> commentsPageResponse = commentRepository.findAllLatestByPostId(postId, pageable);
        return new PagedModel<>(commentsPageResponse.map(CommentResponse::from));
    }

    public CommentResponse getComment(long commentId) {
        Comment comment = getOrThrow(commentId);
        return CommentResponse.from(comment);
    }

    @Transactional
    public CommentResponse createComment(long userId, long postId, CommentCreateRequest request) {
        User user = userService.getOrThrow(userId);
        Post post = postService.getForUpdateOrThrow(postId);
        Comment comment = request.toEntity(user, post);
        post.increaseCommentCount();
        commentRepository.save(comment);
        return CommentResponse.from(comment);
    }

    @PreAuthorize("@commentRepository.findById(#commentId).get().getUser().getId() == principal.userId")
    @Transactional
    public CommentResponse updateComment(long commentId, CommentUpdateRequest request) {
        Comment comment = getOrThrow(commentId);

        if (request.getContent() != null) {
            comment.updateContent(request.getContent());
        }

        return CommentResponse.from(comment);
    }

    @PreAuthorize("@commentRepository.findById(#commentId).get().getUser().getId() == principal.userId")
    @Transactional
    public void deleteComment(long commentId) {
        // soft delete
        Comment comment = getOrThrow(commentId);
        comment.delete();
        Post post = postService.getForUpdateOrThrow(comment.getPost().getId());
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

    public Comment getOrThrow(Long commentId) throws NotFoundException {
        return commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
    }
}
