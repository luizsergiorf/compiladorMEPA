package glazed;


import analisador.Mepa;
import java.util.Comparator;



public class MepaComparator implements Comparator<Mepa> {

    @Override
    public int compare(Mepa o1, Mepa o2) {
        return o1.getInstrucao().compareTo(o2.getInstrucao());
    }

}

