package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import jeziel.compiladordeensamblador.modelo.*;
import jeziel.compiladordeensamblador.modelo.lexer.Lexer;
import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.Parser;
import jeziel.compiladordeensamblador.modelo.parser.ResultadoParser;
import jeziel.compiladordeensamblador.modelo.semantico.AnalizadorSemantico;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controlador implements LectorDeArchivosListener, Initializable {
    private LectorDeArchivos la;
    private Lexer le;
    private final Map<TokenType, String> descripciones = new EnumMap<>(TokenType.class);
    private ControladorTablaCodigos conTabCode;
    private ControladorTablaMaquina conTabMaquina;
    private TableView<Fila> tablaAnalisis;
    private ObservableList<Fila> listaCompletaDatos;
    private final int FILAS_POR_PAGINA = 25;
    private TableColumn<Fila, String> columnaInstruccion;
    private TableColumn<Fila, String> columnaParser;
    private List<Token> tokensActuales;

    @FXML
    private MenuItem seleccionarArchivo;
    @FXML
    private TextArea codigoArea;
    @FXML
    private VBox contenedorTabla;
    @FXML
    private Pagination paginacionTabla;
    @FXML
    private BorderPane panelPrincipal;


    @FXML
    public void SeleccionarUnArchivo() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(seleccionarArchivo.getParentPopup().getScene().getWindow());

        if (selectedFile != null) {
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
                tokensActuales = le.tokenize();

                listaCompletaDatos.clear();

                for (int i = 0; i < tokensActuales.size(); i++) {
                    Token t = tokensActuales.get(i);
                    String descripcion = (t.getType() == TokenType.CONSTANTE) ?
                            "Constante (numérica " + String.valueOf(t.getSub()).toLowerCase() + ")" :
                            descripciones.getOrDefault(t.getType(), "Elemento inválido");

                    listaCompletaDatos.add(new Fila(i + 1, t.getValue(), descripcion));
                }

                actualizarPaginacion();

            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Lectura");
                alert.setHeaderText("No se pudo cargar el archivo");
                alert.setContentText("Ocurrió un problema técnico: " + ex.getMessage());
                alert.showAndWait();
            }
        }
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

        tablaAnalisis = new TableView<>();
        listaCompletaDatos = FXCollections.observableArrayList();

        TableColumn<Fila, Integer> columnaContador = new TableColumn<>("No.");
        columnaContador.setCellValueFactory(new PropertyValueFactory<>("contador"));
        columnaContador.setPrefWidth(40);

        TableColumn<Fila, String> columnaLexema = new TableColumn<>("Identificación");
        columnaLexema.setCellValueFactory(new PropertyValueFactory<>("lexema"));

        TableColumn<Fila, String> columnaTipo = new TableColumn<>("Descripción");
        columnaTipo.setCellValueFactory(new PropertyValueFactory<>("tipoToken"));

        tablaAnalisis.getColumns().addAll(columnaContador, columnaLexema, columnaTipo);
        tablaAnalisis.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        descripciones.put(TokenType.INSTRUCCION, "Instrucción");
        descripciones.put(TokenType.PSEUDOINSTRUCCION, "Pseudoinstrucción");
        descripciones.put(TokenType.REGISTRO, "Registro");
        descripciones.put(TokenType.VARIABLE, "Símbolo");
        descripciones.put(TokenType.IDENTIFICADOR, "Símbolo");
        descripciones.put(TokenType.ETIQUETA, "Etiqueta");
        descripciones.put(TokenType.SEPARADOR, "Símbolo");
        descripciones.put(TokenType.CORCHETE_ABRE, "Símbolo");
        descripciones.put(TokenType.CORCHETE_CIERRA, "Símbolo");
        descripciones.put(TokenType.PARENTESIS_ABRE, "Símbolo");
        descripciones.put(TokenType.PARENTESIS_CIERRA, "Símbolo");
        descripciones.put(TokenType.CARACTER, "Constante (caracter)");
        descripciones.put(TokenType.CADENA, "Constante (cadena)");
        descripciones.put(TokenType.DESCONOCIDO, "Elemento no identificado");
    }
    @FXML
    public void codificar() {
        if (tokensActuales == null || tokensActuales.isEmpty()) {
            return;
        }

        Parser parser = new Parser(tokensActuales);
        ResultadoParser resultado = parser.parsear();
        AnalizadorSemantico semantico = new AnalizadorSemantico();
        semantico.analizar(resultado.getArbol());

        List<jeziel.compiladordeensamblador.modelo.parser.ErrorSintactico> todosLosErrores = new ArrayList<>();

        for (Token t : tokensActuales) {
            if (t.getType() == TokenType.DESCONOCIDO) {
                todosLosErrores.add(new jeziel.compiladordeensamblador.modelo.parser.ErrorSintactico(t, "Error Léxico: Elemento no reconocido"));
            }
        }
        todosLosErrores.addAll(resultado.getErrores());
        todosLosErrores.addAll(semantico.getErrores());

        tablaAnalisis.refresh();

        try {
            FXMLLoader codigos = new FXMLLoader(getClass().getResource("/jeziel/compiladordeensamblador/Tabla-codigos.fxml"));
            Parent nodoTabla = codigos.load();
            conTabCode = codigos.getController();
            conTabCode.cargarSimbolos(semantico.getTablaSimbolos().obtenerTodos());

            if (contenedorTabla.getChildren().size() > 1) {
                contenedorTabla.getChildren().remove(0);
            }
            contenedorTabla.getChildren().add(0, nodoTabla);
            contenedorTabla.setAlignment(javafx.geometry.Pos.CENTER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            if (conTabMaquina == null) {
                FXMLLoader loaderMaquina = new FXMLLoader(getClass().getResource("/jeziel/compiladordeensamblador/Tabla-maquina.fxml"));
                Parent nodoTablaMaquina = loaderMaquina.load();
                conTabMaquina = loaderMaquina.getController();
                panelPrincipal.setRight(nodoTablaMaquina);
            }

            GeneradorDeCodigo generador = new GeneradorDeCodigo(semantico.getTablaSimbolos());

            List<FilaMaquina> codigoGenerado = generador.generar(la.getLineas(), resultado.getArbol(), todosLosErrores);

            conTabMaquina.cargarDatosMaquina(codigoGenerado);

        } catch (IOException e) {
            System.err.println("Error cargando la tabla: " + e.getMessage());
        }
    }


    private void actualizarPaginacion() {
        if (listaCompletaDatos.isEmpty()) {
            paginacionTabla.setPageCount(1);
            tablaAnalisis.setItems(FXCollections.observableArrayList());
            paginacionTabla.setPageFactory(pageIndex -> tablaAnalisis);
            return;
        }

        int numeroDePaginas = (int) Math.ceil((double) listaCompletaDatos.size() / FILAS_POR_PAGINA);
        paginacionTabla.setPageCount(numeroDePaginas);
        paginacionTabla.setPageFactory(pageIndex -> {
            int indiceInicio = pageIndex * FILAS_POR_PAGINA;
            int indiceFin = Math.min(indiceInicio + FILAS_POR_PAGINA, listaCompletaDatos.size());

            ObservableList<Fila> subLista = FXCollections.observableArrayList(
                    listaCompletaDatos.subList(indiceInicio, indiceFin)
            );
            tablaAnalisis.setItems(subLista);

            return tablaAnalisis;
        });
    }

}
