package ktb3.full.community.common.annotation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

import static ktb3.full.community.common.Constants.MESSAGE_POST_CONTENT_PATTERN;

@Pattern(regexp = "^(?s)(?=.*\\S)[\\s\\S]+$")
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = { })
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface PostContentPattern {

    String message() default MESSAGE_POST_CONTENT_PATTERN;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
