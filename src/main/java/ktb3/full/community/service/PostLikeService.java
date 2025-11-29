package ktb3.full.community.service;

import ktb3.full.community.common.exception.PostNotFoundException;
import ktb3.full.community.common.exception.UserNotFoundException;
import ktb3.full.community.domain.entity.Post;
import ktb3.full.community.domain.entity.PostLike;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.repository.PostLikeRepository;
import ktb3.full.community.repository.PostRepository;
import ktb3.full.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public void createOrUpdate(long userId, long postId) {
        Post post = postRepository.findByIdForUpdate(postId).orElseThrow(PostNotFoundException::new);

        PostLike postLike = postLikeRepository.findByUserIdAndPostId(userId, postId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
                    return postLikeRepository.save(PostLike.create(user, post));
                });

        postLike.toggle();
    }
}
