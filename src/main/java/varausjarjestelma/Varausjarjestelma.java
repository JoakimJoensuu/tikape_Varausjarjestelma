package varausjarjestelma;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class Varausjarjestelma {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void lisaaHuone(Scanner s) {
        System.out.println("Lisätään huone");
        System.out.println("");

        System.out.println("Minkä tyyppinen huone on?");
        String tyyppi = s.nextLine();
        System.out.println("Mikä huoneen numeroksi asetetaan?");
        int numero = Integer.valueOf(s.nextLine());
        System.out.println("Kuinka monta euroa huone maksaa yöltä?");
        int hinta = Integer.valueOf(s.nextLine());

        List<String> tyyppiId = jdbcTemplate.query("SELECT id FROM Tyyppi WHERE nimi = ?", (rs, rowNum) -> rs.getString("id"), tyyppi);

        if (tyyppiId.size() > 0) {
            jdbcTemplate.update("INSERT INTO Hotellihuone"
                    + " (numero, tyyppi_id, paivahinta)"
                    + " VALUES (?, ?, ?)",
                    numero, tyyppiId.get(0), hinta);
        } else {
            jdbcTemplate.update("INSERT INTO Tyyppi"
                    + " (nimi)"
                    + " VALUES (?)",
                    tyyppi);
            tyyppiId = jdbcTemplate.query("SELECT id FROM Tyyppi WHERE nimi = ?", (rs, rowNum) -> rs.getString("id"), tyyppi);
            jdbcTemplate.update("INSERT INTO Hotellihuone"
                    + " (numero, tyyppi_id, paivahinta)"
                    + " VALUES (?, ?, ?)",
                    numero, tyyppiId.get(0), hinta);
        }

    }

    public void listaaHuoneet() {
        System.out.println("Listataan huoneet");
        System.out.println("");

        jdbcTemplate.query("SELECT Tyyppi.nimi, Hotellihuone.numero, Hotellihuone.paivahinta FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id;", (rs, rowNum) -> rs.getString("nimi") + ", " + rs.getInt("numero") + ", " + rs.getInt("paivahinta") + " euroa ").forEach(System.out::println);
        System.out.println("");
    }

    public void haeHuoneita(Scanner s) {
        System.out.println("Haetaan huoneita");
        System.out.println("");

        System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");;
        LocalDateTime alku = LocalDateTime.parse(s.nextLine() + " " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Milloin varaus loppuisi (yyyy-MM-dd)?");
        LocalDateTime loppu = LocalDateTime.parse(s.nextLine() + " " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Minkä tyyppinen huone? (tyhjä = ei rajausta)");
        String tyyppi = s.nextLine();
        System.out.println("Minkä hintainen korkeintaan? (tyhjä = ei rajausta)");
        String maksimihinta = s.nextLine();

        List<String> vapaatHuoneet;

        if (tyyppi.equals("") && maksimihinta.equals("")) {
            vapaatHuoneet = jdbcTemplate.query("SELECT Tyyppi.nimi, Hotellihuone.numero, Hotellihuone.paivahinta "
                    + "FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id "
                    + "LEFT JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero "
                    + "WHERE Hotellihuone.numero NOT IN (SELECT Hotellihuone.numero FROM Hotellihuone JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Varaus.alku <= ? AND Varaus.loppu >= ?) OR (Varaus.loppu >= ? AND Varaus.loppu <= ?) OR (Varaus.alku >= ? AND Varaus.alku <= ?)) "
                    + "OR (Varaus.alku IS NULL AND Varaus.loppu is NULL) "
                    + "GROUP BY Hotellihuone.numero;",
                    (rs, rowNum) -> rs.getString("nimi") + ", " + rs.getInt("numero") + ", " + rs.getInt("paivahinta") + " euroa ", alku, loppu, alku, loppu, alku, loppu);
        } else if (!(tyyppi.equals("")) && maksimihinta.equals("")) {
            vapaatHuoneet = jdbcTemplate.query("SELECT Tyyppi.nimi, Hotellihuone.numero, Hotellihuone.paivahinta "
                    + "FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id "
                    + "LEFT JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero "
                    + "WHERE (Hotellihuone.numero NOT IN (SELECT Hotellihuone.numero FROM Hotellihuone JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Varaus.alku <= ? AND Varaus.loppu >= ?) OR (Varaus.loppu >= ? AND Varaus.loppu <= ?) OR (Varaus.alku >= ? AND Varaus.alku <= ?)) "
                    + "OR (Varaus.alku IS NULL AND Varaus.loppu is NULL)) AND Tyyppi.nimi = ? "
                    + "GROUP BY Hotellihuone.numero;",
                    (rs, rowNum) -> rs.getString("nimi") + ", " + rs.getInt("numero") + ", " + rs.getInt("paivahinta") + " euroa ", alku, loppu, alku, loppu, alku, loppu, tyyppi);
        } else if (tyyppi.equals("") && !(maksimihinta.equals(""))) {
            vapaatHuoneet = jdbcTemplate.query("SELECT Tyyppi.nimi, Hotellihuone.numero, Hotellihuone.paivahinta "
                    + "FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id "
                    + "LEFT JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero "
                    + "WHERE (Hotellihuone.numero NOT IN (SELECT Hotellihuone.numero FROM Hotellihuone JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Varaus.alku <= ? AND Varaus.loppu >= ?) OR (Varaus.loppu >= ? AND Varaus.loppu <= ?) OR (Varaus.alku >= ? AND Varaus.alku <= ?)) "
                    + "OR (Varaus.alku IS NULL AND Varaus.loppu is NULL)) AND Hotellihuone.paivahinta <= ? "
                    + "GROUP BY Hotellihuone.numero;", (rs, rowNum) -> rs.getString("nimi") + ", " + rs.getInt("numero") + ", " + rs.getInt("paivahinta") + " euroa ", alku, loppu, alku, loppu, alku, loppu, maksimihinta);
        } else {
            vapaatHuoneet = jdbcTemplate.query("SELECT Tyyppi.nimi, Hotellihuone.numero, Hotellihuone.paivahinta "
                    + "FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id "
                    + "LEFT JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero "
                    + "WHERE (Hotellihuone.numero NOT IN (SELECT Hotellihuone.numero FROM Hotellihuone JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Varaus.alku <= ? AND Varaus.loppu >= ?) OR (Varaus.loppu >= ? AND Varaus.loppu <= ?) OR (Varaus.alku >= ? AND Varaus.alku <= ?)) "
                    + "OR (Varaus.alku IS NULL AND Varaus.loppu is NULL)) "
                    + "AND Hotellihuone.paivahinta <= ? AND Tyyppi.nimi = ? "
                    + "GROUP BY Hotellihuone.numero;", (rs, rowNum) -> rs.getString("nimi") + ", " + rs.getInt("numero") + ", " + rs.getInt("paivahinta") + " euroa ", alku, loppu, alku, loppu, alku, loppu, maksimihinta, tyyppi);
        }

        if (vapaatHuoneet.size() > 0) {
            vapaatHuoneet.forEach((huone) -> {
                System.out.println(huone);
            });
            System.out.println("");
        } else {
            System.out.println("Ei vapaita huoneita.");
        }
    }

    public void lisaaVaraus(Scanner s) {
        System.out.println("Haetaan huoneita");
        System.out.println("");

        System.out.println("Milloin varaus alkaisi (yyyy-MM-dd)?");
        LocalDateTime alku = LocalDateTime.parse(s.nextLine() + " " + "16:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Milloin varaus loppuisi (yyyy-MM-dd)?");
        LocalDateTime loppu = LocalDateTime.parse(s.nextLine() + " " + "10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Minkä tyyppinen huone? (tyhjä = ei rajausta)");
        String tyyppi = s.nextLine();
        System.out.println("Minkä hintainen korkeintaan? (tyhjä = ei rajausta)");
        String maksimihinta = s.nextLine();

        List<String> vapaatHuoneet;

        if (tyyppi.equals("") && maksimihinta.equals("")) {
            vapaatHuoneet = jdbcTemplate.query("SELECT Hotellihuone.numero FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id LEFT JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE Hotellihuone.numero NOT IN (SELECT Hotellihuone.numero FROM Hotellihuone JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Varaus.alku <= ? AND Varaus.loppu >= ?) OR (Varaus.loppu >= ? AND Varaus.loppu <= ?) OR (Varaus.alku >= ? AND Varaus.alku <= ?)) OR (Varaus.alku IS NULL AND Varaus.loppu is NULL) GROUP BY Hotellihuone.numero ORDER BY Hotellihuone.paivahinta DESC;", (rs, rowNum) -> rs.getString("numero"), alku, loppu, alku, loppu, alku, loppu);
        } else if (!(tyyppi.equals("")) && maksimihinta.equals("")) {
            vapaatHuoneet = jdbcTemplate.query("SELECT Hotellihuone.numero FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id LEFT JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Hotellihuone.numero NOT IN (SELECT Hotellihuone.numero FROM Hotellihuone JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Varaus.alku <= ? AND Varaus.loppu >= ?) OR (Varaus.loppu >= ? AND Varaus.loppu <= ?) OR (Varaus.alku >= ? AND Varaus.alku <= ?)) OR (Varaus.alku IS NULL AND Varaus.loppu is NULL)) AND Tyyppi.nimi = ? GROUP BY Hotellihuone.numero ORDER BY Hotellihuone.paivahinta DESC;", (rs, rowNum) -> rs.getString("numero"), alku, loppu, alku, loppu, alku, loppu, tyyppi);
        } else if (tyyppi.equals("") && !(maksimihinta.equals(""))) {
            vapaatHuoneet = jdbcTemplate.query("SELECT Hotellihuone.numero FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id LEFT JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Hotellihuone.numero NOT IN (SELECT Hotellihuone.numero FROM Hotellihuone JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Varaus.alku <= ? AND Varaus.loppu >= ?) OR (Varaus.loppu >= ? AND Varaus.loppu <= ?) OR (Varaus.alku >= ? AND Varaus.alku <= ?)) OR (Varaus.alku IS NULL AND Varaus.loppu is NULL)) AND Hotellihuone.paivahinta <= ? GROUP BY Hotellihuone.numero ORDER BY Hotellihuone.paivahinta DESC;", (rs, rowNum) -> rs.getString("numero"), alku, loppu, alku, loppu, alku, loppu, maksimihinta);
        } else {
            vapaatHuoneet = jdbcTemplate.query("SELECT Hotellihuone.numero FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id LEFT JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero "
                    + "WHERE (Hotellihuone.numero NOT IN (SELECT Hotellihuone.numero FROM Hotellihuone JOIN Varaus ON Varaus.hotellihuone_numero = Hotellihuone.numero WHERE (Varaus.alku <= ? AND Varaus.loppu >= ?) OR (Varaus.loppu >= ? AND Varaus.loppu <= ?) OR (Varaus.alku >= ? AND Varaus.alku <= ?)) OR (Varaus.alku IS NULL AND Varaus.loppu is NULL)) "
                    + "AND Hotellihuone.paivahinta <= ? AND Tyyppi.nimi = ? GROUP BY Hotellihuone.numero ORDER BY Hotellihuone.paivahinta DESC;", (rs, rowNum) -> rs.getString("numero"), alku, loppu, alku, loppu, alku, loppu, maksimihinta, tyyppi);

        }

        if (vapaatHuoneet.size() == 0) {
            System.out.println("Ei vapaita huoneita.");
        } else {
            System.out.println("Huoneita vapaana: " + vapaatHuoneet.size());

            int huoneita = -1;
            while (true) {
                System.out.println("Montako huonetta varataan?");
                huoneita = Integer.valueOf(s.nextLine());
                if (huoneita >= 1 && huoneita <= Integer.parseInt(vapaatHuoneet.get(0))) {
                    break;
                }

                System.out.println("Epäkelpo huoneiden lukumäärä.");
            }

            List<String> lisavarusteet = new ArrayList<>();
            while (true) {
                System.out.println("Syötä lisävaruste, tyhjä lopettaa");
                String lisavaruste = s.nextLine();
                if (lisavaruste.isEmpty()) {
                    break;
                }
                lisavarusteet.add(lisavaruste);
            }

            System.out.println("Syötä varaajan nimi:");
            String nimi = s.nextLine();
            System.out.println("Syötä varaajan puhelinnumero:");
            String puhelinnumero = s.nextLine();
            System.out.println("Syötä varaajan sähköpostiosoite:");
            String sahkoposti = s.nextLine();
            System.out.println(sahkoposti);

            List<String> asiakasId = jdbcTemplate.query("SELECT id FROM Asiakas WHERE puhelinnumero = ?", (rs, rowNum) -> rs.getString("id"), puhelinnumero);

            if (asiakasId.isEmpty()) {

                jdbcTemplate.update("INSERT INTO Asiakas"
                        + " (nimi, sahkoposti, puhelinnumero)"
                        + " VALUES (?, ?, ?)",
                        nimi, sahkoposti, puhelinnumero);
                asiakasId = jdbcTemplate.query("SELECT TOP 1 Id FROM Asiakas ORDER BY Id DESC", (rs, rowNum) -> rs.getString("id"));
            }

            for (int i = 0; i < huoneita; i++) {
                int huoneNumero = Integer.parseInt(vapaatHuoneet.get(i));

                jdbcTemplate.update("INSERT INTO Varaus"
                        + " (hotellihuone_numero, asiakas_id, alku, loppu)"
                        + " VALUES (?, ?, ?, ?)",
                        huoneNumero, asiakasId.get(0), alku, loppu);

                Integer varausId = Integer.parseInt(jdbcTemplate.query("SELECT TOP 1 Id FROM Varaus ORDER BY Id DESC", (rs, rowNum) -> rs.getString("id")).get(0));

                for (int j = 0; j < lisavarusteet.size(); j++) {
                    String lisavaruste = lisavarusteet.get(j).toLowerCase();

                    jdbcTemplate.update("INSERT INTO Lisavaruste"
                            + " (varaus_id, nimi)"
                            + " VALUES (?, ?)",
                            varausId, lisavaruste);

                }
            }
        }
    }

    public void listaaVaraukset() {
        System.out.println("Listataan varaukset");
        System.out.println("");

        List<String> varausIdt = jdbcTemplate.query("SELECT id FROM Varaus ORDER BY alku ASC", (rs, rowNum) -> rs.getString("id"));

        for (String id : varausIdt) {

            String asiakas = jdbcTemplate.query("SELECT Asiakas.Nimi, Asiakas.sahkoposti, Varaus.alku, Varaus.loppu, (SELECT COUNT(*) FROM Lisavaruste WHERE Varaus_id = ?) AS Lisavarusteet, Tyyppi.nimi, Hotellihuone.numero, Hotellihuone.paivahinta, DATEDIFF('DAY', Varaus.alku, Varaus.loppu) AS ero, DATEDIFF('DAY', Varaus.alku, Varaus.loppu) * Hotellihuone.paivahinta AS yhteensa FROM Asiakas LEFT JOIN Varaus ON Varaus.asiakas_id = Asiakas.id JOIN Hotellihuone ON Varaus.hotellihuone_numero = Hotellihuone.numero JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id WHERE varaus.Id = ?",
                    (rs, rowNum) -> rs.getString("Asiakas.nimi") + ", " + rs.getString("Asiakas.sahkoposti") + ", " + rs.getString("alku").substring(0, 10) + ", " + rs.getString("loppu").substring(0, 10) + ", " + rs.getString("ero") + " päivä(ä) " + rs.getString("Lisavarusteet") + " lisavarustetta, " + rs.getString("Tyyppi.nimi") + ", " + rs.getString("Hotellihuone.numero") + ", " + rs.getString("Hotellihuone.paivahinta") + " euroa, yhteensä: " + rs.getString("yhteensa") + " euroa ", id, id).get(0);

            System.out.println(asiakas);
            System.out.println("");

        }
    }

    public void tilastoja(Scanner lukija) {
        System.out.println("Mitä tilastoja tulostetaan?");
        System.out.println("");

        // tilastoja pyydettäessä käyttäjältä kysytään tilasto
        System.out.println(" 1 - Suosituimmat lisävarusteet");
        System.out.println(" 2 - Parhaat asiakkaat");
        System.out.println(" 3 - Varausprosentti huoneittain");
        System.out.println(" 4 - Varausprosentti huonetyypeittäin");

        System.out.println("Syötä komento: ");
        int komento = Integer.valueOf(lukija.nextLine());

        if (komento == 1) {
            suosituimmatLisavarusteet();
        } else if (komento == 2) {
            parhaatAsiakkaat();
        } else if (komento == 3) {
            varausprosenttiHuoneittain(lukija);
        } else if (komento == 4) {
            varausprosenttiHuonetyypeittain(lukija);
        }
    }

    public void suosituimmatLisavarusteet() {
        System.out.println("Tulostetaan suosituimmat lisävarusteet");
        System.out.println("");

        jdbcTemplate.query("SELECT Nimi, COUNT(*) AS varausta FROM Lisavaruste GROUP BY Nimi", (rs, rowNum) -> rs.getString("Nimi") + ", " + rs.getString("Varausta") + " varausta").forEach(System.out::println);
        System.out.println("");
    }

    public void parhaatAsiakkaat() {
        System.out.println("Tulostetaan parhaat asiakkaat");
        System.out.println("");

        jdbcTemplate.query("SELECT TOP 10 Asiakas.nimi, Asiakas.sahkoposti, Asiakas.puhelinnumero, SUM(DATEDIFF('DAY', Varaus.alku, Varaus.loppu) * Hotellihuone.paivahinta) AS yhteensa FROM Asiakas JOIN Varaus ON Varaus.asiakas_id = Asiakas.id JOIN Hotellihuone ON Varaus.hotellihuone_numero = Hotellihuone.numero GROUP BY Nimi ORDER BY yhteensa DESC", (rs, rowNum) -> rs.getString("Asiakas.nimi") + ", " + rs.getString("Asiakas.sahkoposti") + ", " + rs.getString("Asiakas.puhelinnumero") + ", " + rs.getString("yhteensa") + " euroa").forEach(System.out::println);

        System.out.println("");
    }

    public void varausprosenttiHuoneittain(Scanner lukija) {

        System.out.println("");

        List<String> huonenumerot = jdbcTemplate.query("SELECT numero FROM Hotellihuone", (rs, rowNum) -> rs.getString("numero"));

        System.out.println("Mistä lähtien tarkastellaan?");
        LocalDateTime alku = LocalDateTime.parse(lukija.nextLine() + " 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Mihin asti tarkastellaan?");
        LocalDateTime loppu = LocalDateTime.parse(lukija.nextLine() + " 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("");

        System.out.println("Tulostetaan varausprosentti huoneittain");
        for (String huonenumero : huonenumerot) {

            String huone = jdbcTemplate.query("SELECT Tyyppi.nimi, Hotellihuone.numero, Hotellihuone.paivahinta FROM Hotellihuone JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id WHERE Hotellihuone.numero = ?", (rs, rowNum) -> rs.getString("Tyyppi.nimi") + ", " + rs.getString("Hotellihuone.numero") + ", " + rs.getString("Hotellihuone.paivahinta"), huonenumero).get(0);
            List<Date> alkupaivat = jdbcTemplate.query(
                    "SELECT Varaus.alku FROM Varaus JOIN Hotellihuone ON Hotellihuone.numero = Varaus.hotellihuone_numero WHERE Hotellihuone.numero = ? AND ((? < varaus.alku AND varaus.alku < ?) OR (? < varaus.loppu AND varaus.loppu < ?))", (rs, rowNum) -> rs.getTimestamp("varaus.alku"), huonenumero, alku, loppu, alku, loppu);
            List<Date> loppupaivat = jdbcTemplate.query(
                    "SELECT Varaus.loppu FROM Varaus JOIN Hotellihuone ON Hotellihuone.numero = Varaus.hotellihuone_numero WHERE Hotellihuone.numero = ? AND ((? < varaus.alku AND varaus.alku < ?) OR (? < varaus.loppu AND varaus.loppu < ?))", (rs, rowNum) -> rs.getTimestamp("varaus.loppu"), huonenumero, alku, loppu, alku, loppu);

            Date haluttualkupaiva = java.sql.Timestamp.valueOf(alku);
            Date haluttuloppupaiva = java.sql.Timestamp.valueOf(loppu);

            int paiviaVarattuna = 0;
            for (int i = 0; i < alkupaivat.size(); i++) {
                Date alkupaiva = alkupaivat.get(i);
                Calendar cal = Calendar.getInstance();
                cal.setTime(alkupaiva);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                Date alkupaiva2 = cal.getTime();

                Date loppupaiva = loppupaivat.get(i);
                cal.setTime(loppupaiva);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                Date loppupaiva2 = cal.getTime();

                if (haluttualkupaiva.compareTo(alkupaiva2) <= 0 && loppupaiva2.compareTo(haluttuloppupaiva) <= 0) {
                    paiviaVarattuna += TimeUnit.DAYS.convert(loppupaiva2.getTime() - alkupaiva2.getTime(), TimeUnit.MILLISECONDS);
                } else if (haluttualkupaiva.compareTo(alkupaiva2) <= 0 && loppupaiva2.compareTo(haluttuloppupaiva) > 0) {
                    paiviaVarattuna += TimeUnit.DAYS.convert(haluttuloppupaiva.getTime() - alkupaiva2.getTime(), TimeUnit.MILLISECONDS);
                } else if (haluttualkupaiva.compareTo(alkupaiva2) > 0 && loppupaiva2.compareTo(haluttuloppupaiva) <= 0) {
                    paiviaVarattuna += TimeUnit.DAYS.convert(loppupaiva2.getTime() - haluttualkupaiva.getTime(), TimeUnit.MILLISECONDS);

                }
            }

            long paiviaTarkastelussa = TimeUnit.DAYS.convert(haluttuloppupaiva.getTime() - haluttualkupaiva.getTime(), TimeUnit.MILLISECONDS);
            double varausprosentti = (double) paiviaVarattuna / (double) paiviaTarkastelussa * 100;

            System.out.println(huone + " euroa, " + varausprosentti + "%");

        }
        System.out.println("");

    }

    public void varausprosenttiHuonetyypeittain(Scanner lukija) {

        System.out.println("");

        List<String> huonetyypit = jdbcTemplate.query("SELECT id FROM Tyyppi", (rs, rowNum) -> rs.getString("id"));

        System.out.println("Mistä lähtien tarkastellaan?");
        LocalDateTime alku = LocalDateTime.parse(lukija.nextLine() + " 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("Mihin asti tarkastellaan?");
        LocalDateTime loppu = LocalDateTime.parse(lukija.nextLine() + " 00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        System.out.println("");
        System.out.println("Tulostetaan varausprosentti huonetyypeittän");

        for (String huonetyyppi : huonetyypit) {

            String tyyppi = jdbcTemplate.query("SELECT Tyyppi.nimi FROM Tyyppi WHERE Tyyppi.id = ?", (rs, rowNum) -> rs.getString("Tyyppi.nimi") + ", ", huonetyyppi).get(0);
            List<Date> alkupaivat = jdbcTemplate.query(
                    "SELECT Varaus.alku FROM Varaus JOIN Hotellihuone ON Hotellihuone.numero = Varaus.hotellihuone_numero JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id WHERE Tyyppi.id = ? AND ((? < varaus.alku AND varaus.alku < ?) OR (? < varaus.loppu AND varaus.loppu < ?))", (rs, rowNum) -> rs.getTimestamp("varaus.alku"), huonetyyppi, alku, loppu, alku, loppu);
            List<Date> loppupaivat = jdbcTemplate.query(
                    "SELECT Varaus.loppu FROM Varaus JOIN Hotellihuone ON Hotellihuone.numero = Varaus.hotellihuone_numero JOIN Tyyppi ON Tyyppi.id = Hotellihuone.tyyppi_id WHERE Tyyppi.id = ? AND ((? < varaus.alku AND varaus.alku < ?) OR (? < varaus.loppu AND varaus.loppu < ?))", (rs, rowNum) -> rs.getTimestamp("varaus.loppu"), huonetyyppi, alku, loppu, alku, loppu);
            Date haluttualkupaiva = java.sql.Timestamp.valueOf(alku);
            Date haluttuloppupaiva = java.sql.Timestamp.valueOf(loppu);

            int paiviaVarattuna = 0;
            for (int i = 0; i < alkupaivat.size(); i++) {
                Date alkupaiva = alkupaivat.get(i);
                Calendar cal = Calendar.getInstance();
                cal.setTime(alkupaiva);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                Date alkupaiva2 = cal.getTime();

                Date loppupaiva = loppupaivat.get(i);
                cal.setTime(loppupaiva);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                Date loppupaiva2 = cal.getTime();

                if (haluttualkupaiva.compareTo(alkupaiva2) <= 0 && loppupaiva2.compareTo(haluttuloppupaiva) <= 0) {
                    paiviaVarattuna += TimeUnit.DAYS.convert(loppupaiva2.getTime() - alkupaiva2.getTime(), TimeUnit.MILLISECONDS);
                } else if (haluttualkupaiva.compareTo(alkupaiva2) <= 0 && loppupaiva2.compareTo(haluttuloppupaiva) > 0) {
                    paiviaVarattuna += TimeUnit.DAYS.convert(haluttuloppupaiva.getTime() - alkupaiva2.getTime(), TimeUnit.MILLISECONDS);
                } else if (haluttualkupaiva.compareTo(alkupaiva2) > 0 && loppupaiva2.compareTo(haluttuloppupaiva) <= 0) {
                    paiviaVarattuna += TimeUnit.DAYS.convert(loppupaiva2.getTime() - haluttualkupaiva.getTime(), TimeUnit.MILLISECONDS);
                }
            }

            int tietyntyyppistenhuoneidenmaara = alkupaivat.size();
            long paiviaTarkastelussa = TimeUnit.DAYS.convert(haluttuloppupaiva.getTime() - haluttualkupaiva.getTime(), TimeUnit.MILLISECONDS);
            double varausprosentti = (double) paiviaVarattuna / (double) (paiviaTarkastelussa * tietyntyyppistenhuoneidenmaara) * 100;
            
            if (Double.isNaN(varausprosentti)) {
                varausprosentti = 0;
            }

            System.out.println(tyyppi + varausprosentti + "%");
        }
        System.out.println("");
    }

}
