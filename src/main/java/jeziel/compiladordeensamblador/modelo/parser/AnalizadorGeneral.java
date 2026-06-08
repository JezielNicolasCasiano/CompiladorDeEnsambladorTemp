package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class AnalizadorGeneral {
    private ErrorSintactico errorSintactico;
    private List<Token> lineaAAnalizar;
    private LineaAnalizada lineaAnalizada;

    public AnalizadorGeneral(Token primerToken, List<Token> lineaAAnalizar){
        this.lineaAAnalizar = lineaAAnalizar;
    }





    //getters y setters
    public ErrorSintactico getErrorSintactico() {
        return errorSintactico;
    }

    public void setErrorSintactico(ErrorSintactico errorSintactico) {
        this.errorSintactico = errorSintactico;
    }

    public List<Token> getLineaAAnalizar() {
        return lineaAAnalizar;
    }

    public void setLineaAAnalizar(List<Token> lineaAAnalizar) {
        this.lineaAAnalizar = lineaAAnalizar;
    }

    public LineaAnalizada getLineaAnalizada() {
        return lineaAnalizada;
    }

    public void setLineaAnalizada(LineaAnalizada lineaAnalizada) {
        this.lineaAnalizada = lineaAnalizada;
    }
}
