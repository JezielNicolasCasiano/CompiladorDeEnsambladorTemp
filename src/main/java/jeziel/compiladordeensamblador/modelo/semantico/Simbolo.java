package jeziel.compiladordeensamblador.modelo.semantico;

public class Simbolo {

        private String nombre;
        private String tipo;
        private int direccion;
        private int tamano;

        public Simbolo(String nombre, String tipo, int direccion, int tamano) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.direccion = direccion;
            this.tamano = tamano;
        }

        public String getDireccionHex() {
        return String.format("%04X", direccion);
         }


        public String getNombre() { return nombre; }
        public String getTipo() { return tipo; }
        public int getDireccion() { return direccion; }
        public int getTamano() { return tamano; }
    }

