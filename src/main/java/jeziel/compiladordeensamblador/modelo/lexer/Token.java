package jeziel.compiladordeensamblador.modelo.lexer;

public class Token {

    private TokenType type;
    private String value;
    private Enum<?> subtype;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
        this.subtype = null;
    }

    public Token(TokenType type, String value, Enum<?> subtype) {
        this.type = type;
        this.value = value;
        this.subtype = subtype;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Enum<?> getSub() {
        return subtype;
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

        return  value + "         ;" + (subtype != null ? subtype : type);

    }

}