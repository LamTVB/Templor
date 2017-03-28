package templor.exception;

import templor.language_templor.Token;

/**
 * Created by Lam on 24/02/2017.
 */
public class InterpreterException
        extends RuntimeException{

    private final String _message;
    private final Token _node;

    public InterpreterException(
            String message, Token node){

        this._message = message;
        this._node = node;
    }

    @Override
    public String getMessage(){
        if (this._node != null) {
            return this._message + " at line " + this._node.getLine()
                    + " position " + this._node.getPos();
        }

        return this._message + " at line 1 position 1";
    }
}
