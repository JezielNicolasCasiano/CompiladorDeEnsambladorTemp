package jeziel.compiladordeensamblador;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import jeziel.compiladordeensamblador.modelo.lexer.Lexer;
import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.parser.Parser;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;
import jeziel.compiladordeensamblador.modelo.semantico.AnalizadorSemantico;
import jeziel.compiladordeensamblador.modelo.semantico.LineaAnalizadaSemanticamente;
import jeziel.compiladordeensamblador.modelo.codificador.GeneradorCodigo;

import java.util.List;

public class CompilerTest {

    @Test
    public void testJumpsAddressing() {
        List<String> input = List.of(
            ".CODE SEGMENT",
            "START:",
            "    MOV AX, 1",
            "    JMP START",
            "    JS START"
        );

        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertNotNull(tokens);

        Parser parser = new Parser(tokens);
        List<LineaAnalizada> arbolSintactico = parser.parsear();
        assertNotNull(arbolSintactico);

        AnalizadorSemantico analizadorSemantico = new AnalizadorSemantico(arbolSintactico);
        List<LineaAnalizadaSemanticamente> analisisSemantico = analizadorSemantico.analizar();
        List<LineaAnalizadaSemanticamente> tablaDeSimbolos = analizadorSemantico.getTablaDeSimbolos();

        System.out.println("--- TABLA DE SIMBOLOS ---");
        for (LineaAnalizadaSemanticamente sym : tablaDeSimbolos) {
            System.out.println("Simbolo: " + sym.getLineaAnalizada().getTokens().get(0).getValue() + " | Direccion: " + sym.getDireccion());
        }

        System.out.println("--- ANALISIS SEMANTICO ---");
        for (LineaAnalizadaSemanticamente linea : analisisSemantico) {
            System.out.println("Linea: " + linea.getLineaAnalizada().getTokens().get(0).getValue() + " | Direccion: " + linea.getDireccion() + " | Error: " + linea.getErrorSemantico());
        }

        GeneradorCodigo generadorCodigo = new GeneradorCodigo(tablaDeSimbolos, 0x470);
        generadorCodigo.generarParaPrograma(analisisSemantico);

        System.out.println("--- CODIGO MAQUINA ---");
        for (LineaAnalizadaSemanticamente linea : analisisSemantico) {
            System.out.println("Linea: " + linea.getLineaAnalizada().getTokens().get(0).getValue() + " | Direccion: " + linea.getDireccion() + " | Codigo: " + linea.getCodigoMaquina());
        }

        // Verify displacements
        // MOV AX, 1 has size 3. It starts at 0470.
        // JMP START is at 0473. Size is 3. Target is 0470.
        // offset = 0470 - (0473 + 3) = -6 (0xFFFA)
        // Machine code: E9 FA FF -> 11101001 11111010 11111111
        LineaAnalizadaSemanticamente jmpLine = analisisSemantico.get(3);
        assertEquals("11101001 11111010 11111111", jmpLine.getCodigoMaquina());

        // JS START is at 0476. Size is 2. Target is 0470.
        // offset = 0470 - (0476 + 2) = -8 (0xFFF8)
        // Machine code: 78 F8 -> 01111000 11111000
        LineaAnalizadaSemanticamente jsLine = analisisSemantico.get(4);
        assertEquals("01111000 11111000", jsLine.getCodigoMaquina());
    }
}
