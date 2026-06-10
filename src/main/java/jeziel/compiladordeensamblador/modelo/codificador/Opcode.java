package jeziel.compiladordeensamblador.modelo.codificador;

public enum Opcode {
    //NI UN OPERANDO
    CBW(0b10011000, Formato.COMPLETO),
    CLC(0b11111000, Formato.COMPLETO),
    LODSB(0b10101010, Formato.COMPLETO),
    LODSW(0b10101101, Formato.COMPLETO),
    STOSB(0b10101010, Formato.COMPLETO),
    STOSW(0b10101011, Formato.COMPLETO),
    //UN OPERANDO
    DIV(0b1111011, Formato.SOLO_W), // w
    IMUL(0b1111011, Formato.SOLO_W), // w
    INC_MEM(0b1111111, Formato.SOLO_W), // w
    INC_REG(0b01000, Formato.REGISTRO), // Los 3 bits del reg van pegados aquí
    NEG(0b111101, Formato.SOLO_W),
    //SALTOS
    JNS_CORTO(0b01111001, Formato.COMPLETO),
    JNS_LARGO(0b0000111110001001, Formato.COMPLETO),
    JS_CORTO(0b01111000, Formato.COMPLETO),
    JS_LARGO(0b0000111110001000, Formato.COMPLETO),
    LOOPNE(0b11100000, Formato.COMPLETO),
    JG_CORTO(0b01111111, Formato.COMPLETO),
    JGG_LARGO(0b0000111110001111, Formato.COMPLETO),
    JMP(0b11101001, Formato.COMPLETO),
    JNBE_CORTO(0b01110110, Formato.COMPLETO),
    JNBE_LARGO(0b0000111110000110, Formato.COMPLETO),
    //DOS OPERANDOS
    LDS(0b11000101, Formato.COMPLETO),
    ADD_REG_REG(0b0000001, Formato.SOLO_W), // d=1 estático, w
    ADD_MEM_REG(0b000000, Formato.D_W), // dw
    ADD_REGMEM_INM(0b100000, Formato.S_W), // sw
    ADD_ACUM_INM(0b0000010, Formato.SOLO_W), //w
    MOV_REGMEM_REGMEM(0b100010, Formato.D_W), //dw
    MOV_REGMEN_INM(0b1100011, Formato.SOLO_W),//w
    MOV_REG_INM(0b1011,Formato.SOLO_W), //w
    MOV_ACUM_MEM(0b1010001, Formato.SOLO_W), //w
    MOV_REGS_REG(0b10001110, Formato.COMPLETO),
    MOV_REG_REGS(0b10001100, Formato.COMPLETO),
    ROR_REGMEM_1(0b1101000, Formato.SOLO_W),
    ROR_REGMEM_CL(0b1101001, Formato.SOLO_W),
    ROR_REGMEM_INM(0b1100000, Formato.SOLO_W);

    public enum Formato {
        COMPLETO,   // Sin modificar
        SOLO_W,     // Requiere: W
        D_W,        // Requiere: DW
        S_W,        // Requiere: SW
        REGISTRO    // Requiere: reg_code
    }

    public final int binarioBase;
    public final Formato formato;

    Opcode(int binarioBase, Formato formato) {
        this.binarioBase = binarioBase;
        this.formato = formato;
    }

}
