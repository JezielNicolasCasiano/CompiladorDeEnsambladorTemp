package jeziel.compiladordeensamblador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CompiladorDeEnsabladoresAplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CompiladorDeEnsabladoresAplication.class.getResource("Pestana-principal.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Compilador de Ensamblador");
        stage.setScene(scene);
        stage.show();
    }
}
