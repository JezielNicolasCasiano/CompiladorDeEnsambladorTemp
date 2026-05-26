package jeziel.compiladordeensamblador.modelo.semantico;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TablaSimbolo {

        private Map<String, Simbolo> tabla;

        public TablaSimbolo() {
            this.tabla = new HashMap<>();
        }

        public boolean agregar(Simbolo simbolo) {
            String clave = simbolo.getNombre().toUpperCase(); // En ensamblador, no hay case sensitivity
            if (tabla.containsKey(clave)) {
                return false;
            }
            tabla.put(clave, simbolo);
            return true;
        }

        public Simbolo buscar(String nombre) {
            return tabla.get(nombre.toUpperCase());
        }

        public Collection<Simbolo> obtenerTodos() {
            return tabla.values();
        }
    }

