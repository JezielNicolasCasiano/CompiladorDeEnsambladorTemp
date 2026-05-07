package jeziel.compiladordeensamblador.controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import jeziel.compiladordeensamblador.modelo.LectorDeArchivos;
import jeziel.compiladordeensamblador.modelo.LectorDeArchivosListener;
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

    @FXML
    private MenuItem seleccionarArchivo;
    @FXML
    private TextArea codigoArea;
    @FXML
    private Pagination numeracionPagina;
    @FXML
    private Pagination divisionPagina;




    @FXML
    public void SeleccionarUnArchivo(){ //Metodo que instancia un fileChooser para seleccionar el archivo. Es onAction de SeleccionarArchivo
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de Ensamblador (*.asm)", "*.asm");
        fileChooser.getExtensionFilters().add(extFilter);
        File selectedFile = fileChooser.showOpenDialog(seleccionarArchivo.getParentPopup().getScene().getWindow());


        if(selectedFile != null) {
            try {
                codigoArea.clear();
                la.setFile(selectedFile);

                le = new Lexer(la.getLineas());
                paginar(le.tokenize());

            } catch (IOException ex) {
                throw new RuntimeException("Error al intentar leer el archivo .asm", ex);
            }
        } else {
            System.out.println("Selección de archivo cancelada por el usuario.");
        }
    }

    public void paginar(List<Token> tokens){
        int numeroDePaginas = (int) Math.ceil((double) tokens.size() / 25);
        divisionPagina.setPageCount(numeroDePaginas == 0 ? 1 : numeroDePaginas);
        numeracionPagina.currentPageIndexProperty().unbindBidirectional(divisionPagina.currentPageIndexProperty());
        numeracionPagina.currentPageIndexProperty().bindBidirectional(divisionPagina.currentPageIndexProperty());
        divisionPagina.setPageFactory(pageIndex ->{
            TextArea paginaTemporal = new TextArea();
            paginaTemporal.setEditable(false);
            paginaTemporal.getStyleClass().add("content");
            int indiceInicio = pageIndex * 25;
            int indiceFin = Math.min(indiceInicio + 25, tokens.size());

            for (int i = indiceInicio; i < indiceFin; i++) {
                String descripcion;

                if (tokens.get(i).getType() == TokenType.NUMERO) {
                    descripcion = "Constante (numérica " + String.valueOf(tokens.get(i).getSub()).toLowerCase() + ")";
                } else {

                    descripcion = descripciones.getOrDefault(tokens.get(i).getType(), "Elemento inválido");
                }
                paginaTemporal.appendText(String.format("%-15s ; %s\n", tokens.get(i).getValue(), descripcion));;
                //paginaTemporal.appendText(tokens.get(i).toString() + "\n");
            }

            return paginaTemporal;
        });
        numeracionPagina.setPageFactory(pageIndex ->{
            TextArea paginaTemporal = new TextArea();
            paginaTemporal.setEditable(false);
            paginaTemporal.getStyleClass().add("content");
            int indiceInicio = pageIndex * 25;
            int indiceFin = Math.min(indiceInicio + 25, tokens.size());

            for (int i = indiceInicio; i < indiceFin; i++) {
                paginaTemporal.appendText(String.valueOf(i));
                paginaTemporal.appendText("\n");
            }

            return paginaTemporal;
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
        descripciones.put(TokenType.DIRECTIVA, "Pseudoinstrucción");
        descripciones.put(TokenType.REGISTRO, "Registro");
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
}
