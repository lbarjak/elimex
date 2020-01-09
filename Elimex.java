package elimex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;

public class Elimex implements GlobalVariables {

    public static void main(String[] args) throws IOException, FileNotFoundException, OpenXML4JException {
        Date start = new Date();

        readHangzavarXLSX();

        //new HangtechnikaToElimexQuery().query();
        test();

        Date stop = new Date();
        new Dates().diff(start, stop);
    }

    public static void readHangzavarXLSX() throws FileNotFoundException, OpenXML4JException {

        String xlsxName = "hangzavar-xlsx-export-2020-01-07_15_28_01.xlsx";
        try {
            new FromXLSX().read(xlsxName, HANGZAVAR_MAP);
        } catch (IOException | InvalidFormatException ex) {
            Logger.getLogger(Elimex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void test() throws MalformedURLException, IOException {
    	String cikkszam = "WM-EAP3CJ3P3BMG";//RO-DR010, NE-NA3F5M, WM-EAP3CJ3P3BMG
    	HangtechnikaToElimexQuery test = new HangtechnikaToElimexQuery();
    	test.stockType();
    	String oldal = test.oldalak(cikkszam);
    	test.search(cikkszam, oldal);
    }
}
//develop branch
