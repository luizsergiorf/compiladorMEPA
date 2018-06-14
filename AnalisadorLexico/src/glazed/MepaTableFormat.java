package glazed;


import analisadorlexico.Mepa;
import ca.odell.glazedlists.gui.TableFormat;


public class MepaTableFormat implements TableFormat<Mepa>{

    @Override
    public int getColumnCount() {
        return 3; 
    }

    @Override
    public String getColumnName(int column) {
        switch (column){
            case 0:
                return "Rot";
            case 1:
                return "Instrução";
            case 2:
                return "K";
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getColumnValue(Mepa mp, int column) {
        switch (column){
            case 0:
                return mp.getRot();
            case 1:
                return mp.getInstrucao();
            case 2:
                return mp.getK();
            default: 
                throw new IllegalArgumentException();
        }
    }
    
}

