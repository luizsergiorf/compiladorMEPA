/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analisador;

/**
 *
 * @author Alunos
 */
public class Token {
    private String lexema; 
    private String classe;
    private int linha;
    private int coluna;

    public Token(String lexema, String classe, int linha, int coluna) {
        this.lexema = lexema;
        this.classe = classe;
        this.linha = linha;
        this.coluna = coluna;
    }
    
    public Token(){
        
    }

   
  
    /**
     * @return the lexema
     */
    public String getLexema() {
        return lexema;
    }

    /**
     * @param lexema the lexema to set
     */
    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    /**
     * @return the classe
     */
    public String getClasse() {
        return classe;
    }

    /**
     * @param classe the classe to set
     */
    public void setClasse(String classe) {
        this.classe = classe;
    }

    /**
     * @return the linha
     */
    public int getLinha() {
        return linha;
    }

    /**
     * @param linha the linha to set
     */
    public void setLinha(int linha) {
        this.linha = linha;
    }

    /**
     * @return the coluna
     */
    public int getColuna() {
        return coluna;
    }

    /**
     * @param coluna the coluna to set
     */
    public void setColuna(int coluna) {
        this.coluna = coluna;
    }

    @Override
    public String toString() {
        return "TOKEN >> " + "lexema= " + lexema + ", classe= " + classe + ", linha= " + linha + ", coluna= " + coluna + "\n";
    }
    
    
    
}