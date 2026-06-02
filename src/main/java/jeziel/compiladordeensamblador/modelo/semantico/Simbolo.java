package jeziel.compiladordeensamblador.modelo.semantico;

public class Simbolo {
    public enum TipoSext { VARIABLE, ETIQUETA }

    private final String nombre;
    private final TipoSext tipo;
    private final int tamano;
    private int direccion;
    private final String valor;

    public Simbolo(String nombre, TipoSext tipo, int tamano, int direccion, String valor) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.tamano = tamano;
        this.direccion = direccion;
        this.valor = valor;
    }

    public String getNombre() { return nombre; }
    public TipoSext getTipo() { return tipo; }
    public int getTamano() { return tamano; }
    public int getDireccion() { return direccion; }
    public String getValor() { return valor; }

    public void setDireccion(int direccion) { this.direccion = direccion; }
}