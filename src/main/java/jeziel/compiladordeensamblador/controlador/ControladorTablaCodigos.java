package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import jeziel.compiladordeensamblador.modelo.semantico.Simbolo;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class ControladorTablaCodigos implements Initializable {

    @FXML
    private TableView<Simbolo> tablaCodigos;
    @FXML
    private TableColumn<Simbolo, String> columnaSimbolo;
    @FXML
    private TableColumn<Simbolo, String> columnaTipo;
    @FXML
    private TableColumn<Simbolo, Integer> columnaValor;
    @FXML
    private TableColumn<Simbolo, Integer> columnaTamano;
    @FXML private TableColumn<Simbolo, String> columnaDireccion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tablaCodigos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        columnaSimbolo.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        columnaTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        columnaTamano.setCellValueFactory(new PropertyValueFactory<>("tamano"));
        columnaDireccion.setCellValueFactory(new PropertyValueFactory<>("direccionHex"));
    }
    public void cargarSimbolos(Collection<Simbolo> simbolos) {
        ObservableList<Simbolo> listaSimbolos = FXCollections.observableArrayList(simbolos);
        tablaCodigos.setItems(listaSimbolos);
    }
}