package jeziel.compiladordeensamblador.modelo.lexer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private int currentPosition;
    private int currentLine;
    private List<String> input;

    public Lexer(List<String> input) {
        this.input = input;
        this.currentPosition = 0;
        this.currentLine = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (currentLine < input.size()) {
            while (currentPosition < input.get(currentLine).length()) {
                char currentChar = input.get(currentLine).charAt(currentPosition);

                if (Character.isWhitespace(currentChar)) {
                    currentPosition++;
                    continue;
                }

                Token token = nextToken();
                if (token != null) {
                    tokens.add(token);
                } else {
                    throw new RuntimeException("Caracter Desconocido: " + currentChar);

                }
            }
            currentLine++;
        }
        return tokens;
    }

    private Token nextToken() {
        if (currentPosition >= input.get(currentLine).length()) {
            return null;
        }

        String[] tokenPatterns = {
                ";[^\r\n]*",
                "[a-zA-Z_][a-zA-Z0-9_]*:",
                "(?i)\\b(CBW|CLC|LODSB|LODSW|STOSB|STOSW|DIV|IMUL|INC|NEG|ADD|LDS|MOV|ROR|JNS|JS|LOOPNE|JG|JMP|JNBE)\\b",
                "(?i)\\b(ORG|END|DB|DW|EQU|SEGMENT|ENDS|STACK|DATA|CODE|DUP|BYTE PTR|WORD PTR|MACRO|ENDM|PROC|ENDP)\\b",
                "(?i)\\b(AX|BX|CX|DX|SI|DI|BP|SP|AH|AL|BH|BL|CH|CL|DH|DL|CS|DS|SS|ES)\\b",
                "[0-9A-Fa-f]+[hH]",
                "[0-1]+[bB]",
                "[0-9]+",
                "'.'",
                "[a-zA-Z_][a-zA-Z0-9_]*",
                ",",
                "\\[",
                "\\]",
                "\\(",
                "\\)"
        };

        TokenType[] tokenTypes = {
                TokenType.COMENTARIO,
                TokenType.ETIQUETA,
                TokenType.INSTRUCCION,
                TokenType.DIRECTIVA,
                TokenType.REGISTRO,
                TokenType.NUMERO,
                TokenType.NUMERO,
                TokenType.NUMERO,
                TokenType.CARACTER,
                TokenType.IDENTIFICADOR,
                TokenType.SEPARADOR,
                TokenType.CORCHETE_ABRE,
                TokenType.CORCHETE_CIERRA,
                TokenType.PARENTESIS_ABRE,
                TokenType.PARENTESIS_CIERRA
        };

        for (int i = 0; i < tokenPatterns.length; i++) {
            Pattern pattern = Pattern.compile("^" + tokenPatterns[i]);
            Matcher matcher = pattern.matcher(input.get(currentLine).substring(currentPosition));

            if (matcher.find()) {
                String value = matcher.group();
                currentPosition += value.length();
                Enum<?> subtype = resolveSubtype(tokenTypes[i], value);
                return new Token(tokenTypes[i], value, subtype);
            }
        }

        return null;
    }

    private Enum<?> resolveSubtype(TokenType type, String value) {
        String upper = value.toUpperCase();
        switch (type) {
            case INSTRUCCION:
                try { return TokenSubtype.Instruccion.valueOf(upper); }
                catch (IllegalArgumentException e) { return null; }

            case DIRECTIVA:
                try { return TokenSubtype.Directiva.valueOf(upper.replace(" ", "_")); }
                catch (IllegalArgumentException e) { return null; }

            case REGISTRO:
                try { return TokenSubtype.Registro.valueOf(upper); }
                catch (IllegalArgumentException e) { return null; }

            case NUMERO:
                if (upper.endsWith("H")) return TokenSubtype.Numero.HEXADECIMAL;
                if (upper.endsWith("B")) return TokenSubtype.Numero.BINARIO;
                return TokenSubtype.Numero.DECIMAL;

            default:
                return null;
        }
    }
}