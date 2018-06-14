package glazed;


import analisadorlexico.Identificador;
import java.util.Comparator;



public class IdComparator implements Comparator<Identificador> {

    @Override
    public int compare(Identificador o1, Identificador o2) {
        return o1.getLexema().compareTo(o2.getLexema());
    }

}

