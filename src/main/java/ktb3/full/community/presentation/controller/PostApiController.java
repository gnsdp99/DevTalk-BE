package ktb3.full.community.presentation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import ktb3.full.community.dto.response.*;
import ktb3.full.community.security.userdetails.AuthUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ktb3.full.community.dto.request.PostCreateRequest;
import ktb3.full.community.dto.request.PostUpdateRequest;
import ktb3.full.community.presentation.api.PostApi;
import ktb3.full.community.service.PostDeleteService;
import ktb3.full.community.service.PostLikeService;
import ktb3.full.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Validated
@RequiredArgsConstructor
@RequestMapping("/posts")
@RestController
public class PostApiController implements PostApi {

    private final PostService postService;
    private final PostDeleteService postDeleteService;
    private final PostLikeService postLikeService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<PagedModel<PostResponse>>> getAllPosts(@Valid Pageable pageable) {
        PagedModel<PostResponse> response = postService.getAllPosts(pageable);
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.of(response));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiSuccessResponse<PostDetailResponse>> getPostDetail(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Positive @PathVariable("postId") long postId) {
        PostDetailResponse response = postService.getPost(userDetails.getUserId(), postId);
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.of(response));
    }

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<Void>> createPost(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Valid @ModelAttribute PostCreateRequest request
    ) {
        long postId = postService.createPost(userDetails.getUserId(), request);
        return ResponseEntity.created(URI.create(String.format("/posts/%d", postId)))
                .body(ApiSuccessResponse.getBaseResponse());
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<ApiSuccessResponse<Void>> updatePost(
            @Positive @PathVariable("postId") long postId,
            @Valid @ModelAttribute PostUpdateRequest request) {
        postService.updatePost(postId, request);
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.getBaseResponse());
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deletePost(
            @Positive @PathVariable("postId") long postId) {
        postDeleteService.deletePost(postId);
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.getBaseResponse());
    }

    @PatchMapping("/{postId}/like")
    public ResponseEntity<ApiSuccessResponse<Void>> likePost(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Positive @PathVariable("postId") long postId) {
        postLikeService.createOrUpdate(userDetails.getUserId(), postId);
        return ResponseEntity.ok()
                .body(ApiSuccessResponse.getBaseResponse());
    }
}
