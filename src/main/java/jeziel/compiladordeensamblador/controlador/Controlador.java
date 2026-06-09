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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import jeziel.compiladordeensamblador.modelo.*;
import jeziel.compiladordeensamblador.modelo.lexer.Lexer;
import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.Parser;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;
import jeziel.compiladordeensamblador.modelo.semantico.AnalizadorSemantico;
import jeziel.compiladordeensamblador.modelo.semantico.LineaAnalizadaSemanticamente;
import jeziel.compiladordeensamblador.modelo.semantico.DepuradorSemantico;
import javafx.scene.Node;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controlador implements LectorDeArchivosListener, Initializable {
    private LectorDeArchivos la;
    private Lexer le;
    private final Map<TokenType, String> descripciones = new EnumMap<>(TokenType.class);
    private List<FilaLexer> listaFilas;
    private List<Token> tokens;

    @FXML
    private BorderPane panelPrincipal;
    @FXML
    private MenuItem seleccionarArchivo;
    @FXML
    private TextArea codigoArea;
    @FXML
    private Pagination numeracionPagina;
    @FXML
    private VBox contenedorTabla;


    TableView<FilaLexer> tablaLexer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inicializarTableViewLexer();
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


    }

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
            if (!(tokens.get(i).getType() == TokenType.SEPARADOR)){
                listaFilas.add(FilaLexer.crearDesdeToken(i,tokens.get(i),descripciones));
            }
        }

        int numeroDePaginas = (int) Math.ceil((double) listaFilas.size()/ 20);
        numeracionPagina.setPageCount(numeroDePaginas == 0 ? 1 : numeroDePaginas);
        numeracionPagina.setPageFactory(pageIndex ->{
            int indiceInicio = pageIndex * 20;
            int indiceFin = Math.min(indiceInicio + 20, listaFilas.size());
            List<FilaLexer> subLista = listaFilas.subList(indiceInicio, indiceFin);
            tablaLexer.setItems(FXCollections.observableArrayList(subLista));
            return tablaLexer;
        });

    }

    public void inicializarTableViewLexer(){
        tablaLexer = new TableView<>();
        TableColumn<FilaLexer, Integer> colNumero = new TableColumn<>("Número");
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        TableColumn<FilaLexer, String> colSeparacion = new TableColumn<>("Separacion");
        colSeparacion.setCellValueFactory(new PropertyValueFactory<>("separacion"));
        TableColumn<FilaLexer, String> colToken = new TableColumn<>("Identificacion");
        colToken.setCellValueFactory(new PropertyValueFactory<>("token"));
        tablaLexer.getColumns().addAll(colNumero, colSeparacion, colToken);
        tablaLexer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }



    @Override
    public void rellenarCodigo() {
        for(int i = 0; i<la.getLineas().size(); i++){
            codigoArea.appendText(la.getLineas().get(i));
            codigoArea.appendText("\n");
        }
    }



    @FXML public void codificar(){
        if (tokens == null || tokens.isEmpty()) return;

        Parser parser = new Parser(tokens);
        List<LineaAnalizada> arbolSintactico = parser.parsear();

        AnalizadorSemantico analizadorSemantico = new AnalizadorSemantico(arbolSintactico);
        List<LineaAnalizadaSemanticamente> analisisSemantico = analizadorSemantico.analizar();
        List<LineaAnalizadaSemanticamente> tablaDeSimbolos = analizadorSemantico.getTablaDeSimbolos();

        // Metodo de depuración en consola
        DepuradorSemantico.depurar(analisisSemantico);

        try {
            // Cargar y mostrar la tabla de máquina en el panel derecho
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/jeziel/compiladordeensamblador/Tabla-maquina.fxml"));
            loader.setControllerFactory(c -> new ControladorTablaMaquina(analisisSemantico));
            Node tablaMaquinaNode = loader.load();
            panelPrincipal.setRight(tablaMaquinaNode);

            // Cargar y mostrar la tabla de símbolos en el VBox inferior
            FXMLLoader loaderCodigos = new FXMLLoader(getClass().getResource("/jeziel/compiladordeensamblador/Tabla-codigos.fxml"));
            loaderCodigos.setControllerFactory(c -> new ControladorTablaCodigos(tablaDeSimbolos));
            Node tablaCodigosNode = loaderCodigos.load();

            if (tablaCodigosNode instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) tablaCodigosNode).prefWidthProperty().bind(contenedorTabla.widthProperty());
            }

            if (contenedorTabla.getChildren().size() > 1) {
                contenedorTabla.getChildren().subList(1, contenedorTabla.getChildren().size()).clear();
            }
            contenedorTabla.getChildren().add(tablaCodigosNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

