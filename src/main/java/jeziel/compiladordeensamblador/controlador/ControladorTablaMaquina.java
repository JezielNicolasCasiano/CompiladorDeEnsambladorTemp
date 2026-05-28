package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import jeziel.compiladordeensamblador.modelo.FilaMaquina;
import java.util.List;

public class ControladorTablaMaquina {

    @FXML
    private TableView<FilaMaquina> tablaMaquina; // Cambia FilaMaquina por tu clase modelo
    @FXML
    private TableColumn<FilaMaquina, Integer> columnaContador;
    @FXML
    private TableColumn<FilaMaquina, String> columnaLinea;
    @FXML
    private TableColumn<FilaMaquina, String> columnaCodificacion;
    @FXML
    private TableColumn<FilaMaquina, String> columnaError;

    @FXML
    public void initialize() {
        columnaContador.setCellValueFactory(new PropertyValueFactory<>("contador"));
        columnaLinea.setCellValueFactory(new PropertyValueFactory<>("linea"));
        columnaCodificacion.setCellValueFactory(new PropertyValueFactory<>("codigoMaquina"));
        columnaError.setCellValueFactory(new PropertyValueFactory<>("resultado"));
    }

    public void actualizarDatosPagina(List<FilaMaquina> listaSubSeccion) {
        tablaMaquina.setItems(FXCollections.observableArrayList(listaSubSeccion));
    }
}