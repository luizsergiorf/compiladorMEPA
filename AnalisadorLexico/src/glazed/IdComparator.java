package glazed;


import analisadorlexico.Token;
import java.util.Comparator;



public class IdComparator implements Comparator<Token> {

    @Override
    public int compare(Token o1, Token o2) {
        return o1.getLexema().compareTo(o2.getLexema());
    }

}

