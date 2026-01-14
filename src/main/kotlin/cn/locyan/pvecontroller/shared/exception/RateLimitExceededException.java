package cn.locyan.pvecontroller.shared.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {
    private Long timeLeft;

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Long timeLeft) {
        super(message);
        this.timeLeft = timeLeft;
    }
}
