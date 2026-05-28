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
        //switch para redireccionar a metodos especificos que a su vez redireccionen a clases especicializadas

    }
    public void buscarSimbolo(NodoAST nodo){
        if(nodo == null) return;
        Token tokenVarible = nodo.getHijos().get(nodo.getHijos().size()-1).getToken();
        if(nodo.getTipo() == NodoAST.Tipo.DIRECTIVA && tokenVarible.getType() == TokenType.VARIABLE){
            int tamanoSimbolo = 0;
            if (nodo.getHijos().get(0).getToken().getSub() == TokenSubtype.Directiva.DB){
                tamanoSimbolo = 8;
            } else if (nodo.getHijos().get(0).getToken().getSub() == TokenSubtype.Directiva.DW) {
                tamanoSimbolo = 16;
            }
            contextoSemantico.getTablaSimbolos().put(tokenVarible.getValue(),new Simbolo(tokenVarible.getValue(), Simbolo.TipoSext.VARIABLE,tamanoSimbolo,0));
        } else if (nodo.getTipo()== NodoAST.Tipo.ETIQUETA) {
            contextoSemantico.getTablaSimbolos().put(nodo.getToken().getValue().replace(":",""),new Simbolo(nodo.getToken().getValue(), Simbolo.TipoSext.ETIQUETA,0,0));
        }
    }
}


