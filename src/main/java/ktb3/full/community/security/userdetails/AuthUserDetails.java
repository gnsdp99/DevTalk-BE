package ktb3.full.community.security.userdetails;

import lombok.*;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@ToString
@Getter
@Builder
@AllArgsConstructor
public class AuthUserDetails implements UserDetails, CredentialsContainer {

    private final Long userId;
    private final String username;
    private String password;
    private final String profileImageName;

    public static AuthUserDetails onlyUserId(AuthUserDetails userDetails) {
        return AuthUserDetails.builder()
                .userId(userDetails.userId)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
