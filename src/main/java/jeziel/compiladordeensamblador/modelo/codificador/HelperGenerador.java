package jeziel.compiladordeensamblador.modelo.codificador;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.semantico.LineaAnalizadaSemanticamente;

import java.util.List;

public class HelperGenerador {

    public static LineaAnalizadaSemanticamente buscarEnTabla(String nombre, List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        if (tablaDeSimbolos == null || nombre == null) return null;

        String nombreLimpio = nombre.endsWith(":") ? nombre.substring(0, nombre.length() - 1) : nombre;

        for (LineaAnalizadaSemanticamente lineaSem : tablaDeSimbolos) {
            if (lineaSem.getLineaAnalizada() != null && !lineaSem.getLineaAnalizada().getTokens().isEmpty()) {
                Token primerToken = lineaSem.getLineaAnalizada().getTokens().get(0);
                String valToken = primerToken.getValue();
                if (valToken != null) {
                    String valTokenLimpio = valToken.endsWith(":") ? valToken.substring(0, valToken.length() - 1) : valToken;
                    if (valTokenLimpio.equalsIgnoreCase(nombreLimpio)) {
                        return lineaSem;
                    }
                }
            }
        }
        return null;
    }

    public static int parsearDireccionSemantica(String dirStr) {
        if (dirStr == null || dirStr.trim().isEmpty()) return 0;
        try {
            String limpia = dirStr.trim().replace("h", "").replace("H", "");
            return Integer.parseInt(limpia, 16);
        } catch (NumberFormatException e) {
            try {
                // Si no era hex puro, intentar base 10 por seguridad
                return Integer.parseInt(dirStr.trim());
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }
    static String dispSiNecesario(Token t, int rm, List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        if (rm != 0b110) return "";

        String nombre = t.getValue().replace("[", "").replace("]", "");
        LineaAnalizadaSemanticamente sym = buscarEnTabla(nombre, tablaDeSimbolos);

        int dir = (sym != null) ? parsearDireccionSemantica(sym.getDireccion()) : 0;

        return " " + toBin8(dir & 0xFF) + " " + toBin8((dir >> 8) & 0xFF);
    }

    //Calcular distancias
    static int calcularDesplazamiento(Token dest, int tamInstruccion, int direccionActual, List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        String nombre = dest.getValue();
        LineaAnalizadaSemanticamente sym = buscarEnTabla(nombre, tablaDeSimbolos);

        if (sym != null) {
            int destinoDir = parsearDireccionSemantica(sym.getDireccion());
            return destinoDir - (direccionActual + tamInstruccion);
        }
        return 0;
    }

    //Calcular Registros

    public static boolean esRegistro(Token t) {
        return t != null && t.getType() == TokenType.REGISTRO;
    }

    public static boolean esReg16(Token t) {
        if (!esRegistro(t)) return false;
        String reg = t.getValue().toUpperCase();
        return reg.equals("AX") || reg.equals("BX") || reg.equals("CX") || reg.equals("DX")
                || reg.equals("SI") || reg.equals("DI") || reg.equals("BP") || reg.equals("SP");
    }

    public static boolean esReg8(Token t) {
        if (!esRegistro(t)) return false;
        String reg = t.getValue().toUpperCase();
        return reg.equals("AL") || reg.equals("AH") || reg.equals("BL") || reg.equals("BH")
                || reg.equals("CL") || reg.equals("CH") || reg.equals("DL") || reg.equals("DH");
    }

    public static boolean esMemoria(Token t) {
        if (t == null) return false;
        return t.getType() == TokenType.CORCHETE_ABRE
                || t.getType() == TokenType.VARIABLE
                || t.getValue().startsWith("[");
    }

    public static boolean esInmediato(Token t) {
        return t != null && (t.getType() == TokenType.CARACTER || t.getValue().matches("^[0-9].*"));
    }

//RM
    public static int codigoReg(Token t) {
        if (t == null) return 0;
        String reg = t.getValue().toUpperCase();
        switch (reg) {
            // Registros de 16 bits / 8 bits compartiendo código según el bit 'w'
            case "AX": case "AL": return 0b000;
            case "CX": case "CL": return 0b001;
            case "DX": case "DL": return 0b010;
            case "BX": case "BL": return 0b011;
            case "SP": case "AH": return 0b100;
            case "BP": case "CH": return 0b101;
            case "SI": case "DH": return 0b110;
            case "DI": case "BH": return 0b111;
            default: return 0b000;
        }
    }

    static final int MOD_REG = 0b11;
    static final int MOD_MEM = 0b00;

    static int modRM(int mod, int reg, int rm) {
        return (mod << 6) | (reg << 3) | rm;
    }

    public static int codigoMemRM(Token t) {
        if (t == null) return 0b110;

        String contenido = t.getValue().replace("[", "").replace("]", "").toUpperCase();

        // Mapeo estándar de registros base e índice del modo R/M de arquitectura 8086
        switch (contenido) {
            case "BX+SI": return 0b000;
            case "BX+DI": return 0b001;
            case "BP+SI": return 0b010;
            case "BP+DI": return 0b011;
            case "SI":    return 0b100;
            case "DI":    return 0b101;
            case "BP":    return 0b110; // Requiere desplazamiento si MOD es 01 o 10
            case "BX":    return 0b111;
            default:      return 0b110; // Si es una variable directa (ej: [mi_variable])
        }
    }

    static String toBin8(int valor) {
        return String.format("%8s", Integer.toBinaryString(valor & 0xFF)).replace(' ', '0');
    }

    static int contarBytes(String binario) {
        if (binario == null || binario.trim().isEmpty() || binario.startsWith("ERROR")) return 0;
        return binario.trim().split("\\s+").length;
    }

    static int parsearConstante(String valor) {
        String upper = valor.toUpperCase();
        try {
            if (upper.endsWith("H")) return Integer.parseInt(upper.replace("H", ""), 16);
            if (upper.endsWith("B")) return Integer.parseInt(upper.replace("B", ""), 2);
            return Integer.parseInt(upper);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Agrega w al bit 0
    static int construirOpcode(Opcode op, int w) {
        return (op.binarioBase << 1) | w;
    }

    // Agrega d al bit 1 y w al bit 0
    static int construirOpcode(Opcode op, int d, int w) {
        return (op.binarioBase << 2) | (d << 1) | w;
    }
}
