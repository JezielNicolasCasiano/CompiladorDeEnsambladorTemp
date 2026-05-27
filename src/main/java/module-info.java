module jeziel.compiladordeensamblador {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens jeziel.compiladordeensamblador to javafx.fxml;
    exports jeziel.compiladordeensamblador;

    exports jeziel.compiladordeensamblador.controlador;
    opens jeziel.compiladordeensamblador.controlador to javafx.fxml;

    exports jeziel.compiladordeensamblador.modelo;
    opens jeziel.compiladordeensamblador.modelo to javafx.fxml, javafx.base;

}