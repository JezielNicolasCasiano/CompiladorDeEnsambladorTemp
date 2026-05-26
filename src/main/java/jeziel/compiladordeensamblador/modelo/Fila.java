package jeziel.compiladordeensamblador.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Fila {
    private final SimpleIntegerProperty contador;
    private final SimpleStringProperty lexema;
    private final SimpleStringProperty tipoToken;
    private final SimpleStringProperty instruccionOriginal;
    private final SimpleStringProperty resultadoParser;

    public Fila(int contador, String lexema, String tipoToken, String instruccionOriginal, String resultadoParser) {
        this.contador = new SimpleIntegerProperty(contador);
        this.lexema = new SimpleStringProperty(lexema);
        this.tipoToken = new SimpleStringProperty(tipoToken);
        this.instruccionOriginal = new SimpleStringProperty(instruccionOriginal);
        this.resultadoParser = new SimpleStringProperty(resultadoParser);
    }

    public int getContador() { return contador.get(); }
    public String getLexema() { return lexema.get(); }
    public String getTipoToken() { return tipoToken.get(); }
    public String getInstruccionOriginal() { return instruccionOriginal.get(); }
    public String getResultadoParser() { return resultadoParser.get(); }

    public void setInstruccionOriginal(String instruccion) { this.instruccionOriginal.set(instruccion); }
    public void setResultadoParser(String resultado) { this.resultadoParser.set(resultado); }
}