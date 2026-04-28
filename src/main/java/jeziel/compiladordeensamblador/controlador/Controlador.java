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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controlador implements LectorDeArchivosListener, Initializable {
    private LectorDeArchivos la;
    private Lexer le;

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
        File selectedFile = fileChooser.showOpenDialog(seleccionarArchivo.getParentPopup().getScene().getWindow());
        //Debugger
        if (selectedFile != null) {
            System.out.println("Archivo seleccionado: " + selectedFile.getAbsolutePath());
        }
        try {
            codigoArea.clear();
            la.setFile(selectedFile);

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        le = new Lexer(la.getLineas());
        paginar(le.tokenize());
    }

    public void paginar(List<Token> tokens){
        int numeroDePaginas = (int) Math.ceil((double) tokens.size() / 25);
        divisionPagina.setPageCount(numeroDePaginas == 0 ? 1 : numeroDePaginas);
        divisionPagina.setPageFactory(pageIndex ->{
            TextArea paginaTemporal = new TextArea();
            paginaTemporal.setEditable(false);
            paginaTemporal.getStyleClass().add("content");
            int indiceInicio = pageIndex * 25;
            int indiceFin = Math.min(indiceInicio + 25, numeroDePaginas);

            for (int i = indiceInicio; i < indiceFin; i++) {
                paginaTemporal.appendText(tokens.get(i).toString() + "\n");
            }

            return paginaTemporal;
        });
        numeracionPagina.setPageFactory(pageIndex ->{
            TextArea paginaTemporal = new TextArea();
            paginaTemporal.setEditable(false);
            paginaTemporal.getStyleClass().add("content");
            int indiceInicio = pageIndex * 25;
            int indiceFin = Math.min(indiceInicio + 25, numeroDePaginas);

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

    }
}
