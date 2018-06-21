/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analisador;

/**
 *
 * @author LuizSergio
 */
public class Mepa {

    private int endereco;
    private String Rot;
    private String instrucao;
    private String K;

    public Mepa() {
        Rot = "";
        instrucao = "";
        K = "";
    }


    /**
     * @return the Rot
     */
    public String getRot() {
        return Rot;
    }

    /**
     * @param Rot the Rot to set
     */
    public void setRot(String Rot) {
        this.Rot = Rot;
    }

    /**
     * @return the instrucao
     */
    public String getInstrucao() {
        return instrucao;
    }

    /**
     * @param instrucao the instrucao to set
     */
    public void setInstrucao(String instrucao) {
        this.instrucao = instrucao;
    }

    /**
     * @return the K
     */
    public String getK() {
        return K;
    }

    /**
     * @param K the K to set
     */
    public void setK(String K) {
        this.K = K;
    }
    
    
     public int getEndereco() {
        return endereco;
    }


    public void setEndereco(int endereco) {
        this.endereco = endereco;
    }

    public Mepa(String Rot, String instrucao, String K) {
        this.Rot = Rot;
        this.instrucao = instrucao;
        this.K = K;
    }

    @Override
    public String toString() {
        return "Mepa{" + "Rot=" + Rot + ", instrucao=" + instrucao + ", K=" + K + '}' + "\n";
    }
    
}
