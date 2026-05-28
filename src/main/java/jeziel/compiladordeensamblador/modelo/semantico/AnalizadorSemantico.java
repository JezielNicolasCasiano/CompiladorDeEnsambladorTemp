package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.parser.NodoAST;
import jeziel.compiladordeensamblador.modelo.parser.ResultadoParser;

import java.util.List;

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
        //Logica de primera pasada para obeener los simbolos

        for (NodoAST nodoRaiz : arbolSintactico){
            analizarNodo(nodoRaiz);
        }
        return new ResultadoSemantico(contextoSemantico.getTablaSimbolos(), contextoSemantico.getErrores());
    }

    public void analizarNodo(NodoAST nodo){
        if(nodo == null) return;
        switch (nodo.getTipo()) {
            case ETIQUETA:
                // Registrar etiqueta en contexto.getTablaSimbolos()
                for (NodoAST hijo : nodo.getHijos()) {
                    analizarNodo(hijo);
                }
                break;
                case DIRECTIVA:
                    ValidadorDirectivas.validar(nodo, contexto); //Clases especiales donde se programo las reglas del comportamiento, es decir semantica
                    break;

                case INSTRUCCION:
                    ValidadorInstrucciones.validar(nodo, contexto);//Clases especiales donde se programo las reglas del comportamiento, es decir semantica
                    break;

                default:
                    break;
        }

    }
    public void buscarSimbolo(NodoAST nodo){
        if(nodo.getTipo() = NodoAST.Tipo.){


        }
    }
}


