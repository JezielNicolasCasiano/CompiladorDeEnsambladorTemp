package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.parser.NodoAST;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.parser.ErrorSintactico;

import java.util.ArrayList;
import java.util.List;

    public class AnalizadorSemantico {
        private int locationCounter;
        private TablaSimbolo tablaSimbolos;
        private List<ErrorSintactico> erroresSemanticos;
        private final int BASE_ADDRESS = 0x0470;

        public AnalizadorSemantico() {
            this.locationCounter = BASE_ADDRESS;
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
                    // Etiqueta no tiene "valor" literal como tal, pero le pasamos "-"
                    Simbolo simEtiqueta = new Simbolo(nombreEtiqueta, "ETIQUETA", "-", locationCounter, 0);
                    if (!tablaSimbolos.agregar(simEtiqueta)) {
                        erroresSemanticos.add(new ErrorSintactico(nodo.getToken(), "Símbolo redefinido: " + nombreEtiqueta));
                    }
                    for (NodoAST hijo : nodo.getHijos()) {
                        visitarNodo(hijo);
                    }
                    break;

                case DIRECTIVA:
                    procesarDirectiva(nodo);
                    break;

                case INSTRUCCION:
                    locationCounter += calcularTamanoInstruccion(nodo);
                    break;

                default:
                    break;
            }
        }

        private void procesarDirectiva(NodoAST nodo) {
            TokenSubtype.Directiva subtipo = (TokenSubtype.Directiva) nodo.getToken().getSub();

            if (subtipo == null) {
                return;
            }

            switch (subtipo) {
                case DATA:
                case CODE:
                case STACK:
                case DATA_SEGMENT:
                case CODE_SEGMENT:
                case STACK_SEGMENT:
                    break;

                case ORG:
                    if (!nodo.getHijos().isEmpty()) {
                        String val = nodo.getHijos().get(0).getToken().getValue();
                        this.locationCounter = parsearConstanteAEntero(val);
                    }
                    break;

                case EQU:
                    if (!nodo.getHijos().isEmpty() && nodo.getHijos().get(0).getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE) {
                        String nombreVar = nodo.getHijos().get(0).getToken().getValue();
                        String valorStr = nodo.getHijos().get(1).getToken().getValue();
                        Simbolo simVar = new Simbolo(nombreVar, "CONSTANTE (EQU)", valorStr, 0, 0);
                        if (!tablaSimbolos.agregar(simVar)) {
                            erroresSemanticos.add(new ErrorSintactico(nodo.getToken(), "Símbolo redefinido: " + nombreVar));
                        }
                    }
                    break;

                case DB:
                case DW:
                    if (!nodo.getHijos().isEmpty() && nodo.getHijos().get(0).getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE) {
                        String nombreVar = nodo.getHijos().get(0).getToken().getValue();
                        int multiplicador = (subtipo == TokenSubtype.Directiva.DB) ? 1 : 2;

                        int cantValores = 0;
                        StringBuilder valoresConcat = new StringBuilder();

                        for (int i = 1; i < nodo.getHijos().size(); i++) {
                            NodoAST operando = nodo.getHijos().get(i);

                            if (operando.getTipo() == NodoAST.Tipo.OPERANDO_DUP) {
                                String repeticionesStr = operando.getHijos().get(0).getToken().getValue();
                                String valorInterno = operando.getHijos().get(1).getToken().getValue();
                                cantValores += parsearConstanteAEntero(repeticionesStr);
                                valoresConcat.append(repeticionesStr).append(" DUP (").append(valorInterno).append(") ");
                            } else {
                                cantValores += 1;
                                valoresConcat.append(operando.getToken().getValue()).append(" ");
                            }
                        }

                        String tipoStr = "VARIABLE";
                        Simbolo simVar = new Simbolo(nombreVar, tipoStr, valoresConcat.toString().trim(), locationCounter, multiplicador * cantValores);

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

        private int calcularTamanoInstruccion(NodoAST instruccion) {//calculo del tamaño de las lineas de codigo
            TokenSubtype.Instruccion tipoInst = (TokenSubtype.Instruccion) instruccion.getToken().getSub();
            List<NodoAST> operandos = instruccion.getHijos();
            int numOperandos = operandos.size();


            if (numOperandos == 0) {
                return 1;
            }

            if (tipoInst == TokenSubtype.Instruccion.JMP) {
                return 3;
            }


            if (tipoInst == TokenSubtype.Instruccion.JNS ||
                    tipoInst == TokenSubtype.Instruccion.JS ||
                    tipoInst == TokenSubtype.Instruccion.LOOPNE ||
                    tipoInst == TokenSubtype.Instruccion.JG ||
                    tipoInst == TokenSubtype.Instruccion.JNBE) {
                return 2;
            }

            if (numOperandos == 2) {
                NodoAST op1 = operandos.get(0);
                NodoAST op2 = operandos.get(1);

                boolean op1Reg = (op1.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO);
                boolean op2Reg = (op2.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO);
                boolean op2Const = (op2.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE);
                boolean op1Mem = (op1.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || op1.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA);
                boolean op2Mem = (op2.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || op2.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA);

                if (tipoInst == TokenSubtype.Instruccion.LDS) {
                    return 4;
                }
                if (tipoInst == TokenSubtype.Instruccion.ROR) {
                    if (op1Reg) {
                        if (op2Reg && op2.getToken().getValue().equalsIgnoreCase("CL")) return 2;
                        if (op2Const && op2.getToken().getValue().equals("1")) return 2;
                        return 3;
                    } else if (op1Mem) {
                        if (op2Reg && op2.getToken().getValue().equalsIgnoreCase("CL")) return 4;
                        if (op2Const && op2.getToken().getValue().equals("1")) return 4;
                        return 5;
                    }
                }


                if (op1Reg && op2Reg) {
                    return 2;
                }
                if (op1Reg && op2Const) {
                    return esRegistro8Bits(op1.getToken().getValue()) ? 2 : 3;
                }
                if ((op1Reg && op2Mem) || (op1Mem && op2Reg)) {
                    return 4;
                }
                if (op1Mem && op2Const) {
                    return 5;
                }
            }

            if (numOperandos == 1) {
                NodoAST op = operandos.get(0);

                if (op.getTipo() == NodoAST.Tipo.OPERANDO_REGISTRO) {
                    if (tipoInst == TokenSubtype.Instruccion.INC) {
                        return esRegistro8Bits(op.getToken().getValue()) ? 2 : 1;
                    }
                    return 2;
                } else if (op.getTipo() == NodoAST.Tipo.OPERANDO_VARIABLE || op.getTipo() == NodoAST.Tipo.OPERANDO_MEMORIA) {
                    return 4;
                }
            }

            return 2;
        }

        private boolean esRegistro8Bits(String reg) {
            String r = reg.toUpperCase();
            return r.equals("AH") || r.equals("AL") || r.equals("BH") || r.equals("BL") ||
                    r.equals("CH") || r.equals("CL") || r.equals("DH") || r.equals("DL");
        }

        private boolean esSaltoCondicionalOLoop(TokenSubtype.Instruccion tipoInst) {
            return tipoInst == TokenSubtype.Instruccion.JNS ||
                    tipoInst == TokenSubtype.Instruccion.JS ||
                    tipoInst == TokenSubtype.Instruccion.LOOPNE ||
                    tipoInst == TokenSubtype.Instruccion.JG ||
                    tipoInst == TokenSubtype.Instruccion.JNBE;
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
                return 0;
            }
        }

        public TablaSimbolo getTablaSimbolos() { return tablaSimbolos; }
        public List<ErrorSintactico> getErrores() { return erroresSemanticos; }
    }

