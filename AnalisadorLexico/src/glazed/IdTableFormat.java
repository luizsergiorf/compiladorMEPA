package glazed;


import analisadorlexico.Identificador;
import analisadorlexico.Token;
import ca.odell.glazedlists.gui.TableFormat;

/**
 *
 * @author Aluno
 */
public class IdTableFormat implements TableFormat<Identificador>{

    @Override
    public int getColumnCount() {
        return 4; 
    }

    @Override
    public String getColumnName(int column) {
        switch (column){
            case 0:
                return "Lexema";
            case 1:
                return "Classe";
            case 2:
                return "Nivel";
            case 3:
                return "Endereço";
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getColumnValue(Identificador i, int column) {
        switch (column){
            case 0:
                return i.getLexema();
            case 1:
                return i.getClasse();
            case 2:
                return i.getNivel();
            case 3:
                return i.getEndereco();
            default: 
                throw new IllegalArgumentException();
        }
    }
    
}
