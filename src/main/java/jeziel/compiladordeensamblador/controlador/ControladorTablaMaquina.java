package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import jeziel.compiladordeensamblador.modelo.FilaTablaMaquina;
import jeziel.compiladordeensamblador.modelo.semantico.LineaAnalizadaSemanticamente;

import java.util.ArrayList;
import java.util.List;

public class ControladorTablaMaquina {
    private List<LineaAnalizadaSemanticamente> analisisSemantico;
    private List<FilaTablaMaquina> listaFilas;
    private TableView<FilaTablaMaquina> tablaMaquina;

    @FXML
    private Pagination paginationMaquina;

    public ControladorTablaMaquina(List<LineaAnalizadaSemanticamente> analisisSemantico) {
        this.analisisSemantico = analisisSemantico;
    }

    @FXML
    public void initialize() {
        inicializarTableViewMaquina();
        paginarMaquina();
    }

    public void inicializarTableViewMaquina() {
        tablaMaquina = new TableView<>();
        TableColumn<FilaTablaMaquina, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        TableColumn<FilaTablaMaquina, String> colLinea = new TableColumn<>("Línea");
        colLinea.setCellValueFactory(new PropertyValueFactory<>("linea"));
        TableColumn<FilaTablaMaquina, String> colResultado = new TableColumn<>("Resultado");
        colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));
        
        tablaMaquina.getColumns().addAll(colDireccion, colLinea, colResultado);
        tablaMaquina.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    //Metodo de fabricacion de paginas
    public void paginarMaquina() {
        listaFilas = new ArrayList<>();
        if (analisisSemantico != null) {
            for (LineaAnalizadaSemanticamente linea : analisisSemantico) {
                listaFilas.add(FilaTablaMaquina.crearDesdeLineaSemantica(linea));
            }
        }

        int numeroDePaginas = (int) Math.ceil((double) listaFilas.size() / 20);
        paginationMaquina.setPageCount(numeroDePaginas == 0 ? 1 : numeroDePaginas);
        paginationMaquina.setPageFactory(pageIndex -> {
            int indiceInicio = pageIndex * 20;
            int indiceFin = Math.min(indiceInicio + 20, listaFilas.size());
            List<FilaTablaMaquina> subLista = listaFilas.subList(indiceInicio, indiceFin);
            tablaMaquina.setItems(FXCollections.observableArrayList(subLista));
            return tablaMaquina;
        });
    }
}
