package mino.exception;

import mino.language_mino.Token;

/**
 * Created by Lam on 30/03/2017.
 */
public class SemanticException extends RuntimeException {

    private final String message;

    private final Token token;

    public SemanticException(
            String message,
            Token token) {

        this.message = message;
        this.token = token;
    }

    @Override
    public String getMessage() {

        if (this.token != null) {
            return this.message + " at line " + this.token.getLine()
                    + " position " + this.token.getPos();
        }

        return this.message + " at line 1 position 1";
    }

}
