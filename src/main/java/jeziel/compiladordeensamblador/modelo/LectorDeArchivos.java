package jeziel.compiladordeensamblador.modelo;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LectorDeArchivos{
    private File file;
    private List<String> lineas;

    public LectorDeArchivos(LectorDeArchivosListener listener){

    }




    //getters y setters


    public File getFile() {
        return file;
    }

    //Metodo que establece el nuevo archivo y sus lienas en String
    public List<String> setFile(File file) throws IOException {
        this.file = file;
        lineas = Files.readAllLines(Path.of(file.getAbsolutePath()));
        return lineas;
    }
}
