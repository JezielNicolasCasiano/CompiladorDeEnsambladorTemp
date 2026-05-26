package jeziel.compiladordeensamblador.modelo;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.NodoAST;
import jeziel.compiladordeensamblador.modelo.semantico.TablaSimbolo;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;

import java.util.ArrayList;
import java.util.List;

public class GeneradorDeCodigo {

    private int locationCounter;
    private final TablaSimbolo tablaSimbolos;

    public GeneradorDeCodigo(TablaSimbolo tablaSimbolos) {
        this.tablaSimbolos = tablaSimbolos;
        this.locationCounter = 0x0470;
    }

    public List<FilaMaquina> generar(List<NodoAST> arbol) {
        List<FilaMaquina> codigoMaquina = new ArrayList<>();

        for (NodoAST nodo : arbol) {
            if (nodo.getTipo() == NodoAST.Tipo.INSTRUCCION) {
                String contadorHex = String.format("%04X", locationCounter);
                String codificacion = codificarInstruccion(nodo);
                int bytesGenerados = codificacion.replace(" ", "").length() / 2;
                locationCounter += bytesGenerados;

                codigoMaquina.add(new FilaMaquina(contadorHex, codificacion));
            }
            else if (nodo.getTipo() == NodoAST.Tipo.DIRECTIVA) {
                String codificacion = codificarDirectiva(nodo);

                if (!codificacion.isEmpty()) {
                    String contadorHex = String.format("%04X", locationCounter);
                    int bytesGenerados = codificacion.split(" ").length;

                    codigoMaquina.add(new FilaMaquina(contadorHex, codificacion));
                    locationCounter += bytesGenerados;
                }
            }
        }

        return codigoMaquina;
    }

    private String codificarInstruccion(NodoAST instruccion) {
        TokenSubtype.Instruccion subtipo = (TokenSubtype.Instruccion) instruccion.getToken().getSub();
        int numOperandos = instruccion.getHijos().size();

        if (numOperandos == 0) {
            return codificarSinOperandos(subtipo);
        }

        switch (subtipo) {
            case MOV:
                return codificarMOV(instruccion);
            default:
                return "??";
        }
    }

    private String codificarSinOperandos(TokenSubtype.Instruccion inst) {
        switch (inst) {
            case CBW: return "98"; // 10011000 en hex
            case CLC: return "F8"; // 11111000 en hex
            case LODSB: return "AC"; // 10101100 en hex
            case LODSW: return "AD"; // 10101101 en hex
            case STOSB: return "AA"; // 10101010 en hex
            case STOSW: return "AB"; // 10101011 en hex
            default: return "??";
        }
    }

    private String codificarDirectiva(NodoAST nodo) {
        TokenSubtype.Directiva subtipo = (TokenSubtype.Directiva) nodo.getToken().getSub();

        if (subtipo != TokenSubtype.Directiva.DB && subtipo != TokenSubtype.Directiva.DW) {
            return "";
        }

        boolean isWord = (subtipo == TokenSubtype.Directiva.DW);
        StringBuilder codificacion = new StringBuilder();

        for (int i = 1; i < nodo.getHijos().size(); i++) {
            NodoAST operando = nodo.getHijos().get(i);
            codificacion.append(procesarValorDirectiva(operando, isWord));
        }

        return codificacion.toString().trim();
    }

    private String procesarValorDirectiva(NodoAST operando, boolean isWord) {
        StringBuilder res = new StringBuilder();

        if (operando.getTipo() == NodoAST.Tipo.OPERANDO_DUP) {
            int repeticiones = parsearConstanteAEntero(operando.getHijos().get(0).getToken().getValue());
            String valorInterno = operando.getHijos().get(1).getToken().getValue();

            NodoAST nodoTemp = new NodoAST(NodoAST.Tipo.OPERANDO_CONSTANTE, new Token(TokenType.CONSTANTE, valorInterno));
            String valorHex = procesarValorSimple(nodoTemp, isWord);

            for (int j = 0; j < repeticiones; j++) {
                res.append(valorHex).append(" ");
            }
        } else {
            res.append(procesarValorSimple(operando, isWord)).append(" ");
        }
        return res.toString();
    }

    private String procesarValorSimple(NodoAST operando, boolean isWord) {
        String valorStr = operando.getToken().getValue();

        if (valorStr.equals("?")) {
            return isWord ? "00 00" : "00";
        }

        if (operando.getTipo() == NodoAST.Tipo.OPERANDO_CADENA || operando.getTipo() == NodoAST.Tipo.OPERANDO_CARACTER) {
            valorStr = valorStr.substring(1, valorStr.length() - 1);
            StringBuilder hexStr = new StringBuilder();

            for (char c : valorStr.toCharArray()) {
                hexStr.append(String.format("%02X ", (int) c));
            }
            return hexStr.toString().trim();
        }

        int valorInt = parsearConstanteAEntero(valorStr);
        if (isWord) {
            String hex = String.format("%04X", valorInt & 0xFFFF);
            return hex.substring(2, 4) + " " + hex.substring(0, 2);
        } else {
            return String.format("%02X", valorInt & 0xFF);
        }
    }

    private int parsearConstanteAEntero(String constanteStr) {
        constanteStr = constanteStr.toUpperCase();
        try {
            if (constanteStr.endsWith("H")) {
                return Integer.parseInt(constanteStr.substring(0, constanteStr.length() - 1), 16);
            } else if (constanteStr.endsWith("B")) {
                return Integer.parseInt(constanteStr.substring(0, constanteStr.length() - 1), 2);
            } else {
                return Integer.parseInt(constanteStr);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Retorna el código de 3 bits del registro según la tabla de Intel
    private int obtenerCodigoRegistro(String reg) {
        switch (reg.toUpperCase()) {
            case "AL": case "AX": return 0; // 000
            case "CL": case "CX": return 1; // 001
            case "DL": case "DX": return 2; // 010
            case "BL": case "BX": return 3; // 011
            case "AH": case "SP": return 4; // 100
            case "CH": case "BP": return 5; // 101
            case "DH": case "SI": return 6; // 110
            case "BH": case "DI": return 7; // 111
            default: return 0;
        }
    }

    private int obtenerBitW(String reg) {
        String r = reg.toUpperCase();
        if (r.equals("AH") || r.equals("AL") || r.equals("BH") || r.equals("BL") ||
                r.equals("CH") || r.equals("CL") || r.equals("DH") || r.equals("DL")) {
            return 0;
        }
        return 1;
    }

    private String armarModRM(int mod, int reg, int rm) {
        int modrm = (mod << 6) | (reg << 3) | rm;
        return String.format("%02X", modrm);
    }

    private String codificarMOV(NodoAST instruccion) {
        List<NodoAST> operandos = instruccion.getHijos();
        if (operandos.size() != 2) return "??";

        NodoAST destino = operandos.get(0);
        NodoAST fuente = operandos.get(1);

        boolean destReg = (destino.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO);
        boolean fuenteReg = (fuente.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO);
        boolean fuenteConst = (fuente.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE);
        boolean destMem = (destino.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || destino.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA);
        boolean fuenteMem = (fuente.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || fuente.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA);

        String regDest = destReg ? destino.getToken().getValue() : "";
        String regFuent = fuenteReg ? fuente.getToken().getValue() : "";

        if (destReg && fuenteReg) {
            int w = obtenerBitW(regDest);
            int opcode = 0b10001010 | w;

            int mod = 3;
            int reg = obtenerCodigoRegistro(regDest);
            int rm = obtenerCodigoRegistro(regFuent);

            String byte1 = String.format("%02X", opcode);
            String byte2 = armarModRM(mod, reg, rm);
            return byte1 + " " + byte2;
        }

        if (destReg && fuenteConst) {
            int w = obtenerBitW(regDest);
            int reg = obtenerCodigoRegistro(regDest);
            int opcode = 0b10110000 | (w << 3) | reg;
            String byte1 = String.format("%02X", opcode);

            boolean isWord = (w == 1);
            String inmHex = procesarValorSimple(fuente, isWord);

            return byte1 + " " + inmHex;
        }


        if (destReg && fuenteMem) {
            int w = obtenerBitW(regDest);
            int opcode = 0b10001010 | w;
            int mod = 0;
            int reg = obtenerCodigoRegistro(regDest);
            int rm = 6;

            String byte1 = String.format("%02X", opcode);
            String byte2 = armarModRM(mod, reg, rm);

            String nombreVar = fuente.getHijos().isEmpty() ? fuente.getToken().getValue() : fuente.getHijos().get(0).getToken().getValue();
            jeziel.compiladordeensamblador.modelo.semantico.Simbolo sim = tablaSimbolos.buscar(nombreVar);
            int dir = (sim != null) ? sim.getDireccion() : 0;

            String dirHex = String.format("%04X", dir);
            String dirLittleEndian = dirHex.substring(2, 4) + " " + dirHex.substring(0, 2);

            return byte1 + " " + byte2 + " " + dirLittleEndian;
        }


        if (destMem && fuenteReg) {
            int w = obtenerBitW(regFuent);
            int opcode = 0b10001000 | w;
            int mod = 0;
            int reg = obtenerCodigoRegistro(regFuent);
            int rm = 6;

            String byte1 = String.format("%02X", opcode);
            String byte2 = armarModRM(mod, reg, rm);

            String nombreVar = destino.getHijos().isEmpty() ? destino.getToken().getValue() : destino.getHijos().get(0).getToken().getValue();
            jeziel.compiladordeensamblador.modelo.semantico.Simbolo sim = tablaSimbolos.buscar(nombreVar);
            int dir = (sim != null) ? sim.getDireccion() : 0;

            String dirHex = String.format("%04X", dir);
            String dirLittleEndian = dirHex.substring(2, 4) + " " + dirHex.substring(0, 2);

            return byte1 + " " + byte2 + " " + dirLittleEndian;
        }

        return "NO_IMPLEMENTADO";
    }

}