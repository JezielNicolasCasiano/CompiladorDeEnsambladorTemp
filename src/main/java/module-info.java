module jeziel.compiladordeensamblador {
    requires javafx.controls;
    requires javafx.fxml;


    opens jeziel.compiladordeensamblador to javafx.fxml;
    exports jeziel.compiladordeensamblador;
}