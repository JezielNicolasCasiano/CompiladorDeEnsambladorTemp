package jeziel.compiladordeensamblador.modelo;

public class Fila {

    private final int contador;
    private final String lexema;
    private final String tipoToken;

    public Fila(int contador, String lexema, String tipoToken) {
        this.contador = contador;
        this.lexema = lexema;
        this.tipoToken = tipoToken;
    }

    public int getContador() {
        return contador;
    }

    public String getLexema() {
        return lexema;
    }

    public String getTipoToken() {
        return tipoToken;
    }
}