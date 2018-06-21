package glazed;


import analisador.Token;
import ca.odell.glazedlists.gui.TableFormat;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Aluno
 */
public class TokenTableFormat implements TableFormat<Token>{

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
                return "Linha";
            case 3:
                return "Coluna";
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Object getColumnValue(Token tk, int column) {
        switch (column){
            case 0:
                return tk.getLexema();
            case 1:
                return tk.getClasse();
            case 2:
                return tk.getLinha();
            case 3:
                return tk.getColuna();
            default: 
                throw new IllegalArgumentException();
        }
    }
    
}
