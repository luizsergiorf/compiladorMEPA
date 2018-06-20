/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analisador;

/**
 *
 * @author felli
 */
public class Identificador {
    
    private String lexema; 
    private String classe;
    private int nivel;
    private int endereco;
    
    public Identificador(String lexema, String classe, int nivel, int endereco) {
        this.lexema = lexema;
        this.classe = classe;
        this.nivel = nivel;
        this.endereco = endereco;
    }
    
    public Identificador(){
        
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
     * @return the nivel
     */
    public int getNivel() {
        return nivel;
    }

    /**
     * @param nivel the nivel to set
     */
    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    /**
     * @return the deslocamento
     */
    public int getEndereco() {
        return endereco;
    }

    /**
     * @param deslocamento the deslocamento to set
     */
    public void setEndereco(int endereco) {
        this.endereco = endereco;
    }
    
    @Override
    public String toString() {
        return "TOKEN >> " + "lexema= " + lexema + ", classe= " + classe + ", endereco= " + endereco + "\n";
    }
    
    
}
