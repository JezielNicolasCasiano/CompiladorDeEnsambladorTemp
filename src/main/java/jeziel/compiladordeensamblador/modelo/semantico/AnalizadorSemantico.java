package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.parser.NodoAST;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.parser.ErrorSintactico; // Reutilizamos tu clase de error o creamos una ErrorSemantico

import java.util.ArrayList;
import java.util.List;

    public class AnalizadorSemantico {
        private int locationCounter; // El Instruction Pointer (IP) simulado
        private TablaSimbolo tablaSimbolos;
        private List<ErrorSintactico> erroresSemanticos; // Puedes crear una clase ErrorSemantico si prefieres

        public AnalizadorSemantico() {
            this.locationCounter = 0;
            this.tablaSimbolos = new TablaSimbolo();
            this.erroresSemanticos = new ArrayList<>();
        }

        public void analizar(List<NodoAST> arbol) {
            for (NodoAST nodo : arbol) {
                visitarNodo(nodo);
            }
        }

        private void visitarNodo(NodoAST nodo) {
            switch (nodo.getTipo()) {
                case ETIQUETA:
                    String nombreEtiqueta = nodo.getToken().getValue().replace(":", "");
                    Simbolo simEtiqueta = new Simbolo(nombreEtiqueta, "ETIQUETA", locationCounter, 0);
                    if (!tablaSimbolos.agregar(simEtiqueta)) {
                        erroresSemanticos.add(new ErrorSintactico(nodo.getToken(), "Símbolo redefinido: " + nombreEtiqueta));
                    }

                    // Una etiqueta puede tener hijos (si es Etiqueta: Instruccion)
                    for (NodoAST hijo : nodo.getHijos()) {
                        visitarNodo(hijo);
                    }
                    break;

                case DIRECTIVA:
                    procesarDirectiva(nodo);
                    break;

                case INSTRUCCION:
                    // Por ahora, asumimos una heurística básica.
                    // En 8086 real, depende de los operandos. Lo refinaremos después.
                    locationCounter += calcularTamanoInstruccion(nodo);
                    break;

                default:
                    break;
            }
        }

        private void procesarDirectiva(NodoAST nodo) {
            TokenSubtype.Directiva subtipo = (TokenSubtype.Directiva) nodo.getToken().getSub();

            // 1. AÑADE ESTA VALIDACIÓN: Si el subtipo es null, abortamos para no causar un NPE
            if (subtipo == null) {
                return;
            }

            // 2. Ahora el switch es seguro
            switch (subtipo) {
                case ORG:
                    // El ORG cambia el location counter directamente
                    // Asumimos que el primer hijo es una constante
                    if (!nodo.getHijos().isEmpty()) {
                        String valorH = nodo.getHijos().get(0).getToken().getValue();
                        locationCounter = parsearConstanteAEntero(valorH);
                    }
                    break;

                case DB:
                case DW:
                    // El parser debe poner el nombre de la variable como primer hijo
                    if (!nodo.getHijos().isEmpty() && nodo.getHijos().get(0).getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE) {
                        String nombreVar = nodo.getHijos().get(0).getToken().getValue();
                        int multiplicador = (subtipo == TokenSubtype.Directiva.DB) ? 1 : 2;
                        int cantValores = nodo.getHijos().size() - 1; // Le restamos el nombre de la variable

                        String tipoStr = (multiplicador == 1) ? "VAR_8BITS" : "VAR_16BITS";
                        Simbolo simVar = new Simbolo(nombreVar, tipoStr, locationCounter, multiplicador * cantValores);

                        if (!tablaSimbolos.agregar(simVar)) {
                            erroresSemanticos.add(new ErrorSintactico(nodo.getToken(), "Variable redefinida: " + nombreVar));
                        }

                        locationCounter += (multiplicador * cantValores);
                    }
                    break;
                default:
                    break;
            }
        }

        private int calcularTamanoInstruccion(NodoAST instruccion) {
            // TODO: Lógica real del ModR/M
            // Por ahora, un parche para que avance el Location Counter:
            // Sin operandos (LODSB, CBW) = 1 byte
            // Con 1 o 2 operandos = 2 o 3 bytes aprox
            int numOperandos = instruccion.getHijos().size();
            if (numOperandos == 0) return 1;
            if (numOperandos == 1) return 2;
            return 3;
        }

        private int parsearConstanteAEntero(String constanteStr) {
            constanteStr = constanteStr.toUpperCase();
            try {
                if (constanteStr.endsWith("H")) {
                    return Integer.parseInt(constanteStr.substring(0, constanteStr.length() - 1), 16);
                } else if (constanteStr.endsWith("B")) {
                    return Integer.parseInt(constanteStr.substring(0, constanteStr.length() - 1), 2);
                } else {
                    return Integer.parseInt(constanteStr);
                }
            } catch (NumberFormatException e) {
                return 0; // Manejo de error si la constante es inválida
            }
        }

        public TablaSimbolo getTablaSimbolos() { return tablaSimbolos; }
        public List<ErrorSintactico> getErrores() { return erroresSemanticos; }
    }

