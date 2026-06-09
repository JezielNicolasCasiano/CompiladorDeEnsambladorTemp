package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

public class LineaAnalizadaSemanticamente {
    private LineaAnalizada lineaAnalizada;
    private String tamanoInstruccion;
    private String direccion;
    private String dDeTipoDeDireccionamiento;

    public LineaAnalizadaSemanticamente(LineaAnalizada lineaAnalizada) {
        this.lineaAnalizada = lineaAnalizada;
    }

    //getters y setters

    public LineaAnalizada getLineaAnalizada() {
        return lineaAnalizada;
    }

    public void setLineaAnalizada(LineaAnalizada lineaAnalizada) {
        this.lineaAnalizada = lineaAnalizada;
    }

    public String getTamanoInstruccion() {
        return tamanoInstruccion;
    }

    public void setTamanoInstruccion(String tamanoInstruccion) {
        this.tamanoInstruccion = tamanoInstruccion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getdDeTipoDeDireccionamiento() {
        return dDeTipoDeDireccionamiento;
    }

    public void setdDeTipoDeDireccionamiento(String dDeTipoDeDireccionamiento) {
        this.dDeTipoDeDireccionamiento = dDeTipoDeDireccionamiento;
    }
}
