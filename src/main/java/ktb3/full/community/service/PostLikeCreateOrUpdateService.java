package ktb3.full.community.service;

import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.PostLike;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.response.PostLikeRespnose;
import ktb3.full.community.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostLikeCreateOrUpdateService {

    private final PostLikeRepository postLikeRepository;
    private final UserService userService;
    private final PostService postService;

    @Transactional
    public PostLikeRespnose createOrUpdate(long userId, long postId) {
        Post post = postService.getForUpdateOrThrow(postId);

        PostLike postLike = postLikeRepository.findByUserIdAndPostId(userId, postId)
                .orElseGet(() -> {
                    User user = userService.getOrThrow(userId);
                    return postLikeRepository.save(PostLike.create(user, post));
                });

        postLike.toggle();

        return new PostLikeRespnose(postLike.isLiked(), post.getLikeCount());
    }
}
