package jeziel.compiladordeensamblador.modelo.parser;
import jeziel.compiladordeensamblador.modelo.lexer.*;
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
        OPERANDO_CADENA,
        OPERANDO_OFFSET,
        ERROR_LEXICO,
        ERROR_SINTACTICO
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

    //metodo para reconstruir desde el nodo

// En NodoAST.java
    public String reconstruirTexto() {
        if (this.tipo == null) return "";

        switch (this.tipo) {
            case INSTRUCCION:
                StringBuilder sbInst = new StringBuilder();
                sbInst.append(this.token.getValue());

                if (!this.hijos.isEmpty()) {
                    sbInst.append(" ");
                    for (int i = 0; i < this.hijos.size(); i++) {
                        sbInst.append(this.hijos.get(i).reconstruirTexto());
                        if (i < this.hijos.size() - 1) {
                            sbInst.append(", ");
                        }
                    }
                }
                return sbInst.toString();

            case DIRECTIVA:
                StringBuilder sbDir = new StringBuilder();
                if (!this.hijos.isEmpty() && this.hijos.get(this.hijos.size() - 1).getTipo() == Tipo.OPERANDO_VARIABLE) {
                    NodoAST nodoVariable = this.hijos.get(this.hijos.size() - 1);
                    sbDir.append(nodoVariable.reconstruirTexto()).append(" ");
                    sbDir.append(this.token.getValue());

                    if (this.hijos.size() > 1) {
                        sbDir.append(" ");
                        for (int i = 0; i < this.hijos.size() - 1; i++) {
                            sbDir.append(this.hijos.get(i).reconstruirTexto());
                            if (i < this.hijos.size() - 2) {
                                sbDir.append(", ");
                            }
                        }
                    }
                } else {
                    sbDir.append(this.token.getValue());
                    if (!this.hijos.isEmpty()) {
                        sbDir.append(" ");
                        for (int i = 0; i < this.hijos.size(); i++) {
                            sbDir.append(this.hijos.get(i).reconstruirTexto());
                            if (i < this.hijos.size() - 1) sbDir.append(", ");
                        }
                    }
                }
                return sbDir.toString();

            case ETIQUETA:
                StringBuilder sbEtiq = new StringBuilder();
                sbEtiq.append(this.token.getValue());
                for (NodoAST hijo : this.hijos) {
                    sbEtiq.append(" ").append(hijo.reconstruirTexto());
                }
                return sbEtiq.toString();

            case OPERANDO_MEMORIA:
                StringBuilder sbMem = new StringBuilder();
                sbMem.append("[");
                for (int i = 0; i < this.hijos.size(); i++) {
                    sbMem.append(this.hijos.get(i).reconstruirTexto());
                }
                sbMem.append("]");
                return sbMem.toString();

            case OPERANDO_REGISTRO:
            case OPERANDO_CONSTANTE:
            case OPERANDO_VARIABLE:
            case OPERANDO_CARACTER:
            case OPERANDO_CADENA:
            case ERROR_LEXICO:
            case ERROR_SINTACTICO:
                if (!this.hijos.isEmpty()) {
                    StringBuilder sbHijos = new StringBuilder();
                    for (NodoAST hijo : this.hijos) {
                        sbHijos.append(hijo.reconstruirTexto()).append(" ");
                    }
                    return sbHijos.toString().trim();
                }
                return this.token != null ? this.token.getValue() : "";

            default:
                return this.token != null ? this.token.getValue() : "";
        }
    }
}
