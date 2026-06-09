package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

import java.util.List;

public class AnalizadorSemanticaInstruccion extends AnalizadorSemanticoGeneral {

    private String dDeTipoDeDireccionamiento = "-";

    public AnalizadorSemanticaInstruccion(Token primerToken, LineaAnalizada lineaAnalizada, List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        super(primerToken, lineaAnalizada, tablaDeSimbolos);
    }

    public String getdDeTipoDeDireccionamiento() {
        return dDeTipoDeDireccionamiento;
    }

    @Override
    public void analizar() {
        TokenSubtype.Instruccion subtipo = (TokenSubtype.Instruccion) getPrimerToken().getSub();
        List<Token> tokens = getLineaAAnalizar();

        switch (subtipo) {
            case CBW:
            case CLC:
            case LODSB:
            case LODSW:
            case STOSB:
            case STOSW:
                dDeTipoDeDireccionamiento = "-";
                break;

            case DIV:
            case IMUL:
            case INC:
            case NEG:
                dDeTipoDeDireccionamiento = "-";
                validarUnOperandoRegistroOMemoria(subtipo, tokens);
                break;

            case INT:
                dDeTipoDeDireccionamiento = "-";
                validarInt(tokens);
                break;

            case JNS:
            case JS:
            case LOOPNE:
            case JG:
            case JMP:
            case JNBE:
                dDeTipoDeDireccionamiento = "-";
                validarSalto(tokens);
                break;

            case ADD:
            case LDS:
            case MOV:
            case ROR:
                validarDosOperandos(subtipo, tokens);
                break;

            default:
                break;
        }
    }

    private void validarUnOperandoRegistroOMemoria(TokenSubtype.Instruccion inst, List<Token> tokens) {
        if (tokens.size() < 2) return;
        Token operando = tokens.get(1);

        if (operando.getType() == TokenType.CONSTANTE) {
            ErrorSemantico error = new ErrorSemantico(operando);
            error.setMensajeError("El operando de la instrucción " + inst + " no puede ser una constante.");
            setErrorSemantico(error);
            return;
        }

        if (esVariableOSimbolo(operando)) {
            if (!existeSimbolo(operando.getValue())) {
                ErrorSemantico error = new ErrorSemantico(operando);
                error.setMensajeError("Símbolo no definido: " + operando.getValue());
                setErrorSemantico(error);
            }
        }
    }

    private void validarInt(List<Token> tokens) {
        if (tokens.size() < 2) return;
        Token operando = tokens.get(1);

        if (operando.getType() != TokenType.CONSTANTE) {
            ErrorSemantico error = new ErrorSemantico(operando);
            error.setMensajeError("El operando de INT debe ser una constante.");
            setErrorSemantico(error);
            return;
        }

        long val = obtenerValorNumerico(operando);
        if (val < 0 || val > 255) {
            ErrorSemantico error = new ErrorSemantico(operando);
            error.setMensajeError("La interrupción " + operando.getValue() + " excede el rango válido (0-255).");
            setErrorSemantico(error);
        }
    }

    private void validarSalto(List<Token> tokens) {
        if (tokens.size() < 2) return;
        Token operando = tokens.get(1);

        if (!existeSimbolo(operando.getValue())) {
            ErrorSemantico error = new ErrorSemantico(operando);
            error.setMensajeError("Símbolo de salto no definido: " + operando.getValue());
            setErrorSemantico(error);
        }
    }

    private void validarDosOperandos(TokenSubtype.Instruccion inst, List<Token> tokens) {
        if (tokens.size() < 4) return;
        Token op1 = tokens.get(1);
        Token op2 = tokens.get(3);

        // Validar que los símbolos estén definidos
        if (esVariableOSimbolo(op1) && !existeSimbolo(op1.getValue())) {
            ErrorSemantico error = new ErrorSemantico(op1);
            error.setMensajeError("Símbolo no definido: " + op1.getValue());
            setErrorSemantico(error);
            return;
        }
        if (esVariableOSimbolo(op2) && !existeSimbolo(op2.getValue())) {
            ErrorSemantico error = new ErrorSemantico(op2);
            error.setMensajeError("Símbolo no definido: " + op2.getValue());
            setErrorSemantico(error);
            return;
        }

        // Restricción de memoria a memoria
        if (esOperandoMemoria(op1) && esOperandoMemoria(op2)) {
            ErrorSemantico error = new ErrorSemantico(op1);
            error.setMensajeError("No se permiten operaciones de memoria a memoria.");
            setErrorSemantico(error);
            return;
        }

        //  Restricciones específicas para LDS
        if (inst == TokenSubtype.Instruccion.LDS) {
            if (!esRegistroDe16Bits(op1)) {
                ErrorSemantico error = new ErrorSemantico(op1);
                error.setMensajeError("El primer operando de LDS debe ser un registro de 16 bits.");
                setErrorSemantico(error);
                return;
            }
            if (!esOperandoMemoria(op2)) {
                ErrorSemantico error = new ErrorSemantico(op2);
                error.setMensajeError("El segundo operando de LDS debe ser una dirección de memoria.");
                setErrorSemantico(error);
                return;
            }
            dDeTipoDeDireccionamiento = "1"; // REG (op1) es destino
            return;
        }

        // Restricciones específicas para ROR
        if (inst == TokenSubtype.Instruccion.ROR) {
            boolean esValido = (op2.getType() == TokenType.CONSTANTE && obtenerValorNumerico(op2) == 1) ||
                               (op2.getType() == TokenType.REGISTRO && op2.getValue().equalsIgnoreCase("CL"));
            if (!esValido) {
                ErrorSemantico error = new ErrorSemantico(op2);
                error.setMensajeError("El segundo operando de ROR debe ser el número 1 o el registro CL.");
                setErrorSemantico(error);
                return;
            }
            dDeTipoDeDireccionamiento = "-";
            return;
        }

        // Restricciones para Registros de Segmento (MOV / ADD)
        if (esRegistroSegmento(op1)) {
            if (op1.getValue().equalsIgnoreCase("CS")) {
                ErrorSemantico error = new ErrorSemantico(op1);
                error.setMensajeError("El registro CS no puede ser el destino de una operación de modificación.");
                setErrorSemantico(error);
                return;
            }
            if (esRegistroSegmento(op2)) {
                ErrorSemantico error = new ErrorSemantico(op2);
                error.setMensajeError("No se permite transferir datos directamente entre registros de segmento.");
                setErrorSemantico(error);
                return;
            }
            if (op2.getType() == TokenType.CONSTANTE) {
                ErrorSemantico error = new ErrorSemantico(op2);
                error.setMensajeError("No se permite mover una constante directamente a un registro de segmento.");
                setErrorSemantico(error);
                return;
            }
        }
        if (esRegistroSegmento(op2)) {
            if (esRegistroSegmento(op1)) {
                ErrorSemantico error = new ErrorSemantico(op1);
                error.setMensajeError("No se permite transferir datos directamente entre registros de segmento.");
                setErrorSemantico(error);
                return;
            }
        }

        // Validación de tamaños de operandos
        int size1 = obtenerTamanoOperando(op1);
        int size2 = obtenerTamanoOperando(op2);

        // Si ambos tienen un tamaño definido y no coinciden:
        if (size1 != -1 && size2 != -1 && size1 != size2) {
            ErrorSemantico error = new ErrorSemantico(op1);
            error.setMensajeError("Conflicto de tamaños de operando: " + size1 + " bits vs " + size2 + " bits.");
            setErrorSemantico(error);
            return;
        }

        // Si uno es registro/memoria y el otro es constante:
        if (op2.getType() == TokenType.CONSTANTE) {
            long val = obtenerValorNumerico(op2);
            int lim = (size1 == 8) ? 255 : 65535;
            if (val < 0 || val > lim) {
                ErrorSemantico error = new ErrorSemantico(op2);
                error.setMensajeError("La constante " + op2.getValue() + " excede el tamaño del operando destino (" + size1 + " bits).");
                setErrorSemantico(error);
                return;
            }
        }

        // Calcular el bit de dirección d
        if (inst == TokenSubtype.Instruccion.MOV || inst == TokenSubtype.Instruccion.ADD) {
            if (op2.getType() == TokenType.CONSTANTE) {
                dDeTipoDeDireccionamiento = "-"; // Instrucción de inmediato
            } else if (op1.getType() == TokenType.REGISTRO) {
                dDeTipoDeDireccionamiento = "1"; // op1 es destino
            } else if (op2.getType() == TokenType.REGISTRO) {
                dDeTipoDeDireccionamiento = "0"; // op2 es origen, memoria es destino
            } else {
                dDeTipoDeDireccionamiento = "-";
            }
        }
    }

    private boolean existeSimbolo(String nombre) {
        String n = normalizarNombre(nombre);
        for (LineaAnalizadaSemanticamente sym : getTablaDeSimbolos()) {
            Token symToken = sym.getLineaAnalizada().getTokens().getFirst();
            if (normalizarNombre(symToken.getValue()).equalsIgnoreCase(n)) {
                return true;
            }
        }
        return false;
    }

    private String normalizarNombre(String nombre) {
        if (nombre.endsWith(":")) {
            return nombre.substring(0, nombre.length() - 1);
        }
        return nombre;
    }

    private boolean esVariableOSimbolo(Token token) {
        return token.getType() == TokenType.VARIABLE || token.getType() == TokenType.IDENTIFICADOR;
    }

    private boolean esOperandoMemoria(Token token) {
        return esVariableOSimbolo(token) || token.getType() == TokenType.COMPUESTO;
    }

    private boolean esRegistroSegmento(Token token) {
        if (token.getType() != TokenType.REGISTRO) return false;
        String val = token.getValue().toUpperCase();
        return val.equals("CS") || val.equals("DS") || val.equals("SS") || val.equals("ES");
    }

    private boolean esRegistroDe16Bits(Token token) {
        if (token.getType() != TokenType.REGISTRO) return false;
        String val = token.getValue().toUpperCase();
        switch (val) {
            case "AX": case "BX": case "CX": case "DX":
            case "SI": case "DI": case "BP": case "SP":
            case "CS": case "DS": case "SS": case "ES":
                return true;
            default:
                return false;
        }
    }

    private int obtenerTamanoOperando(Token token) {
        if (token.getType() == TokenType.REGISTRO) {
            String val = token.getValue().toUpperCase();
            switch (val) {
                case "AH": case "AL":
                case "BH": case "BL":
                case "CH": case "CL":
                case "DH": case "DL":
                    return 8;
                default:
                    return 16;
            }
        }

        if (esVariableOSimbolo(token)) {
            String n = normalizarNombre(token.getValue());
            for (LineaAnalizadaSemanticamente sym : getTablaDeSimbolos()) {
                Token symToken = sym.getLineaAnalizada().getTokens().getFirst();
                if (normalizarNombre(symToken.getValue()).equalsIgnoreCase(n)) {
                    List<Token> symTokens = sym.getLineaAnalizada().getTokens();
                    if (symTokens.size() > 1) {
                        Token sizeToken = symTokens.get(1);
                        if (sizeToken.getSub() == TokenSubtype.Directiva.DB) {
                            return 8;
                        }
                        if (sizeToken.getSub() == TokenSubtype.Directiva.DW) {
                            return 16;
                        }
                    }
                }
            }
        }

        return -1; // Desconocido o no aplica
    }

    private long obtenerValorNumerico(Token tokenConst) {
        try {
            String lexeme = tokenConst.getValue();
            Object constSubtype = tokenConst.getSub();
            if (constSubtype == TokenSubtype.Constante.HEXADECIMAL) {
                String hex = lexeme.substring(0, lexeme.length() - 1);
                return Long.parseLong(hex, 16);
            } else if (constSubtype == TokenSubtype.Constante.BINARIO) {
                String bin = lexeme.substring(0, lexeme.length() - 1);
                return Long.parseLong(bin, 2);
            } else {
                return Long.parseLong(lexeme);
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
