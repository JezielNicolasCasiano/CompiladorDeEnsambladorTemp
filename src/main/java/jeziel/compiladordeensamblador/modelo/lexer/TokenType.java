package jeziel.compiladordeensamblador.modelo.lexer;


public enum TokenType {
    // Instrucciones
    INSTRUCCION,
    DIRECTIVA, //PseudoInstruccion
    // Ensamblador
    REGISTRO,
    IDENTIFICADOR,
    ETIQUETA,
    NUMERO,
    CARACTER,
    COMENTARIO,
    INTERRUPCION,
    // Símbolos
    SEPARADOR,        // ,
    CORCHETE_ABRE,    // [
    CORCHETE_CIERRA,  // ]
    PARENTESIS_ABRE,  // (
    PARENTESIS_CIERRA // )
}