package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorLineaSintactica {
    private List<Token> tokens;
    private LineaAnalizada linea;
    private int tokenActualContador;
    private int lineaActualContador;
    private Token primerToken;


    public AnalizadorLineaSintactica(List<Token> tokens){
        this.tokens = tokens;
        tokenActualContador = 1;
        primerToken = tokens.getFirst();
    }

    public LineaAnalizada analizarLinea(){
        /*En esta parte tiene que haber un ciclo que vaya analizando linea por linea y cuando alcance el limite y todo esta bien devolver la linea,
        el evento que devuelve la linea es que los siguientes tokens en su atributo linea sean diferente a la linea que tiene este anlizador como atributo
         */
        lineaActualContador = tokens.get(tokenActualContador).getLinea();
        linea = new LineaAnalizada(lineaActualContador, obtenerLineaComoSublista());
        //Declaracion de las clasese de analisis especifico
        AnalizarEtiqueta analizarEtiqueta;
        AnalizarInstruccion analizarInstruccion;
        AnalizarPseudoinstruccion analizarPseudoinstruccion;
        AnalizarVariable analizarVariable;
        //y clase qeu se va a regregar a parser LineaAnalizada
        // Se utiliza un switch para mandarlos a clases dedicadas a la autenticacion de la sintaxis dependiendo de cual es el primer token de la linea
        switch(primerToken.getType()){
            case PSEUDOINSTRUCCION -> {
                analizarPseudoinstruccion = new AnalizarPseudoinstruccion(primerToken, obtenerLineaComoSublista()); //Se manda la sublista completa de la linea a analizar
                analizarPseudoinstruccion.analizar();
                if(analizarPseudoinstruccion.getErrorSintactico() != null) linea.setErrorSintactico(analizarPseudoinstruccion.getErrorSintactico());
            }
            case INSTRUCCION -> {
                analizarInstruccion = new AnalizarInstruccion(primerToken, obtenerLineaComoSublista());
                analizarInstruccion.analizar();
                if(analizarInstruccion.getErrorSintactico() != null) linea.setErrorSintactico(analizarInstruccion.getErrorSintactico());
            }
            case ETIQUETA -> {
                analizarEtiqueta = new AnalizarEtiqueta(primerToken, obtenerLineaComoSublista());
                analizarEtiqueta.analizar();
                if (analizarEtiqueta.getErrorSintactico() != null) linea.setErrorSintactico(analizarEtiqueta.getErrorSintactico());
            }
            case VARIABLE -> {
                analizarVariable = new AnalizarVariable(primerToken, obtenerLineaComoSublista());
                analizarVariable.analizar();
                if(analizarVariable.getErrorSintactico() != null) linea.setErrorSintactico(analizarVariable.getErrorSintactico());
            }
            default -> {
                linea.setErrorSintactico(new ErrorSintactico(tokens.get(tokenActualContador)));
                linea.getErrorSintactico().setMensajeError("Primer token de la linea invalido");
            }
        }
        return linea;
    }

    public List<Token> obtenerLineaComoSublista(){
        List<Token> tokensLinea = new ArrayList<>();

        while(tokenActualContador < tokens.size() && tokens.get(tokenActualContador).getLinea() == lineaActualContador){
            tokensLinea.add(tokens.get(tokenActualContador));
            tokenActualContador++;
        }
        primerToken = tokens.get(tokenActualContador);
        return tokensLinea;
    }







    //getters y setters
    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void setTokenActualContador(int tokenActualContador) {
        this.tokenActualContador = tokenActualContador;
    }

    public int getLineaActualContador() {
        return lineaActualContador;
    }

    public void setLineaActualContador(int lineaActualContador) {
        this.lineaActualContador = lineaActualContador;
    }

    public LineaAnalizada getLinea() {
        return linea;
    }

    public void setLinea(LineaAnalizada linea) {
        this.linea = linea;
    }

    public int getTokenActualContador() {
        return tokenActualContador;
    }

    public Token getPrimerToken() {
        return primerToken;
    }

    public void setPrimerToken(Token primerToken) {
        this.primerToken = primerToken;
    }
}
