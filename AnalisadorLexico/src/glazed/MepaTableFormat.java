package glazed;


import analisador.Mepa;
import ca.odell.glazedlists.gui.TableFormat;


public class MepaTableFormat implements TableFormat<Mepa>{

    @Override
    public int getColumnCount() {
        return 4; 
    }

    @Override
    public String getColumnName(int column) {
        switch (column){
            case 0:
                return "Endereço";
            case 1:
                return "Rot";
            case 2:
                return "Instrução";
            case 3:
                return "K";
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getColumnValue(Mepa mp, int column) {
        switch (column){
            case 0:
                return mp.getEndereco();
            case 1:
                return mp.getRot();
            case 2:
                return mp.getInstrucao();
            case 3:
                return mp.getK();
            default: 
                throw new IllegalArgumentException();
        }
    }
    
}

