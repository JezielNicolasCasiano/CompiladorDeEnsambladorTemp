package jeziel.compiladordeensamblador.modelo;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.ErrorSintactico;
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

    public List<FilaMaquina> generar(List<String> lineasArchivo, List<NodoAST> arbol, List<ErrorSintactico> errores) {
        List<FilaMaquina> codigoMaquina = new ArrayList<>();
        this.locationCounter = 0x0470;

        for (String linea : lineasArchivo) {
            String lineaTrim = linea.trim();
            if (lineaTrim.isEmpty() || lineaTrim.startsWith(";")) {
                continue;
            }

            NodoAST nodo = buscarNodoParaLinea(linea, arbol);
            ErrorSintactico error = buscarErrorParaLinea(linea, errores);

            if (nodo != null) {
                String contadorHex = String.format("%04X", locationCounter);
                String codificacion = "";

                if (nodo.getTipo() == NodoAST.Tipo.INSTRUCCION) {
                    codificacion = codificarInstruccion(nodo);
                    int bytes = codificacion.replace(" ", "").length() / 2;
                    locationCounter += bytes;
                } else if (nodo.getTipo() == NodoAST.Tipo.DIRECTIVA) {
                     codificacion = codificarDirectiva(nodo);
                    int bytes = codificacion.split(" ").length;
                    if (!codificacion.isEmpty()) locationCounter += bytes;


                    String estado = (error == null) ? "OK" : error.getMensaje();

                    codigoMaquina.add(new FilaMaquina(
                            String.format("%04X", locationCounter - (codificacion.isEmpty() ? 0 : bytes)),
                            linea,
                            codificacion.isEmpty() ? "---" : codificacion,
                            estado
                    ));
                }
                codigoMaquina.add(new FilaMaquina(contadorHex, linea, codificacion, "OK"));

            } else if (error != null) {
                codigoMaquina.add(new FilaMaquina("----", linea, "----", error.getMensaje()));
            } else {
                codigoMaquina.add(new FilaMaquina(String.format("%04X", locationCounter), linea, "", ""));
            }
        }
        return codigoMaquina;
    }

    private NodoAST buscarNodoParaLinea(String linea, List<NodoAST> arbol) {
        for (NodoAST n : arbol) {
            if (linea.contains(n.getToken().getValue())) return n;
        }
        return null;
    }

    private ErrorSintactico buscarErrorParaLinea(String linea, List<ErrorSintactico> errores) {
        for (ErrorSintactico e : errores) {
            if (e.getToken() != null && linea.contains(e.getToken().getValue())) return e;
        }
        return null;
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
            case ADD:
                return codificarADD(instruccion);
            case INC: case NEG: case DIV: case IMUL:
                return codificar1Operando(instruccion);
            case LDS: case ROR:
                return codificarLDS_ROR(instruccion);
            case JMP: case JNS: case JS: case LOOPNE: case JG: case JNBE:
                return codificarSaltos(instruccion);
            default:
                return "??";
        }
    }

    private String obtenerDireccionLittleEndian(NodoAST nodoMemoria) {
        String nombreVar = nodoMemoria.getHijos().isEmpty() ? nodoMemoria.getToken().getValue() : nodoMemoria.getHijos().get(0).getToken().getValue();
        jeziel.compiladordeensamblador.modelo.semantico.Simbolo sim = tablaSimbolos.buscar(nombreVar);
        int dir = (sim != null) ? sim.getDireccion() : 0;
        String dirHex = String.format("%04X", dir);
        return dirHex.substring(2, 4) + " " + dirHex.substring(0, 2);
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

    private String codificarADD(NodoAST instruccion) {
        NodoAST destino = instruccion.getHijos().get(0);
        NodoAST fuente = instruccion.getHijos().get(1);

        boolean destReg = (destino.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO);
        boolean fuenteReg = (fuente.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO);
        boolean fuenteConst = (fuente.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE);
        boolean destMem = (destino.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || destino.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA);
        boolean fuenteMem = (fuente.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || fuente.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA);

        String regDest = destReg ? destino.getToken().getValue() : "";
        String regFuent = fuenteReg ? fuente.getToken().getValue() : "";

        if (destReg && fuenteReg) {
            int w = obtenerBitW(regDest);
            int opcode = 0b00000000 | w;
            String byte1 = String.format("%02X", opcode);
            String byte2 = armarModRM(3, obtenerCodigoRegistro(regFuent), obtenerCodigoRegistro(regDest));
            return byte1 + " " + byte2;
        }

        if (destReg && fuenteConst) {
            int w = obtenerBitW(regDest);
            int opcode = 0b10000000 | w;
            String byte1 = String.format("%02X", opcode);
            String byte2 = armarModRM(3, 0, obtenerCodigoRegistro(regDest));
            boolean isWord = (w == 1);
            return byte1 + " " + byte2 + " " + procesarValorSimple(fuente, isWord);
        }

        if (destReg && fuenteMem) {
            int w = obtenerBitW(regDest);
            int opcode = 0b00000010 | w;
            String byte1 = String.format("%02X", opcode);
            String byte2 = armarModRM(0, obtenerCodigoRegistro(regDest), 6);
            return byte1 + " " + byte2 + " " + obtenerDireccionLittleEndian(fuente);
        }

        if (destMem && fuenteReg) {
            int w = obtenerBitW(regFuent);
            int opcode = 0b00000000 | w;
            String byte1 = String.format("%02X", opcode);
            String byte2 = armarModRM(0, obtenerCodigoRegistro(regFuent), 6);
            return byte1 + " " + byte2 + " " + obtenerDireccionLittleEndian(destino);
        }

        return "??";
    }
    private String codificar1Operando(NodoAST instruccion) {
        TokenSubtype.Instruccion inst = (TokenSubtype.Instruccion) instruccion.getToken().getSub();
        NodoAST op = instruccion.getHijos().get(0);

        boolean isReg = (op.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO);
        boolean isMem = (op.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || op.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA);
        String regVal = isReg ? op.getToken().getValue() : "";

        if (inst == TokenSubtype.Instruccion.INC && isReg && obtenerBitW(regVal) == 1) {
            int opcode = 0b01000000 | obtenerCodigoRegistro(regVal); // 40 + reg
            return String.format("%02X", opcode);
        }

        int w = isReg ? obtenerBitW(regVal) : 1;

        int baseOpcode = (inst == TokenSubtype.Instruccion.INC) ? 0b11111110 : 0b11110110;
        int opcode = baseOpcode | w;

        int regExt = 0;
        if (inst == TokenSubtype.Instruccion.INC) regExt = 0;      // 000
        else if (inst == TokenSubtype.Instruccion.NEG) regExt = 3; // 011
        else if (inst == TokenSubtype.Instruccion.IMUL) regExt = 5;// 101
        else if (inst == TokenSubtype.Instruccion.DIV) regExt = 6; // 110

        String byte1 = String.format("%02X", opcode);

        if (isReg) {
            String byte2 = armarModRM(3, regExt, obtenerCodigoRegistro(regVal));
            return byte1 + " " + byte2;
        } else if (isMem) {
            String byte2 = armarModRM(0, regExt, 6);
            return byte1 + " " + byte2 + " " + obtenerDireccionLittleEndian(op);
        }
        return "??";
    }

    private String codificarLDS_ROR(NodoAST instruccion) {
        TokenSubtype.Instruccion inst = (TokenSubtype.Instruccion) instruccion.getToken().getSub();
        NodoAST op1 = instruccion.getHijos().get(0);
        NodoAST op2 = instruccion.getHijos().get(1);

        if (inst == TokenSubtype.Instruccion.LDS) {
            String byte1 = "C5";
            String byte2 = armarModRM(0, obtenerCodigoRegistro(op1.getToken().getValue()), 6);
            return byte1 + " " + byte2 + " " + obtenerDireccionLittleEndian(op2);
        }

        if (inst == TokenSubtype.Instruccion.ROR) {
            boolean op1Reg = (op1.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO);
            boolean isCL = (op2.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO && op2.getToken().getValue().equalsIgnoreCase("CL"));

            int w = op1Reg ? obtenerBitW(op1.getToken().getValue()) : 1;
            int baseOpcode = isCL ? 0b11010010 : 0b11010000;
            int opcode = baseOpcode | w;

            String byte1 = String.format("%02X", opcode);
            String byte2 = "";

            if (op1Reg) {
                byte2 = armarModRM(3, 1, obtenerCodigoRegistro(op1.getToken().getValue())); // reg=001(ROR)
                return byte1 + " " + byte2;
            } else {
                byte2 = armarModRM(0, 1, 6);
                return byte1 + " " + byte2 + " " + obtenerDireccionLittleEndian(op1);
            }
        }
        return "??";
    }

    private String codificarSaltos(NodoAST instruccion) {
        TokenSubtype.Instruccion inst = (TokenSubtype.Instruccion) instruccion.getToken().getSub();
        String nombreEtiqueta = instruccion.getHijos().get(0).getToken().getValue();

        jeziel.compiladordeensamblador.modelo.semantico.Simbolo sim = tablaSimbolos.buscar(nombreEtiqueta);
        int dirDestino = (sim != null) ? sim.getDireccion() : 0;

        int tamañoInst = (inst == TokenSubtype.Instruccion.JMP) ? 3 : 2;
        int dirSiguienteInstruccion = locationCounter + tamañoInst;
        int desplazamiento = dirDestino - dirSiguienteInstruccion;

        if (inst == TokenSubtype.Instruccion.JMP) {
            String hex = String.format("%04X", desplazamiento & 0xFFFF);
            return "E9 " + hex.substring(2, 4) + " " + hex.substring(0, 2);
        } else {
            String opcode = "";
            switch (inst) {
                case JS: opcode = "78"; break;
                case JNS: opcode = "79"; break;
                case JNBE: opcode = "77"; break;
                case JG: opcode = "7F"; break;
                case LOOPNE: opcode = "E0"; break;
                default: opcode = "00";
            }
            String despHex = String.format("%02X", desplazamiento & 0xFF);
            return opcode + " " + despHex;
        }
    }

    private String reconstruirLinea(NodoAST nodo) {
        StringBuilder sb = new StringBuilder();
        if (nodo.getTipo() == NodoAST.Tipo.ETIQUETA) {
            sb.append(nodo.getToken().getValue()).append(" ");
            for (NodoAST hijo : nodo.getHijos()) sb.append(reconstruirLinea(hijo)).append(" ");
        } else if (nodo.getTipo() == NodoAST.Tipo.INSTRUCCION) {
            sb.append(nodo.getToken().getValue()).append(" ");
            for (int i = 0; i < nodo.getHijos().size(); i++) {
                sb.append(reconstruirLinea(nodo.getHijos().get(i)));
                if (i < nodo.getHijos().size() - 1) sb.append(", "); // Inyectar coma
            }
        } else if (nodo.getTipo() == NodoAST.Tipo.DIRECTIVA) {
            if (!nodo.getHijos().isEmpty() && nodo.getHijos().get(0).getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE) {
                sb.append(nodo.getHijos().get(0).getToken().getValue()).append(" ");
                sb.append(nodo.getToken().getValue()).append(" ");
                for (int i = 1; i < nodo.getHijos().size(); i++) {
                    sb.append(reconstruirLinea(nodo.getHijos().get(i)));
                    if (i < nodo.getHijos().size() - 1) sb.append(", ");
                }
            } else {
                sb.append(nodo.getToken().getValue()).append(" ");
                for (NodoAST hijo : nodo.getHijos()) sb.append(reconstruirLinea(hijo)).append(" ");
            }
        } else if (nodo.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA) {
            sb.append("[");
            for (NodoAST hijo : nodo.getHijos()) sb.append(reconstruirLinea(hijo));
            sb.append("]");
        } else if (nodo.getTipo() == NodoAST.Tipo.OPERANDO_DUP) {
            sb.append(nodo.getHijos().get(0).getToken().getValue()).append(" DUP(");
            sb.append(nodo.getHijos().get(1).getToken().getValue()).append(")");
        } else {
            if (nodo.getToken() != null) sb.append(nodo.getToken().getValue());
        }
        return sb.toString().trim();
    }

    private String buscarError(NodoAST nodo, List<jeziel.compiladordeensamblador.modelo.parser.ErrorSintactico> errores) {
        if (nodo.getToken() != null) {
            for (jeziel.compiladordeensamblador.modelo.parser.ErrorSintactico e : errores) {
                if (e.getToken() == nodo.getToken()) return e.getMensaje();
            }
        }
        for (NodoAST hijo : nodo.getHijos()) {
            String e = buscarError(hijo, errores);
            if (!e.isEmpty()) return e;
        }
        return "";
    }
}