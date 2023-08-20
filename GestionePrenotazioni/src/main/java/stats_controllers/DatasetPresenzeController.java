package stats_controllers;

import data_access.Gateway;
import org.jfree.data.category.DefaultCategoryDataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DatasetPresenzeController {

    public static DefaultCategoryDataset getPlotDataset(String annoSelezionato) throws SQLException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Map<String, Integer>> presenzeForMese = getPresenzeForMese();

        for(Map.Entry<String, Map<String, Integer>> entryAnni : presenzeForMese.entrySet()){
            String anno = entryAnni.getKey();

            if(Objects.equals(anno, annoSelezionato)) {
                for(Map.Entry<String, Integer> entryPresenze : entryAnni.getValue().entrySet()){
                    String mese = entryPresenze.getKey();
                    int presenze = entryPresenze.getValue();

                    dataset.addValue(presenze, "Mese", mese);
                }
            }
        }

        return dataset;
    }

    // Ricava il dataset per la tabella
    public static Map<String, Map<String, Integer>> getTableDataset() throws SQLException {
        return getPresenzeForMese();
    }

    // Ricava le presenze per ogni mese dal database
    private static Map<String, Map<String, Integer>> getPresenzeForMese() throws SQLException {
        Map<String, Map<String, Integer>> presenzeMap = new HashMap<>();

        String query = "SELECT " +
                "substr(Arrivo, 4, 7) AS mese, " +
                "Persone, Arrivo, Partenza " +
                "FROM Prenotazioni " +
                "ORDER BY mese";

        ResultSet rs = new Gateway().execSelectQuery(query);

        while (rs.next()) {
            int persone = rs.getInt("Persone");
            String anno = rs.getString("mese").substring(3, 7);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate arrivo = LocalDate.parse(rs.getString("Arrivo"), formatter);
            LocalDate partenza = LocalDate.parse(rs.getString("Partenza"), formatter);

            while (!arrivo.isAfter(partenza)) {
                String meseCorrente = arrivo.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                int giorniNelMese = arrivo.lengthOfMonth();
                int giorniMeseCorrente;

                if (arrivo.getMonth() == partenza.getMonth()) {
                    giorniMeseCorrente = Math.min(partenza.getDayOfMonth() - arrivo.getDayOfMonth(),
                            (int) ChronoUnit.DAYS.between(arrivo, partenza));
                } else {
                    giorniMeseCorrente = Math.min(giorniNelMese - arrivo.getDayOfMonth() + 1,
                            (int) ChronoUnit.DAYS.between(arrivo, partenza) + 1);
                }

                // Calcola le presenze per il mese corrente
                int presenzeMeseCorrente = giorniMeseCorrente * persone;
//                String annoCorrente = arrivo.format(DateTimeFormatter.ofPattern("yyyy"));
//                String annoSuccessivo = arrivo.plusYears(1).format(DateTimeFormatter.ofPattern("yyyy"));

                presenzeMap
                        .computeIfAbsent(anno, k -> new HashMap<>())
                        .merge(meseCorrente, presenzeMeseCorrente, Integer::sum);

                //FIXME: quando c'è una prenotazione a cavallo tra due anni viene contata bene sul grafico ma non sulla tabella

                arrivo = arrivo.plusMonths(1).withDayOfMonth(1);
            }
        }

        rs.close();

        for (Map.Entry<String, Map<String, Integer>> yearEntry : presenzeMap.entrySet()) {
            Map<String, Integer> monthMap = yearEntry.getValue();
            yearEntry.setValue(invertMonthMapOrder(monthMap));
        }

        return presenzeMap;
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

    // Serve per modificare l'ordine dei mesi
    public static Map<String, Integer> invertMonthMapOrder(Map<String, Integer> monthMap) {
        LinkedHashMap<String, Integer> invertedMonthMap = new LinkedHashMap<>();
        List<String> monthKeys = new ArrayList<>(monthMap.keySet());

        Collections.reverse(monthKeys);

        for (String monthKey : monthKeys) {
            invertedMonthMap.put(monthKey, monthMap.get(monthKey));
        }

        return invertedMonthMap;
    }
}
