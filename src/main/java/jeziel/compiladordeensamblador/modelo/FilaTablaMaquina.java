package jeziel.compiladordeensamblador.modelo;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.semantico.LineaAnalizadaSemanticamente;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FilaTablaMaquina {
    private final String linea;
    private final String resultado;
    private final String direccion;

    public FilaTablaMaquina(String linea, String resultado, String direccion) {
        this.linea = linea;
        this.resultado = resultado;
        this.direccion = direccion;
    }

    public String getLinea() {
        return linea;
    }

    public String getResultado() {
        return resultado;
    }

    public String getDireccion() {
        return direccion;
    }

    public static FilaTablaMaquina crearDesdeLineaSemantica(LineaAnalizadaSemanticamente lineaSemantica) {
        StringBuilder sb = new StringBuilder();
        if (lineaSemantica.getLineaAnalizada() != null && lineaSemantica.getLineaAnalizada().getTokens() != null) {
            for (Token t : lineaSemantica.getLineaAnalizada().getTokens()) {
                sb.append(t.getValue()).append(" ");
            }
        }
        String lineaStr = sb.toString().trim();

        String resultado = "Correcta";
        if (lineaSemantica.getLineaAnalizada() != null && lineaSemantica.getLineaAnalizada().tieneError()) {
            resultado = lineaSemantica.getLineaAnalizada().getErrorSintactico().getMensajeError();
        } else if (lineaSemantica.getErrorSemantico() != null) {
            resultado = lineaSemantica.getErrorSemantico().getMensajeError();
        } else if (lineaSemantica.getCodigoMaquina() != null) {
            resultado = binarioAHex(lineaSemantica.getCodigoMaquina());
        }

        String direccion = lineaSemantica.getDireccion() != null ? lineaSemantica.getDireccion() : "";
        return new FilaTablaMaquina(lineaStr, resultado, direccion);
    }

    private static String binarioAHex(String binario) {
        if (binario == null || binario.isBlank()) return "";
        if (binario.startsWith("ERROR")) return binario;
        return Arrays.stream(binario.trim().split("\\s+"))
                .map(b -> b.matches("[01]{8}") ? String.format("%02X", Integer.parseInt(b, 2)) : b)
                .collect(Collectors.joining(" "));
    }
}
