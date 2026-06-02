package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import jeziel.compiladordeensamblador.modelo.FilaLexer;
import jeziel.compiladordeensamblador.modelo.FilaMaquina;
import java.util.List;

public class ControladorTablaMaquina {
    private TableView<FilaMaquina> tablaAnalizador;
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Pagination paginacionTablaMaquina;

    @FXML
    public void initialize() {
        tablaAnalizador = new TableView<>();

        tablaAnalizador.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<FilaMaquina, Integer> colContador = new TableColumn<>("Contador");
        colContador.setCellValueFactory(new PropertyValueFactory<>("contador"));
        colContador.setPrefWidth(50);


        TableColumn<FilaMaquina, String> colLinea = new TableColumn<>("Linea");
        colLinea.setCellValueFactory(new PropertyValueFactory<>("linea"));
        colLinea.setPrefWidth(150);

        TableColumn<FilaMaquina, String> colCodigoMaquina = new TableColumn<>("Codigo Maquina");
        colCodigoMaquina.setCellValueFactory(new PropertyValueFactory<>("codigoMaquina"));
        colCodigoMaquina.setPrefWidth(250);

        TableColumn<FilaMaquina, String> colResultado = new TableColumn<>("Resultado");
        colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));
        colResultado.setPrefWidth(250);

        tablaAnalizador.getColumns().addAll(colContador, colLinea, colCodigoMaquina, colResultado);

        rootPane.getChildren().add(tablaAnalizador);

        AnchorPane.setTopAnchor(tablaAnalizador, 0.0);
        AnchorPane.setBottomAnchor(tablaAnalizador, 0.0);
        AnchorPane.setLeftAnchor(tablaAnalizador, 0.0);
        AnchorPane.setRightAnchor(tablaAnalizador, 0.0);
    }

    public void actualizarDatosPagina(List<FilaMaquina> listaSubSeccion) {
        tablaAnalizador.setItems(FXCollections.observableArrayList(listaSubSeccion));
    }


}