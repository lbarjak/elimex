package elimex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.FileWriter;

public class HangtechnikaToElimexQuery implements GlobalVariables {

    int counter = 0;

    final ArrayList<String> gyartokElimex = new ArrayList<>(Arrays.asList(
            "Adam Hall",
            "BMS",
            "Elimex",
            "Eminence",
            "Gravity",
            "ID-AL",
            "Klotz",
            "König & Meyer",
            "LD Systems",
            "Neutrik",
            "NTI Audio",
            "Palmer",
            "WorldMix"));

    final ArrayList<String> exclude = new ArrayList<>(Arrays.asList(
            "PA-PCAB212**",
            "PA-PCAB212OB**",
            "KL-LY225S, fekete",
            "KM-23110-316-55/CR",
            "NTE-10/3",
            "NTE-1",
            "KL-VD062SH",
            "NA-3FDM",
            "NA-3MDF",
            "NBB-75DFIB",
            "NC-3FP1",
            "NC-5FDL1",
            "NC-5FX",
            "NC-5MDL1",
            "NC-5MX",
            "NDJ-1",
            "NL-8FC",
            "KL-D10KL",
            "NE-8FDP",
            "NE-8FDPB",
            "NE-8FDVYK",
            "NE-8FDYC6",
            "NE-8FDYC6B",
            "NE-8MC"
    ));

    StringBuilder oldal;

    public void stockType() {
        ELIMEX_HT.put("raktáron", "1-2 nap");
        ELIMEX_HT.put("date", "2-4 hét");
        ELIMEX_HT.put("a megrendeléstől számított 1-5 munkanap", "1-5 munkanap");
        ELIMEX_HT.put("a megrendeléstől számított 5-10 munkanap", "5-10 munkanap");
        ELIMEX_HT.put("a megrendeléstől számított 10-15 munkanap", "2-4 hét");
        ELIMEX_HT.put("a megrendeléstől számított 3-4 hét", "2-4 hét");
        ELIMEX_HT.put("a megrendeléstől számított 4-5 hét", "2-4 hét");
        ELIMEX_HT.put("-- átmeneti készlethiány --", "Jelenleg nem elérhető!");
        ELIMEX_HT.put("Keressen a részletekért", "Hívjon!");
        ELIMEX_HT.put("csak előzetes ajánlat alapján", "Hívjon!");
        ELIMEX_HT.put("rendeléskor egyeztetjük", "Hívjon!");
        ELIMEX_HT.put("Kifutó típus, már csak a készlet erejéig szállítható! raktáron", "1-2 nap");
        ELIMEX_HT.put("Kifutó típus, már csak a készlet erejéig szállítható!", "Már nem szállítjuk");
        ELIMEX_HT.put("Megszűnt! Már nem tudjuk szállítani!", "Már nem szállítjuk");
    }

    ArrayList<String> nincs = new ArrayList<>();
    ArrayList<String> toNetsoft = new ArrayList<>();

    public void query() throws MalformedURLException, IOException {

        toNetsoft.add("Termék kód" + ";" + "Nettó eladási egységár");

        stockType();

        int indexOfGyarto;
        int indexOfRaktarkeszlet1;
        int indexOfStátusz;
        indexOfGyarto = HANGZAVAR_MAP.get("columns").get("Cikkszám").indexOf("Gyártó");
        indexOfRaktarkeszlet1 = HANGZAVAR_MAP.get("columns").get("Cikkszám").indexOf("Raktárkészlet 1");
        indexOfStátusz = HANGZAVAR_MAP.get("columns").get("Cikkszám").indexOf("Státusz (engedélyezett (1) v. letiltott (0) v. kifutott (2))");

        LinkedHashMap<String, ArrayList<String>> export = new LinkedHashMap<>();
        ELIMEX_UPLOAD_TO_SHOPRENTER.put("export", export);
        ELIMEX_UPLOAD_TO_SHOPRENTER.get("export").put("Cikkszám", new ArrayList<>(Arrays.asList("Cikkszám", "Nincs készleten állapot")));
        LinkedHashMap<String, ArrayList<String>> columns = new LinkedHashMap<>();
        ELIMEX_UPLOAD_TO_SHOPRENTER.put("columns", columns);
        ELIMEX_UPLOAD_TO_SHOPRENTER.get("columns").put("sku", new ArrayList<>(Arrays.asList("sku", "stockStatusName")));
        ELIMEX_UPLOAD_TO_SHOPRENTER.get("columns").put("Cikkszám", new ArrayList<>(Arrays.asList("Cikkszám", "Nincs készleten állapot")));

        for (String k : HANGZAVAR_MAP.get("export").keySet()) {
            ArrayList<String> v = HANGZAVAR_MAP.get("export").get(k);

            if (v.get(indexOfGyarto) != null
                    && gyartokElimex.contains((String) v.get(indexOfGyarto))
                    && !exclude.contains(k)) {
                ELIMEX_MAP.put(k, v);
            }
        }

        for (String k : ELIMEX_MAP.keySet()) {
            ArrayList<String> v = ELIMEX_MAP.get(k);
            System.out.println(k);

            oldal = new StringBuilder();

            URL url = new URL("https://elimex.hu/gyorskereses?q=" + k);
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                oldal.append(inputLine);
            }

            if (oldal.toString().contains("Nincs a keresésnek megfelelő találat.")) {
                nincs.add(k
                        + ";"
                        + v.get(indexOfRaktarkeszlet1).toString().replace(".0", "")
                        + ";"
                        + v.get(indexOfStátusz).toString().replace(".0", ""));
            } else {
                search(k, oldal.toString());
            }
//            if(counter == 20) {
//                break;
//            }
        }

        CopyToXLSX copytoxlsx_elimex = new CopyToXLSX();
        copytoxlsx_elimex.write(ELIMEX_UPLOAD_TO_SHOPRENTER);
        String time = new Dates().now();
        copytoxlsx_elimex.writeout("new_elimex" + time + ".xlsx");

        writeToFileCSV("elimex_to_netsoft_upload", toNetsoft);

        nincsToScreen();
    }

    public void nincsToScreen() {

        System.out.println("Ezek nálunk vannak de az Elimex-en nincsenek fent + Raktárkészlet 1 + Státusz:");
        for (int i = 0; i < nincs.size(); i++) {
            System.out.println(nincs.get(i));
        }
    }

    public void search(String k, String page) {

        Document doc = Jsoup.parse(oldal.toString());
        String bodyText = doc.body().text();
        ArrayList<String> row = new ArrayList<>(Arrays.asList(k, "", "-"));
        String match_a_nincsKeszletenAllapot = "";
        String match_b_kifutoTipusRaktáron = "";
        String match_c_bruttoAlapar = "";
        String integerNettoAlaparString = "";

        Pattern pattern_a_nincsKeszletenAllapot = Pattern.compile(
                "raktáron"
                + "|201\\d.\\d{2}.\\d{2}"
                + "|a megrendeléstől számított 1-5 munkanap"
                + "|a megrendeléstől számított 5-10 munkanap"
                + "|a megrendeléstől számított 10-15 munkanap"
                + "|a megrendeléstől számított 3-4 hét"
                + "|a megrendeléstől számított 4-5 hét"
                + "|-- átmeneti készlethiány --"
                + "|Keressen a részletekért"
                + "|csak előzetes ajánlat alapján"
                + "|rendeléskor egyeztetjük"
                + "|Kifutó típus, már csak a készlet erejéig szállítható!"
                + "|Megszűnt! Már nem tudjuk szállítani!");
        Pattern pattern_b_kifutoTipusRaktaron = Pattern.compile("raktáron");
        Pattern pattern_c_bruttoAlapar = Pattern.compile("(?<=Az Ön Ára: )\\d{0,3} ?\\d{0,3} ?\\d{0,3}(?= Ft)");

        if (bodyText.contains(k)) {
            Matcher matcher_a_nincsKeszletenAllapot = pattern_a_nincsKeszletenAllapot.matcher(bodyText);
            Matcher matcher_c_bruttoAlapar = pattern_c_bruttoAlapar.matcher(bodyText);
            if (matcher_a_nincsKeszletenAllapot.find()) {
                match_a_nincsKeszletenAllapot = matcher_a_nincsKeszletenAllapot.group();
            }
            if ("Kifutó típus, már csak a készlet erejéig szállítható!".equals(match_a_nincsKeszletenAllapot)) {
                Matcher matcher_b_kifutoTipusRaktaron = pattern_b_kifutoTipusRaktaron.matcher(bodyText);
                if (matcher_b_kifutoTipusRaktaron.find()) {
                    match_b_kifutoTipusRaktáron = " " + matcher_b_kifutoTipusRaktaron.group();
                }
            }
            if (matcher_c_bruttoAlapar.find()) {
                match_c_bruttoAlapar = matcher_c_bruttoAlapar.group();
                Integer integerNettoAlapar = (int) ((Float.parseFloat(match_c_bruttoAlapar.replace(" ", "")) + 0.5) / 1.27);
                integerNettoAlaparString = integerNettoAlapar.toString();
            }
            match_a_nincsKeszletenAllapot += match_b_kifutoTipusRaktáron;
            match_a_nincsKeszletenAllapot = ELIMEX_HT.get(match_a_nincsKeszletenAllapot);
            row.set(1, match_a_nincsKeszletenAllapot);//stockType szerint, pl. >>ELIMEX_HT.put("raktáron", "1-2 nap"<< stb);
            //row.set(2, integerNettoAlaparString + "");
            ELIMEX_UPLOAD_TO_SHOPRENTER.get("export").put(k, row);//Ide csak a "Cikkszám" és a "Nincs készleten állapot" kell
            toNetsoft.add(k + ";" + integerNettoAlaparString);//"Termék kód" "Nettó eladási egységár"
            System.out.println(counter + " " + (100 * counter++ / 2281));
        }
    }
    private void writeToFileCSV(String nameOfFile, ArrayList<String> toCSVFile) {

        String time = new Dates().now();
        FileWriter fw;
        try {
            fw = new FileWriter(nameOfFile + time + ".csv");
            for (String row : toCSVFile) {
                fw.write(row + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
