package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import jeziel.compiladordeensamblador.modelo.FilaCodigo;

import java.util.List;

public class ControladorTablaCodigos {

    @FXML
    private TableView<FilaCodigo> tablaCodigos;

    @FXML
    private TableColumn<FilaCodigo, String> columnaSimbolo;

    @FXML
    private TableColumn<FilaCodigo, String> columnaTipo;

    @FXML
    private TableColumn<FilaCodigo, String> columnaValor;

    @FXML
    private TableColumn<FilaCodigo, String> columnaTamano;

    @FXML
    private TableColumn<FilaCodigo, String> columnaDireccion;

    @FXML
    public void initialize() {
        tablaCodigos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        columnaSimbolo.setCellValueFactory(new PropertyValueFactory<>("simbolo"));
        columnaTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        columnaValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        columnaTamano.setCellValueFactory(new PropertyValueFactory<>("tamano"));
        columnaDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
    }

    public void actualizarDatos(List<FilaCodigo> listaCodigos) {
        tablaCodigos.setItems(FXCollections.observableArrayList(listaCodigos));
    }
}