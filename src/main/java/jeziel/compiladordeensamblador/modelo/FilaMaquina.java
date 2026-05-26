package jeziel.compiladordeensamblador.modelo;

import javafx.beans.property.SimpleStringProperty;

public class FilaMaquina {
    private final SimpleStringProperty contador;
    private final SimpleStringProperty codificacion;

    public FilaMaquina(String contador, String codificacion) {
        this.contador = new SimpleStringProperty(contador);
        this.codificacion = new SimpleStringProperty(codificacion);
    }

    public String getContador() { return contador.get(); }
    public String getCodificacion() { return codificacion.get(); }

    public void setContador(String contador) { this.contador.set(contador); }
    public void setCodificacion(String codificacion) { this.codificacion.set(codificacion); }
}