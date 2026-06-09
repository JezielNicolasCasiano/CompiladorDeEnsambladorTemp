package jeziel.compiladordeensamblador.modelo;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.semantico.LineaAnalizadaSemanticamente;
import java.util.List;

public class FilaCodigo {
    private final String simbolo;
    private final String tipo;
    private final String valor;
    private final String tamano;
    private final String direccion;

    public FilaCodigo(String simbolo, String tipo, String valor, String tamano) {
        this.simbolo = simbolo;
        this.tipo = tipo;
        this.valor = valor;
        this.tamano = tamano;
        this.direccion = "-";
    }

    public FilaCodigo(String simbolo, String tipo, String valor, String tamano, String direccion) {
        this.simbolo = simbolo;
        this.tipo = tipo;
        this.valor = valor;
        this.tamano = tamano;
        this.direccion = direccion;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public String getTamano() {
        return tamano;
    }

    public String getDireccion() {
        return direccion;
    }

    public static FilaCodigo crearDesdeLineaSemantica(LineaAnalizadaSemanticamente lineaSemantica) {
        if (lineaSemantica == null || lineaSemantica.getLineaAnalizada() == null) {
            return null;
        }

        List<Token> tokens = lineaSemantica.getLineaAnalizada().getTokens();
        if (tokens == null || tokens.isEmpty()) {
            return null;
        }

        Token primerToken = tokens.get(0);
        String simbolo = primerToken.getValue();
        if (simbolo.endsWith(":")) {
            simbolo = simbolo.substring(0, simbolo.length() - 1);
        }

        String tipo = "-";
        String valor = "-";
        String tamano = "0";

        if (primerToken.getType() == TokenType.ETIQUETA) {
            tipo = "Etiqueta";
            valor = "-";
            tamano = "-";
        } else if (primerToken.getType() == TokenType.VARIABLE) {
            // El valor empieza en el índice 2
            if (tokens.size() > 2) {
                Token tokenValor = tokens.get(2);
                tipo = determinarTipo(tokenValor);

                StringBuilder valorSb = new StringBuilder();
                for (int i = 2; i < tokens.size(); i++) {
                    valorSb.append(tokens.get(i).getValue()).append(" ");
                }
                valor = valorSb.toString().trim();
            }

            int bytes = calcularBytes(tokens, lineaSemantica.getTamanoInstruccion());
            tamano = String.valueOf(bytes);
        }

        String direccion = lineaSemantica.getDireccion() != null ? lineaSemantica.getDireccion() : "-";

        return new FilaCodigo(simbolo, tipo, valor, tamano, direccion);
    }

    private static String determinarTipo(Token tokenValor) {
        if (tokenValor == null) return "-";
        if (tokenValor.getType() == TokenType.CADENA) {
            return "Cadena";
        } else if (tokenValor.getType() == TokenType.CONSTANTE) {
            Object sub = tokenValor.getSub();
            if (sub == TokenSubtype.Constante.HEXADECIMAL) {
                return "Hexadecimal";
            } else if (sub == TokenSubtype.Constante.BINARIO) {
                return "Binario";
            } else {
                return "Decimal";
            }
        } else if (tokenValor.getType() == TokenType.PSEUDOINSTRUCCION && tokenValor.getSub() == TokenSubtype.Directiva.DUP) {
            String valStr = tokenValor.getValue();
            int start = valStr.indexOf('(');
            int end = valStr.lastIndexOf(')');
            if (start != -1 && end != -1 && end > start + 1) {
                String inner = valStr.substring(start + 1, end).trim();
                if (inner.equals("?")) return "Decimal";
                if ((inner.startsWith("'") && inner.endsWith("'")) || (inner.startsWith("\"") && inner.endsWith("\""))) {
                    return "Cadena";
                }
                String upper = inner.toUpperCase();
                if (upper.endsWith("H")) return "Hexadecimal";
                if (upper.endsWith("B")) return "Binario";
                return "Decimal";
            }
            return "DUP";
        }
        return "-";
    }

    private static int calcularBytes(List<Token> tokens, String tamanoInstruccion) {
        if (tokens == null || tokens.size() < 3) return 0;
        int baseSize = 0;
        if ("BYTE".equals(tamanoInstruccion)) {
            baseSize = 1;
        } else if ("WORD".equals(tamanoInstruccion)) {
            baseSize = 2;
        } else {
            return 0; // EQU u otros ocupan 0 bytes
        }

        // Si es DUP, p. ej. var db 32 dup(0)
        if (tokens.size() == 4) {
            Token tokenCount = tokens.get(2);
            Token tokenDup = tokens.get(3);
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
        for (int i = 2; i < tokens.size(); i += 2) {
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
}