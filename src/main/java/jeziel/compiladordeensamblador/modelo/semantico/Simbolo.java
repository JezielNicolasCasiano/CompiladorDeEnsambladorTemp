package jeziel.compiladordeensamblador.modelo.semantico;

public class Simbolo {

    private String nombre;
    private String tipo;
    private String valor;
    private int direccion;
    private int tamano;

    public Simbolo(String nombre, String tipo, String valor, int direccion, int tamano) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.valor = valor;
        this.direccion = direccion;
        this.tamano = tamano;
    }

    public String getDireccionHex() {
        return String.format("%04X", direccion);
    }

    public String getTamanoStr() {
        if (tamano == 0) {
            return "-";
        }
        return tamano == 1 ? "1 byte" : tamano + " bytes";
    }

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getValor() { return valor; }
    public int getDireccion() { return direccion; }
    public int getTamano() { return tamano; }
}