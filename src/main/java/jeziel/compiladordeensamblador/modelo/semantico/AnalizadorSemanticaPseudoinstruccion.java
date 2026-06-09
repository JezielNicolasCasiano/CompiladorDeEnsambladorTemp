package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

import java.util.List;

public class AnalizadorSemanticaPseudoinstruccion extends AnalizadorSemanticoGeneral {

    public AnalizadorSemanticaPseudoinstruccion(Token primerToken, LineaAnalizada lineaAnalizada, List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        super(primerToken, lineaAnalizada, tablaDeSimbolos);
    }

    @Override
    public void analizar() {
        TokenSubtype.Directiva subtipo = (TokenSubtype.Directiva) getPrimerToken().getSub();
        List<Token> tokens = getLineaAAnalizar();

        switch (subtipo) {
            case ORG:
                validarOrg(tokens);
                break;

            case END:
                validarEnd(tokens);
                break;

            default:
                break;
        }
    }

    private void validarOrg(List<Token> tokens) {
        if (tokens.size() < 2) return;
        Token op = tokens.get(1);

        if (op.getType() != TokenType.CONSTANTE) {
            ErrorSemantico error = new ErrorSemantico(op);
            error.setMensajeError("El operando de ORG debe ser una constante.");
            setErrorSemantico(error);
            return;
        }

        long val = obtenerValorNumerico(op);
        if (val < 0 || val > 65535) {
            ErrorSemantico error = new ErrorSemantico(op);
            error.setMensajeError("La dirección de ORG " + op.getValue() + " excede el rango de 16 bits (0-65535).");
            setErrorSemantico(error);
        }
    }

    private void validarEnd(List<Token> tokens) {
        if (tokens.size() < 2) return;
        Token op = tokens.get(1);

        if (op.getType() == TokenType.VARIABLE || op.getType() == TokenType.IDENTIFICADOR) {
            if (!existeSimbolo(op.getValue())) {
                ErrorSemantico error = new ErrorSemantico(op);
                error.setMensajeError("Símbolo de punto de entrada no definido: " + op.getValue());
                setErrorSemantico(error);
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
