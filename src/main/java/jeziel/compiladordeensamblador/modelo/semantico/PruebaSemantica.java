package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Lexer;
import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;
import jeziel.compiladordeensamblador.modelo.parser.Parser;

import java.util.Arrays;
import java.util.List;

public class PruebaSemantica {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBAS DE ANALISIS SEMANTICO ===");

        // Creamos un código de ensamblador de prueba con errores semánticos variados
        List<String> lineasCodigo = Arrays.asList(
            "VAR_B DB 5",                // Correcto: variable byte
            "VAR_W DW 300",              // Correcto: variable word
            "VAR_DUP DB DUP(0)",         // Correcto: variable byte con DUP
            "VAR_ERR DB 300",            // Error semántico: constante 300 excede BYTE (0-255)
            "VAR_B DB 10",               // Error semántico: símbolo duplicado VAR_B
            "START:",                    // Correcto: etiqueta
            "START:",                    // Error semántico: etiqueta duplicada START
            "MOV AX, BX",                // Correcto: registro-registro, d = 1
            "MOV AL, VAR_B",             // Correcto: registro(8)-memoria(8), d = 1
            "MOV VAR_W, DX",             // Correcto: memoria(16)-registro(16), d = 0
            "MOV AL, VAR_W",             // Error semántico: conflicto de tamaños (8 bits vs 16 bits)
            "MOV VAR_B, VAR_W",          // Error semántico: no se permite memoria a memoria
            "MOV CS, AX",                // Error semántico: CS no puede ser destino
            "MOV DS, 10",                // Error semántico: no se permite mover constante a registro de segmento
            "MOV DS, ES",                // Error semántico: no se permite mover entre registros de segmento
            "ADD AX, 70000",             // Error semántico: constante excede el tamaño de destino (16 bits, max 65535)
            "LDS AX, VAR_W",             // Correcto: LDS, d = 1
            "LDS AL, VAR_W",             // Error semántico: LDS primer operando debe ser registro de 16 bits
            "ROR AX, 1",                 // Correcto: ROR con 1, d = -
            "ROR AX, 5",                 // Error semántico: ROR segundo operando debe ser 1 o CL
            "JMP START",                 // Correcto: Salto a etiqueta definida
            "JMP NO_EXISTE"              // Error semántico: Símbolo de salto no definido
        );

        System.out.println("\nCódigo a analizar:");
        for (int i = 0; i < lineasCodigo.size(); i++) {
            System.out.printf("%2d: %s\n", i + 1, lineasCodigo.get(i));
        }

        // Tokenización
        Lexer lexer = new Lexer(lineasCodigo);
        List<Token> tokens = lexer.tokenize();

        // Parseo Sintáctico
        Parser parser = new Parser(tokens);
        List<LineaAnalizada> arbolSintactico = parser.parsear();

        // Análisis Semántico
        AnalizadorSemantico analizadorSemantico = new AnalizadorSemantico(arbolSintactico);
        List<LineaAnalizadaSemanticamente> analisis = analizadorSemantico.analizar();

        System.out.println("\n=== RESULTADOS DEL ANALISIS SEMANTICO ===");
        for (LineaAnalizadaSemanticamente lineaSem : analisis) {
            LineaAnalizada la = lineaSem.getLineaAnalizada();
            String tokensStr = la.getTokens().stream()
                .map(t -> t.getValue() + " (" + t.getType() + ")")
                .reduce((t1, t2) -> t1 + ", " + t2).orElse("");

            System.out.printf("\nLínea %d: %s\n", la.getNumeroLinea(), tokensStr);
            if (lineaSem.getTamanoInstruccion() != null && !lineaSem.getTamanoInstruccion().equals("-")) {
                System.out.println("  Tamaño: " + lineaSem.getTamanoInstruccion());
            }
            if (lineaSem.getdDeTipoDeDireccionamiento() != null) {
                System.out.println("  d (Direccionamiento): " + lineaSem.getdDeTipoDeDireccionamiento());
            }

            if (la.tieneError()) {
                System.out.println("  [ERROR SINTACTICO]: " + la.getErrorSintactico().getMensajeError());
            } else if (lineaSem.getErrorSemantico() != null) {
                System.out.println("  [ERROR SEMANTICO]: " + lineaSem.getErrorSemantico().getMensajeError());
            } else {
                System.out.println("  [OK]");
            }
        }

        System.out.println("\n=== TABLA DE SIMBOLOS EXTRAIDA ===");
        for (LineaAnalizadaSemanticamente sym : analizadorSemantico.getTablaDeSimbolos()) {
            Token primerToken = sym.getLineaAnalizada().getTokens().getFirst();
            System.out.printf("  Simbolo: %-10s | Tipo: %-5s | Linea: %d\n",
                primerToken.getValue(),
                sym.getTamanoInstruccion(),
                sym.getLineaAnalizada().getNumeroLinea()
            );
        }
    }
}
