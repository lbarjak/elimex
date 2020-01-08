package elimex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;

public class Elimex implements GlobalVariables {

    public static void main(String[] args) throws IOException, FileNotFoundException, OpenXML4JException {
        Date start = new Date();

        readHangzavarXLSX();

        new HangtechnikaToElimexQuery().query();

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
}
