package ktb3.full.community.service;

import ktb3.full.community.common.exception.*;
import ktb3.full.community.domain.entity.User;
import ktb3.full.community.dto.request.UserAccountUpdateRequest;
import ktb3.full.community.dto.request.UserPasswordUpdateRequest;
import ktb3.full.community.dto.request.UserRegisterRequest;
import ktb3.full.community.dto.response.UserProfileResponse;
import ktb3.full.community.dto.response.UserValidationResponse;
import ktb3.full.community.dto.response.UserAccountResponse;
import ktb3.full.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;
    private final PasswordEncoder passwordEncoder;

    public UserValidationResponse validateEmailAvailable(String email) {
        return new UserValidationResponse(!userRepository.existsByEmail(email));
    }

    public UserValidationResponse validateNicknameAvailable(String nickname) {
        return new UserValidationResponse(!userRepository.existsByNickname(nickname));
    }

    @Transactional
    public long register(UserRegisterRequest request) {
        validateEmailDuplication(request.getEmail());
        validateNicknameDuplication(request.getNickname());
        String profileImageName = imageUploadService.saveImageAndGetPath(request.getProfileImage());
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        return userRepository.save(request.toUserEntity(encodedPassword, profileImageName)).getId();
    }

    public UserAccountResponse getUserAccount(long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return UserAccountResponse.from(user);
    }

    public UserProfileResponse getUserProfile(long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public void updateAccount(long userId, UserAccountUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (request.getNickname() != null) {
            validateNicknameDuplication(request.getNickname());
            user.updateNickname(request.getNickname());
        }

        if (request.getProfileImage() != null) {
            String profileImageName = imageUploadService.saveImageAndGetPath(request.getProfileImage());
            user.updateProfileImageName(profileImageName);
        }
    }

    @Transactional
    public void updatePassword(long userId, UserPasswordUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updatePassword(passwordEncoder.encode(request.getPassword()));
    }

    private void validateEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicatedEmailException();
        }
    }

    private void validateNicknameDuplication(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicatedNicknameException();
        }
    }
}
