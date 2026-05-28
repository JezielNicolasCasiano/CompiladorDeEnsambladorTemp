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
    public void buscarSimbolo(NodoAST nodo){
        if(nodo == null) return;

        if(nodo.getTipo() == NodoAST.Tipo.DIRECTIVA && nodo.getHijos().get(nodo.getHijos().size()-1).getToken().getType() == TokenType.VARIABLE){
            Token tokenVarible = nodo.getHijos().get(nodo.getHijos().size()-1).getToken();
            int tamanoSimbolo = 0;
            if (nodo.getHijos().get(0).getToken().getSub() == TokenSubtype.Directiva.DB){
                tamanoSimbolo = 1;
            } else if (nodo.getHijos().get(0).getToken().getSub() == TokenSubtype.Directiva.DW) {
                tamanoSimbolo = 2;
            } else if (contextoSemantico.getTablaSimbolos().containsKey(nodo.getHijos().get(nodo.getHijos().size()-1).getToken().getValue())) {
                contextoSemantico.registrarError(new ErrorSemantico(nodo.getToken(), "El identificador ya existe: '" + nodo.getHijos().get(nodo.getHijos().size()-1).getToken().getValue() + "'", arbolSintactico.indexOf(nodo)));
                return;
            }
            int tamanoTotal = (nodo.getHijos().size()-1) * tamanoSimbolo;
            contextoSemantico.getTablaSimbolos().put(tokenVarible.getValue(),new Simbolo(tokenVarible.getValue(), Simbolo.TipoSext.VARIABLE,tamanoTotal,0));
        } else if (nodo.getTipo()== NodoAST.Tipo.ETIQUETA) {
            if (contextoSemantico.getTablaSimbolos().containsKey(nodo.getToken().getValue().replace(":", ""))) {
                contextoSemantico.registrarError(new ErrorSemantico(nodo.getToken(), "El identificador ya existe: '" + nodo.getToken().getValue().replace(":", "") + "'", arbolSintactico.indexOf(nodo)));
            } else {
                contextoSemantico.getTablaSimbolos().put(nodo.getToken().getValue().replace(":", ""), new Simbolo(nodo.getToken().getValue(), Simbolo.TipoSext.ETIQUETA, 0, 0));
            }
        }
    }
}


