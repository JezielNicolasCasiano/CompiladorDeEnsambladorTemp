package jeziel.compiladordeensamblador.modelo.codificador;

public enum Registros {
    //WORD
    AX(0b000),
    CX(0b001),
    DX(0b010),
    BX(0b011),
    SP(0b100),
    BP(0b101),
    SI(0b110),
    DI(0b111),
    // BYTE
    AL(0b000),
    CL(0b001),
    DL(0b010),
    BL(0b011),
    AH(0b100),
    CH(0b101),
    DH(0b110),
    BH(0b111),
    // SEGMENTO 1
    ES_2(0b00),
    CS_2(0b01),
    SS_2(0b10),
    DS_2(0b11),
    // SEGMENTO 2
    ES_3(0b000),
    CS_3(0b001),
    SS_3(0b010),
    DS_3(0b011);

    public final int binarioRegistro;

    Registros(int binarioRegistro) {
        this.binarioRegistro = binarioRegistro;
    }
}
