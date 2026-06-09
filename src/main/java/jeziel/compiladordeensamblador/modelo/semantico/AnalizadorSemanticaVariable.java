package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

import java.util.List;

public class AnalizadorSemanticaVariable extends AnalizadorSemanticoGeneral {

    public AnalizadorSemanticaVariable(Token primerToken, LineaAnalizada lineaAnalizada, List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        super(primerToken, lineaAnalizada, tablaDeSimbolos);
    }

    @Override
    public void analizar() {
        boolean tieneIdentificador = (getPrimerToken().getType() == TokenType.VARIABLE);

        if (tieneIdentificador) {
            String nombre = getPrimerToken().getValue();
            //Validar que el símbolo no esté duplicado
            for (LineaAnalizadaSemanticamente sym : getTablaDeSimbolos()) {
                if (sym.getLineaAnalizada() != getLineaAnalizada()) {
                    Token symToken = sym.getLineaAnalizada().getTokens().getFirst();
                    String symNombre = symToken.getValue();
                    // Normalizar nombres
                    if (symNombre.endsWith(":")) {
                        symNombre = symNombre.substring(0, symNombre.length() - 1);
                    }
                    if (symNombre.equalsIgnoreCase(nombre)) {
                        ErrorSemantico error = new ErrorSemantico(getPrimerToken());
                        error.setMensajeError("Símbolo duplicado: " + nombre);
                        setErrorSemantico(error);
                        return;
                    }
                }
            }
        }

        // Validar tipos y tamaños de los valores asignados
        int minSize = tieneIdentificador ? 3 : 2;
        if (getLineaAAnalizar().size() < minSize) {
            return; //Ya se atrapó en sintáctico
        }

        Token tokenTamano = tieneIdentificador ? getLineaAAnalizar().get(1) : getPrimerToken();
        Object subtipoDirectiva = tokenTamano.getSub();
        boolean esByte = (subtipoDirectiva == TokenSubtype.Directiva.DB);
        boolean esWord = (subtipoDirectiva == TokenSubtype.Directiva.DW);

        int idxValor = tieneIdentificador ? 2 : 1;

        int expectedSizeForDup = tieneIdentificador ? 4 : 3;
        if (getLineaAAnalizar().size() == expectedSizeForDup) {
            Token tokenCount = getLineaAAnalizar().get(idxValor);
            Token tokenDup = getLineaAAnalizar().get(idxValor + 1);
            if (tokenCount.getType() == TokenType.CONSTANTE &&
                tokenDup.getType() == TokenType.PSEUDOINSTRUCCION &&
                tokenDup.getSub() == TokenSubtype.Directiva.DUP) {

                long countValue;
                try {
                    String lexeme = tokenCount.getValue();
                    Object constSubtype = tokenCount.getSub();
                    if (constSubtype == TokenSubtype.Constante.HEXADECIMAL) {
                        String hex = lexeme.substring(0, lexeme.length() - 1);
                        countValue = Long.parseLong(hex, 16);
                    } else if (constSubtype == TokenSubtype.Constante.BINARIO) {
                        String bin = lexeme.substring(0, lexeme.length() - 1);
                        countValue = Long.parseLong(bin, 2);
                    } else {
                        countValue = Long.parseLong(lexeme);
                    }
                } catch (NumberFormatException e) {
                    ErrorSemantico error = new ErrorSemantico(tokenCount);
                    error.setMensajeError("Formato de factor de repetición numérico inválido");
                    setErrorSemantico(error);
                    return;
                }

                if (countValue <= 0) {
                    ErrorSemantico error = new ErrorSemantico(tokenCount);
                    error.setMensajeError("El factor de repetición debe ser un número entero positivo.");
                    setErrorSemantico(error);
                    return;
                }
                validarValorParaTipo(tokenDup, esByte, esWord);
                return;
            }
        }

        for (int i = idxValor; i < getLineaAAnalizar().size(); i += 2) {
            Token tokenValor = getLineaAAnalizar().get(i);
            if (!validarValorParaTipo(tokenValor, esByte, esWord)) {
                return;
            }
        }
    }

    private boolean validarValorParaTipo(Token tokenValor, boolean esByte, boolean esWord) {
        if (tokenValor.getType() == TokenType.CONSTANTE) {
            return validarConstante(tokenValor, esByte, esWord);
        }

        if (tokenValor.getType() == TokenType.CADENA) {
            String str = tokenValor.getValue();
            //Quitar comillas si están presentes
            if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
                str = str.substring(1, str.length() - 1);
            }
            if (esWord && str.length() > 2) {
                ErrorSemantico error = new ErrorSemantico(tokenValor);
                error.setMensajeError("La cadena es demasiado larga");
                setErrorSemantico(error);
                return false;
            }
        }

        if (tokenValor.getType() == TokenType.PSEUDOINSTRUCCION && tokenValor.getSub() == TokenSubtype.Directiva.DUP) {
            // Analizar expresión DUP, por ejemplo DUP(0) o DUP(?)
            String valStr = tokenValor.getValue();
            String innerValue = obtenerValorDeDup(valStr);
            if (!innerValue.isEmpty() && !innerValue.equals("?")) {
                if ((innerValue.startsWith("'") && innerValue.endsWith("'")) || (innerValue.startsWith("\"") && innerValue.endsWith("\""))) {
                    Token tempToken = new Token(TokenType.CADENA, innerValue, tokenValor.getLinea());
                    return validarValorParaTipo(tempToken, esByte, esWord);
                }
                // Crear un token temporal para validar el valor interno
                Token tempToken = new Token(TokenType.CONSTANTE, innerValue, tokenValor.getLinea());
                // Autodetectar el subtipo de constante
                String upper = innerValue.toUpperCase();
                if (upper.endsWith("H")) {
                    tempToken.setSubtype(TokenSubtype.Constante.HEXADECIMAL);
                } else if (upper.endsWith("B")) {
                    tempToken.setSubtype(TokenSubtype.Constante.BINARIO);
                } else {
                    tempToken.setSubtype(TokenSubtype.Constante.DECIMAL);
                }
                return validarConstante(tempToken, esByte, esWord);
            }
        }

        return true;
    }

    private boolean validarConstante(Token tokenConst, boolean esByte, boolean esWord) {
        long value;
        try {
            String lexeme = tokenConst.getValue();
            Object constSubtype = tokenConst.getSub();
            if (constSubtype == TokenSubtype.Constante.HEXADECIMAL) {
                String hex = lexeme.substring(0, lexeme.length() - 1);
                value = Long.parseLong(hex, 16);
            } else if (constSubtype == TokenSubtype.Constante.BINARIO) {
                String bin = lexeme.substring(0, lexeme.length() - 1);
                value = Long.parseLong(bin, 2);
            } else {
                value = Long.parseLong(lexeme);
            }
        } catch (NumberFormatException e) {
            ErrorSemantico error = new ErrorSemantico(tokenConst);
            error.setMensajeError("Formato numérico inválido");
            setErrorSemantico(error);
            return false;
        }

        if (esByte && (value < 0 || value > 255)) {
            ErrorSemantico error = new ErrorSemantico(tokenConst);
            error.setMensajeError("El valor excede el tamaño");
            setErrorSemantico(error);
            return false;
        }

        if (esWord && (value < 0 || value > 65535)) {
            ErrorSemantico error = new ErrorSemantico(tokenConst);
            error.setMensajeError("El valor excede el tamaño");
            setErrorSemantico(error);
            return false;
        }

        return true;
    }

    private String obtenerValorDeDup(String dupStr) {
        int start = dupStr.indexOf('(');
        int end = dupStr.lastIndexOf(')');
        if (start != -1 && end != -1 && end > start + 1) {
            return dupStr.substring(start + 1, end).trim();
        }
        return "";
    }
}
