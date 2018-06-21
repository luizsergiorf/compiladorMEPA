/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import analisador.Token;
import analisador.Mepa;
import analisador.Memoria;
import analisador.Identificador;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import glazed.IdTableFormat;
import glazed.MepaTableFormat;
import glazed.TokenTableFormat;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.text.Caret;
import other.FiltroDeArquivos;
import other.UtilidadesArquivos;

/**
 *
 * @author FELLIPE PRATES \\ LUIZ SERGIO \\ TIAGO ELIAS DATA = 31-05-2018
 */
public class TelaPrincipal extends javax.swing.JFrame {

    /**
     * Creates new form TelaPrincipal
     */
    private JanelaAjuda dialog = null;

    File arquivo = null;
    String conteudo = "";
    String linguagem = "";

    Token tokenAux;
    boolean validaCompila;
    boolean validaId;
    int indexToken;
    int indexIdentificador;

    //VARIAVEIS PARA GERACAO DO MEPA
    int Addr = 0; //endereço da pilha mepa
    String opMul;
    String opAd;
    int S = 0; // Variável cujo objetivo é determinar o endereço para uma variável na Pilha de Dados
    int R = 0; // Variável cujo objetivo é determinar um endereço para um rótulo na Pilha de Código
    int RotFor = 0;
    int RotEnd = 0;
    int RotWhile = 0;
    int RotRepeat = 0;
    String rel;
    int RotElse = 0;
    int RotEndIf = 0;
    int RotEndWhile = 0;
    int RotEndRepeat = 0;

    //VARIAVEIS PARA LEXEMA
    int tamanhoTabela = 68;
    int numerosLinhas = 0;
    private final EventList<Token> tokens = new BasicEventList<>();
    private final EventList<Identificador> identificadores = new BasicEventList<>();
    private final EventList<Mepa> codigoMepa = new BasicEventList<>();
    private final EventList<Memoria> memoria = new BasicEventList<>();

    Token tk = new Token();
    Identificador ident = new Identificador();

    String simbolos = "+-*/=^<>()[]{}.,:;'#$"; //simbolos que devem ser reconhecidos
    char[] vetSimbolos = simbolos.toCharArray(); // vetor de char para acessar por char e comparar por posicao 

    //TODAS AS PALAVRAS RESERVADAS
    String TableReservada[]
            = {"and", "downto", "in", "packed", "to", "array", "else", "inline", "procedure", "type", "asm", "end", "interface",
                "program", "unit", "begin", "file", "label", "record", "until", "case", "for", "mod", "repeat", "until", "const",
                "foward", "nil", "set", "uses", "constructor", "function", "not", "shl", "var", "destructor", "goto", "object",
                "shr", "while", "div", "if", "of", "string", "with", "do", "implementation", "or", "then", "xor", "shotint", "integer", "longint", "byte",
                "word", "real", "single", "double", "extended", "comp", "string", "char", "boolean", "end.", "writeln", "readln", "read", "write"};

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

        while (((token.getLexema().charAt(i) >= 48) && (token.getLexema().charAt(i) <= 57)) //numeros
                || ((token.getLexema().charAt(i) >= 65) && (token.getLexema().charAt(i) <= 90)) //alfabeto maiusculo
                || ((token.getLexema().charAt(i) >= 97) && (token.getLexema().charAt(i) <= 122))) { //alfabeto minusculo

            lexema = lexema + token.getLexema().charAt(i);

            //condiçao de parada
            if (i != tamanho - 1) {
                i++;
            } else {
                break;
            }
        }

        boolean reservada = palavraReservada(token.getLexema());
        boolean simbolo = getSpecialCharacterCount(token.getLexema(), true);

        //System.out.println("SIMBOLO " + reservada + simbolo);
        if (reservada) {
            token.setClasse("Palavra Reservada");
        } else if (simbolo) {

            String ce = token.getLexema();
            //System.out.println("MOSTRA - " + ce);

            if (ce.charAt(0) == '.') {
                token.setClasse("cPt");
            }
            if (ce.charAt(0) == '"') {
                token.setClasse("cStr");
            }
            if (ce.charAt(0) == '´') {
                token.setClasse("cStr");
            }
            if (ce.charAt(0) == '\'') {
                token.setClasse("cStr");
            }
            if (":".equals(ce)) {
                token.setClasse("2 pontos");
            }
            if ("{".equals(ce)) {
                token.setClasse("ChA");
            }
            if ("}".equals(ce)) {
                token.setClasse("ChF");
            }
            if (",".equals(ce)) {
                token.setClasse("cVir");
            }
            if ("=".equals(ce)) {
                token.setClasse("cEQ");
            }
            if ("(".equals(ce)) {
                token.setClasse("cLPar");
            }
            if (")".equals(ce)) {
                token.setClasse("cDPar");
            }
            if ("+".equals(ce)) {
                token.setClasse("cAdd");
            }
            if ("-".equals(ce)) {
                token.setClasse("cSub");
            }
            if ("/".equals(ce)) {
                token.setClasse("cDiv");
            }
            if ("*".equals(ce)) {
                token.setClasse("cMul");
            }
            if ("<".equals(ce)) {
                token.setClasse("cLT");
            }
            if (">".equals(ce)) {
                token.setClasse("cGT");
            }
            if (";".equals(ce)) {
                token.setClasse("cPVir");
            }
            if (">=".equals(ce)) {
                token.setClasse("cGE");
            }
            if ("<=".equals(ce)) {
                token.setClasse("cLE");
            }
            if ("<>".equals(ce)) {
                token.setClasse("cNE");
            }
            if (":=".equals(ce)) {
                token.setClasse("cAtr");
            }
            //token.setClasse("Caractere Especial");
        } else {
            token.setClasse("cId");
        }

        //token.setLexema(lexema);
        return token;
    }

    public boolean getSpecialCharacterCount(String s, boolean validaReal) {
        if (s == null || s.trim().isEmpty()) {
            System.out.println("Incorrect format of string");
            return false;
        }
        Pattern p = Pattern.compile("[^A-Za-z0-9_]"); // aceita ponto
        Pattern p2 = Pattern.compile("[^A-Za-z0-9]"); // não aceita ponto

        if (validaReal) {
            Matcher m = p.matcher(s);
            boolean b = m.find();
            return b == true;
        } else {
            Matcher m = p2.matcher(s);
            boolean b = m.find();
            return b == true;
        }

    }

    public TelaPrincipal() {
        System.out.println("==== SOFTWARE DA DISCIPLINA DE COMPILADORES ====");
        initComponents();
        jTextArea.setText("");
        jTextAreaSaida.setText("");
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
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabelExecutar = new javax.swing.JLabel();
        jLabelCompilar = new javax.swing.JLabel();
        jLabelInfo = new javax.swing.JLabel();
        jLabelCompilarExecutar = new javax.swing.JLabel();
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
        jMenuItemCompExec = new javax.swing.JMenuItem();
        jMenuSobre = new javax.swing.JMenu();
        jMenuItemProjeto = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("COMPILADOR");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));

        jPanel2.setBackground(new java.awt.Color(153, 153, 153));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Resultado / (SAIDA)"));

        jTextAreaSaida.setEditable(false);
        jTextAreaSaida.setColumns(20);
        jTextAreaSaida.setFont(new java.awt.Font("Monospaced", 0, 14)); // NOI18N
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
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
        );

        jTextArea.setColumns(20);
        jTextArea.setFont(new java.awt.Font("Courier New", 0, 16)); // NOI18N
        jTextArea.setRows(5);
        jTextArea.setText("Program Somatorio\nVar\n\tSoma, Conta, Ini, Fim : integer;\nBegin\n\tIni := 0;\n\tFim := 0;\n\tConta := 0;\n\tSoma := 0;\n\tread( Ini, Fim );\n\tFor Conta := Ini To Fim do begin\n\t\tSoma := Soma + Conta;\n\tEnd;\n\twrite( Soma );\nEnd.");
        jScrollPane1.setViewportView(jTextArea);

        jTabbedPaneEdicao.addTab("Fonte", jScrollPane1);

        jTable1.setModel(GlazedListsSwing.eventTableModel(tokens, new TokenTableFormat()));
        jTable1.setCellSelectionEnabled(true);
        jTable1.setRequestFocusEnabled(false);
        jTable1.setRowHeight(20);
        jScrollPane3.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 770, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(170, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPaneEdicao.addTab("Itens Lexicos", jPanel3);

        jTable2.setModel(GlazedListsSwing.eventTableModel(identificadores, new IdTableFormat()));
        jTable2.setCellSelectionEnabled(true);
        jScrollPane4.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(488, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jTabbedPaneEdicao.addTab("Tabela de Símbolos", jPanel5);

        jTable3.setModel(GlazedListsSwing.eventTableModel(codigoMepa, new MepaTableFormat()));
        jTable3.setCellSelectionEnabled(true);
        jScrollPane5.setViewportView(jTable3);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(488, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPaneEdicao.addTab("MEPA", jPanel6);

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
                .addComponent(jTabbedPaneEdicao, javax.swing.GroupLayout.PREFERRED_SIZE, 460, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPaneEdicao.getAccessibleContext().setAccessibleName("Edição");

        jPanel4.setBackground(new java.awt.Color(51, 51, 51));

        jLabelExecutar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/btnPlay.png"))); // NOI18N
        jLabelExecutar.setToolTipText("Executar");
        jLabelExecutar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelExecutarMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelExecutarMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabelExecutarMousePressed(evt);
            }
        });

        jLabelCompilar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/compilarbtn.png"))); // NOI18N
        jLabelCompilar.setToolTipText("Compilar");
        jLabelCompilar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelCompilarMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabelCompilarMousePressed(evt);
            }
        });

        jLabelInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/ajudabtn.png"))); // NOI18N
        jLabelInfo.setToolTipText("Ajuda");
        jLabelInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelInfoMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabelInfoMousePressed(evt);
            }
        });

        jLabelCompilarExecutar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/compexecbtn.png"))); // NOI18N
        jLabelCompilarExecutar.setToolTipText("Compilar & Executar");
        jLabelCompilarExecutar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelCompilarExecutarMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jLabelCompilarExecutarMousePressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelCompilar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelExecutar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelCompilarExecutar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelInfo)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelCompilarExecutar)
                    .addComponent(jLabelInfo)
                    .addComponent(jLabelCompilar)
                    .addComponent(jLabelExecutar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenuArquivo.setText("Arquivo");

        jMenuItemNovo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemNovo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/novobtn.png"))); // NOI18N
        jMenuItemNovo.setText("Novo");
        jMenuItemNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNovoActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemNovo);

        jMenuItemAbrir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/openbtn.png"))); // NOI18N
        jMenuItemAbrir.setText("Abrir");
        jMenuItemAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAbrirActionPerformed(evt);
            }
        });
        jMenuArquivo.add(jMenuItemAbrir);

        jMenuItemSalvar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/savebtn.png"))); // NOI18N
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

        jMenuItemSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/sairbtn.png"))); // NOI18N
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
        jMenuItemRecortar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/tesourabtn.png"))); // NOI18N
        jMenuItemRecortar.setText("Recortar");
        jMenuItemRecortar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRecortarActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemRecortar);

        jMenuItemCopiar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCopiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/copiarbtn.png"))); // NOI18N
        jMenuItemCopiar.setText("Copiar");
        jMenuItemCopiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopiarActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemCopiar);

        jMenuItemColar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemColar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/colarbtn.png"))); // NOI18N
        jMenuItemColar.setText("Colar");
        jMenuItemColar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemColarActionPerformed(evt);
            }
        });
        jMenuEditar.add(jMenuItemColar);

        jMenuBar1.add(jMenuEditar);

        jMenuProjeto.setText("Projeto");

        jMenuItemCompilar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        jMenuItemCompilar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/compilaricon.png"))); // NOI18N
        jMenuItemCompilar.setText("Compilar");
        jMenuItemCompilar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCompilarActionPerformed(evt);
            }
        });
        jMenuProjeto.add(jMenuItemCompilar);

        jMenuItemCompExec.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0));
        jMenuItemCompExec.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/compexecIco.png"))); // NOI18N
        jMenuItemCompExec.setText("Compilar & Executar");
        jMenuItemCompExec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCompExecActionPerformed(evt);
            }
        });
        jMenuProjeto.add(jMenuItemCompExec);

        jMenuBar1.add(jMenuProjeto);

        jMenuSobre.setText("Sobre");

        jMenuItemProjeto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagens/infoicon.png"))); // NOI18N
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

            //RENICIALIZANDO AS ARRAYS QUANDO ABRIR OUTRO ARQUIVO
            codigoMepa.clear();
            identificadores.clear();
            memoria.clear();
            tokens.clear();
            jTextAreaSaida.setBackground(new java.awt.Color(255, 255, 255));
            jTextAreaSaida.setText("");

        }
        if (conteudo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Arquivo não contém nada");
        }
        System.out.println("\n====FILE OPEN====\n\n" + conteudo);


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

        switch (JOptionPane.showConfirmDialog(null, "Deseja salvar arquivo?")) {
            case 0:
                System.out.println("botao yes clicado");

                String novoConteudo = jTextArea.getText();
                if (arquivo == null) {

                    int opc = 0;
                    JFileChooser j = new JFileChooser(UtilidadesArquivos.getDiretorioDoPrograma());

                    j.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    opc = j.showSaveDialog(this);

                    System.out.println(opc);

                    if (opc == 0) {
                        arquivo = j.getSelectedFile();
                        UtilidadesArquivos.salvarEmArquivoTexto(jTextArea.getText(), arquivo);
                    }

                } else {
                    UtilidadesArquivos.salvarEmArquivoTexto(novoConteudo, arquivo);
                }

                System.exit(0);
                break;
            case 1:
                System.out.println("botao no clicado");
                System.exit(0);
                break;
            case 2:
                System.out.println("botao cancel clicado");
                this.setDefaultCloseOperation(TelaPrincipal.DO_NOTHING_ON_CLOSE);
                break;
        }

    }//GEN-LAST:event_jMenuItemSairActionPerformed

    private void jMenuItemProjetoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemProjetoActionPerformed
        // TODO add your handling code here:
        System.out.println("PROJETO IFTM 9 PERIODO");
        JOptionPane.showMessageDialog(rootPane, "PROJETO COMPILADORES\n"
                + "IFTM / ENGENHARIA DA COMPUTAÇÃO\n"
                + "======================================\n"
                + "FELLIPE PRATES - LUIZ SERGIO - TIAGO ELIAS");
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
        if (opc == 1) { //se clicou em cancelar

        }

    }//GEN-LAST:event_jMenuItemSalvarComoActionPerformed

    private void jMenuItemNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNovoActionPerformed
        // TODO add your handling code here:

        if (!jTextArea.getText().equals("")) {

            int opc = 0;
            JFileChooser j = new JFileChooser(UtilidadesArquivos.getDiretorioDoPrograma());

            j.setFileSelectionMode(JFileChooser.FILES_ONLY);
            opc = j.showSaveDialog(this);

            System.out.println(opc);

            if (opc == 0) {
                arquivo = j.getSelectedFile();
                UtilidadesArquivos.salvarEmArquivoTexto(jTextArea.getText(), arquivo);

                jTextArea.setText("");
                arquivo = null;
                conteudo = "";
            }
            if (opc == 1) { //se clicou em cancelar

            }

        }

    }//GEN-LAST:event_jMenuItemNovoActionPerformed

    public void analiseLexica() {
        tokens.clear();
        identificadores.clear();
        jTextAreaSaida.setText("");
        String codigo = jTextArea.getText();
        numerosLinhas = 0;
        codigoMepa.clear();
        tk = null;

        if (!codigo.isEmpty()) {

            int tamanho = codigo.length(); //tamanho do codigo
            String linhas[] = codigo.split("\n"); //divindo a string por linhas
            numerosLinhas = linhas.length; //numero de linhas
            boolean valida = true;
            boolean validaReal = true;
            int i = 0;
            int j = 0;
            int tam = 0;
            String lexema = "";

            for (i = 0; i < numerosLinhas; i++) {
                tam = linhas[i].length(); //tamanho da linha
                try {
                    for (j = 0; j < tam; j++) {

                        if (linhas[i].charAt(j) > 32) { //verificando se o caractere eh diferente de um espaço ou tabulaçao

                            //para eliminar os comentarios
                            if (linhas[i].charAt(j) == '{') {
                                while (linhas[i].charAt(j) != '}') {
                                    j++;
                                }
                            } //para cadeia de string
                            else if (linhas[i].charAt(j) == '"' || linhas[i].charAt(j) == '\'' || linhas[i].charAt(j) == '´') {
                                int cadeia = 0;
                                while (cadeia != 2) {

                                    lexema = lexema + linhas[i].charAt(j);

                                    if (linhas[i].charAt(j) == '"' || linhas[i].charAt(j) == '\'' || linhas[i].charAt(j) == '´') {
                                        cadeia++;
                                    }
                                    j++;
                                }
                                j--;
                                if (!"".equals(lexema)) {
                                    tk = new Token();
                                    tk.setLexema(lexema);
                                    tk.setLinha(i + 1);
                                    tk.setColuna(j - lexema.length() + 1);
                                    tokens.add(tk);
                                    lexema = "";
                                }

                            } // para verificar as combinaçoes <> >= <= :=
                            else if ((linhas[i].charAt(j) == ':' && linhas[i].charAt(j + 1) == '=' && j + 1 != tam)
                                    || (linhas[i].charAt(j) == '>' && linhas[i].charAt(j + 1) == '=' && j + 1 != tam)
                                    || (linhas[i].charAt(j) == '<' && linhas[i].charAt(j + 1) == '=' && j + 1 != tam)
                                    || (linhas[i].charAt(j) == '<' && linhas[i].charAt(j + 1) == '>') && j + 1 != tam) {

                                if (!"".equals(lexema)) {

                                    tk = new Token();
                                    tk.setLexema(lexema);
                                    tk.setLinha(i + 1);
                                    tk.setColuna(j + 1);
                                    tokens.add(tk);
                                    lexema = "";
                                } else {
                                    lexema = "";
                                }

                                lexema = lexema + linhas[i].charAt(j) + linhas[i].charAt(j + 1) + "";
                                j++;

                                if (!"".equals(lexema)) {
                                    tk = new Token();
                                    tk.setLexema(lexema);
                                    tk.setLinha(i + 1);
                                    tk.setColuna(j + 1);
                                    tokens.add(tk);
                                    lexema = "";
                                }
                            } else if (getSpecialCharacterCount((linhas[i].charAt(j) + ""), validaReal)) {
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
                                validaReal = true; // reiniciando
                            } else {
                                if (linhas[i].charAt(j) == 46) { // ponto, se ja ocrrer 1 x ponto não deixa passar mais ponto
                                    validaReal = false;
                                }
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
                            lexema = "";

                        }
                    }

                } catch (Exception e) {
                    //jTextAreaSaida.append("ERRO NA SINTAXE DO CODIGO! - LINHA " + (i + 1) + "\n");
                    System.out.println("ERRO > " + e);
                }
                //System.out.println("TESTE LEXEMA - " + lexema);
                if (!"".equals(lexema)) {

                    if (lexema.toLowerCase().equals("end.")) { //deixando tudo minusculo

                        tk = new Token();
                        tk.setLexema("end");
                        tk.setLinha(i + 1);
                        tk.setColuna(j - lexema.length() + 1);
                        tokens.add(tk);

                        tk = new Token();
                        tk.setLexema(".");
                        tk.setLinha(i + 1);
                        tk.setColuna(j);
                        tokens.add(tk);

                    } else {
                        tk = new Token();
                        tk.setLexema(lexema);
                        tk.setLinha(i + 1);
                        tk.setColuna(j - lexema.length() + 1);
                        tokens.add(tk);
                        lexema = "";
                    }

                }

            }

            for (Token token : tokens) {

                //deixando a string minuscula
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

                if (token.getLexema().equals(".")) {
                    token.setClasse("cPt");
                }
            }

            tk = new Token();
            tk.setLexema("Eof");
            tk.setClasse("cEof");
            tk.setLinha(i);
            tk.setColuna(j + 1);
            tokens.add(tk);

            //-------------------
            //IDENTIFICADORES
            //-------------------
            int endereco = -1;
            boolean verifica = false;
            List<Identificador> identAux;

            for (Token token : tokens) {
                if (token.getClasse().equals("cId")) {

                    ident = new Identificador();
                    ident.setLexema(token.getLexema());
                    ident.setClasse(token.getClasse());
                    ident.setNivel(0);
                    ident.setEndereco(endereco);

                    for (Identificador aux : identificadores) {
                        verifica = aux.getLexema().equals(ident.getLexema());
                        if (verifica) {
                            break;
                        }
                    }

                    if (!verifica) {
                        identificadores.add(ident);
                    }
                    verifica = false;
                    endereco++;
                }
            }

        } else {
            String saida = jTextAreaSaida.getText();
            saida = saida + "ERRO - Digite o Código;\n";
            jTextAreaSaida.setText(saida);
        }
    }

    public void analiseSintatica() {
        //textMepa.setText("");
        indexToken = 0;
        indexIdentificador = 0;
        codigoMepa.clear();
        end = 0;

        Addr = 0; //endereço da pilha mepa
        opMul = "";
        opAd = "";
        S = 0; // Variável cujo objetivo é determinar o endereço para uma variável na Pilha de Dados
        R = 0; // Variável cujo objetivo é determinar um endereço para um rótulo na Pilha de Código
        RotFor = 0;
        RotEnd = 0;
        RotWhile = 0;

        tokenAux = null;

        RotRepeat = 0;
        rel = "";
        RotElse = 0;
        RotEndIf = 0;


        program();
    }

    public void program() {

        tokenAux = tokens.get(indexToken); // pegando lexema do indice
        validaCompila = tokenAux.getLexema().equals("program"); //verifica se esta escrito program

        if (validaCompila) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // atualizar lexama
            validaCompila = tokenAux.getClasse().equals("cId");

            if (validaCompila) {
                acao1();
                indexToken++;
                tokenAux = tokens.get(indexToken); // atualzia lexama

                //ir para funcao corpo
                corpo();

                //if (tokenAux.getLexema().equals("end")) {
                //    indexToken++;
                //    tokenAux = tokens.get(indexToken); // atualzia lexama
                //}
                if (tokenAux.getLexema().equals(".")) {
                    acao30();

                    int nlinhas;
                    String linhas[] = jTextAreaSaida.getText().split("\n"); //divindo a string por linhas
                    nlinhas = linhas.length; //numero de linhas

                    if (nlinhas > 1) {
                        jTextAreaSaida.setBackground(new java.awt.Color(255, 209, 176)); //setando cor vermelha
                        System.out.println("ANALISE SINTATICA FEITA!");
                        System.out.println("FIM DO CODIGO MAS COM ERRO!");
                        jTextAreaSaida.append("FIM DO CODIGO, MAS TEVE ERRO.\n");
                        JOptionPane.showMessageDialog(null, "ENCONTROU ALGUM ERRO!", "COMPILADO", 1);

                    } else {
                        jTextAreaSaida.setBackground(new java.awt.Color(188, 255, 233)); //setando cor se o codigo deu certo
                        System.out.println("ANALISE SINTATICA FEITA!");
                        System.out.println("FIM DO CODIGO!");
                        jTextAreaSaida.append("FIM DO CODIGO!\n");
                        JOptionPane.showMessageDialog(null, "CODIGO ESTÁ CORRETO!", "COMPILADO", 1);
                    }

                }
                //System.out.println("FIM - " + tokenAux.toString());
            } else {
                System.out.println("Error declarar nome Função.");
                jTextAreaSaida.append("Error - Declarar nome Função.\n");
            }

        } else {
            System.out.println("Error - Iniciar o codigo com program.");
            jTextAreaSaida.append("Error - Iniciar o codigo com program.\n");
            //break;
        }
    }

    int end = 0; //endereco MEPA

    public void Geracode(String Rot, String Inst, String K) {
        Mepa mepaAux;
        mepaAux = new Mepa();
        mepaAux.setInstrucao(Inst);
        mepaAux.setK(K);
        mepaAux.setRot(Rot);
        mepaAux.setEndereco(end);
        end++;
        codigoMepa.add(mepaAux); //adicionando ao MEPA
        //textMepa.setText(codigoMepa.toString());
    }

    public void corpo() {
        declara();
        //verifica se tem o begin apos o <corpo>
        if (tokenAux.getLexema().equals("begin")) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // começar com a <sentencas>
            sentencas();

            if (!tokenAux.getLexema().equals("end")) {

                //System.out.println(" TESTE - " + tokenAux.toString());
                System.out.println("Error - Declarar end ou ; no codigo.");
                jTextAreaSaida.append("Error - Declarar end ou ; no codigo.  LINHA = " + tokenAux.getLinha() + "\n");
            } else {

                indexToken++;
                tokenAux = tokens.get(indexToken); // começar com a <sentencas>

                // System.out.println("TESTE CORPO - " + tokenAux.toString());
            }

        } else {
            System.out.println("Error - Declarar begin.");
            jTextAreaSaida.append("Error - Declarar begin.  LINHA = " + tokenAux.getLinha() + "\n");
        }
    }

    public void sentencas() {
        comando();
        //System.out.println("CHEGUEI NO PV - " + tokenAux.toString());
        mais_sentencas();
    }

    public void mais_sentencas() {

        if (tokenAux.getLexema().equals(";")) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // começar com a <sentencas>
            cont_sentencas();
        } else {
        }
    }

    public void cont_sentencas() {

        if (!tokenAux.getLexema().equals("Eof")) {
            sentencas();
        }
    }

    public void comando() {

        if (tokenAux.getLexema().equals("read")) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // começar com a <var_read>
            //verificando se abriu parenteses
            if (tokenAux.getLexema().equals("(")) {
                indexToken++;
                tokenAux = tokens.get(indexToken);
                var_read();

                //depois dos var_read e mais_var_read...
                if (!tokenAux.getLexema().equals(")")) {
                    System.out.println("Error - Fechar parenteses.");
                    jTextAreaSaida.append("Error - Fechar parenteses.   LINHA = " + tokenAux.getLinha() + "\n");
                } else {
                    indexToken++;
                    tokenAux = tokens.get(indexToken);
                }
            } else {
                System.out.println("Error - Abrir parenteses.");
                jTextAreaSaida.append("Error - Abrir parenteses.    LINHA = " + tokenAux.getLinha() + "\n");
            }
        } else if (tokenAux.getLexema().equals("write")) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // começar com a <var_read>
            //verificando se abriu parenteses
            if (tokenAux.getLexema().equals("(")) {
                indexToken++;
                tokenAux = tokens.get(indexToken);
                var_write();

                //System.out.println("WRITE " + tokenAux.toString());
                //depois dos var_read e mais_var_read...
                if (!tokenAux.getLexema().equals(")")) {
                    System.out.println("Error - Fechar parenteses.");
                    jTextAreaSaida.append("Error - Fechar parenteses.   LINHA = " + tokenAux.getLinha() + "\n");
                } else {
                    indexToken++;
                    tokenAux = tokens.get(indexToken);
                }
            } else {
                System.out.println("Error - Abrir parenteses.");
                jTextAreaSaida.append("Error - Abrir parenteses.    LINHA = " + tokenAux.getLinha() + "\n");
            }
        } else if (tokenAux.getLexema().equals("for")) {
            indexToken++;
            tokenAux = tokens.get(indexToken);

            if (tokenAux.getClasse().equals("cId")) {
                acao25();

                indexToken++;
                tokenAux = tokens.get(indexToken);

                if (tokenAux.getLexema().equals(":=")) {
                    indexToken++;
                    tokenAux = tokens.get(indexToken);
                    expressao();
                    acao26();

                    if (tokenAux.getLexema().equals("to")) {
                        acao27();
                        indexToken++;
                        tokenAux = tokens.get(indexToken);

                        expressao();
                        acao28();

                        if (tokenAux.getLexema().equals("do")) {
                            indexToken++;
                            tokenAux = tokens.get(indexToken);

                            if (tokenAux.getLexema().equals("begin")) {
                                indexToken++;
                                tokenAux = tokens.get(indexToken);
                                sentencas();

                                if (tokenAux.getLexema().equals("end")) {
                                    acao29();
                                    indexToken++;
                                    tokenAux = tokens.get(indexToken);
                                }

                            } else {
                                System.out.println("Error - Declarar palavra reservada begin.");
                                jTextAreaSaida.append("Error - Declarar palavra reservada begin.    LINHA = " + tokenAux.getLinha() + "\n");
                            }
                        } else {
                            System.out.println("Error - Declarar palavra reservada do.");
                            jTextAreaSaida.append("Error - Declarar palavra reservada do.    LINHA = " + tokenAux.getLinha() + "\n");
                        }

                    } else {
                        System.out.println("Error - Declarar palavra reservada to.");
                        jTextAreaSaida.append("Error - Declarar palavra reservada to.    LINHA = " + tokenAux.getLinha() + "\n");
                    }

                } else {
                    System.out.println("Error - Declarar atribuição :=.");
                    jTextAreaSaida.append("Error - Declarar atribuição :=.    LINHA = " + tokenAux.getLinha() + "\n");
                }

            } else {
                System.out.println("Error - Variavel desconhecida.");
                jTextAreaSaida.append("Error - Variavel desconhecida.    LINHA = " + tokenAux.getLinha() + "\n");
            }

        } else if (tokenAux.getLexema().equals("repeat")) {
            acao23();

            indexToken++;
            tokenAux = tokens.get(indexToken);
            sentencas();
            if (tokenAux.getLexema().equals("until")) {
                indexToken++;
                tokenAux = tokens.get(indexToken);
                if (tokenAux.getLexema().equals("(")) {

                    indexToken++;
                    tokenAux = tokens.get(indexToken);

                    condicao();

                    if (tokenAux.getLexema().equals(")")) {

                        acao24();
                        indexToken++;
                        tokenAux = tokens.get(indexToken);
                    } else {
                        System.out.println("Error - Fechar parenteses (.");
                        jTextAreaSaida.append("Error - Fechar parenteses (.    LINHA = " + tokenAux.getLinha() + "\n");
                    }

                } else {
                    System.out.println("Error - Abrir parenteses (.");
                    jTextAreaSaida.append("Error - Abrir parenteses (.    LINHA = " + tokenAux.getLinha() + "\n");
                }

            } else {
                System.out.println("Error - Declarar palavra reservada until.");
                jTextAreaSaida.append("Error - Declarar palavra reservada until.    LINHA = " + tokenAux.getLinha() + "\n");

            }

        } else if (tokenAux.getLexema().equals("while")) {
            acao20();
            indexToken++;
            tokenAux = tokens.get(indexToken);

            if (tokenAux.getLexema().equals("(")) {
                indexToken++;
                tokenAux = tokens.get(indexToken);
                condicao();

                if (tokenAux.getLexema().equals(")")) {
                    acao21();
                    indexToken++;
                    tokenAux = tokens.get(indexToken);
                    if (tokenAux.getLexema().equals("do")) {
                        indexToken++;
                        tokenAux = tokens.get(indexToken);
                        if (tokenAux.getLexema().equals("begin")) {
                            indexToken++;
                            tokenAux = tokens.get(indexToken);
                            sentencas();

                            if (tokenAux.getLexema().equals("end")) {
                                acao22();
                                indexToken++;
                                tokenAux = tokens.get(indexToken);
                            } else {
                                System.out.println("Error - Declarar palavra reservada end.");
                                jTextAreaSaida.append("Error - Declarar palavra reservada end.    LINHA = " + tokenAux.getLinha() + "\n");
                            }
                        }

                    } else {
                        System.out.println("Error - Declarar palavra reservada do.");
                        jTextAreaSaida.append("Error - Declarar palavra reservada do.    LINHA = " + tokenAux.getLinha() + "\n");
                    }

                } else {
                    System.out.println("Error - Fechar parenteses ).");
                    jTextAreaSaida.append("Error - Fechar parenteses ).    LINHA = " + tokenAux.getLinha() + "\n");
                }

            } else {
                System.out.println("Error - Abrir parenteses (.");
                jTextAreaSaida.append("Error - Abrir parenteses (.    LINHA = " + tokenAux.getLinha() + "\n");
            }

        } else if (tokenAux.getLexema().equals("if")) {

            //ESTA FUNCIONANDO PERFEITAMENTE
            indexToken++;
            tokenAux = tokens.get(indexToken);
            //verificando se abriu parenteses
            if (tokenAux.getLexema().equals("(")) {

                indexToken++;
                tokenAux = tokens.get(indexToken);

                condicao();

                if (tokenAux.getLexema().equals(")")) {
                    acao17();

                    indexToken++;
                    tokenAux = tokens.get(indexToken);

                    if (tokenAux.getLexema().equals("then")) {
                        indexToken++;
                        tokenAux = tokens.get(indexToken);

                        if (tokenAux.getLexema().equals("begin")) {
                            indexToken++;
                            tokenAux = tokens.get(indexToken);
                            sentencas();

                            if (tokenAux.getLexema().equals("end")) {
                                acao18();
                                pfalsa();
                                acao19();

                                //System.out.println("TESTE  - " + tokenAux.toString());
                                if (!tokenAux.getLexema().equals(";")) {
                                    indexToken++;
                                    tokenAux = tokens.get(indexToken);
                                }

                            } else {
                                System.out.println("Error - Declarar palavra reservada end.");
                                jTextAreaSaida.append("Error - Declarar palavra reservada end.    LINHA = " + tokenAux.getLinha() + "\n");
                            }
                        } else {
                            System.out.println("Error - Declarar palavra reservada begin.");
                            jTextAreaSaida.append("Error - Declarar palavra reservada begin.    LINHA = " + tokenAux.getLinha() + "\n");
                        }
                    } else {
                        System.out.println("Error - Declarar palavra reservada then.");
                        jTextAreaSaida.append("Error - Declarar palavra reservada then.    LINHA = " + tokenAux.getLinha() + "\n");
                    }

                } else {
                    System.out.println("Error - Fechar parenteses.");
                    jTextAreaSaida.append("Error - Fechar parenteses.    LINHA = " + tokenAux.getLinha() + "\n");
                }
            } else {
                System.out.println("Error - Abrir parenteses.");
                jTextAreaSaida.append("Error - Abrir parenteses.    LINHA = " + tokenAux.getLinha() + "\n");
            }

        } //o unico diferente... ele verifica se for um identificador
        else if (tokenAux.getClasse().equals("cId")) {

            acao13();

            indexToken++;
            tokenAux = tokens.get(indexToken);//atualizando

            if (tokenAux.getLexema().equals(":=")) {
                indexToken++;
                tokenAux = tokens.get(indexToken);//atualizando
                expressao();
                acao14();

            } else {
                System.out.println("Error - Declarar atribuição :=.");
                jTextAreaSaida.append("Error - Declarar atribuição :=.  LINHA = " + tokenAux.getLinha() + "\n");
            }

        } else if (tokenAux.getLexema().equals("end")) {

        } else {
            //System.out.println("ALGUM ERRO- " + tokenAux.toString());
            //System.out.println("Error - Declarar variavel ou metodo.");
            //jTextAreaSaida.append("Error - Declarar variavel ou metodo. LINHA = " + tokenAux.getLinha() + "\n");
        }
    }

    public void pfalsa() {
        //System.out.println("ELSE - " + tokenAux.toString());
        indexToken++;
        tokenAux = tokens.get(indexToken);//atualizando

        // System.out.println("TESTE 2 - " + tokenAux.toString());
        if (tokenAux.getLexema().equals("else")) {
            indexToken++;
            tokenAux = tokens.get(indexToken);//atualizando
            if (tokenAux.getLexema().equals("begin")) {

                indexToken++;
                tokenAux = tokens.get(indexToken);

                sentencas();

                if (!tokenAux.getLexema().equals("end")) {
                    System.out.println("Error - Declarar palavra reservada end.");
                    jTextAreaSaida.append("Error - Declarar palavra reservada end.  LINHA = " + tokenAux.getLinha() + "\n");
                } else {

                }

            } else {
                System.out.println("Error - Declarar palavra reservada begin.");
                jTextAreaSaida.append("Error - Declarar palavra reservada begin.  LINHA = " + tokenAux.getLinha() + "\n");
            }
        } else {

            // System.out.println("TESTE 3 - " + tokenAux.toString());
        }

    }

    public void condicao() {
        expressao();

        relacao();
        acao15();
        indexToken++;
        tokenAux = tokens.get(indexToken);//atualizando
        expressao();
        acao16();
    }

    public void relacao() {
        if (!tokenAux.getLexema().equals("=")) {
            if (!tokenAux.getLexema().equals(">")) {
                if (!tokenAux.getLexema().equals("<")) {
                    if (!tokenAux.getLexema().equals(">=")) {
                        if (!tokenAux.getLexema().equals("<=")) {
                            if (!tokenAux.getLexema().equals("<>")) {
                                System.out.println("Error - Declarar operador de comparação.");
                                jTextAreaSaida.append("Error - Declarar operador de comparação.  LINHA = " + tokenAux.getLinha() + "\n");
                            }
                        }
                    }
                }
            }
        }
    }

    public int GerarRotulo() {
        R++;
        return R;
    }

    public void expressao() {
        termo();
        outros_termos();

    }

    public void termo() {
        fator();
        mais_fatores();
    }

    public void outros_termos() {

        if (tokenAux.getLexema().equals("+") || tokenAux.getLexema().equals("-")) {

            //System.out.println("ENTREI = " + tokenAux.toString());
            op_ad();
            acao9();
            indexToken++;
            tokenAux = tokens.get(indexToken);

            termo(); //verifica se tem uma variavel ou numero
            acao10();
            //indexToken++;
            //tokenAux = tokens.get(indexToken);
            outros_termos();
        }
    }

    public void op_ad() {

        if (!tokenAux.getLexema().equals("+")) {
            if (!tokenAux.getLexema().equals("-")) {
                System.out.println("Error - Falta do Operador de + ou -.");
                jTextAreaSaida.append("Error - Falta do Operador de + ou -. LINHA = " + tokenAux.getLinha() + "\n");
            }
        }

    }

    public void fator() {
        if (tokenAux.getClasse().equals("cId")) {
            acao7();
        } else if (tokenAux.getClasse().equals("cInt")) { //se for um numero
            //intnum(); verificar se é um numero mas estou fazendo com o if acima
            acao8();
        } else if (tokenAux.getLexema().equals("(")) {
            indexToken++;
            tokenAux = tokens.get(indexToken);
            expressao();
            //indexToken++;
            //tokenAux = tokens.get(indexToken);
            if (!tokenAux.getLexema().equals(")")) {
                System.out.println("Error - Fechar parenteses.");
                jTextAreaSaida.append("Error - Fechar parenteses.   LINHA = " + tokenAux.getLinha() + "\n");
            }
        } else {
            System.out.println("Error - Expressao ou variavel faltando.");
            jTextAreaSaida.append("Error - Expressao ou variavel faltando.  LINHA = " + tokenAux.getLinha() + "\n");

        }
    }

    public void mais_fatores() {

        try {

            indexToken++;
            tokenAux = tokens.get(indexToken);
            //ele anda depois que fechar uma expressao

            if (tokenAux.getLexema().equals("*") || tokenAux.getLexema().equals("/")) {

                op_mul();
                acao11();

                indexToken++;
                tokenAux = tokens.get(indexToken);
                fator();
                acao12();
                mais_fatores();
            }
        } catch (Error e) {
            System.out.println("Error - Terminar a sintexe.");
            jTextAreaSaida.append("Error - Terminar a sintaxe.  LINHA = " + tokenAux.getLinha() + "\n");

        }
    }

    public void op_mul() {

        if (!tokenAux.getLexema().equals("*")) {
            if (!tokenAux.getLexema().equals("/")) {
                System.out.println("Error - Falta do Operador de * ou /.");
                jTextAreaSaida.append("Error - Falta do Operador de * ou /. LINHA = " + tokenAux.getLinha() + "\n");
            }
        }
    }

    public void var_write() {
        if (tokenAux.getClasse().equals("cId")) {
            acao6();
            mais_var_write();
        }
    }

    public void mais_var_write() {
        indexToken++;
        tokenAux = tokens.get(indexToken);

        if (tokenAux.getLexema().equals(",")) {
            indexToken++;
            tokenAux = tokens.get(indexToken);
            var_write();
        }
    }

    public void var_read() {
        if (tokenAux.getClasse().equals("cId")) {
            acao5();
            mais_var_read();
        }
    }

    public void mais_var_read() {
        indexToken++;
        tokenAux = tokens.get(indexToken);

        if (tokenAux.getLexema().equals(",")) {
            indexToken++;
            tokenAux = tokens.get(indexToken);
            var_read();
        }
    }

    public void declara() {

        //verifica a palavra reservada var
        validaCompila = tokenAux.getLexema().equals("var"); //verifica se esta escrito program
        if (validaCompila) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // atualzia lexama
            dvar();

            indexToken++;
            tokenAux = tokens.get(indexToken); // atualzia lexama
            mais_dc();
        } else {
            System.out.println("Error - Declarar falta de Variavel.");
            jTextAreaSaida.append("Error - Declarar Variavel.   Linha = " + tokenAux.getLinha() + "\n");
        }

    }

    public void mais_dc() {

        validaCompila = tokenAux.getLexema().equals(";"); //verifica se esta escrito program

        if (validaCompila) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // atualzia lexama
            cont_dc();
        } else {
            System.out.println("Error - Falta de ponto virgula (;).");
            jTextAreaSaida.append("Error - Falta de ponto virgula (;).  Linha = " + tokenAux.getLinha() + "\n");
        }
    }

    public void cont_dc() {

        validaCompila = tokenAux.getClasse().equals("cId");
        if (validaCompila) {
            dvar();
            indexToken++;
            tokenAux = tokens.get(indexToken); // atualzia lexama
            mais_dc();

        }
    }

    public void dvar() {
        variaveis();
        indexToken++;
        tokenAux = tokens.get(indexToken); // atualzia lexama

        //verificando 2 pontos apos as variaveis
        validaCompila = tokenAux.getLexema().equals(":"); //verifica se é 2 pontos
        if (validaCompila) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // atualzia lexama

            tipo_var();
        }
    }

    public void tipo_var() {
        if (!tokenAux.getLexema().equals("integer") || !tokenAux.getLexema().equals("real") || !tokenAux.getLexema().equals("real")) {
            System.out.println("ERRO - Tipo de variavel declarada  esta errado.");
            jTextAreaSaida.append("Error - Tipo de variavel declarada  esta errado. Linha = " + tokenAux.getLinha() + "\n");
        }
    }

    public void variaveis() {
        validaCompila = tokenAux.getClasse().equals("cId"); //verifica se é um cId

        //se verdadeiro
        if (validaCompila) {
            acao2();
            indexToken++;
            tokenAux = tokens.get(indexToken); // atualzia lexama
            mais_var();
        } else {
            System.out.println("Error -  Inesperado identificador.");
            jTextAreaSaida.append("Error - Inesperado identificador.    Linha = " + tokenAux.getLinha() + "\n");
        }
    }

    public void mais_var() {
        if (tokenAux.getLexema().equals(",")) {
            indexToken++;
            tokenAux = tokens.get(indexToken); // atualzia lexama
            variaveis();
        }
    }

    //-------------------------
    //AÇÕES PARA GERAR O MEPA
    //-------------------------
    public void acao1() {
        identificadores.get(indexIdentificador).setClasse("function");
        Geracode(null, "INPP", null);
        S = -1;
        R = 0;
        indexIdentificador++;
    }

    public void acao2() {

        if (!identificadores.get(indexIdentificador).getClasse().equals("var")) {
            identificadores.get(indexIdentificador).setClasse("var");
            S = S + 1;
            Geracode(null, "AMEM", "1");
            //ja adicionei o endereço
            identificadores.get(indexIdentificador).setEndereco(S);
            indexIdentificador++;
        } else {
            System.out.println("Error - tentativa de redeclaração do id.");
            jTextAreaSaida.append("Error - tentativa de redeclaração do id. Linha = " + tokenAux.getLinha() + "\n");
        }
    }

    //ACAO 3 e ACAO 3 NAO UTILIZADAS
    public void acao5() {

        //encontrar o endereço correto do identificador
        indexIdentificador = 0;
        for (Identificador i : identificadores) {
            if (!i.getLexema().equals(tokenAux.getLexema())) {
                indexIdentificador++;
            } else {
                break;
            }
        }

        if (identificadores.get(indexIdentificador).getClasse().equals("var")) {
            Geracode(null, "LEIT", null);
            Geracode(null, "ARMZ", identificadores.get(indexIdentificador).getEndereco() + "");
        } else {
            System.out.println("Error - Variavel não declarada." + tokenAux.getLexema());
            jTextAreaSaida.append("Error - Variavel não declarada.  Linha = " + tokenAux.getLinha() + "\n");
        }

    }

    public void acao6() {
        //encontrar o endereço correto do identificador
        indexIdentificador = 0;
        for (Identificador i : identificadores) {
            if (!i.getLexema().equals(tokenAux.getLexema())) {
                indexIdentificador++;
            } else {
                break;
            }
        }

        if (identificadores.get(indexIdentificador).getClasse().equals("var")) {
            Geracode(null, "CRVL", identificadores.get(indexIdentificador).getEndereco() + "");
            Geracode(null, "IMPR", null);
        } else {
            System.out.println("Error - Variavel não declarada." + tokenAux.getLexema());
            jTextAreaSaida.append("Error - Variavel não declarada.  Linha = " + tokenAux.getLinha() + "\n");
        }
    }

    public void acao7() {
        indexIdentificador = 0;
        for (Identificador i : identificadores) {
            if (!i.getLexema().equals(tokenAux.getLexema())) {
                indexIdentificador++;
            } else {
                break;
            }
        }

        if (identificadores.get(indexIdentificador).getClasse().equals("var")) {
            Geracode(null, "CRVL", identificadores.get(indexIdentificador).getEndereco() + "");
        } else {
            System.out.println("Error - Variavel não declarada." + tokenAux.getLexema());
            jTextAreaSaida.append("Error - Variavel não declarada.  Linha = " + tokenAux.getLinha() + "\n");
        }
    }

    public void acao8() {
        //System.out.println("TESTE NUMERO - " + tokenAux.toString());
        Geracode(null, "CRCT", tokenAux.getLexema());
    }

    public void acao9() {
        opAd = tokenAux.getLexema();
    }

    public void acao10() {
        if (opAd.equals("+")) {
            Geracode(null, "SOMA", null);
        }
        if (opAd.equals("-")) {
            Geracode(null, "SUBT", null);
        }
    }

    public void acao11() {
        opMul = tokenAux.getLexema();
    }

    public void acao12() {
        if (opMul.equals("*")) {
            Geracode(null, "MULT", null);
        }
        if (opMul.equals("/")) {
            Geracode(null, "DIVI", null);
        }
    }

    public void acao13() {
        indexIdentificador = 0;
        for (Identificador i : identificadores) {
            if (!i.getLexema().equals(tokenAux.getLexema())) {
                indexIdentificador++;
            } else {
                break;
            }
        }

        if (identificadores.get(indexIdentificador).getClasse().equals("var")) {
            Addr = indexIdentificador;
        } else {
            System.out.println("Error - Variavel não declarada." + tokenAux.getLexema());
            jTextAreaSaida.append("Error - Variavel não declarada.  Linha = " + tokenAux.getLinha() + "\n");
        }

    }

    public void acao14() {
        Geracode(null, "ARMZ", identificadores.get(Addr).getEndereco() + "");
    }

    public void acao15() {
        rel = tokenAux.getLexema();
    }

    public void acao16() {

        if (rel.equals("=")) {
            Geracode(null, "CMIG", null);
        }
        if (rel.equals("<")) {
            Geracode(null, "CMME", null);
        }
        if (rel.equals(">")) {
            Geracode(null, "CMMA", null);
        }
        if (rel.equals("<=")) {
            Geracode(null, "CMEG", null);
        }
        if (rel.equals(">=")) {
            Geracode(null, "CMAG", null);
        }
        if (rel.equals("<>")) {
            Geracode(null, "CMDG", null);
        }
    }

    public void acao17() {
        RotElse = GerarRotulo();
        RotEndIf = GerarRotulo();
        //RotEnd = RotElse;
        Geracode(null, "DSVF", "ROT" + RotElse);

    }

    public void acao18() {
        Geracode(null, "DSVS", "ROT" + RotEndIf);
        Geracode("ROT" + RotElse, "NADA", null);
    }

    public void acao19() {
        Geracode("ROT" + (RotEndIf), "NADA", null);
        //R-=2;
        RotElse -= 2;
        RotEndIf -= 2;
    }

    public void acao20() {
        RotWhile = GerarRotulo();
        RotEnd = GerarRotulo();
        Geracode("ROT" + RotWhile, "NADA", null);
    }

    public void acao21() {
        Geracode(null, "DSVF", "ROT" + RotEnd);
    }

    public void acao22() {
        Geracode(null, "DSVS", "ROT" + RotWhile);
        Geracode("ROT" + RotEnd, "NADA", null);
    }

    public void acao23() {
        RotRepeat = GerarRotulo();
        Geracode("ROT" + RotRepeat, "NADA", null);
    }

    public void acao24() {
        Geracode(null, "DSVF", "ROT" + RotRepeat);
    }

    public void acao25() {
        indexIdentificador = 0;
        for (Identificador i : identificadores) {
            if (!i.getLexema().equals(tokenAux.getLexema())) {
                indexIdentificador++;
            } else {
                break;
            }
        }

        if (identificadores.get(indexIdentificador).getClasse().equals("var")) {
            Addr = indexIdentificador;
        } else {
            System.out.println("Error - Variavel não declarada." + tokenAux.getLexema());
            jTextAreaSaida.append("Error - Variavel não declarada.  Linha = " + tokenAux.getLinha() + "\n");
        }
    }

    public void acao26() {
        //talvez precisa de ajuste no endereço 

        Geracode(null, "ARMZ", identificadores.get(Addr).getEndereco() + "");
    }

    //vetor de enderecos para for
    //int Addr2[];
    ArrayList<Integer> Addr2 = new ArrayList<>();

    public void acao27() {
        RotFor = GerarRotulo();
        RotEnd = GerarRotulo();
        Geracode("ROT" + RotFor, "NADA", null);
        Geracode(null, "CRVL", identificadores.get(Addr).getEndereco() + "");

        System.out.println("IndexIndentificador - " + indexIdentificador + " Addr = " + Addr);
        //Addr2[pos] = Addr;
        Addr2.add(Addr);
    }

    public void acao28() {
        Geracode(null, "CMEG", null);
        Geracode(null, "DSVF", "ROT" + RotEnd);
    }

    public void acao29() {

        Geracode(null, "CRVL", identificadores.get(Addr2.get(Addr2.size() - 1)).getEndereco() + ""); //antes era Addr
        Geracode(null, "CRCT", "1");
        Geracode(null, "SOMA", null);
        Geracode(null, "ARMZ", identificadores.get(Addr2.get(Addr2.size() - 1)).getEndereco() + "");
        Geracode(null, "DSVS", "ROT" + RotFor);
        Geracode("ROT" + RotEnd, "NADA", null);

        //R = R-2; //voltando os Rotulos para fechar as funçoes
        RotEnd -= 2;
        RotFor -= 2;
        Addr2.remove(Addr2.size() - 1);

    }

    public void acao30() {
        Geracode(null, "PARA", null);
    }

    int topoMemoria = 0;
    int topoMepa = 0;
    Memoria memoriaaux = new Memoria();

    public void executarMepa() {
        memoria.clear();

        if (!codigoMepa.isEmpty()) {
            OUTER:
            for (topoMepa = 0; topoMepa < codigoMepa.size(); topoMepa++) {
                memoriaaux = new Memoria();
                switch (codigoMepa.get(topoMepa).getInstrucao()) {
                    case "INPP":
                        topoMemoria = -1;
                        break;
                    case "PARA":
                        System.out.println("EXECUÇÃO DO MEPA FEITA!");
                        jTextAreaSaida.append("EXECUÇÃO DO CODIGO TERMINADA!\n");
                        break OUTER;
                    case "AMEM":
                        topoMemoria = topoMemoria + Integer.parseInt(codigoMepa.get(topoMepa).getK());
                        memoriaaux.setEndereco("" + topoMemoria);
                        memoriaaux.setValor(0);
                        memoria.add(memoriaaux);
                        break;
                    case "LEIT":
                        topoMemoria = topoMemoria + 1;
                        String auxx = "";
                        for (Identificador id : identificadores) {
                            if (id.getEndereco() == (Integer.parseInt(codigoMepa.get(topoMepa + 1).getK()))) {

                                //PARA QUE ACEITA APENAS NUMEROS
                                int valor = 0;
                                boolean valida = false;
                                String acumulador = "";
                                while (valida == false) {
                                    try {
                                        acumulador = JOptionPane.showInputDialog("Digite qual o valor para " + id.getLexema() + ":");
                                        valor = Integer.parseInt(acumulador);
                                        //System.out.println("DIGITADO - " + valor);
                                        valida = true;
                                    } catch (NumberFormatException e) {
                                        JOptionPane.showMessageDialog(null, "Informe apenas números inteiros!", "ENTRADA", 0);
                                        valida = false;
                                    }
                                }

                                auxx = acumulador;
                                //auxx = JOptionPane.showInputDialog("Digite qual o valor para " + id.getLexema() + ":");
                                memoriaaux.setEndereco("" + topoMemoria);
                                memoriaaux.setValor(Integer.parseInt(auxx));
                                memoria.add(memoriaaux);
                                break;
                            }
                        }
                        break;
                    case "IMPR":
                        JOptionPane.showMessageDialog(null, "Resultado:" + memoria.get(topoMemoria).getValor());
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "CRCT":
                        topoMemoria = topoMemoria + 1;
                        memoriaaux.setEndereco("" + topoMemoria);
                        memoriaaux.setValor(Integer.parseInt(codigoMepa.get(topoMepa).getK()));
                        memoria.add(memoriaaux);
                        break;
                    case "CRVL":
                        topoMemoria = topoMemoria + 1;
                        memoriaaux.setEndereco("" + topoMemoria);
                        memoriaaux.setValor(memoria.get(Integer.parseInt(codigoMepa.get(topoMepa).getK())).getValor());
                        memoria.add(memoriaaux);
                        break;
                    case "SOMA":
                        memoria.get(topoMemoria - 1).setValor(memoria.get(topoMemoria - 1).getValor() + memoria.get(topoMemoria).getValor());
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "SUBT":
                        memoria.get(topoMemoria - 1).setValor(memoria.get(topoMemoria - 1).getValor() - memoria.get(topoMemoria).getValor());
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "MULT":
                        memoria.get(topoMemoria - 1).setValor(memoria.get(topoMemoria - 1).getValor() * memoria.get(topoMemoria).getValor());
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "DIVI":
                        memoria.get(topoMemoria - 1).setValor(memoria.get(topoMemoria - 1).getValor() / memoria.get(topoMemoria).getValor());
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "INVR":
                        memoria.get(topoMemoria).setValor(-memoria.get(topoMemoria).getValor());
                        break;
                    case "CONJ":
                        if (memoria.get(topoMemoria - 1).getValor() == 1 && memoria.get(topoMemoria).getValor() == 1) {
                            memoria.get(topoMemoria - 1).setValor(1);
                        } else {
                            memoria.get(topoMemoria - 1).setValor(0);
                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "DISJ":
                        if (memoria.get(topoMemoria - 1).getValor() == 1 || memoria.get(topoMemoria).getValor() == 1) {
                            memoria.get(topoMemoria - 1).setValor(1);
                        } else {
                            memoria.get(topoMemoria - 1).setValor(0);
                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "NEGA":
                        memoria.get(topoMemoria).setValor(1 - memoria.get(topoMemoria).getValor());
                        break;
                    case "CMME":
                        if (memoria.get(topoMemoria - 1).getValor() < memoria.get(topoMemoria).getValor()) {
                            memoria.get(topoMemoria - 1).setValor(1);
                        } else {
                            memoria.get(topoMemoria - 1).setValor(0);
                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "CMMA":
                        if (memoria.get(topoMemoria - 1).getValor() > memoria.get(topoMemoria).getValor()) {
                            memoria.get(topoMemoria - 1).setValor(1);
                        } else {
                            memoria.get(topoMemoria - 1).setValor(0);
                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "CMIG":
                        if (memoria.get(topoMemoria - 1).getValor() == memoria.get(topoMemoria).getValor()) {
                            memoria.get(topoMemoria - 1).setValor(1);
                        } else {
                            memoria.get(topoMemoria - 1).setValor(0);
                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "CMDG":
                        if (memoria.get(topoMemoria - 1).getValor() != memoria.get(topoMemoria).getValor()) {
                            memoria.get(topoMemoria - 1).setValor(1);
                        } else {
                            memoria.get(topoMemoria - 1).setValor(0);
                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "CMEG":
                        if (memoria.get(topoMemoria - 1).getValor() <= memoria.get(topoMemoria).getValor()) {
                            memoria.get(topoMemoria - 1).setValor(1);
                        } else {
                            memoria.get(topoMemoria - 1).setValor(0);
                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "CMAG":
                        if (memoria.get(topoMemoria - 1).getValor() >= memoria.get(topoMemoria).getValor()) {
                            memoria.get(topoMemoria - 1).setValor(1);
                        } else {
                            memoria.get(topoMemoria - 1).setValor(0);
                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "ARMZ":
                        memoria.get(Integer.parseInt(codigoMepa.get(topoMepa).getK())).setValor(memoria.get(topoMemoria).getValor());
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    case "DSVS":
                        for (Mepa j : codigoMepa) {
                            if (codigoMepa.get(topoMepa).getK().equalsIgnoreCase(j.getRot())) {
                                topoMepa = j.getEndereco() - 1;
                                break;
                            }
                        }
                        break;
                    case "DSVF":
                        if (memoria.get(topoMemoria).getValor() == 0) {
                            for (Mepa j : codigoMepa) {
                                if (codigoMepa.get(topoMepa).getK().equals(j.getRot())) {
                                    topoMepa = j.getEndereco();
                                    break;
                                }
                            }
                        } else {

                        }
                        memoria.remove(topoMemoria);
                        topoMemoria = topoMemoria - 1;
                        break;
                    //System.out.println("APENAS ANDA NA PILHA DE CODIGO");
                    case "NADA":
                        break;
                    default:
                        break;
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "COMPILE O CODIGO ANTES DE EXECUTAR", "EXECUTAR", 2);
        }
    }


    private void jMenuItemCompilarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCompilarActionPerformed
        //A PARTE DIFICIL
        analiseLexica();
        System.out.println("ANALISE LEXICA FEITA!");
        jTextAreaSaida.setBackground(new java.awt.Color(255, 209, 176)); //setando cor vermelha de erro
        analiseSintatica();
    }//GEN-LAST:event_jMenuItemCompilarActionPerformed

    private void jLabelExecutarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExecutarMouseEntered
        // TODO add your handling code here:

    }//GEN-LAST:event_jLabelExecutarMouseEntered

    private void jLabelExecutarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExecutarMouseExited
        // TODO add your handling code here:
        ImageIcon II = new ImageIcon(getClass().getResource("/imagens/btnPlay.png"));
        jLabelExecutar.setIcon(II);
    }//GEN-LAST:event_jLabelExecutarMouseExited

    private void jLabelExecutarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelExecutarMousePressed
        // TODO add your handling code here:
        ImageIcon II = new ImageIcon(getClass().getResource("/imagens/btnPlayPress.png"));
        jLabelExecutar.setIcon(II);

        //funcao de executar o MEPA
        executarMepa();

    }//GEN-LAST:event_jLabelExecutarMousePressed

    private void jLabelCompilarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelCompilarMousePressed
        // TODO add your handling code here:
        ImageIcon II = new ImageIcon(getClass().getResource("/imagens/compilarPress.png"));
        jLabelCompilar.setIcon(II);
        //A PARTE DIFICIL
        analiseLexica();
        System.out.println("ANALISE LEXICA FEITA! - " + tokens.size() + " LEXICOS");
        jTextAreaSaida.setBackground(new java.awt.Color(255, 209, 176)); //setando cor vermelha de erro
        analiseSintatica();
    }//GEN-LAST:event_jLabelCompilarMousePressed

    private void jLabelCompilarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelCompilarMouseExited
        // TODO add your handling code here:
        ImageIcon II = new ImageIcon(getClass().getResource("/imagens/compilarbtn.png"));
        jLabelCompilar.setIcon(II);
    }//GEN-LAST:event_jLabelCompilarMouseExited

    private void jLabelInfoMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelInfoMousePressed
        // TODO add your handling code here:
        ImageIcon II = new ImageIcon(getClass().getResource("/imagens/ajudabtnpress.png"));
        jLabelInfo.setIcon(II);

        dialog = new JanelaAjuda(this, rootPaneCheckingEnabled); //abrir centralisada
        dialog.setModal(false); //se a tela fica presa ou nao
        dialog.setVisible(true); //Chama a dialog
        dialog = null; //Deixa o garbage collector agir    

    }//GEN-LAST:event_jLabelInfoMousePressed

    private void jLabelInfoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelInfoMouseExited
        // TODO add your handling code here:
        ImageIcon II = new ImageIcon(getClass().getResource("/imagens/ajudabtn.png"));
        jLabelInfo.setIcon(II);
    }//GEN-LAST:event_jLabelInfoMouseExited

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:

        switch (JOptionPane.showConfirmDialog(null, "Deseja salvar arquivo?")) {
            case 0:
                System.out.println("botao yes clicado");

                String novoConteudo = jTextArea.getText();
                if (arquivo == null) {

                    int opc = 0;
                    JFileChooser j = new JFileChooser(UtilidadesArquivos.getDiretorioDoPrograma());

                    j.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    opc = j.showSaveDialog(this);

                    System.out.println(opc);

                    if (opc == 0) {
                        arquivo = j.getSelectedFile();
                        UtilidadesArquivos.salvarEmArquivoTexto(jTextArea.getText(), arquivo);
                    }

                } else {
                    UtilidadesArquivos.salvarEmArquivoTexto(novoConteudo, arquivo);
                }

                System.exit(0);
                break;
            case 1:
                System.out.println("botao no clicado");
                System.exit(0);
                break;
            case 2:
                System.out.println("botao cancel clicado");
                this.setDefaultCloseOperation(TelaPrincipal.DO_NOTHING_ON_CLOSE);
                break;
        }

    }//GEN-LAST:event_formWindowClosing

    private void jLabelCompilarExecutarMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelCompilarExecutarMousePressed
        // TODO add your handling code here:
        ImageIcon II = new ImageIcon(getClass().getResource("/imagens/compexecbtnpress.png"));
        jLabelCompilarExecutar.setIcon(II);
        //A PARTE DIFICIL
        analiseLexica();
        System.out.println("ANALISE LEXICA FEITA!");
        jTextAreaSaida.setBackground(new java.awt.Color(255, 209, 176)); //setando cor vermelha de erro
        analiseSintatica();
        //funcao de executar o MEPA
        executarMepa();
    }//GEN-LAST:event_jLabelCompilarExecutarMousePressed

    private void jLabelCompilarExecutarMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelCompilarExecutarMouseExited
        // TODO add your handling code here:
        ImageIcon II = new ImageIcon(getClass().getResource("/imagens/compexecbtn.png"));
        jLabelCompilarExecutar.setIcon(II);
    }//GEN-LAST:event_jLabelCompilarExecutarMouseExited

    private void jMenuItemCompExecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCompExecActionPerformed
        // TODO add your handling code here:
        //A PARTE DIFICIL
        analiseLexica();
        System.out.println("ANALISE LEXICA FEITA!");
        jTextAreaSaida.setBackground(new java.awt.Color(255, 209, 176)); //setando cor vermelha de erro
        analiseSintatica();
        //funcao de executar o MEPA
        executarMepa();
    }//GEN-LAST:event_jMenuItemCompExecActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

        // lookandfeel muda de cores
        // biblioteca     looksdemo-2.3.1.jar
        //SkyBlue()
        //BrownSugar()
        // DarkStar()  
        //DesertGreen()
        //Silver()
        //ExperienceRoyale()
//        try {
//            PlasticLookAndFeel.setPlasticTheme(new DarkStar());
//            try {
//                UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
//            } catch (InstantiationException ex) {
//                Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IllegalAccessException ex) {
//                Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (UnsupportedLookAndFeelException ex) {
//                Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        } catch (ClassNotFoundException ex) {
//            ex.printStackTrace();
//        }
//        SwingUtilities.updateComponentTreeUI(this);

    }//GEN-LAST:event_formWindowOpened

    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private void copy() {
        //clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String text = jTextArea.getSelectedText();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
    }

    private void cut() {

        String texto = jTextArea.getText();
        String texto_recortado = jTextArea.getSelectedText();

        StringBuilder build = new StringBuilder(texto);
        texto = "" + build.replace(jTextArea.getSelectionStart(), jTextArea.getSelectionEnd(), ""); //retiro a string selecionada
        jTextArea.setText(texto); //retorno o texto pra minha jTextArea

        StringSelection selection = new StringSelection(texto_recortado);
        clipboard.setContents(selection, null);
    }

    private void paste() {

        Transferable contents = clipboard.getContents(this);
        if (contents == null) {
            // TODO vazio  
        } else if (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String texto = (String) contents.getTransferData(DataFlavor.stringFlavor);
                // TODO processar o texto  
                int pos = jTextArea.getCaretPosition(); //pegando a posicao do cursor
                jTextArea.insert(texto, pos);
            } catch (UnsupportedFlavorException | IOException ex) {
                Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // não contem texto (imagem?)  
        }
    }


    private void jMenuItemColarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemColarActionPerformed
        // TODO add your handling code here:
        paste();
    }//GEN-LAST:event_jMenuItemColarActionPerformed

    private void jMenuItemCopiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopiarActionPerformed
        // TODO add your handling code here:
        copy();
    }//GEN-LAST:event_jMenuItemCopiarActionPerformed

    private void jMenuItemRecortarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRecortarActionPerformed
        // TODO add your handling code here:
        cut();
    }//GEN-LAST:event_jMenuItemRecortarActionPerformed

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
            UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
            //UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
            //UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelCompilar;
    private javax.swing.JLabel jLabelCompilarExecutar;
    private javax.swing.JLabel jLabelExecutar;
    private javax.swing.JLabel jLabelInfo;
    private javax.swing.JMenu jMenuArquivo;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuEditar;
    private javax.swing.JMenuItem jMenuItemAbrir;
    private javax.swing.JMenuItem jMenuItemColar;
    private javax.swing.JMenuItem jMenuItemCompExec;
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
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPaneEdicao;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextArea jTextArea;
    private javax.swing.JTextArea jTextAreaSaida;
    // End of variables declaration//GEN-END:variables
}
