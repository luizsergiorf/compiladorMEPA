package other;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UtilidadesArquivos {

    public static String getExtensaoArquivo(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static String getNomeArquivo(File f) {
        String nome;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            nome = s.substring(0, i);
            return nome;
        } else {
            return s;
        }
    }

    public static File acrescentarExtensao(File f, String extensao) {
        File novoArquivo = f;
        if (getExtensaoArquivo(f) == null) {
            novoArquivo = new File(f.getAbsolutePath() + "." + extensao);
        }
        return novoArquivo;
    }

    public static String getCaractereNovaLinha() {
        return System.getProperty("line.separator");
    }

    public static String getDiretorioDoPrograma() {
        return System.getProperty("user.dir");
    }

    public static String lerArquivoTexto(File arquivo) {
        StringBuilder texto = new StringBuilder();
        String novaLinha = getCaractereNovaLinha();
        String linha;

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(arquivo),
                        UtiidadelUnicode.getCharSet(arquivo)))) {
                    linha = in.readLine();
                    while (linha != null) {
                        texto.append(linha).append(novaLinha);
                        linha = in.readLine();
                    }
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(UtilidadesArquivos.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UtilidadesArquivos.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UtilidadesArquivos.class.getName()).log(Level.SEVERE, null, ex);
                }
                return texto.toString();
    }

    public static void salvarEmArquivoTexto(String conteudo, File arquivo) {
        File arquivoOK = acrescentarExtensao(arquivo, "txt");

        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(arquivoOK),
                        UtiidadelUnicode.getCharSet(conteudo)))) {
                    out.write(conteudo);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(UtilidadesArquivos.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UtilidadesArquivos.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UtilidadesArquivos.class.getName()).log(Level.SEVERE, null, ex);
                }
    }

    public static boolean existeArquivo(File f) {
        return f.exists() && !f.isDirectory();
    }

    private UtilidadesArquivos() {
    }

}
