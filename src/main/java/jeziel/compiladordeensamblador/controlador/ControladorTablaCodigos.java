package jeziel.compiladordeensamblador.controlador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import jeziel.compiladordeensamblador.modelo.FilaCodigo;
import jeziel.compiladordeensamblador.modelo.semantico.LineaAnalizadaSemanticamente;

import java.util.ArrayList;
import java.util.List;

public class ControladorTablaCodigos {
    private List<LineaAnalizadaSemanticamente> tablaDeSimbolos;
    private List<FilaCodigo> listaFilas;
    private TableView<FilaCodigo> tablaCodigos;

    @FXML
    private Pagination paginationCodigos;

    public ControladorTablaCodigos(List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        this.tablaDeSimbolos = tablaDeSimbolos;
    }

    @FXML
    public void initialize() {
        inicializarTableViewCodigos();
        paginarCodigos();
    }

    public void inicializarTableViewCodigos() {
        tablaCodigos = new TableView<>();
        
        TableColumn<FilaCodigo, String> colSimbolo = new TableColumn<>("Símbolo");
        colSimbolo.setCellValueFactory(new PropertyValueFactory<>("simbolo"));
        
        TableColumn<FilaCodigo, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        
        TableColumn<FilaCodigo, String> colValor = new TableColumn<>("Valor");
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        
        TableColumn<FilaCodigo, String> colTamano = new TableColumn<>("Tamaño");
        colTamano.setCellValueFactory(new PropertyValueFactory<>("tamano"));
        
        TableColumn<FilaCodigo, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        
        tablaCodigos.getColumns().addAll(colSimbolo, colTipo, colValor, colTamano, colDireccion);
        tablaCodigos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void paginarCodigos() {
        listaFilas = new ArrayList<>();
        if (tablaDeSimbolos != null) {
            for (LineaAnalizadaSemanticamente linea : tablaDeSimbolos) {
                FilaCodigo fila = FilaCodigo.crearDesdeLineaSemantica(linea);
                if (fila != null) {
                    listaFilas.add(fila);
                }
            }
        }

        int numeroDePaginas = (int) Math.ceil((double) listaFilas.size() / 13);
        paginationCodigos.setPageCount(numeroDePaginas == 0 ? 1 : numeroDePaginas);
        paginationCodigos.setPageFactory(pageIndex -> {
            int indiceInicio = pageIndex * 13;
            int indiceFin = Math.min(indiceInicio + 13, listaFilas.size());
            List<FilaCodigo> subLista = listaFilas.subList(indiceInicio, indiceFin);
            tablaCodigos.setItems(FXCollections.observableArrayList(subLista));
            return tablaCodigos;
        });
    }
}