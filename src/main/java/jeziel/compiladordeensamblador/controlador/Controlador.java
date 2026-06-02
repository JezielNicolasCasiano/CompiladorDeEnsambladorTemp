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
    @FXML
    private VBox contenedorTabla;


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
            if (!(tokens.get(i).getType() == TokenType.SEPARADOR)){
                listaFilas.add(FilaLexer.crearDesdeToken(i,tokens.get(i),descripciones));
            }
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
        List<FilaCodigo> listaCodigos = new ArrayList<>();
        List<String> renglones = new ArrayList<>();

        pe = new Parser(tokens);
        ResultadoParser resultadoParser = pe.parsear();
        an = new AnalizadorSemantico(resultadoParser);
        ResultadoSemantico resultadoSemantico = an.analizarSemantica();

        String linea = "";
        String codigoMaquina ="----";
        int contadorErroresSintacticos = 0;
        int contadorErroresSemanticos = 0;
        for (int i = 0; i<resultadoParser.getArbol().size() ; i++){
            String resultado = "Correcto";
            NodoAST nodoActual = resultadoParser.getArbol().get(i);
            int numLineaOriginal = (nodoActual.getToken() != null) ? nodoActual.getToken().getLinea() : la.getLineas().size();

            String textoOriginal = la.getLineas().get(numLineaOriginal - 1);

            if (textoOriginal.contains(";")) {
                textoOriginal = textoOriginal.substring(0, textoOriginal.indexOf(";"));
            }

            linea = textoOriginal.stripLeading();

            for (jeziel.compiladordeensamblador.modelo.parser.ErrorSintactico errSint : resultadoParser.getErrores()) {
                int errLinea = (errSint.getToken() != null) ? errSint.getToken().getLinea() : la.getLineas().size();
                if (errLinea == numLineaOriginal) {
                    resultado = errSint.getMensaje();
                    break;
                }
            }

            if (resultado.equals("Correcto")) {
                for (jeziel.compiladordeensamblador.modelo.semantico.ErrorSemantico errSem : resultadoSemantico.getErrores()) {
                    if (errSem.getPosicionArbolAST() == i) {
                        resultado = errSem.getMensaje();
                        break;
                    }
                }
            }

            todasLasFilasMaquina.add(new FilaMaquina(i, linea, codigoMaquina, resultado));

            if (nodoActual.getTipo() == NodoAST.Tipo.DIRECTIVA && !nodoActual.getHijos().isEmpty()) {
                NodoAST ultimoHijo = nodoActual.getHijos().get(nodoActual.getHijos().size() - 1);

                if (ultimoHijo.getToken() != null && ultimoHijo.getToken().getType() == TokenType.VARIABLE) {
                    String varNombre = ultimoHijo.getToken().getValue();

                    String tamanoStr = "Indeterminado";
                    if (nodoActual.getToken().getSub() == jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype.Directiva.DB) {
                        tamanoStr = "8 bits";
                    } else if (nodoActual.getToken().getSub() == jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype.Directiva.DW) {
                        tamanoStr = "16 bits";
                    }

                    String valorStr = "-";
                    String tipoDetallado = "Variable";
                    NodoAST primerHijo = nodoActual.getHijos().get(0);

                    if (primerHijo.getTipo() == NodoAST.Tipo.OPERANDO_CONSTANTE && primerHijo.getToken() != null) {
                        valorStr = primerHijo.getToken().getValue();
                        if (primerHijo.getToken().getSub() == jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype.Constante.HEXADECIMAL) {
                            tipoDetallado = "Constante Hexadecimal";
                        } else if (primerHijo.getToken().getSub() == jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype.Constante.BINARIO) {
                            tipoDetallado = "Constante Binaria";
                        } else {
                            tipoDetallado = "Constante Decimal";
                        }
                    } else if (primerHijo.getTipo() == NodoAST.Tipo.OPERANDO_CARACTER && primerHijo.getToken() != null) {
                        valorStr = primerHijo.getToken().getValue();
                        tipoDetallado = "Constante Caracter";
                    } else if (primerHijo.getTipo() == NodoAST.Tipo.OPERANDO_CADENA && primerHijo.getToken() != null) {
                        valorStr = primerHijo.getToken().getValue();
                        tipoDetallado = "Constante Cadena";
                    }

                    listaCodigos.add(new FilaCodigo(varNombre, tipoDetallado, valorStr, tamanoStr));
                }
            } else if (nodoActual.getTipo() == NodoAST.Tipo.ETIQUETA) {
                String etiqNombre = nodoActual.getToken().getValue().replace(":", "");
                listaCodigos.add(new FilaCodigo(etiqNombre, "Etiqueta", "-", "0 bits"));
            }
        }


        try {
            FXMLLoader loaderMaquina = new FXMLLoader(getClass().getResource("/jeziel/compiladordeensamblador/Tabla-maquina.fxml"));
            AnchorPane vistaTablaMaquina = loaderMaquina.load();
            ControladorTablaMaquina controladorMaquina = loaderMaquina.getController();
            Pagination paginationDerecho = new Pagination();
            int elementosPorPaginaMaquina = 20;
            paginationDerecho.setPrefWidth(550);
            paginationDerecho.setMinWidth(550);
            paginationDerecho.setMaxWidth(550);
            int numeroDePaginasMaquina = (int) Math.ceil((double) todasLasFilasMaquina.size() / elementosPorPaginaMaquina);
            paginationDerecho.setPageCount(numeroDePaginasMaquina == 0 ? 1 : numeroDePaginasMaquina);
            paginationDerecho.setPageFactory(pageIndex -> {
                int indiceInicio = pageIndex * elementosPorPaginaMaquina;
                int indiceFin = Math.min(indiceInicio + elementosPorPaginaMaquina, todasLasFilasMaquina.size());
                List<FilaMaquina> subLista = todasLasFilasMaquina.subList(indiceInicio, indiceFin);
                controladorMaquina.actualizarDatosPagina(subLista);
                return vistaTablaMaquina;
            });

            panelPrincipal.setRight(paginationDerecho);
            FXMLLoader loaderCodigos = new FXMLLoader(getClass().getResource("/jeziel/compiladordeensamblador/Tabla-codigos.fxml"));
            AnchorPane vistaTablaCodigos = loaderCodigos.load();
            ControladorTablaCodigos controladorCodigos = loaderCodigos.getController();

            Pagination paginationInferior = new Pagination();
            int elementosPorPaginaCodigos = 10;
            int numeroDePaginasCodigos = (int) Math.ceil((double) listaCodigos.size() / elementosPorPaginaCodigos);

            paginationInferior.setPageCount(numeroDePaginasCodigos == 0 ? 1 : numeroDePaginasCodigos);
            paginationInferior.setPageFactory(pageIndex -> {
                int indiceInicio = pageIndex * elementosPorPaginaCodigos;
                int indiceFin = Math.min(indiceInicio + elementosPorPaginaCodigos, listaCodigos.size());
                List<FilaCodigo> subListaCodigos = listaCodigos.subList(indiceInicio, indiceFin);
                controladorCodigos.actualizarDatos(subListaCodigos);
                return vistaTablaCodigos;
            });
            VBox.setVgrow(paginationInferior, javafx.scene.layout.Priority.ALWAYS);

            if (contenedorTabla.getChildren().size() > 1) {
                contenedorTabla.getChildren().remove(0);
            }
            contenedorTabla.getChildren().add(0, paginationInferior);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Interfaz");
            alert.setHeaderText("No se pudieron cargar las vistas de análisis");
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

