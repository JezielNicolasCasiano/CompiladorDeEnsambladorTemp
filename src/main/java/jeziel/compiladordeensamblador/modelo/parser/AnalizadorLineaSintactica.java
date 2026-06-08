package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorLineaSintactica {
    private List<Token> tokens;
    private List<Token> linea;
    private int tokenActualContador;
    private int lineaActualContador;
    private boolean avanzar;
    private Token primerToken;
    private AnalizarInstruccion analizarInstruccion;
    private AnalizarPseudoinstruccion analizarPseudoinstruccion;
    private AnalizarVariable analizarVariable;

    public AnalizadorLineaSintactica(List<Token> tokens){
        this.tokens = tokens;
        tokenActualContador = 0;
        lineaActualContador = 0;
    }

    public List<Token> analizarLinea(){

        /*En esta parte tiene que haber un ciclo que vaya analizando linea por linea y cuando alcance el limite y todo esta bien devolver la linea,
        el evento que devuelve la linea es que los siguientes tokens en su atributo linea sean diferente a la linea que tiene este anlizador como atributo
         */

        primerToken = tokens.getFirst();
        while(avanzar){
            // Se utiliza un switch para mandarlos a clases dedicadas a la autenticacion de la sintaxis dependiendo de cual es el primer token de la linea

            switch(primerToken.getType()){
                case PSEUDOINSTRUCCION -> analizarPseudoinstruccion = new AnalizarPseudoinstruccion(primerToken, obtenerLineaComoSublista());
                //Ciclo para seguir mandando los siguientes tokens

            }
        }
        return linea;
    }


    public List<Token> obtenerLineaComoSublista(){
        List<Token> linea = new ArrayList<>();
        while(tokens.get(tokenActualContador +1).getLinea() == lineaActualContador){
            tokenActualContador += 1;
            linea.add(tokens.get(tokenActualContador));
        }
        primerToken = tokens.get(tokenActualContador+1);
        return linea;
    }







    //getters y setters
    public List<Token> getTokens() {
        return tokens;
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Token> getLinea() {
        return linea;
    }

    public void setLinea(List<Token> linea) {
        this.linea = linea;
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

    public boolean isAvanzar() {
        return avanzar;
    }

    public void setAvanzar(boolean avanzar) {
        this.avanzar = avanzar;
    }
}
