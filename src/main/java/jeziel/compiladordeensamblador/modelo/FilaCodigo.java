package jeziel.compiladordeensamblador.modelo;

public class FilaCodigo {
    private final String simbolo;
    private final String tipo;
    private final String valor;
    private final String tamano;
    private final String direccion;

    public FilaCodigo(String simbolo, String tipo, String valor, String tamano) {
        this.simbolo = simbolo;
        this.tipo = tipo;
        this.valor = valor;
        this.tamano = tamano;
        this.direccion = "-";
    }


    public String getSimbolo() {
        return simbolo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public String getTamano() {
        return tamano;
    }

    public String getDireccion() {
        return direccion;
    }
}