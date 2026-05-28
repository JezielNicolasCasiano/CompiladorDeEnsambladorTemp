package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.parser.NodoAST;

import java.util.List;

public class ValidadorInstrucciones {

    public static void validar(NodoAST nodoInstruccion, ContextoSemantico contexto, List<NodoAST> arbolParser) {
        TokenSubtype.Instruccion instruccion = (TokenSubtype.Instruccion) nodoInstruccion.getToken().getSub();

        switch (instruccion) {
            case MOV:
            case ADD:
            case LDS:
                validarDosOperandos(nodoInstruccion, contexto, instruccion,  arbolParser);
                break;
            case INC:
            case NEG:
            case DIV:
            case IMUL:
                validarUnOperando(nodoInstruccion, contexto, instruccion, arbolParser);
                break;
            case JMP:
            case JNS:
            case JS:
            case JG:
            case JNBE:
            case LOOPNE:
                validarSaltos(nodoInstruccion, contexto,arbolParser);
                break;
            case ROR:
                validarRor(nodoInstruccion, contexto, arbolParser);
                break;
            case CBW:
            case CLC:
            case LODSB:
            case LODSW:
            case STOSB:
            case STOSW:
                break;
        }
    }

    private static void validarDosOperandos(NodoAST nodo, ContextoSemantico contexto, TokenSubtype.Instruccion inst, List<NodoAST> arbolParser) {
        if (nodo.getHijos().size() < 2) return;

        NodoAST destino = nodo.getHijos().get(0);
        NodoAST fuente = nodo.getHijos().get(1);

        // Destino no puede ser una constante
        if (destino.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE) {
            contexto.registrarError(new ErrorSemantico(destino.getToken(), "El operando destino no puede ser un valor inmediato.",arbolParser.indexOf(nodo)));
        }

        // Memoria a memoria no está permitido en 8086
        if (esMemoriaOVariable(destino) && esMemoriaOVariable(fuente)) {
            contexto.registrarError(new ErrorSemantico(nodo.getToken(), "Movimiento de memoria a memoria no permitido.",arbolParser.indexOf(nodo)));
        }

        // Reglas específicas para LDS
        if (inst == TokenSubtype.Instruccion.LDS) {
            if (destino.getTipo() != NodoAST.Tipo.OPERANDO_REGISTRO || obtenerTamanoOperando(destino, contexto) != 2) {
                contexto.registrarError(new ErrorSemantico(destino.getToken(), "LDS requiere un registro de 16 bits como destino.",arbolParser.indexOf(nodo)));
            }
            if (!esMemoriaOVariable(fuente)) {
                contexto.registrarError(new ErrorSemantico(fuente.getToken(), "LDS requiere un operando de memoria como fuente.", arbolParser.indexOf(nodo)));
            }
        }

        // Comprobación de tamaños (8 bits vs 16 bits)
        int tamDestino = obtenerTamanoOperando(destino, contexto);
        int tamFuente = obtenerTamanoOperando(fuente, contexto);

        // Si ambos tamaños son conocidos y no coinciden, es un error (una constante da 0, por lo que se adapta al destino)
        if (tamDestino > 0 && tamFuente > 0 && tamDestino != tamFuente) {
            contexto.registrarError(new ErrorSemantico(nodo.getToken(), "Incompatibilidad de tamaños entre operandos.",arbolParser.indexOf(nodo)));
        }
    }

    private static void validarUnOperando(NodoAST nodo, ContextoSemantico contexto, TokenSubtype.Instruccion inst,  List<NodoAST> arbolParser) {
        if (nodo.getHijos().isEmpty()) return;

        NodoAST destino = nodo.getHijos().get(0);

        if (destino.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE) {
            contexto.registrarError(new ErrorSemantico(destino.getToken(), "La instrucción " + inst + " requiere un registro o memoria, no un valor inmediato.", arbolParser.indexOf(nodo)));
        }
    }

    private static void validarSaltos(NodoAST nodo, ContextoSemantico contexto,  List<NodoAST> arbolParser) {
        if (nodo.getHijos().isEmpty()) return;

        NodoAST destino = nodo.getHijos().get(0);

        if (destino.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE) {
            String nombreDestino = destino.getToken().getValue();

            if (!contexto.getTablaSimbolos().containsKey(nombreDestino)) {
                contexto.registrarError(new ErrorSemantico(destino.getToken(), "Etiqueta o variable no definida: '" + nombreDestino + "'", arbolParser.indexOf(nodo)));
            }
        }
    }

    private static void validarRor(NodoAST nodo, ContextoSemantico contexto,  List<NodoAST> arbolParser) {
        if (nodo.getHijos().size() < 2) return;

        NodoAST destino = nodo.getHijos().get(0);
        NodoAST fuente = nodo.getHijos().get(1);

        if (destino.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE) {
            contexto.registrarError(new ErrorSemantico(destino.getToken(), "El destino de ROR debe ser registro o memoria.", arbolParser.indexOf(nodo)));
        }

        if (fuente.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO) {
            if (!fuente.getToken().getValue().equalsIgnoreCase("CL")) {
                contexto.registrarError(new ErrorSemantico(fuente.getToken(), "ROR solo acepta el registro 'CL' como multiplicador de rotación.", arbolParser.indexOf(nodo)));
            }
        } else if (fuente.getTipo() != NodoAST.Tipo.OPERANDO_CONSTANTE) {
            contexto.registrarError(new ErrorSemantico(fuente.getToken(), "La cantidad de rotación debe ser constante o CL.", arbolParser.indexOf(nodo)));
        }
    }

    // metodos auxiliares

    private static boolean esMemoriaOVariable(NodoAST nodo) {
        return nodo.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || nodo.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA;
    }

    private static int obtenerTamanoOperando(NodoAST operando, ContextoSemantico contexto) {
        if (operando.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO) {
            String reg = operando.getToken().getValue().toUpperCase();
            // Registros de 8 bits terminan en L o H (AL, AH, BL...)
            if (reg.endsWith("H") || reg.endsWith("L")) {
                return 1; // 1 byte (8 bits)
            }
            return 2; // 2 bytes (16 bits)
        }
        else if (operando.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE) {
            String nombreVar = operando.getToken().getValue();
            if (contexto.getTablaSimbolos().containsKey(nombreVar)) {
                return contexto.getTablaSimbolos().get(nombreVar).getTamano();
            }
        }
        else if (operando.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE) {
            return 0; // 0 indica que la constante se adapta al tamaño del destino
        }

        return -1; // Memoria o tamaño indeterminado
    }
}