package elimex;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface GlobalVariables {
    
	//A hangzavar webáruház "total" exportja XLSX fájlba
    LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> HANGZAVAR_MAP = new LinkedHashMap<>();
    //Elimex honlapról levett friss adatok
    LinkedHashMap<String, ArrayList<String>> ELIMEX_MAP = new LinkedHashMap<>();
    //Elimex -> Hangtechnika elérhetőségek
    LinkedHashMap<String, String> ELIMEX_HT = new LinkedHashMap<>();
    //HANGZAVAR_MAP-ből leszűrt Elimex termékek
    LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> ELIMEX_UPLOAD_TO_SHOPRENTER = new LinkedHashMap<>();
}
