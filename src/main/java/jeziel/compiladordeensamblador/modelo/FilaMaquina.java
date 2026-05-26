package jeziel.compiladordeensamblador.modelo;

import javafx.beans.property.SimpleStringProperty;

public class FilaMaquina {
    private final SimpleStringProperty contador;
    private final SimpleStringProperty lineaCompleta;
    private final SimpleStringProperty codificacion;
    private final SimpleStringProperty error;

    public FilaMaquina(String contador, String lineaCompleta, String codificacion, String error) {
        this.contador = new SimpleStringProperty(contador);
        this.lineaCompleta = new SimpleStringProperty(lineaCompleta);
        this.codificacion = new SimpleStringProperty(codificacion);
        this.error = new SimpleStringProperty(error);
    }

    public String getContador() { return contador.get(); }
    public String getLineaCompleta() { return lineaCompleta.get(); }
    public String getCodificacion() { return codificacion.get(); }
    public String getError() { return error.get(); }

    public void setContador(String contador) { this.contador.set(contador); }
    public void setLineaCompleta(String lineaCompleta) { this.lineaCompleta.set(lineaCompleta); }
    public void setCodificacion(String codificacion) { this.codificacion.set(codificacion); }
    public void setError(String error) { this.error.set(error); }
}