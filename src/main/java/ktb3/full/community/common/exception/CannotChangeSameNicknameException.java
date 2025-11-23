package ktb3.full.community.common.exception;

import ktb3.full.community.common.exception.base.BadRequestException;

public class CannotChangeSameNicknameException extends BadRequestException {

    @Override
    public ApiErrorCode getApiErrorCode() {
        return ApiErrorCode.CANNOT_CHANGE_SAME_PASSWORD;
    }
}
