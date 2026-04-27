package jeziel.compiladordeensamblador.modelo;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LectorDeArchivos{
    private File file;
    private List<String> lineas;
    private LectorDeArchivosListener listener;

    public LectorDeArchivos(LectorDeArchivosListener listener){
        this.listener = listener;
    }




    //getters y setters


    public File getFile() {
        return file;
    }

    //Metodo que establece el nuevo archivo y sus lienas en String
    public void setFile(File file) throws IOException {
        this.file = file;
    }

    public List<String> getLineas() {
        return lineas;
    }
}
