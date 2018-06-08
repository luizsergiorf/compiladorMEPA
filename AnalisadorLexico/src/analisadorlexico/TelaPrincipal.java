/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analisadorlexico;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import glazed.TokenTableFormat;
import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import other.FiltroDeArquivos;
import other.UtilidadesArquivos;

/**
 *
 * @author FELLIPE PRATES \\ LUIZ SERGIO \\ TIAGO ELIAS DATA = 17-03-2018
 */
public class TelaPrincipal extends javax.swing.JFrame {

    /**
     * Creates new form TelaPrincipal
     */
    File arquivo = null;
    String conteudo = "";
    String linguagem = "";

    int tamanhoTabela = 68;
    int numerosLinhas = 0;
    private EventList<Token> tokens = new BasicEventList<>();
    Token tk = new Token();

    String simbolos = "+-*/=^<>()[]{}.,:;'#$"; //simbolos que devem ser reconhecidos
    char[] vetSimbolos = simbolos.toCharArray(); // vetor de char para acessar por char e comparar por posicao 

    String TableReservada[]
            = {"and", "downto", "in", "packed", "to", "array", "else", "inline", "procedure", "type", "asm", "end", "interface",
                "program", "unit", "begin", "file", "label", "record", "until", "case", "for", "mod", "repeat", "until", "const",
                "foward", "nil", "set", "uses", "constructor", "function", "not", "shl", "var", "destructor", "Goto", "object",
                "shr", "while", "div", "if", "of", "string", "with", "do", "implementation", "or", "then", "xor", "integer","real","boolean",
                "char","enumerado","subintervalo","string","array","record","set","file","text","pointer","then","begin","functio","do","while","longint"};
    
    public boolean pegarSimbolos(String palavra) {
        boolean valida = false;
        for (char c : vetSimbolos) {
            if (palavra.equals(c + "")) {
                valida = true;
                break;
            }
        }
        return valida;
    }

    public boolean palavraReservada(String palavra) {
        boolean validar = false;
        for (String s : TableReservada) {
            if (s.equals(palavra)) {
                validar = true;
                break;
            }
        }
        return validar;
    }

    public Token RotinaNumeros(Token token) {

        String lexema = "";
        int i = 0;
        int tamanho = token.getLexema().length();
        //Token tk = new Token();
        
        while (token.getLexema().charAt(i) >= 48 && token.getLexema().charAt(i) <= 57) {
            lexema = lexema + token.getLexema().charAt(i);

            if (i != tamanho - 1) {
                i++;
            } else {
                break;
            }
        }
        token.setClasse("cInt");

        if (token.getLexema().charAt(i) == 46) { //se for ponto (.)
            lexema = lexema + token.getLexema().charAt(i);
            i++;

            if ((token.getLexema().charAt(i) >= 48) && (token.getLexema().charAt(i) <= 57) && i < tamanho) {

                while ((token.getLexema().charAt(i) >= 48) && (token.getLexema().charAt(i) <= 57)) {
                    lexema = lexema + token.getLexema().charAt(i);

                    if (i != tamanho - 1) {
                        i++;
                    } else {
                        break;
                    }
                }
                token.setClasse("cReal");
            } else {
                jTextAreaSaida.setText("Erro Lexico - Caractere + Caractere + Desconhecido");
            }
        }

        return token;
    }

    public Token RotinaIdentificador(Token token) {
        String lexema = "";
        int tamanho = token.getLexema().length();
        int i = 0;
        //Token tk = new Token();

        while (((token.getLexema().charAt(i) >= 48) && (token.getLexema().charAt(i) <= 57))
                || ((token.getLexema().charAt(i) >= 65) && (token.getLexema().charAt(i) <= 90))
                || ((token.getLexema().charAt(i) >= 97) && (token.getLexema().charAt(i) <= 122))) {

            lexema = lexema + token.getLexema().charAt(i);

            if (i != tamanho - 1) {
                i++;
            } else {
                break;
            }
        }
        
        System.out.println("teste 3 " + token.getLexema());
        boolean reservada = palavraReservada(token.getLexema());
        boolean simbolo = getSpecialCharacterCount(token.getLexema());
        
        System.out.println("teste 4 " + reservada + simbolo);

        if (reservada) {
            token.setClasse("Palavra Reservada");
        }
        else if (simbolo) {
            token.setClasse("Caractere Especial");
        } else {
            token.setClasse("cId");
        }

        //token.setLexema(lexema);
        return token;
    }

    public boolean getSpecialCharacterCount(String s) {
        if (s == null || s.trim().isEmpty()) {
            System.out.println("Incorrect format of string");
            return false;
        }
        Pattern p = Pattern.compile("[^A-Za-z0-9.]");
        Matcher m = p.matcher(s);
        boolean b = m.find();
        if (b == true) {
            return true;
        } else {
        }
        return false;
    }
    
    int indice=0;
    public void analisadorSintaticoRecursivo(){
        S(tokens.get(indice));
    }
    
    public void S(Token t){
        Rule(t);
        S(t);
    }
    public void Rule(Token t){
        Head(t);
        Corpo(t);
        indice++;
        if(t.getLexema().equals('.')){
            jTextAreaSaida.setText("ANALISE FEITA COM SUCESSO!");
        }
    }
    public void Head(Token t){
        if(IdPread(t)){
            indice++;
            t = tokens.get(indice);
            Args(t);
        }
        else jTextAreaSaida.setText("ERRO SINTATICO!");
    }
    public boolean IdPread(Token t){
        if(t.getLexema().equals('p')    ||  t.getLexema().equals('q')   ||  t.getLexema().equals('r') || t.getLexema().equals('s')){
            return true;
        }
        else return false;
    }
    public void Args(Token t){
        if(t.getLexema().equals('(')){
            indice++;
            t = tokens.get(indice);
            idArg(t);
            t = tokens.get(indice);
            LArgs(t);
        }
        else jTextAreaSaida.setText("ERRO OPERANDO (");
    }
    public void idArg(Token t){
        if(idVar(t)){
           indice++;
        }else if(Cons(t)){
            indice++;
        } else Head(t);
    }
    public boolean idVar(Token t){
        if(t.getLexema().equals('X')    ||  t.getLexema().equals('Y')   ||  t.getLexema().equals('Z')){
            return true;
        }
        else return false;
    }
    public boolean Cons(Token t){
        if(t.getLexema().equals('a')    ||  t.getLexema().equals('b')   ||  t.getLexema().equals('c')   ||  t.getLexema().equals('d')   ||  t.getLexema().equals('e')){
            return true;
        }
        else return false;
    }
    public void LArgs(Token t){
        if(t.getLexema().equals(',')){
            indice++;
            t = tokens.get(indice);
            idArg(t);
            LArgs(t);
        }
        else jTextAreaSaida.setText("ERRO SINTATICO VIRGULA ,");
    }
    public void Corpo(Token t){
        if(t.getLexema().equals(':')){
            indice ++;
            t = tokens.get(indice);
            if(t.getLexema().equals('-')){
                Head(t);
                LCorpo(t);
            }
        } 
    }
    public void LCorpo(Token t){
        if(t.getLexema().equals(',')){
            indice++;
            t = tokens.get(indice);
            Head(t);
            LCorpo(t);
        }
        else jTextAreaSaida.setText("ERRO SINTATICO");
    }


    public TelaPrincipal() {
        initComponents();
        System.out.println("TESTANDO GITHUB!!!");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaSaida = new javax.swing.JTextArea();
        jTabbedPaneEdicao = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuArquivo = new javax.swing.JMenu();
        jMenuItemNovo = new javax.swing.JMenuItem();
        jMenuItemAbrir = new javax.swing.JMenuItem();
        jMenuItemSalvar = new javax.swing.JMenuItem();
        jMenuItemSalvarComo = new javax.swing.JMenuItem();
        jMenuItemSair = new javax.swing.JMenuItem();
        jMenuEditar = new javax.swing.JMenu();
        jMenuItemRecortar = new javax.swing.JMenuItem();
        jMenuItemCopiar = new javax.swing.JMenuItem();
        jMenuItemColar = new javax.swing.JMenuItem();
        jMenuProjeto = new javax.swing.JMenu();
        jMenuItemCompilar = new javax.swing.JMenuItem();
        jMenuSobre = new javax.swing.JMenu();
        jMenuItemProjeto = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ANALISADOR LEXICO");
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Resultado / (SAIDA)"));

        jTextAreaSaida.setColumns(20);
        jTextAreaSaida.setRows(5);
        jScrollPane2.setViewportView(jTextAreaSaida);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextArea.setBackground(new java.awt.Color(255, 255, 204));
        jTextArea.setColumns(20);
        jTextArea.setRows(5);
        jScrollPane1.setViewportView(jTextArea);

        jTabbedPaneEdicao.addTab("Fonte", jScrollPane1);

        jTable1.setModel(GlazedListsSwing.eventTableModel(tokens, new TokenTableFormat()));
        jTable1.setRequestFocusEnabled(false);
        jTable1.setRowHeight(20);
        jScrollPane3.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 524, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(317, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPaneEdicao.addTab("Tabela de Símbolos", jPanel3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPaneEdicao)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPaneEdicao)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPaneEdicao.getAccessibleContext().setAccessibleName("Edição");

        jPanel4.setBackground(new java.awt.Color(153, 153, 153));

        jLabel1.setText("PLAY");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenuArquivo.setText("Arquivo");

        jMenuItemNovo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemNovo.setText("Novo");
        jMenuItemNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNovoActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemNovo);

        jMenuItemAbrir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemAbrir.setText("Abrir");
        jMenuItemAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAbrirActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemAbrir);

        jMenuItemSalvar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSalvar.setText("Salvar");
        jMenuItemSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSalvarActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemSalvar);

        jMenuItemSalvarComo.setText("Salvar Como");
        jMenuItemSalvarComo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSalvarComoActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemSalvarComo);

        jMenuItemSair.setText("Sair");
        jMenuItemSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSairActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemSair);

        jMenuBar1.add(jMenuArquivo);

        jMenuEditar.setText("Editar");

        jMenuItemRecortar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemRecortar.setText("Recortar");
        jMenuEditar.add(jMenuItemRecortar);

        jMenuItemCopiar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCopiar.setText("Copiar");
        jMenuEditar.add(jMenuItemCopiar);

        jMenuItemColar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemColar.setText("Colar");
        jMenuEditar.add(jMenuItemColar);

        jMenuBar1.add(jMenuEditar);

        jMenuProjeto.setText("Projeto");

        jMenuItemCompilar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        jMenuItemCompilar.setText("Compilar");
        jMenuItemCompilar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCompilarActionPerformed(evt);
            }
        });
        jMenuProjeto.add(jMenuItemCompilar);

        jMenuBar1.add(jMenuProjeto);

        jMenuSobre.setText("Sobre");

        jMenuItemProjeto.setText("Projeto");
        jMenuItemProjeto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemProjetoActionPerformed(evt);
            }
        });
        jMenuSobre.add(jMenuItemProjeto);

        jMenuBar1.add(jMenuSobre);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAbrirActionPerformed
        // TODO add your handling code here:
        JFileChooser jfc = new JFileChooser(UtilidadesArquivos.getDiretorioDoPrograma());
        jfc.setFileFilter(new FiltroDeArquivos());

        String extensao, tipo, valores[];

        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            arquivo = jfc.getSelectedFile();
            extensao = UtilidadesArquivos.getExtensaoArquivo(arquivo);
            tipo = UtilidadesArquivos.getNomeArquivo(arquivo).contains("int") ? "int" : "string";

            if (extensao.equals("txt")) {  // Texto
                conteudo = UtilidadesArquivos.lerArquivoTexto(arquivo);
                //linguagem=conteudo;
                //valores = conteudo.split(UtilidadesArquivos.getCaractereNovaLinha());
                jTextArea.setText(conteudo);
            }
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        }
        if (conteudo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Arquivo não contém nada");
        }
        System.out.println(conteudo);


    }//GEN-LAST:event_jMenuItemAbrirActionPerformed

    private void jMenuItemSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSalvarActionPerformed
        // TODO add your handling code here:
        String novoConteudo = jTextArea.getText();
        if (arquivo == null) {
            jMenuItemSalvarComoActionPerformed(evt);
        } else {
            UtilidadesArquivos.salvarEmArquivoTexto(novoConteudo, arquivo);
        }

    }//GEN-LAST:event_jMenuItemSalvarActionPerformed

    private void jMenuItemSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSairActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jMenuItemSairActionPerformed

    private void jMenuItemProjetoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemProjetoActionPerformed
        // TODO add your handling code here:
        System.out.println("PROJETO IFTM 9 PERIODO");
        JOptionPane.showMessageDialog(rootPane, "PROJETO COMPILADORES\nIFTM\nENGENHARIA DA COMPUTAÇÃO");
    }//GEN-LAST:event_jMenuItemProjetoActionPerformed

    private void jMenuItemSalvarComoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSalvarComoActionPerformed
        // TODO add your handling code here:
        int opc = 0;
        JFileChooser j = new JFileChooser(UtilidadesArquivos.getDiretorioDoPrograma());

        j.setFileSelectionMode(JFileChooser.FILES_ONLY);
        opc = j.showSaveDialog(this);

        System.out.println(opc);

        if (opc == 0) {
            arquivo = j.getSelectedFile();
            UtilidadesArquivos.salvarEmArquivoTexto(jTextArea.getText(), arquivo);
        }

    }//GEN-LAST:event_jMenuItemSalvarComoActionPerformed

    private void jMenuItemNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNovoActionPerformed
        // TODO add your handling code here:
        jTextArea.setText("");
        arquivo = null;
        conteudo = "";
    }//GEN-LAST:event_jMenuItemNovoActionPerformed

    private void jMenuItemCompilarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCompilarActionPerformed
        //A PARTE DIFICIL
        tokens.clear();
        String codigo = jTextArea.getText();
        numerosLinhas = 0;
        tk = null;

        if (!codigo.isEmpty()) {

            int tamanho = codigo.length(); //tamanho do codigo
            String linhas[] = codigo.split("\n"); //divindo a string por linhas
            numerosLinhas = linhas.length; //numero de linhas
            boolean valida = true;

            int i = 0;
            int j = 0;
            int tam = 0;
            String lexema = "";

            for (i = 0; i < numerosLinhas; i++) {
                tam = linhas[i].length(); //tamanho da linha

                for (j = 0; j < tam; j++) {
                    if (linhas[i].charAt(j) != 32) {
                        if (getSpecialCharacterCount(linhas[i].charAt(j)+"")) {
                            if (!"".equals(lexema)) {
                                tk = new Token();
                                tk.setLexema(lexema);
                                tk.setLinha(i + 1);
                                tk.setColuna(j - lexema.length() + 1);
                                tokens.add(tk);
                                lexema = "";
                            }
                            tk = new Token();
                            tk.setLexema(linhas[i].charAt(j) + "");
                            tk.setLinha(i + 1);
                            tk.setColuna(j + 1);
                            tokens.add(tk);
                        } else {
                            lexema = lexema + linhas[i].charAt(j);
                        }
                    } else {

                        if (!"".equals(lexema)) {
                            tk = new Token();
                            tk.setLexema(lexema);
                            tk.setLinha(i + 1);
                            tk.setColuna(j - lexema.length() + 1);
                            tokens.add(tk);
                            lexema = "";
                        }

                    }
                }
                if (!"".equals(lexema)) {
                    tk = new Token();
                    tk.setLexema(lexema);
                    tk.setLinha(i + 1);
                    tk.setColuna(j - lexema.length() + 1);
                    tokens.add(tk);
                    lexema = "";
                }

            }

            for (Token token : tokens) {
                
                token.setLexema(token.getLexema().toLowerCase());
                
                if (token.getLexema().charAt(0) >= 48 && token.getLexema().charAt(0) <= 57) {
                    token = RotinaNumeros(token);
                    //tokens.get(i).setClasse(token.getClasse());
                    token.setClasse(token.getClasse());
                } else {
                    RotinaIdentificador(token);
                    token.setClasse(token.getClasse());
                    //tokens.get(i).setClasse(token.getClasse());
                }

                System.out.println("teste " + token.getClasse());
            }
            //System.out.println("CHEGOU AQUI");
            System.out.println("###TODOS TOKENS###\n" + tokens.toString());
        } else {
            String saida = jTextAreaSaida.getText();
            saida = saida + "ERRO - Digite o Código;\n";
            jTextAreaSaida.setText(saida);
        }
        
        analisadorSintaticoRecursivo();
        indice=0;
                
        
        
    }//GEN-LAST:event_jMenuItemCompilarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenuArquivo;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuEditar;
    private javax.swing.JMenuItem jMenuItemAbrir;
    private javax.swing.JMenuItem jMenuItemColar;
    private javax.swing.JMenuItem jMenuItemCompilar;
    private javax.swing.JMenuItem jMenuItemCopiar;
    private javax.swing.JMenuItem jMenuItemNovo;
    private javax.swing.JMenuItem jMenuItemProjeto;
    private javax.swing.JMenuItem jMenuItemRecortar;
    private javax.swing.JMenuItem jMenuItemSair;
    private javax.swing.JMenuItem jMenuItemSalvar;
    private javax.swing.JMenuItem jMenuItemSalvarComo;
    private javax.swing.JMenu jMenuProjeto;
    private javax.swing.JMenu jMenuSobre;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPaneEdicao;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea;
    private javax.swing.JTextArea jTextAreaSaida;
    // End of variables declaration//GEN-END:variables
}
