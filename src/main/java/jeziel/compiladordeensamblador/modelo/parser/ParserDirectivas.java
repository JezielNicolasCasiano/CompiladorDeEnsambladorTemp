package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.*;
import jeziel.compiladordeensamblador.modelo.semantico.Simbolo;

import java.util.List;

public class ParserDirectivas {

    public static NodoAST parse(Parser p, List<ErrorSintactico> errores) {
        Token dir = p.consumir(TokenType.PSEUDOINSTRUCCION);
        if (dir == null) return null;

        NodoAST nodo = new NodoAST(NodoAST.Tipo.DIRECTIVA, dir);
        String val = dir.getValue().toUpperCase().replace(" ", "_");
        TokenSubtype.Directiva subtipo;

        try {
            subtipo = TokenSubtype.Directiva.valueOf(val);
        } catch (IllegalArgumentException e) {
            p.lanzarError(dir, "Pseudoinstruccion no reconocida: '" + dir.getValue() + "'");
            return nodo;
        }

        switch (subtipo) {
            // DB / DW — variable: valor o lista de valores
            case DB:
            case DW:
                if (!nodo.getHijos().isEmpty() && nodo.getHijos().get(0).getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE) {
                    String nombreVar = nodo.getHijos().get(0).getToken().getValue();
                    int multiplicador = (subtipo == TokenSubtype.Directiva.DB) ? 1 : 2;

                    // --- CÁLCULO DE TAMAÑO TOMANDO EN CUENTA EL DUP ---
                    int cantValores = 0;
                    for (int i = 1; i < nodo.getHijos().size(); i++) {
                        NodoAST operando = nodo.getHijos().get(i);

                        if (operando.getTipo() == NodoAST.Tipo.OPERANDO_DUP) {
                            // El hijo 0 guarda el token del multiplicador (ej. "128")
                            String repeticionesStr = operando.getHijos().get(0).getToken().getValue();
                            cantValores += parsearConstanteAEntero(repeticionesStr);

                            // Para el futuro (Segunda Pasada):
                            // El valor interno (ej. '0' o 'a') está en operando.getToken().getValue() que es el String "dup(0)".
                            // En la segunda pasada solo tendrás que hacer un substring o regex para extraer lo que está entre los paréntesis.
                        } else {
                            // Variable normal (ej. 'hola' o 45H)
                            cantValores += 1;
                        }
                    }
                    String tipoStr = (multiplicador == 1) ? "VAR_8BITS" : "VAR_16BITS";
                    Simbolo simVar = new Simbolo(nombreVar, tipoStr, locationCounter, multiplicador * cantValores);

                    if (!tablaSimbolos.agregar(simVar)) {
                        erroresSemanticos.add(new ErrorSintactico(nodo.getToken(), "Variable redefinida: " + nombreVar));
                    }

                    locationCounter += (multiplicador * cantValores);
                }
                break;

            // EQU
            case EQU:
                nodo.agregarHijo(p.parseOperando());
                break;

            // ORG
            case ORG:
                nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_CONSTANTE, p.consumir(TokenType.CONSTANTE)));
                break;

            // SEGMENT 
            case SEGMENT:
            case ENDS:
            case PROC:
            case ENDP:
            case MACRO:
            case ENDM:
            case END:
            case STACK:
            case DATA:
            case CODE:
                if (p.estipo(TokenType.VARIABLE)) {
                    nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, p.consumir()));
                }
                break;




                default:
                break;
        }

        return nodo;
    }
}
