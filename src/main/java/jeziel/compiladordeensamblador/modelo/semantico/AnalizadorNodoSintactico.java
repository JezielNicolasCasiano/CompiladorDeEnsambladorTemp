package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

import java.util.List;

public class AnalizadorNodoSintactico {
    private List<LineaAnalizada> arbolSintactico;
    private int lineaSintacticaActual;
    private LineaAnalizadaSemanticamente linea;

    public AnalizadorNodoSintactico(List<LineaAnalizada> arbolSintactico){
        this.arbolSintactico = arbolSintactico;
        this.lineaSintacticaActual = 0;
    }

    public void reset() {
        this.lineaSintacticaActual = 0;
    }

    public LineaAnalizadaSemanticamente analizar(List<LineaAnalizadaSemanticamente> tablaDeSimbolos){
        if (lineaSintacticaActual >= arbolSintactico.size()) {
            return null;
        }

        LineaAnalizada lineaActual = arbolSintactico.get(lineaSintacticaActual);
        linea = new LineaAnalizadaSemanticamente(lineaActual);

        // 1. Si la línea ya tiene un error sintáctico, se agrega tal cual
        if (lineaActual.tieneError()) {
            lineaSintacticaActual++;
            return linea;
        }

        if (lineaActual.getTokens().isEmpty()) {
            lineaSintacticaActual++;
            return linea;
        }

        Token primerToken = lineaActual.getTokens().getFirst();

        // 2. Si es una variable o etiqueta, ya fueron procesadas en buscarSimbolos.
        // Las buscamos y retornamos el DTO pre-analizado.
        if (primerToken.getType() == TokenType.VARIABLE || primerToken.getType() == TokenType.ETIQUETA) {
            for (LineaAnalizadaSemanticamente sym : tablaDeSimbolos) {
                if (sym.getLineaAnalizada() == lineaActual) {
                    lineaSintacticaActual++;
                    return sym;
                }
            }
            // En caso raro de que no se encontrara
            lineaSintacticaActual++;
            return linea;
        }

        // 3. Procesar instrucciones y pseudoinstrucciones
        if (primerToken.getType() == TokenType.INSTRUCCION) {
            AnalizadorSemanticaInstruccion analizadorInst = new AnalizadorSemanticaInstruccion(primerToken, lineaActual, tablaDeSimbolos);
            analizadorInst.analizar();
            linea.setErrorSemantico(analizadorInst.getErrorSemantico());
            linea.setdDeTipoDeDireccionamiento(analizadorInst.getdDeTipoDeDireccionamiento());
        } else if (primerToken.getType() == TokenType.PSEUDOINSTRUCCION) {
            AnalizadorSemanticaPseudoinstruccion analizadorPseudo = new AnalizadorSemanticaPseudoinstruccion(primerToken, lineaActual, tablaDeSimbolos);
            analizadorPseudo.analizar();
            linea.setErrorSemantico(analizadorPseudo.getErrorSemantico());
            linea.setdDeTipoDeDireccionamiento("-");
        } else {
            linea.setdDeTipoDeDireccionamiento("-");
        }

        lineaSintacticaActual++;
        return linea;
    }

    public LineaAnalizadaSemanticamente buscarSimbolos(List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        if (lineaSintacticaActual >= arbolSintactico.size()) {
            return null;
        }

        LineaAnalizada lineaActual = arbolSintactico.get(lineaSintacticaActual);

        if (lineaActual.tieneError() || lineaActual.getTokens().isEmpty()) {
            lineaSintacticaActual++;
            return null;
        }

        Token primerToken = lineaActual.getTokens().getFirst();

        switch (primerToken.getType()) {
            case VARIABLE:
                linea = new LineaAnalizadaSemanticamente(lineaActual);
                AnalizadorSemanticaVariable analizadorVar = new AnalizadorSemanticaVariable(primerToken, lineaActual, tablaDeSimbolos);
                analizadorVar.analizar();
                linea.setErrorSemantico(analizadorVar.getErrorSemantico());
                linea.setdDeTipoDeDireccionamiento("-");

                // Asignar el tamaño al DTO
                if (lineaActual.getTokens().size() > 1) {
                    Token sizeToken = lineaActual.getTokens().get(1);
                    if (sizeToken.getSub() == TokenSubtype.Directiva.DB) {
                        linea.setTamanoInstruccion("BYTE");
                    } else if (sizeToken.getSub() == TokenSubtype.Directiva.DW) {
                        linea.setTamanoInstruccion("WORD");
                    } else if (sizeToken.getSub() == TokenSubtype.Directiva.EQU) {
                        linea.setTamanoInstruccion("EQU");
                    }
                }

                lineaSintacticaActual++;
                return linea;

            case ETIQUETA:
                linea = new LineaAnalizadaSemanticamente(lineaActual);
                AnalizadorSemanticaEtiqueta analizadorEtq = new AnalizadorSemanticaEtiqueta(primerToken, lineaActual, tablaDeSimbolos);
                analizadorEtq.analizar();
                linea.setErrorSemantico(analizadorEtq.getErrorSemantico());
                linea.setdDeTipoDeDireccionamiento("-");
                linea.setTamanoInstruccion("-");

                lineaSintacticaActual++;
                return linea;

            default:
                lineaSintacticaActual++;
                return null;
        }
    }

    public int getLineaSintacticaActual() {
        return lineaSintacticaActual;
    }
}
