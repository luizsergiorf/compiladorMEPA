package other;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UtiidadelUnicode {

    public static String getCharSet(File arquivo) throws FileNotFoundException, 
                                                                   IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(arquivo));
        CharsetDetector cd = new CharsetDetector();
        cd.setText(bis);
        CharsetMatch cm = cd.detect();
        if (cm != null) {
            return cm.getName();
        } else {
            return "UTF-8";
        }
    }

    public static String getCharSet(String texto) throws FileNotFoundException, IOException {
        CharsetDetector cd = new CharsetDetector();
        cd.setText(texto.getBytes());
        CharsetMatch cm = cd.detect();
        if (cm != null) {
            return cm.getName();
        } else {
            return "UTF-8";
        }
    }

    private UtiidadelUnicode() {
    }
}