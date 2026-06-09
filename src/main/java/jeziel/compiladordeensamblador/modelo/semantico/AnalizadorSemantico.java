package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;
import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSemantico {
    private List<LineaAnalizada> arbolSintactico;
    private AnalizadorNodoSintactico analizadorNodoSintactico;
    private List<LineaAnalizadaSemanticamente> tablaDeSimbolos;
    private List<LineaAnalizadaSemanticamente> analisisSemantico;

    public AnalizadorSemantico(List<LineaAnalizada> arbolSintactico) {
        this.arbolSintactico = arbolSintactico;
    }

    // Metodo para ir recorriendo el arbolSintactico e ir analizando semanticamente, primero para encontrar y rellenar la tabla de simbolos y despues
    // para todos los demas tokens
    public List<LineaAnalizadaSemanticamente> analizarBuscandoSimbolos(){
        this.tablaDeSimbolos = new ArrayList<>();
        this.analizadorNodoSintactico = new AnalizadorNodoSintactico(arbolSintactico);

        while (analizadorNodoSintactico.getLineaSintacticaActual() < arbolSintactico.size()) {
            LineaAnalizadaSemanticamente sym = analizadorNodoSintactico.buscarSimbolos(tablaDeSimbolos);
            if (sym != null) {
                tablaDeSimbolos.add(sym);
            }
        }
        return tablaDeSimbolos;
    }

    public List<LineaAnalizadaSemanticamente> analizar() {
        // Asegurar que la tabla de símbolos está construida
        if (this.tablaDeSimbolos == null) {
            analizarBuscandoSimbolos();
        }

        this.analisisSemantico = new ArrayList<>();
        this.analizadorNodoSintactico.reset();

        Integer pc = null;

        while (analizadorNodoSintactico.getLineaSintacticaActual() < arbolSintactico.size()) {
            LineaAnalizadaSemanticamente lineaSem = analizadorNodoSintactico.analizar(tablaDeSimbolos);
            if (lineaSem != null) {
                boolean isStart = false;
                List<Token> tokens = lineaSem.getLineaAnalizada().getTokens();
                if (tokens != null && !tokens.isEmpty()) {
                    Token primerToken = tokens.getFirst();
                    if (esPseudoinstruccionInicio(primerToken)) {
                        isStart = true;
                        pc = 0x470; // starts at 470h
                    }
                }

                // If PC is initialized, assign address
                if (pc != null) {
                    lineaSem.setDireccion(String.format("%04X", pc));
                } else {
                    lineaSem.setDireccion("-");
                }

                // If correct (no syntax/semantic error), increase PC
                boolean esCorrecta = lineaSem.getLineaAnalizada() != null 
                    && !lineaSem.getLineaAnalizada().tieneError() 
                    && lineaSem.getErrorSemantico() == null;

                if (esCorrecta && pc != null) {
                    // Update PC for ORG specially if it is a correct ORG pseudoinstruction
                    if (tokens != null && !tokens.isEmpty()) {
                        Token primerToken = tokens.getFirst();
                        if (primerToken.getType() == TokenType.PSEUDOINSTRUCCION && primerToken.getSub() == TokenSubtype.Directiva.ORG) {
                            if (tokens.size() >= 2) {
                                Token op = tokens.get(1);
                                if (op.getType() == TokenType.CONSTANTE) {
                                    long orgVal = obtenerValorNumerico(op);
                                    if (orgVal >= 0 && orgVal <= 65535) {
                                        pc = (int) orgVal;
                                    }
                                }
                            }
                        } else {
                            // Normal line: increase by its size in bytes
                            int size = calcularTamanoLinea(lineaSem);
                            pc += size;
                        }
                    }
                }

                analisisSemantico.add(lineaSem);
            }
        }

        return analisisSemantico;
    }

    private boolean esPseudoinstruccionInicio(Token token) {
        if (token.getType() != TokenType.PSEUDOINSTRUCCION) {
            return false;
        }
        Object sub = token.getSub();
        return sub == TokenSubtype.Directiva.STACK ||
               sub == TokenSubtype.Directiva.STACK_SEGMENT ||
               sub == TokenSubtype.Directiva.DATA ||
               sub == TokenSubtype.Directiva.DATA_SEGMENT ||
               sub == TokenSubtype.Directiva.CODE ||
               sub == TokenSubtype.Directiva.CODE_SEGMENT;
    }

    private int calcularTamanoLinea(LineaAnalizadaSemanticamente lineaSem) {
        if (lineaSem == null || lineaSem.getLineaAnalizada() == null) {
            return 0;
        }
        
        List<Token> tokens = lineaSem.getLineaAnalizada().getTokens();
        if (tokens == null || tokens.isEmpty()) {
            return 0;
        }

        Token primerToken = tokens.getFirst();

        // 1. Si es una variable/directiva (DB, DW, EQU)
        String tamanoInst = lineaSem.getTamanoInstruccion();
        if ("BYTE".equals(tamanoInst) || "WORD".equals(tamanoInst) || "EQU".equals(tamanoInst)) {
            return calcularBytesVariable(tokens, tamanoInst);
        }

        // 2. Si es una instrucción
        if (primerToken.getType() == TokenType.INSTRUCCION) {
            return calcularTamanoInstruccion(lineaSem.getLineaAnalizada());
        }

        // En cualquier otro caso (etiquetas, pseudoinstrucciones como segment, ends, org, etc.), el tamaño es 0.
        return 0;
    }

    private int calcularBytesVariable(List<Token> tokens, String tamanoInstruccion) {
        if (tokens == null || tokens.isEmpty()) return 0;
        int baseSize = 0;
        if ("BYTE".equals(tamanoInstruccion)) {
            baseSize = 1;
        } else if ("WORD".equals(tamanoInstruccion)) {
            baseSize = 2;
        } else {
            return 0; // EQU u otros ocupan 0 bytes
        }

        boolean tieneIdentificador = (tokens.getFirst().getType() == TokenType.VARIABLE);
        int idxValor = tieneIdentificador ? 2 : 1;

        // Si es DUP, p. ej. var db 32 dup(0) o db 32 dup(0)
        int expectedSizeForDup = tieneIdentificador ? 4 : 3;
        if (tokens.size() == expectedSizeForDup) {
            Token tokenCount = tokens.get(idxValor);
            Token tokenDup = tokens.get(idxValor + 1);
            if (tokenCount.getType() == TokenType.CONSTANTE &&
                tokenDup.getType() == TokenType.PSEUDOINSTRUCCION &&
                tokenDup.getSub() == TokenSubtype.Directiva.DUP) {

                long count = 1;
                try {
                    String lexeme = tokenCount.getValue();
                    Object constSubtype = tokenCount.getSub();
                    if (constSubtype == TokenSubtype.Constante.HEXADECIMAL) {
                        String hex = lexeme.substring(0, lexeme.length() - 1);
                        count = Long.parseLong(hex, 16);
                    } else if (constSubtype == TokenSubtype.Constante.BINARIO) {
                        String bin = lexeme.substring(0, lexeme.length() - 1);
                        count = Long.parseLong(bin, 2);
                    } else {
                        count = Long.parseLong(lexeme);
                    }
                } catch (NumberFormatException e) {
                    count = 1;
                }

                String valStr = tokenDup.getValue();
                int start = valStr.indexOf('(');
                int end = valStr.lastIndexOf(')');
                int elementSize = baseSize;
                if (start != -1 && end != -1 && end > start + 1) {
                    String inner = valStr.substring(start + 1, end).trim();
                    if ((inner.startsWith("'") && inner.endsWith("'")) || (inner.startsWith("\"") && inner.endsWith("\""))) {
                        String innerStr = inner.substring(1, inner.length() - 1);
                        elementSize = innerStr.length() * baseSize;
                    }
                }
                return (int) (count * elementSize);
            }
        }

        // De lo contrario, lista de constantes / cadenas
        int totalBytes = 0;
        for (int i = idxValor; i < tokens.size(); i += 2) {
            Token t = tokens.get(i);
            if (t.getType() == TokenType.CADENA) {
                String str = t.getValue();
                if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
                    str = str.substring(1, str.length() - 1);
                }
                totalBytes += str.length() * baseSize;
            } else {
                totalBytes += baseSize;
            }
        }
        return totalBytes;
    }

    private int calcularTamanoInstruccion(LineaAnalizada linea) {
        if (linea == null || linea.getTokens().isEmpty()) {
            return 0;
        }
        Token primerToken = linea.getTokens().getFirst();
        if (primerToken.getType() != TokenType.INSTRUCCION) {
            return 0;
        }

        TokenSubtype.Instruccion subtipo = (TokenSubtype.Instruccion) primerToken.getSub();
        List<Token> tokens = linea.getTokens();

        switch (subtipo) {
            case CBW:
            case CLC:
            case LODSB:
            case LODSW:
            case STOSB:
            case STOSW:
                return 1;

            case INT:
                return 2;

            case JNS:
            case JS:
            case LOOPNE:
            case JG:
            case JMP:
            case JNBE:
                return 2;

            case DIV:
            case IMUL:
            case NEG:
                if (tokens.size() >= 2) {
                    Token op = tokens.get(1);
                    if (op.getType() == TokenType.REGISTRO) {
                        return 2;
                    } else { // Memoria
                        if (op.getType() == TokenType.COMPUESTO && op.getValue().contains("[")) {
                            return 2; // indirecto
                        }
                        return 4; // directo var
                    }
                }
                return 2;

            case INC:
                if (tokens.size() >= 2) {
                    Token op = tokens.get(1);
                    if (op.getType() == TokenType.REGISTRO) {
                        if (esRegistroDe16Bits(op)) {
                            return 1;
                        } else {
                            return 2;
                        }
                    } else { // Memoria
                        if (op.getType() == TokenType.COMPUESTO && op.getValue().contains("[")) {
                            return 2; // indirecto
                        }
                        return 4; // directo var
                    }
                }
                return 1;

            case ROR:
                if (tokens.size() >= 4) {
                    Token op1 = tokens.get(1);
                    if (op1.getType() == TokenType.REGISTRO) {
                        return 2;
                    } else {
                        if (op1.getType() == TokenType.COMPUESTO && op1.getValue().contains("[")) {
                            return 2;
                        }
                        return 4;
                    }
                }
                return 2;

            case LDS:
                if (tokens.size() >= 4) {
                    Token op2 = tokens.get(3);
                    if (op2.getType() == TokenType.COMPUESTO && op2.getValue().contains("[")) {
                        return 2;
                    }
                    return 4;
                }
                return 2;

            case ADD:
            case MOV:
                if (tokens.size() >= 4) {
                    Token op1 = tokens.get(1);
                    Token op2 = tokens.get(3);

                    if (op1.getType() == TokenType.REGISTRO && op2.getType() == TokenType.REGISTRO) {
                        return 2;
                    }
                    
                    if (op1.getType() == TokenType.REGISTRO && op2.getType() == TokenType.CONSTANTE) {
                        // Registro a inmediato
                        if (esRegistroDe16Bits(op1)) {
                            return 3;
                        } else {
                            return 2;
                        }
                    }

                    if (op1.getType() != TokenType.REGISTRO && op2.getType() == TokenType.CONSTANTE) {
                        // Memoria a inmediato
                        boolean isIndirect = (op1.getType() == TokenType.COMPUESTO && op1.getValue().contains("["));
                        return isIndirect ? 4 : 6;
                    }

                    // Uno es memoria y el otro registro
                    Token memOp = (op1.getType() == TokenType.REGISTRO) ? op2 : op1;
                    boolean isIndirect = (memOp.getType() == TokenType.COMPUESTO && memOp.getValue().contains("["));
                    return isIndirect ? 2 : 4;
                }
                return 2;

            default:
                return 0;
        }
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

    public List<LineaAnalizadaSemanticamente> getTablaDeSimbolos() {
        return tablaDeSimbolos;
    }

    public List<LineaAnalizadaSemanticamente> getAnalisisSemantico() {
        return analisisSemantico;
    }
}
