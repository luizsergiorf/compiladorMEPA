package glazed;


import analisador.Identificador;
import analisador.Memoria;
import java.util.Comparator;



public class MemoriaComparator implements Comparator<Memoria> {

    @Override
    public int compare(Memoria o1, Memoria o2) {
        return o1.getEndereco().compareTo(o2.getEndereco());
    }

}


