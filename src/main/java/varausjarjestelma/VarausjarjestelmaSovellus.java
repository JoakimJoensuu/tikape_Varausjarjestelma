package varausjarjestelma;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class VarausjarjestelmaSovellus implements CommandLineRunner {

    public static void main(String[] args) {

        SpringApplication.run(VarausjarjestelmaSovellus.class);
    }

    @Autowired
    Tekstikayttoliittyma tekstikayttoliittyma;

    @Override
    public void run(String... args) throws Exception {
        alustaTietokanta();

        Scanner lukija = new Scanner(System.in);
        tekstikayttoliittyma.kaynnista(lukija);
    }

    private static void alustaTietokanta() {
        // mikäli poistat vahingossa tietokannan voit ajaa tämän metodin jolloin 
        // tietokantataulu luodaan uudestaan

        try (Connection conn = DriverManager.getConnection("jdbc:h2:./hotelliketju", "sa", "")) {
            conn.prepareStatement("DROP TABLE Tyyppi IF EXISTS;").executeUpdate();
            conn.prepareStatement(
                    "CREATE TABLE Tyyppi("
                    + "id Integer AUTO_INCREMENT, "
                    + "nimi Varchar(255), "
                    + "PRIMARY KEY (id));").executeUpdate();

            conn.prepareStatement("DROP TABLE Asiakas IF EXISTS;").executeUpdate();
            conn.prepareStatement(
                    "CREATE TABLE Asiakas("
                    + "id Integer AUTO_INCREMENT, "
                    + "nimi Varchar(255), "
                    + "sahkoposti Varchar(255), "
                    + "puhelinnumero Varchar(255), "
                    + "PRIMARY KEY (id));"
            ).executeUpdate();

            conn.prepareStatement("DROP TABLE Hotellihuone IF EXISTS;").executeUpdate();
            conn.prepareStatement(
                    "CREATE TABLE Hotellihuone("
                    + "numero Integer, "
                    + "tyyppi_id Integer, "
                    + "paivahinta Integer, "
                    + "PRIMARY KEY (numero), "
                    + "FOREIGN KEY (tyyppi_id) REFERENCES Tyyppi (id));"
            ).executeUpdate();

            conn.prepareStatement("DROP TABLE Varaus IF EXISTS;").executeUpdate();
            conn.prepareStatement(
                    "CREATE TABLE Varaus("
                    + "id Integer AUTO_INCREMENT, "
                    + "hotellihuone_numero Integer, "
                    + "asiakas_id Integer, "
                    + "alku Timestamp, "
                    + "loppu Timestamp, "
                    + "PRIMARY KEY (id), "
                    + "FOREIGN KEY (hotellihuone_numero) REFERENCES Hotellihuone (Numero), "
                    + "FOREIGN KEY (asiakas_id) REFERENCES Asiakas (Id));"
            ).executeUpdate();

            conn.prepareStatement("DROP TABLE Lisavaruste IF EXISTS;").executeUpdate();
            conn.prepareStatement(
                    "CREATE TABLE Lisavaruste("
                    + "Varaus_id Integer, "
                    + "nimi Varchar(255), "
                    + "FOREIGN KEY (Varaus_id) REFERENCES Varaus (Id));"
            ).executeUpdate();

            conn.prepareStatement("INSERT INTO Asiakas"
                    + " (nimi, sahkoposti, puhelinnumero)"
                    + " VALUES ('Matti Meikalainen', 'email.is@breached.com', '0401234567');").executeUpdate();

            conn.prepareStatement("INSERT INTO Asiakas"
                    + " (nimi, sahkoposti, puhelinnumero)"
                    + " VALUES ('Matti Meikalainen2', 'email.is@breached.com2', '04012345672');").executeUpdate();

            conn.prepareStatement("INSERT INTO Tyyppi"
                    + " (nimi)"
                    + " VALUES ('asd');").executeUpdate();

            conn.prepareStatement("INSERT INTO Hotellihuone"
                    + " (numero, tyyppi_id, paivahinta)"
                    + " VALUES (1, 1, 11);").executeUpdate();

            conn.prepareStatement("INSERT INTO Varaus"
                    + " (hotellihuone_numero, asiakas_id, alku, loppu)"
                    + " VALUES (1, 1, '2000-02-10 16:00:00', '2000-02-15 10:00:00');").executeUpdate();

            conn.prepareStatement("INSERT INTO Lisavaruste"
                    + " (Varaus_id, nimi)"
                    + " VALUES (1, 'keppi');").executeUpdate();

            conn.prepareStatement("INSERT INTO Lisavaruste"
                    + " (Varaus_id, nimi)"
                    + " VALUES (1, 'kkuokka');").executeUpdate();

            conn.prepareStatement("INSERT INTO Hotellihuone"
                    + " (numero, tyyppi_id, paivahinta)"
                    + " VALUES (2, 1, 12);").executeUpdate();

            conn.prepareStatement("INSERT INTO Varaus"
                    + " (hotellihuone_numero, asiakas_id, alku, loppu)"
                    + " VALUES (2, 2, '2000-02-20 16:00:00', '2000-02-21 10:00:00');").executeUpdate();

            conn.prepareStatement("INSERT INTO Tyyppi"
                    + " (nimi)"
                    + " VALUES ('asdf');").executeUpdate();

            conn.prepareStatement("INSERT INTO Hotellihuone"
                    + " (numero, tyyppi_id, paivahinta)"
                    + " VALUES (3, 2, 13);").executeUpdate();

            conn.prepareStatement("INSERT INTO Hotellihuone"
                    + " (numero, tyyppi_id, paivahinta)"
                    + " VALUES (4, 2, 14);").executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(VarausjarjestelmaSovellus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
