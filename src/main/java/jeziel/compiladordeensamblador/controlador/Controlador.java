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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controlador implements LectorDeArchivosListener, Initializable {
    private LectorDeArchivos la;
    private Lexer le;
    private final Map<TokenType, String> descripciones = new EnumMap<>(TokenType.class);
    private final int FILAS_POR_PAGINA = 25;
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


                for (int i = 0; i < tokensActuales.size(); i++) {
                    Token t = tokensActuales.get(i);
                    String descripcion = (t.getType() == TokenType.CONSTANTE) ?
                            "Constante (numérica " + String.valueOf(t.getSub()).toLowerCase() + ")" :
                            descripciones.getOrDefault(t.getType(), "Elemento inválido");

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



        try {
            FXMLLoader codigos = new FXMLLoader(getClass().getResource("/jeziel/compiladordeensamblador/Tabla-codigos.fxml"));
            Parent nodoTabla = codigos.load();

            if (contenedorTabla.getChildren().size() > 1) {
                contenedorTabla.getChildren().remove(0);
            }
            contenedorTabla.getChildren().add(0, nodoTabla);
            contenedorTabla.setAlignment(javafx.geometry.Pos.CENTER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void actualizarPaginacion() {

    }

}
