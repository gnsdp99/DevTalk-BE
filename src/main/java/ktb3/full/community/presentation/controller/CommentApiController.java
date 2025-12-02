package ktb3.full.community.presentation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import ktb3.full.community.security.userdetails.AuthUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ktb3.full.community.dto.request.CommentCreateRequest;
import ktb3.full.community.dto.request.CommentUpdateRequest;
import ktb3.full.community.dto.response.ApiResponse;
import ktb3.full.community.dto.response.CommentResponse;
import ktb3.full.community.presentation.api.CommentApi;
import ktb3.full.community.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RequiredArgsConstructor
@RestController
public class CommentApiController implements CommentApi {

    private final CommentService commentService;

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<PagedModel<CommentResponse>>> getAllComments(
            @Valid Pageable pageable,
            @Positive @PathVariable("postId") long postId) {
        PagedModel<CommentResponse> response = commentService.getAllComments(postId, pageable);
        return ResponseEntity.ok()
                .body(ApiResponse.success(response));
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(@Positive @PathVariable("commentId") long commentId) {
        CommentResponse response = commentService.getComment(commentId);
        return ResponseEntity.ok()
                .body(ApiResponse.success(response));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Positive @PathVariable("postId") long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.createComment(userDetails.getUserId(), postId, request);
        return ResponseEntity.created(URI.create(String.format("/comments/%d", response.getCommentId())))
                .body(ApiResponse.success(response));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @Positive @PathVariable("commentId") long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        commentService.updateComment(commentId, request);
        return ResponseEntity.ok()
                .body(ApiResponse.success());
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @Positive @PathVariable("commentId") long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok()
                .body(ApiResponse.success());
    }
}
