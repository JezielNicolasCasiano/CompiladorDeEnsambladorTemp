package jeziel.compiladordeensamblador.modelo.lexer;

public class Token {

        private TokenType type;
        private String value;
        private Enum<?> subtype;
        private final int linea;

        public Token(TokenType type, String value, int linea) {
            this.type = type;
            this.value = value;
            this.subtype = null;
            this.linea = linea;
        }

        public Token(TokenType type, String value, Enum<?> subtype, int linea) {
            this.type = type;
            this.value = value;
            this.subtype = subtype;
            this.linea = linea;
        }

        public int getLinea() { return linea; }


    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Enum<?> getSub() {
        return subtype;
    }
    public void setValue(String value){
        this.value = value;
    }

    public void setSubtype(Enum<?> subtype) {
        this.subtype = subtype;
    }

    @Override
    public String toString() {
        /*return "Token{type=" + type
                + ", value='" + value + "'"
                + (subtype != null ? ", subtype=" + subtype : "")
                + "}"; */ //Debugger

        return  value + "         ;" + type;

    }

}