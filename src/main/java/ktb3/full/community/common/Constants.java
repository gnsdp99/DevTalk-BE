package ktb3.full.community.common;

public abstract class Constants {

    // Message
    public static final String MESSAGE_NOT_NULL_EMAIL = "이메일은 필수입니다.";
    public static final String MESSAGE_NOT_NULL_NICKNAME = "닉네임은 필수입니다.";
    public static final String MESSAGE_NOT_NULL_PASSWORD = "비밀번호는 필수입니다.";
    public static final String MESSAGE_NOT_NULL_POST_TITLE = "제목은 필수입니다.";
    public static final String MESSAGE_NOT_NULL_POST_CONTENT = "내용은 필수입니다.";
    public static final String MESSAGE_NOT_NULL_COMMENT_CONTENT = "내용은 필수입니다.";

    public static final String MESSAGE_NULLABLE_NOT_BLANK_NICKNAME = "닉네임은 공백일 수 없습니다.";
    public static final String MESSAGE_NULLABLE_NOT_BLANK_PASSWORD = "비밀번호는 공백일 수 없습니다.";
    public static final String MESSAGE_NULLABLE_NOT_BLANK_POST_TITLE = "제목은 공백일 수 없습니다.";
    public static final String MESSAGE_NULLABLE_NOT_BLANK_POST_CONTENT = "내용은 공백일 수 없습니다.";
    public static final String MESSAGE_NULLABLE_NOT_BLANK_COMMENT_CONTENT = "내용은 공백일 수 없습니다.";

    // Name
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";

    // Value
    public static final int MAXIMUM_SESSIONS = 1;

    // API
    public static final String[] WHITE_LIST = {
            "/api/actuator/health",
            "/api/users/email-validation",
            "/api/users/nickname-validation",
            "/api/users/check",
            "/images/**",
    };

    public static final String REGISTER = "/api/users";
    public static final String LOGIN = "/api/users/login";
    public static final String LOGOUT = "/api/user/logout";
    public static final String GET_POST_LIST = "/api/posts";

    // etc
    public static final String DELETED_AUTHOR = "(탈퇴한 회원)";
}
