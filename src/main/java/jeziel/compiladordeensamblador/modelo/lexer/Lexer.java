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
                assert token != null;
                if (token.getType() != TokenType.COMENTARIO && token.getType() != TokenType.SEPARADOR) {
                    tokens.add(token);
                }
            }
            currentPosition = 0;
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
                "[a-zA-Z][a-zA-Z0-9_]{0,9}:",
                "(?i)\\b(CBW|CLC|LODSB|LODSW|STOSB|STOSW|DIV|IMUL|INC|NEG|ADD|LDS|MOV|ROR|JNS|JS|LOOPNE|JG|JMP|JNBE|INT|OFFSET)\\b",
                "(?i)(BYTE PTR|WORD PTR|ORG|END|DB|DW|EQU|SEGMENT|ENDS|STACK|DATA|CODE|DUP|MACRO|ENDM|PROC|ENDP)\\b",
                "(?i)(.STACK|.DATA|.CODE|.MODEL|.STARTUP|.EXIT)",
                "(?i)\\b(AX|BX|CX|DX|SI|DI|BP|SP|AH|AL|BH|BL|CH|CL|DH|DL|CS|DS|SS|ES)\\b",
                "0[0-9A-Fa-f]+[hH]",
                "0[0-1]+[bB]",
                "[0-9]+",
                "'.'",
                "[a-zA-Z][a-zA-Z0-9_]{0,9}",
                ",",
                "\\[",
                "\\]",
                "\\(",
                "\\)",
                "\""           //"\"[^\"]*\"" para cadenas (por si acaso)
        };

        TokenType[] tokenTypes = {
                TokenType.COMENTARIO,
                TokenType.ETIQUETA,
                TokenType.INSTRUCCION,
                TokenType.PSEUDOINSTRUCCION,
                TokenType.PSEUDOINSTRUCCION,
                TokenType.REGISTRO,
                TokenType.CONSTANTE,
                TokenType.CONSTANTE,
                TokenType.CONSTANTE,
                TokenType.CARACTER,
                TokenType.VARIABLE,
                TokenType.SEPARADOR,
                TokenType.CORCHETE_ABRE,
                TokenType.CORCHETE_CIERRA,
                TokenType.PARENTESIS_ABRE,
                TokenType.PARENTESIS_CIERRA,
                TokenType.COMILLA
        };

        for (int i = 0; i < tokenPatterns.length; i++) {
            Pattern pattern = Pattern.compile("^" + tokenPatterns[i]);
            Matcher matcher = pattern.matcher(input.get(currentLine).substring(currentPosition));

            if (matcher.find()) {
                String value = matcher.group();
                int endPos = currentPosition + value.length();
                char nextChar = endPos < input.get(currentLine).length()
                        ? input.get(currentLine).charAt(endPos)
                        : 0;

                if (tokenTypes[i] == TokenType.CONSTANTE && Character.isLetter(nextChar)) {
                    break;
                }

                if (tokenTypes[i] == TokenType.VARIABLE && nextChar == ':') {
                    break;
                }

                currentPosition = endPos;
                Enum<?> subtype = resolveSubtype(tokenTypes[i], value);
                return new Token(tokenTypes[i], value, subtype);
            }
        }

        String textoSobrante = input.get(currentLine).substring(currentPosition);
        String[] partes = textoSobrante.split("\\s+", 2);
        String palabraDesconocida = partes[0];
        currentPosition += palabraDesconocida.length();

        return new Token(TokenType.DESCONOCIDO, palabraDesconocida);
    }

    private Enum<?> resolveSubtype(TokenType type, String value) {
        String upper = value.toUpperCase();
        switch (type) {
            case INSTRUCCION:
                try { return TokenSubtype.Instruccion.valueOf(upper); }
                catch (IllegalArgumentException e) { return null; }

            case PSEUDOINSTRUCCION:
                try { return TokenSubtype.Directiva.valueOf(upper.replace(" ", "_").replace(".", "")); }
                catch (IllegalArgumentException e) { return null; }

            case REGISTRO:
                try { return TokenSubtype.Registro.valueOf(upper); }
                catch (IllegalArgumentException e) { return null; }

            case CONSTANTE:
                if (upper.endsWith("H")) return TokenSubtype.Constante.HEXADECIMAL;
                if (upper.endsWith("B")) return TokenSubtype.Constante.BINARIO;
                return TokenSubtype.Constante.DECIMAL;

            default:
                return null;
        }
    }
}