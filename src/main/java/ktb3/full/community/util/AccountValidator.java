package ktb3.full.community.util;

import ktb3.full.community.common.Constants;
import ktb3.full.community.domain.entity.User;

public class AccountValidator {

    public static Long getUserId(User user) {
        return isUserInactive(user) ? null : user.getId();
    }

    public static String getAuthorName(User user) {
        return isUserInactive(user) ? Constants.DELETED_AUTHOR : user.getNickname();
    }

    public static String getAuthorProfileImageName(User user) {
        return isUserInactive(user) ? null : user.getProfileImageName();
    }

    private static boolean isUserInactive(User user) {
        return user == null || user.isDeleted();
    }
}
