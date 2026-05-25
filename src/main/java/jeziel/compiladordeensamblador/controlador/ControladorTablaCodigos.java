package jeziel.compiladordeensamblador.controlador;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class ControladorTablaCodigos implements Initializable {

    @FXML
    private TableView<String> tablaCodigos;
    /*@FXML
    private TableColumn columnaSimbolo;
    @FXML
    private TableColumn columnaTipo;
    @FXML
    private TableColumn columnaValor;
    @FXML
    private TableColumn columnaTamano;
    @FXML
    private TableColumn columnaDireccion;*/


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tablaCodigos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }
}
