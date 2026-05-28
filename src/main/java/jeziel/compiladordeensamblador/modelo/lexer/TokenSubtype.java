package jeziel.compiladordeensamblador.modelo.lexer;

public class TokenSubtype {
    public enum Instruccion {
        CBW,CLC,LODSB,LODSW,STOSB,STOSW,DIV,IMUL,INC,NEG,ADD,LDS,MOV,ROR,JNS,JS,LOOPNE,JG,JMP,JNBE, INT
    }
    public enum Directiva {
        ORG,
        END,
        DB, DW,
        EQU,
        SEGMENT, ENDS,
        // Segmentos
        STACK,
        DATA,
        CODE,
        // ??
        DUP,
        BYTE_PTR,
        WORD_PTR,
        // Macros
        MACRO,
        ENDM,
        // Procedimientos
        PROC,
        ENDP,
        //
        OFFSET,
        STACK_SEGMENT, DATA_SEGMENT, CODE_SEGMENT,
        MODEL, STARTUP, EXIT
    }
    public enum Registro {
        // 16 bits
        AX, BX, CX, DX,
        SI, DI, BP, SP,
        // 8 bits
        AH, AL,
        BH, BL,
        CH, CL,
        DH, DL,
        // Segmento
        CS, DS, SS, ES
    }

    public enum Constante {
        DECIMAL,
        HEXADECIMAL,
        BINARIO
    }

}