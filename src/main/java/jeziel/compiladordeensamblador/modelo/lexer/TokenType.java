package jeziel.compiladordeensamblador.modelo.lexer;


public enum TokenType {
    // Instrucciones
    INSTRUCCION,
    PSEUDOINSTRUCCION,
    CONSTANTE,
    COMILLA,
    VARIABLE,//PseudoInstruccion
    // Ensamblador
    REGISTRO,
    IDENTIFICADOR,
    ETIQUETA,
    CARACTER,
    COMENTARIO,
    INTERRUPCION,
    // Símbolos
    SEPARADOR,        // ,
    CORCHETE_ABRE,    // [
    CORCHETE_CIERRA,  // ]
    PARENTESIS_ABRE,  // (
    PARENTESIS_CIERRA, // )
    DESCONOCIDO,
    CADENA
}