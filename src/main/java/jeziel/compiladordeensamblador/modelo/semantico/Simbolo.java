package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

public class Simbolo {
    public enum TipoSext { VARIABLE, ETIQUETA }

    private final String nombre;
    private final TipoSext tipo;
    private final int tamano;
    private int direccion;

    public Simbolo(String nombre, TipoSext tipo, int tamano, int direccion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tamano = tamano;
        this.direccion = direccion;
    }

    public String getNombre() { return nombre; }
    public TipoSext getTipo() { return tipo; }
    public int getTamano() { return tamano; }
    public int getDireccion() { return direccion; }
    public void setDireccion(int direccion) { this.direccion = direccion; }
}