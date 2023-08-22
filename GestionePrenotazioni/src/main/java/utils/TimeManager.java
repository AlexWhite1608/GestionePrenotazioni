package utils;

import data_access.Gateway;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Gestisce i filtri della visualizzazione delle prenotazioni
public class TimeManager {

    private static final String INITIAL_YEAR = "2023";
    private static ArrayList<String> yearsPrenotazioni = new ArrayList<>();

    // Ritorna gli anni dell'attività per il filtraggio delle prenotazioni
    public static ArrayList<String> getPrenotazioniYears() throws SQLException {
        yearsPrenotazioni.add("Tutto");     // Mostra tutti gli anni
        yearsPrenotazioni.add(INITIAL_YEAR);
        addCurrentYearIfNotPresent(yearsPrenotazioni);

        // In base agli anni delle prenotazioni aggiunge eventuali nuovi anni
        String yearsQuery = "SELECT Arrivo, Partenza FROM Prenotazioni";
        ResultSet rs = new Gateway().execSelectQuery(yearsQuery);

        while (rs.next()) {
            String annoArrivo = rs.getString("Arrivo").substring(6, 10);
            String annoPartenza = rs.getString("Partenza").substring(6, 10);

            if(!yearsPrenotazioni.contains(annoArrivo)){
                yearsPrenotazioni.add(annoArrivo);
            } else if (!yearsPrenotazioni.contains(annoPartenza)) {
                yearsPrenotazioni.add(annoPartenza);
            }
        }

        rs.close();

        return yearsPrenotazioni;
    }

    // Ritorna gli anni dell'attività per il display dei grafici nelle statistiche
    public static ArrayList<String> getPlotYears() throws SQLException {
        ArrayList<String> yearsPlot = new ArrayList<>();

        yearsPlot.add(INITIAL_YEAR);
        addCurrentYearIfNotPresent(yearsPlot);

        // In base agli anni delle prenotazioni aggiunge eventuali nuovi anni
        String yearsQuery = "SELECT Arrivo, Partenza FROM Prenotazioni";
        ResultSet rs = new Gateway().execSelectQuery(yearsQuery);

        while (rs.next()) {
            String annoArrivo = rs.getString("Arrivo").substring(6, 10);
            String annoPartenza = rs.getString("Partenza").substring(6, 10);

            if(!yearsPlot.contains(annoArrivo)){
                yearsPlot.add(annoArrivo);
            } else if (!yearsPlot.contains(annoPartenza)) {
                yearsPlot.add(annoPartenza);
            }
        }

        rs.close();

        return yearsPlot;
    }

    // Aggiunge l'anno corrente se non è già presente
    private static void addCurrentYearIfNotPresent(ArrayList<String> years) {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        if (!years.contains(currentYear)) {
            years.add(currentYear);
        }
    }

    // Ritorna i mesi dell'anno per le statistiche
    public static ArrayList<String> getYearMonths() {
        ArrayList<String> months = new ArrayList<>();

        for (Month month : Month.values()) {
            String meseItaliano = month.getDisplayName(
                    TextStyle.FULL,
                    Locale.ITALIAN
            );

            String meseMaiuscola = meseItaliano.substring(0, 1).toUpperCase() + meseItaliano.substring(1);
            months.add(meseMaiuscola);
        }

        return months;
    }

    //TODO: sposta funzione in Utils??
    public static Map<String, Map<String, Integer>> convertMap(Map<String, Map<String, Integer>> mapToConvert) {
        Map<String, Map<String, Integer>> datasetConMesiItaliani = new HashMap<>();

        for (Map.Entry<String, Map<String, Integer>> entry : mapToConvert.entrySet()) {
            String anno = entry.getKey();
            Map<String, Integer> mesiPresenze = entry.getValue();
            Map<String, Integer> mesiPresenzeItaliani = new HashMap<>();

            for (Map.Entry<String, Integer> mesePresenzeEntry : mesiPresenze.entrySet()) {
                String meseAnno = mesePresenzeEntry.getKey();
                Integer presenze = mesePresenzeEntry.getValue();

                // Converte il mese dall formato "mm/yyyy" a Month
                String[] parts = meseAnno.split("/");
                int mese = Integer.parseInt(parts[0]);
                Month month = Month.of(mese);

                // Ottieni il nome del mese in italiano con la prima lettera maiuscola
                String nomeMese = month.getDisplayName(TextStyle.FULL, Locale.ITALIAN);
                String meseMaiuscola = nomeMese.substring(0, 1).toUpperCase() + nomeMese.substring(1);

                // Aggiungi alla mappa con i mesi in italiano
                mesiPresenzeItaliani.put(meseMaiuscola, presenze);
            }

            // Aggiungi alla mappa principale con i mesi in italiano
            datasetConMesiItaliani.put(anno, mesiPresenzeItaliani);
        }

        return datasetConMesiItaliani;
    }
}
