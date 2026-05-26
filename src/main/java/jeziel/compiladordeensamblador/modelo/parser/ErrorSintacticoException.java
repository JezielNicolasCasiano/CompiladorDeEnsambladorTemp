package jeziel.compiladordeensamblador.modelo.parser;


public class ErrorSintacticoException extends RuntimeException {

    private final ErrorSintactico error;

    public ErrorSintacticoException(ErrorSintactico error) {
        super(error.getMensaje());
        this.error = error;
    }

    public ErrorSintactico getError() { return error; }
}
