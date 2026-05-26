package jeziel.compiladordeensamblador.modelo;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Fila {
    private final SimpleIntegerProperty contador;
    private final SimpleStringProperty lexema;
    private final SimpleStringProperty tipoToken;

    public Fila(int contador, String lexema, String tipoToken) {
        this.contador = new SimpleIntegerProperty(contador);
        this.lexema = new SimpleStringProperty(lexema);
        this.tipoToken = new SimpleStringProperty(tipoToken);
    }

    public int getContador() { return contador.get(); }
    public String getLexema() { return lexema.get(); }
    public String getTipoToken() { return tipoToken.get(); }
}