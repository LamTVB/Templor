package templor.exception;

/**
 * Created by Lam on 24/02/2017.
 */
public class InterpreterException
        extends RuntimeException{

    private final String _message;
    private final Integer _line;
    private final Integer _pos;

    public InterpreterException(
            String message,Integer line, Integer pos){

        this._message = message;
        this._line = line;
        this._pos = pos;
    }

    @Override
    public String getMessage(){
        if (this._line != null && this._pos != null) {
            return this._message + " at line " + this._line
                    + " position " + this._pos;
        }

        return this._message + " at line 1 position 1";
    }
}
