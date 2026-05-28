package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.parser.NodoAST;

import java.util.List;

public class ValidadorDirectivas {
    private static List<NodoAST> arbolParser;

    public ValidadorDirectivas(List<NodoAST> arbolParser){
        this.arbolParser = arbolParser;
    }

    public static void validar(NodoAST nodoDirectiva, ContextoSemantico contexto) {
        if (nodoDirectiva.getToken() == null || nodoDirectiva.getToken().getSub() == null) return;

        TokenSubtype.Directiva directiva = (TokenSubtype.Directiva) nodoDirectiva.getToken().getSub();

        switch (directiva) {
            case DB:
            case DW:
                validarLimitesDatos(nodoDirectiva, contexto, directiva);
                break;
            case EQU:
                validarEqu(nodoDirectiva, contexto);
                break;
            case ORG:
            case SEGMENT:
            case ENDS:
            case PROC:
            case ENDP:
            case MACRO:
            case ENDM:
            case END:
            case DATA:
            case STACK:
            case CODE:
                // Estructurales y de control: resueltas en pasada 1 o no requieren validación de límites.
                break;
            default:
                break;
        }
    }

    private static void validarLimitesDatos(NodoAST nodo, ContextoSemantico contexto, TokenSubtype.Directiva tipo) {
        // Recorremos todos los hijos. Recordar que el último puede ser la variable en sí.
        for (int i = 0; i < nodo.getHijos().size(); i++) {
            NodoAST hijo = nodo.getHijos().get(i);

            // Ignoramos la evaluación del nombre de la variable (si está al final de la lista de hijos)
            if (hijo.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE && i == nodo.getHijos().size() - 1) {
                continue;
            }

            if (hijo.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE) {
                try {
                    int valor = decodificarConstante(hijo.getToken());

                    if (tipo == TokenSubtype.Directiva.DB) {
                        // DB permite 8 bits (-128 a 255)
                        if (valor < -128 || valor > 255) {
                            contexto.registrarError(new ErrorSemantico(hijo.getToken(),
                                    "El valor " + valor + " excede el límite de 8 bits para DB.",arbolParser.indexOf(nodo)));
                        }
                    } else if (tipo == TokenSubtype.Directiva.DW) {
                        // DW permite 16 bits (-32768 a 65535)
                        if (valor < -32768 || valor > 65535) {
                            contexto.registrarError(new ErrorSemantico(hijo.getToken(),
                                    "El valor " + valor + " excede el límite de 16 bits para DW.",arbolParser.indexOf(nodo)));
                        }
                    }
                } catch (NumberFormatException e) {
                    contexto.registrarError(new ErrorSemantico(hijo.getToken(),
                            "Formato numérico inválido: " + hijo.getToken().getValue(), arbolParser.indexOf(nodo)));
                }
            } else if (hijo.getTipo() == NodoAST.Tipo.OPERANDO_CARACTER || hijo.getTipo() == NodoAST.Tipo.OPERANDO_CADENA) {
                // Cadenas y caracteres son válidos en DB.
                if (tipo == TokenSubtype.Directiva.DW && hijo.getTipo() == NodoAST.Tipo.OPERANDO_CADENA) {
                    contexto.registrarError(new ErrorSemantico(hijo.getToken(),
                            "Las cadenas de texto largas se definen comúnmente con DB, no con DW.", arbolParser.indexOf(nodo)));
                }
            } else {
                contexto.registrarError(new ErrorSemantico(hijo.getToken(),
                        "Operando no válido para la inicialización de memoria.", arbolParser.indexOf(nodo)));
            }
        }
    }

    private static void validarEqu(NodoAST nodo, ContextoSemantico contexto) {
        if (nodo.getHijos().isEmpty()) {
            contexto.registrarError(new ErrorSemantico(nodo.getToken(), "EQU requiere un operando de valor.", arbolParser.indexOf(nodo)));
            return;
        }

        // En EQU, el primer hijo es el valor constante
        NodoAST operandoValor = nodo.getHijos().get(0);
        if (operandoValor.getTipo() != NodoAST.Tipo.OPERANDO_CONSTANTE) {
            contexto.registrarError(new ErrorSemantico(operandoValor.getToken(), "EQU debe inicializarse con una constante.",arbolParser.indexOf(nodo)));
        }
    }

    private static int decodificarConstante(Token token) throws NumberFormatException {
        String valor = token.getValue().toUpperCase();

        if (token.getSub() == TokenSubtype.Constante.HEXADECIMAL) {
            return Integer.parseInt(valor.replace("H", ""), 16);
        } else if (token.getSub() == TokenSubtype.Constante.BINARIO) {
            return Integer.parseInt(valor.replace("B", ""), 2);
        }

        return Integer.parseInt(valor);
    }
}