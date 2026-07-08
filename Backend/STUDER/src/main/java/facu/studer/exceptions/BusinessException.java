package facu.studer.exceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String messageKey;
    private final Object[] args;


    public BusinessException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = null;
    }


    public BusinessException(String messageKey, Object... args) {
        super(messageKey);
        this.messageKey = messageKey;
        this.args = args;
    }


    public BusinessException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause);
        this.messageKey = messageKey;
        this.args = args;
    }
}
