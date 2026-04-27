package jeziel.compiladordeensamblador.controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import jeziel.compiladordeensamblador.modelo.LectorDeArchivos;
import jeziel.compiladordeensamblador.modelo.LectorDeArchivosListener;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controlador implements LectorDeArchivosListener, Initializable {
    private LectorDeArchivos la;

    @FXML
    private MenuItem seleccionarArchivo;


    @Override
    public void SeleccionarArchivo() {

    }

    @FXML
    public void SeleccionarUnArchivo(){ //Metodo que instancia un fileChooser para seleccionar el archivo. Es onAction de SeleccionarArchivo
        FileChooser fileChooser = new FileChooser();
        seleccionarArchivo.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(seleccionarArchivo.getParentPopup().getScene().getWindow());
            //Debugger
            if (selectedFile != null) {
                System.out.println("Archivo seleccionado: " + selectedFile.getAbsolutePath());
            }
            try {
                la.setFile(selectedFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        la = new LectorDeArchivos(this);
    }
}
