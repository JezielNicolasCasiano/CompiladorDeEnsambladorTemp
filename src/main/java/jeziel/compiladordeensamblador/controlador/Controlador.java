package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import jeziel.compiladordeensamblador.modelo.FilaLexer;
import jeziel.compiladordeensamblador.modelo.FilaMaquina;
import jeziel.compiladordeensamblador.modelo.LectorDeArchivos;
import jeziel.compiladordeensamblador.modelo.LectorDeArchivosListener;
import jeziel.compiladordeensamblador.modelo.lexer.Lexer;
import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.NodoAST;
import jeziel.compiladordeensamblador.modelo.parser.Parser;
import jeziel.compiladordeensamblador.modelo.parser.ResultadoParser;
import jeziel.compiladordeensamblador.modelo.semantico.AnalizadorSemantico;
import jeziel.compiladordeensamblador.modelo.semantico.ResultadoSemantico;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controlador implements LectorDeArchivosListener, Initializable {
    private LectorDeArchivos la;
    private Lexer le;
    private final Map<TokenType, String> descripciones = new EnumMap<>(TokenType.class);
    private List<FilaLexer> listaFilas;
    private Parser pe;
    private List<Token> tokens;
    private AnalizadorSemantico an;

    @FXML
    private BorderPane panelPrincipal;
    @FXML
    private MenuItem seleccionarArchivo;
    @FXML
    private TextArea codigoArea;
    @FXML
    private Pagination numeracionPagina;


    TableView<FilaLexer> tablaLexer;


    @FXML
    public void SeleccionarUnArchivo(){ //Metodo que instancia un fileChooser para seleccionar el archivo. Es onAction de SeleccionarArchivo
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(seleccionarArchivo.getParentPopup().getScene().getWindow());


        if(selectedFile != null) {
            if (!selectedFile.getName().toLowerCase().endsWith(".asm")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Archivo no válido");
                alert.setHeaderText(null);
                alert.setContentText("Por favor, selecciona únicamente un archivo con extensión .asm");
                alert.showAndWait();
                return;
            }
            try {
                codigoArea.clear();
                la.setFile(selectedFile);

                le = new Lexer(la.getLineas());
                tokens = le.tokenize();
                paginar(tokens);

            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Lectura");
                alert.setHeaderText("No se pudo cargar el archivo");
                alert.setContentText("Ocurrió un problema técnico: " + ex.getMessage());
                alert.showAndWait();
            }
        }
    }

    public void paginar(List<Token> tokens){
        listaFilas = new ArrayList<>();
        for(int i = 0; i < tokens.size(); i++){
            listaFilas.add(FilaLexer.crearDesdeToken(i,tokens.get(i),descripciones));
        }
        int numeroDePaginas = (int) Math.ceil((double) tokens.size() / 20);
        numeracionPagina.setPageCount(numeroDePaginas == 0 ? 1 : numeroDePaginas);
        numeracionPagina.setPageFactory(pageIndex ->{
            int indiceInicio = pageIndex * 20;
            int indiceFin = Math.min(indiceInicio + 20, listaFilas.size());
            List<FilaLexer> subLista = listaFilas.subList(indiceInicio, indiceFin);
            tablaLexer.setItems(FXCollections.observableArrayList(subLista));
            return tablaLexer;
        });

    }



    @Override
    public void rellenarCodigo() {
        for(int i = 0; i<la.getLineas().size(); i++){
            codigoArea.appendText(la.getLineas().get(i));
            codigoArea.appendText("\n");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        la = new LectorDeArchivos(this);
        codigoArea.setEditable(false);

        descripciones.put(TokenType.INSTRUCCION, "Instrucción");
        descripciones.put(TokenType.PSEUDOINSTRUCCION, "Pseudoinstrucción");
        descripciones.put(TokenType.REGISTRO, "Registro");
        descripciones.put(TokenType.VARIABLE, "Símbolo");
        descripciones.put(TokenType.IDENTIFICADOR, "Símbolo");
        descripciones.put(TokenType.ETIQUETA, "Símbolo");
        descripciones.put(TokenType.SEPARADOR, "Símbolo");
        descripciones.put(TokenType.CORCHETE_ABRE, "Símbolo");
        descripciones.put(TokenType.CORCHETE_CIERRA, "Símbolo");
        descripciones.put(TokenType.PARENTESIS_ABRE, "Símbolo");
        descripciones.put(TokenType.PARENTESIS_CIERRA, "Símbolo");
        descripciones.put(TokenType.CARACTER, "Constante (caracter)");
        descripciones.put(TokenType.DESCONOCIDO, "Elemento no identificado");
        inicializarTablaLexer();
    }

    @FXML public void codificar(){
        //obteniendo resultados
        List<FilaMaquina> todasLasFilasMaquina = new ArrayList<>();
        List<String> renglones = new ArrayList<>();

        pe = new Parser(tokens);
        ResultadoParser resultadoParser = pe.parsear();
        an = new AnalizadorSemantico(resultadoParser);
        ResultadoSemantico resultadoSemantico = an.analizarSemantica();
        String resultado = "";
        String linea = "";
        String codigoMaquina ="----";
        int contadorErroresSintacticos = 0;
        int contadorErroresSemanticos = 0;
        for (int i = 0; i<resultadoParser.getArbol().size() ; i++){
            NodoAST nodoActual = resultadoParser.getArbol().get(i);
            linea =  nodoActual.reconstruirTexto();
            if (nodoActual.getTipo() == NodoAST.Tipo.ERROR_LEXICO || nodoActual.getTipo() == NodoAST.Tipo.ERROR_SINTACTICO) {
                if (contadorErroresSintacticos < resultadoParser.getErrores().size()) {
                    resultado = resultadoParser.getErrores().get(contadorErroresSintacticos).getMensaje();
                    contadorErroresSintacticos++;
                }
            }

            if (contadorErroresSemanticos < resultadoSemantico.getErrores().size()) {
                if (resultadoSemantico.getErrores().get(contadorErroresSemanticos).getPosicionArbolAST() == i) {
                    resultado = resultadoSemantico.getErrores().get(contadorErroresSemanticos).getMensaje();
                    contadorErroresSemanticos++;
                }
            }

            todasLasFilasMaquina.add(new FilaMaquina(i, linea, codigoMaquina, resultado));
        }


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/jeziel/compiladordeensamblador/Tabla-maquina.fxml"));
            AnchorPane vistaTablaMaquina = loader.load();
            ControladorTablaMaquina controladorMaquina = loader.getController();
            Pagination paginationDerecho = new Pagination();
            int elementosPorPagina = 20;
            int numeroDePaginas = (int) Math.ceil((double) todasLasFilasMaquina.size() / elementosPorPagina);
            paginationDerecho.setPageCount(numeroDePaginas == 0 ? 1 : numeroDePaginas);
            paginationDerecho.setPageFactory(pageIndex -> {
                int indiceInicio = pageIndex * elementosPorPagina;
                int indiceFin = Math.min(indiceInicio + elementosPorPagina, todasLasFilasMaquina.size());
                List<FilaMaquina> subLista = todasLasFilasMaquina.subList(indiceInicio, indiceFin);
                controladorMaquina.actualizarDatosPagina(subLista);
                return vistaTablaMaquina;
            });

            panelPrincipal.setRight(paginationDerecho);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Interfaz");
            alert.setHeaderText("No se pudo cargar la vista de código máquina");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    private void inicializarTablaLexer() {
        tablaLexer = new TableView<>();

        tablaLexer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<FilaLexer, Integer> colContador = new TableColumn<>("Numero");
        colContador.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colContador.setPrefWidth(50);


        TableColumn<FilaLexer, String> colToken = new TableColumn<>("Separación");
        colToken.setCellValueFactory(new PropertyValueFactory<>("separacion"));
        colToken.setPrefWidth(150);

        TableColumn<FilaLexer, String> colDesc = new TableColumn<>("Identificación");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("token"));
        colDesc.setPrefWidth(250);

        tablaLexer.getColumns().addAll(colContador, colToken, colDesc);

    }
}

