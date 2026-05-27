package compilador8086;

import java.util.ArrayList;
import java.util.List;

public class NodoAST {

    public enum Tipo {
        PROGRAMA,
        ETIQUETA,
        INSTRUCCION,
        DIRECTIVA,
        OPERANDO_REGISTRO,
        OPERANDO_CONSTANTE,
        OPERANDO_VARIABLE,
        OPERANDO_MEMORIA,
        OPERANDO_CARACTER,
        OPERANDO_CADENA
    }

    private final Tipo tipo;
    private final Token token;
    private final List<NodoAST> hijos;

    public NodoAST(Tipo tipo, Token token) {
        this.tipo = tipo;
        this.token = token;
        this.hijos = new ArrayList<>();
    }

    public void agregarHijo(NodoAST hijo) {
        if (hijo != null) hijos.add(hijo);
    }

    public Tipo getTipo()          { return tipo; }
    public Token getToken()        { return token; }
    public List<NodoAST> getHijos(){ return hijos; }

    @Override
    public String toString() {
        return tipo + (token != null ? "(" + token.getValue() + ")" : "");
    }
}
