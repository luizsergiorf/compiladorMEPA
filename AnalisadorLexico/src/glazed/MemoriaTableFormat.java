package glazed;


import analisador.Memoria;
import ca.odell.glazedlists.gui.TableFormat;

/**
 *
 * @author Aluno
 */
public class MemoriaTableFormat implements TableFormat<Memoria>{

    @Override
    public int getColumnCount() {
        return 2; 
    }

    @Override
    public String getColumnName(int column) {
        switch (column){
            case 0:
                return "Endereço";
            case 1:
                return "Valor";
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getColumnValue(Memoria m, int column) {
        switch (column){
            case 0:
                return m.getEndereco();
            case 1:
                return m.getValor();
            default: 
                throw new IllegalArgumentException();
        }
    }
    
}
