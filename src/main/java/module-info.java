module jeziel.compiladordeensamblador {
    requires javafx.controls;
    requires javafx.fxml;
    opens jeziel.compiladordeensamblador.controlador to javafx.fxml;
    opens jeziel.compiladordeensamblador to javafx.fxml;
    opens jeziel.compiladordeensamblador.modelo to javafx.base;
    exports jeziel.compiladordeensamblador;
}