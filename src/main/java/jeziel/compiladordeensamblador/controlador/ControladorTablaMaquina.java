package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import jeziel.compiladordeensamblador.modelo.FilaMaquina;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class ControladorTablaMaquina implements Initializable {

    @FXML
    private TableView<FilaMaquina> tablaMaquina;
    @FXML
    private TableColumn<FilaMaquina, String> columnaContador;
    @FXML
    private TableColumn<FilaMaquina, String> columnaCodificacion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tablaMaquina.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        columnaContador.setCellValueFactory(new PropertyValueFactory<>("contador"));
        columnaCodificacion.setCellValueFactory(new PropertyValueFactory<>("codificacion"));
    }

    public void cargarDatosMaquina(Collection<FilaMaquina> datos) {
        ObservableList<FilaMaquina> listaDatos = FXCollections.observableArrayList(datos);
        tablaMaquina.setItems(listaDatos);
    }
}