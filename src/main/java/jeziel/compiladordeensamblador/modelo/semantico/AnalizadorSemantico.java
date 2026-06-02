package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.NodoAST;
import jeziel.compiladordeensamblador.modelo.parser.ResultadoParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalizadorSemantico {
    private  ResultadoParser resultadoParser;
    private ContextoSemantico contextoSemantico;
    private List<NodoAST> arbolSintactico;



    public AnalizadorSemantico(ResultadoParser resultadoParser){
        this.resultadoParser = resultadoParser;
        this.contextoSemantico = new ContextoSemantico();
        this.arbolSintactico = resultadoParser.getArbol();


    }

    public ResultadoSemantico analizarSemantica(){
        for (NodoAST nodoInicial : arbolSintactico){
            buscarSimbolo(nodoInicial);
        }
        for (NodoAST nodoRaiz : arbolSintactico){
            analizarNodo(nodoRaiz);
        }
        return new ResultadoSemantico(contextoSemantico.getTablaSimbolos(), contextoSemantico.getErrores());
    }

    public void analizarNodo(NodoAST nodo){
        if(nodo == null) return;
        switch (nodo.getTipo()) {
            case INSTRUCCION:
                ValidadorInstrucciones.validar(nodo, contextoSemantico,arbolSintactico);
                break;
            case DIRECTIVA:
                ValidadorDirectivas.validar(nodo, contextoSemantico, arbolSintactico);
                break;
            default:
                break;
        }
    }
    public void buscarSimbolo(NodoAST nodo) {
        if (nodo == null) return;

        if (nodo.getTipo() == NodoAST.Tipo.DIRECTIVA && !nodo.getHijos().isEmpty() &&
                nodo.getHijos().get(nodo.getHijos().size() - 1).getToken().getType() == TokenType.VARIABLE) {

            Token tokenVarible = nodo.getHijos().get(nodo.getHijos().size() - 1).getToken();

            if (contextoSemantico.getTablaSimbolos().containsKey(tokenVarible.getValue())) {
                contextoSemantico.registrarError(new ErrorSemantico(nodo.getToken(),
                        "El identificador ya existe: '" + tokenVarible.getValue() + "'", arbolSintactico.indexOf(nodo)));
                return;
            }

            int tamanoSimbolo = (nodo.getToken().getSub() == TokenSubtype.Directiva.DB) ? 1 : 2;
            int tamanoTotal = 0;
            String valorSimbolo = "-";

            NodoAST primerHijo = nodo.getHijos().get(0);
            if (primerHijo.getTipo() != NodoAST.Tipo.OPERANDO_VARIABLE && primerHijo.getToken() != null) {
                valorSimbolo = primerHijo.getToken().getValue();
            }

            for (int i = 0; i < nodo.getHijos().size() - 1; i++) {
                NodoAST hijo = nodo.getHijos().get(i);
                if (hijo.getTipo() == NodoAST.Tipo.OPERANDO_CADENA || hijo.getTipo() == NodoAST.Tipo.OPERANDO_CARACTER) {
                    String texto = hijo.getToken().getValue().replaceAll("^[\"']|[\"']$", "");
                    tamanoTotal += texto.length();
                } else {
                    tamanoTotal += tamanoSimbolo;
                }
            }

            contextoSemantico.getTablaSimbolos().put(tokenVarible.getValue(),
                    new Simbolo(tokenVarible.getValue(), Simbolo.TipoSext.VARIABLE, tamanoTotal, 0, valorSimbolo));

        } else if (nodo.getTipo() == NodoAST.Tipo.ETIQUETA) {
            String nombreEtiq = nodo.getToken().getValue().replace(":", "");
            if (contextoSemantico.getTablaSimbolos().containsKey(nombreEtiq)) {
                contextoSemantico.registrarError(new ErrorSemantico(nodo.getToken(),
                        "El identificador ya existe: '" + nombreEtiq + "'", arbolSintactico.indexOf(nodo)));
            } else {
                contextoSemantico.getTablaSimbolos().put(nombreEtiq,
                        new Simbolo(nombreEtiq, Simbolo.TipoSext.ETIQUETA, 0, 0, "-"));
            }
        }
    }
}


