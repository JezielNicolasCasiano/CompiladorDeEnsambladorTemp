package jeziel.compiladordeensamblador.modelo.codificador;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;
import jeziel.compiladordeensamblador.modelo.semantico.LineaAnalizadaSemanticamente;

import java.util.ArrayList;
import java.util.List;

import static jeziel.compiladordeensamblador.modelo.codificador.HelperGenerador.*;

public class GeneradorCodigo {

    private final List<LineaAnalizadaSemanticamente> tablaDeSimbolos;
    private int direccionActual;
    private LineaAnalizadaSemanticamente lineaActual;

    public GeneradorCodigo(List<LineaAnalizadaSemanticamente> tablaDeSimbolos, int direccionInicio) {
        this.tablaDeSimbolos = tablaDeSimbolos;
        this.direccionActual = direccionInicio;
    }

    // Método helper para asociar código máquina generado a cada línea analizada semánticamente
    public void generarParaPrograma(List<LineaAnalizadaSemanticamente> analisisSemantico) {
        for (LineaAnalizadaSemanticamente lineaSem : analisisSemantico) {
            if (lineaSem.getLineaAnalizada() != null && !lineaSem.getLineaAnalizada().tieneError() && lineaSem.getErrorSemantico() == null) {
                List<Token> tokens = lineaSem.getLineaAnalizada().getTokens();
                lineaActual = lineaSem;
                if (tokens == null || tokens.isEmpty()) {
                    continue;
                }

                // Set current address based on the parsed line address
                this.direccionActual = HelperGenerador.parsearDireccionSemantica(lineaSem.getDireccion());

                Token primerToken = tokens.get(0);
                String binario = null;

                if (primerToken.getSub() instanceof TokenSubtype.Instruccion) {
                    binario = generarInstruccion(tokens);
                }
                else if (primerToken.getType() == TokenType.ETIQUETA) {
                    List<Token> subTokens = tokens.subList(1, tokens.size());
                    if (!subTokens.isEmpty() && subTokens.get(0).getSub() instanceof TokenSubtype.Instruccion) {
                        binario = generarInstruccion(subTokens);
                    }
                }

                if (binario != null) {
                    lineaSem.setCodigoMaquina(binario);
                }
            }
        }
    }

    // Entrada principal
    public List<String> generar(List<LineaAnalizada> programaAnalizado) throws Exception {
        List<String> resultado = new ArrayList<>();

        for (LineaAnalizada linea : programaAnalizado) {

            if (linea.tieneError()) {
                throw new Exception("Error sintáctico en la línea " + linea.getNumeroLinea()
                        + ": " + linea.getErrorSintactico());
            }

            List<Token> tokens = linea.getTokens();
            if (tokens == null || tokens.isEmpty()) {
                continue;
            }

            Token primerToken = tokens.get(0);
            String binario = null;

            if (primerToken.getSub() instanceof TokenSubtype.Instruccion) {
                binario = generarInstruccion(tokens);
            }
            else if (primerToken.getType() == TokenType.ETIQUETA) {
                List<Token> subTokens = tokens.subList(1, tokens.size());
                if (!subTokens.isEmpty() && subTokens.get(0).getSub() instanceof TokenSubtype.Instruccion) {
                    binario = generarInstruccion(subTokens);
                }
            }

            if (binario != null) {
                resultado.add(binario);
                direccionActual += contarBytes(binario);
            }
        }
        return resultado;
    }

    private String generarInstruccion(List<Token> tokens) {
        Token tInstruccion = tokens.get(0);
        TokenSubtype.Instruccion subtipo = (TokenSubtype.Instruccion) tInstruccion.getSub();
        if (subtipo == null) return null;

        /*Token dest;
        if(subtipo.toString().toUpperCase().startsWith("J")){
                 dest = tablaDeSimbolos.get(tablaDeSimbolos.indexOf(lineaActual)).getLineaAnalizada().getTokens().get(1);
        } else {
             dest = tokens.size() > 1 ? tokens.get(1) : null;
        }*/
        Token dest = tokens.size() > 1 ? tokens.get(1) : null;


        int indiceFuente = (tokens.size() > 2 && tokens.get(2).getType() == TokenType.SEPARADOR) ? 3 : 2;
        Token fuente = tokens.size() > indiceFuente ? tokens.get(indiceFuente) : null;

        switch (subtipo) {
            case MOV:    return codificarMOV(dest, fuente);
            case ADD:    return codificarADD(dest, fuente);
            case INC:    return codificarINC(dest);
            case NEG:    return codificarNEG(dest);
            case DIV:    return codificarDIV(dest);
            case IMUL:   return codificarIMUL(dest);
            case ROR:    return codificarROR(dest, fuente);
            case LDS:    return codificarLDS(dest, fuente);
            case JMP:    return codificarJMP(dest);
            case INT:    return codificarINT(dest);
            case JNS:    return codificarSaltoCorto(Opcode.JNS_CORTO, dest);
            case JS:     return codificarSaltoCorto(Opcode.JS_CORTO, dest);
            case JG:     return codificarSaltoCorto(Opcode.JG_CORTO, dest);
            case JNBE:   return codificarSaltoCorto(Opcode.JNBE_CORTO, dest);
            case LOOPNE: return codificarSaltoCorto(Opcode.LOOPNE, dest);
            case CBW:    return toBin8(Opcode.CBW.binarioBase);
            case CLC:    return toBin8(Opcode.CLC.binarioBase);
            case LODSB:  return toBin8(Opcode.LODSB.binarioBase);
            case LODSW:  return toBin8(Opcode.LODSW.binarioBase);
            case STOSB:  return toBin8(Opcode.STOSB.binarioBase);
            case STOSW:  return toBin8(Opcode.STOSW.binarioBase);
            default:     return null;
        }
    }
    // Métodos de codificación
    private String codificarMOV(Token dest, Token fuente) {
        if (esReg16(dest) && esReg16(fuente)) {
            int opcode = construirOpcode(Opcode.MOV_REGMEM_REGMEM, 1, 1);
            int modrm  = modRM(MOD_REG, codigoReg(dest), codigoReg(fuente));
            return toBin8(opcode) + " " + toBin8(modrm);
        }
        if (esReg8(dest) && esReg8(fuente)) {
            int opcode = construirOpcode(Opcode.MOV_REGMEM_REGMEM, 1, 0);
            int modrm  = modRM(MOD_REG, codigoReg(dest), codigoReg(fuente));
            return toBin8(opcode) + " " + toBin8(modrm);
        }
        if (esReg16(dest) && esInmediato(fuente)) {
            int opcode = (Opcode.MOV_REG_INM.binarioBase << 4) | (1 << 3) | codigoReg(dest);
            int valor  = parsearConstante(fuente.getValue());
            return toBin8(opcode) + " " + toBin8(valor & 0xFF) + " " + toBin8((valor >> 8) & 0xFF);
        }
        if (esReg8(dest) && esInmediato(fuente)) {
            int opcode = (Opcode.MOV_REG_INM.binarioBase << 4) | (0 << 3) | codigoReg(dest);
            int valor  = parsearConstante(fuente.getValue());
            return toBin8(opcode) + " " + toBin8(valor & 0xFF);
        }
        if (esReg16(dest) && esMemoria(fuente)) {
            int opcode = construirOpcode(Opcode.MOV_REGMEM_REGMEM, 1, 1);
            int rm     = codigoMemRM(fuente);
            int modrm  = modRM(MOD_MEM, codigoReg(dest), rm);
            return toBin8(opcode) + " " + toBin8(modrm) + dispSiNecesario(fuente, rm, tablaDeSimbolos);
        }
        if (esReg8(dest) && esMemoria(fuente)) {
            int opcode = construirOpcode(Opcode.MOV_REGMEM_REGMEM, 1, 0);
            int rm     = codigoMemRM(fuente);
            int modrm  = modRM(MOD_MEM, codigoReg(dest), rm);
            return toBin8(opcode) + " " + toBin8(modrm) + dispSiNecesario(fuente, rm, tablaDeSimbolos);
        }
        if (esMemoria(dest) && esReg16(fuente)) {
            int opcode = construirOpcode(Opcode.MOV_REGMEM_REGMEM, 0, 1);
            int rm     = codigoMemRM(dest);
            int modrm  = modRM(MOD_MEM, codigoReg(fuente), rm);
            return toBin8(opcode) + " " + toBin8(modrm) + dispSiNecesario(dest, rm, tablaDeSimbolos);
        }
        if (esMemoria(dest) && esReg8(fuente)) {
            int opcode = construirOpcode(Opcode.MOV_REGMEM_REGMEM, 0, 0);
            int rm     = codigoMemRM(dest);
            int modrm  = modRM(MOD_MEM, codigoReg(fuente), rm);
            return toBin8(opcode) + " " + toBin8(modrm) + dispSiNecesario(dest, rm, tablaDeSimbolos);
        }
        if (esMemoria(dest) && esInmediato(fuente)) {
            int opcode = construirOpcode(Opcode.MOV_REGMEN_INM, 1);
            int rm     = codigoMemRM(dest);
            int modrm  = modRM(MOD_MEM, 0b000, rm);
            int valor  = parsearConstante(fuente.getValue());
            return toBin8(opcode) + " " + toBin8(modrm)
                    + dispSiNecesario(dest, rm, tablaDeSimbolos)
                    + " " + toBin8(valor & 0xFF) + " " + toBin8((valor >> 8) & 0xFF);
        }
        return "ERROR: combinacion MOV no soportada";
    }

    private String codificarADD(Token dest, Token fuente) {
        if (esReg16(dest) && esReg16(fuente)) {
            int opcode = construirOpcode(Opcode.ADD_REG_REG, 1);
            int modrm  = modRM(MOD_REG, codigoReg(dest), codigoReg(fuente));
            return toBin8(opcode) + " " + toBin8(modrm);
        }
        if (esReg8(dest) && esReg8(fuente)) {
            int opcode = construirOpcode(Opcode.ADD_REG_REG, 0);
            int modrm  = modRM(MOD_REG, codigoReg(dest), codigoReg(fuente));
            return toBin8(opcode) + " " + toBin8(modrm);
        }
        if ((esReg16(dest) || esReg8(dest)) && esInmediato(fuente)) {
            int w     = esReg16(dest) ? 1 : 0;
            int valor = parsearConstante(fuente.getValue());
            int modrm = modRM(MOD_REG, 0b000, codigoReg(dest));
            if (w == 1 && valor >= -128 && valor <= 127) {
                int opcode = construirOpcode(Opcode.ADD_REGMEM_INM, 1, 1);
                return toBin8(opcode) + " " + toBin8(modrm) + " " + toBin8(valor & 0xFF);
            }
            int opcode = construirOpcode(Opcode.ADD_REGMEM_INM, 0, w);
            return w == 1
                    ? toBin8(opcode) + " " + toBin8(modrm) + " " + toBin8(valor & 0xFF) + " " + toBin8((valor >> 8) & 0xFF)
                    : toBin8(opcode) + " " + toBin8(modrm) + " " + toBin8(valor & 0xFF);
        }
        if (esReg16(dest) && esMemoria(fuente)) {
            int opcode = construirOpcode(Opcode.ADD_MEM_REG, 1, 1);
            int rm     = codigoMemRM(fuente);
            int modrm  = modRM(MOD_MEM, codigoReg(dest), rm);
            return toBin8(opcode) + " " + toBin8(modrm) + dispSiNecesario(fuente, rm, tablaDeSimbolos);
        }
        if (esMemoria(dest) && esReg16(fuente)) {
            int opcode = construirOpcode(Opcode.ADD_MEM_REG, 0, 1);
            int rm     = codigoMemRM(dest);
            int modrm  = modRM(MOD_MEM, codigoReg(fuente), rm);
            return toBin8(opcode) + " " + toBin8(modrm) + dispSiNecesario(dest, rm, tablaDeSimbolos);
        }
        return "ERROR: combinacion ADD no soportada";
    }

    private String codificarINC(Token dest) {
        if (esReg16(dest)) {
            int opcode = (Opcode.INC_REG.binarioBase << 3) | codigoReg(dest);
            return toBin8(opcode);
        }
        int w      = esReg8(dest) ? 0 : 1;
        int opcode = construirOpcode(Opcode.INC_MEM, w);
        int rm     = esRegistro(dest) ? codigoReg(dest) : codigoMemRM(dest);
        int mod    = esRegistro(dest) ? MOD_REG : MOD_MEM;
        return toBin8(opcode) + " " + toBin8(modRM(mod, 0b000, rm)) + dispSiNecesario(dest, rm, tablaDeSimbolos);
    }

    private String codificarNEG(Token dest) { return codificarUnario(dest, Opcode.NEG, 0b011); }
    private String codificarDIV(Token dest) { return codificarUnario(dest, Opcode.DIV, 0b110); }
    private String codificarIMUL(Token dest) { return codificarUnario(dest, Opcode.IMUL, 0b101); }

    private String codificarUnario(Token dest, Opcode op, int extension) {
        int w      = esReg16(dest) ? 1 : 0;
        int opcode = construirOpcode(op, w);
        int rm     = esRegistro(dest) ? codigoReg(dest) : codigoMemRM(dest);
        int mod    = esRegistro(dest) ? MOD_REG : MOD_MEM;
        return toBin8(opcode) + " " + toBin8(modRM(mod, extension, rm)) + dispSiNecesario(dest, rm, tablaDeSimbolos);
    }

    private String codificarROR(Token dest, Token fuente) {
        int w      = esReg16(dest) ? 1 : 0;
        int rm     = esRegistro(dest) ? codigoReg(dest) : codigoMemRM(dest);
        int mod    = esRegistro(dest) ? MOD_REG : MOD_MEM;
        boolean esCL = esRegistro(fuente) && fuente.getValue().equalsIgnoreCase("CL");
        int opcode = esCL ? construirOpcode(Opcode.ROR_REGMEM_CL, w) : construirOpcode(Opcode.ROR_REGMEM_1, w);
        return toBin8(opcode) + " " + toBin8(modRM(mod, 0b001, rm)) + dispSiNecesario(dest, rm, tablaDeSimbolos);
    }

    private String codificarLDS(Token dest, Token fuente) {
        int rm    = codigoMemRM(fuente);
        int modrm = modRM(MOD_MEM, codigoReg(dest), rm);
        return toBin8(Opcode.LDS.binarioBase) + " " + toBin8(modrm) + dispSiNecesario(fuente, rm, tablaDeSimbolos);
    }

    private String codificarJMP(Token dest) {
        int dispCorto = calcularDesplazamiento(dest, 2, direccionActual, tablaDeSimbolos);
        int dispLargo = calcularDesplazamiento(dest, 3, direccionActual, tablaDeSimbolos);
        return toBin8(Opcode.JMP.binarioBase) + " " + toBin8(dispLargo & 0xFF) + " " + toBin8((dispLargo >> 8) & 0xFF);
    }

    private String codificarSaltoCorto(Opcode opcode, Token dest) {
        int disp = calcularDesplazamiento(dest, 2, direccionActual, tablaDeSimbolos);
        return toBin8(opcode.binarioBase) + " " + toBin8(disp & 0xFF);
    }

    private String codificarINT(Token dest) {
        if (dest == null) return "ERROR: Falta operando para INT";
        int vector = parsearConstante(dest.getValue());
        return toBin8(0xCD) + " " + toBin8(vector & 0xFF);
    }
}
