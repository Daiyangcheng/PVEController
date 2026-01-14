package cn.locyan.pvecontroller.shared.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class CaptchaException extends RuntimeException {
    private List<String> errors;

    public CaptchaException(String message) {
        super(message);
    }

    public CaptchaException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

}
