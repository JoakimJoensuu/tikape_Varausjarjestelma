package varausjarjestelma;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Tekstikayttoliittyma {

    @Autowired
    private Varausjarjestelma varausjarjestelma;

    public void kaynnista(Scanner lukija) {
        while (true) {
            System.out.println("Komennot: ");
            System.out.println(" x - lopeta");
            System.out.println(" 1 - lisaa huone");
            System.out.println(" 2 - listaa huoneet");
            System.out.println(" 3 - hae huoneita");
            System.out.println(" 4 - lisaa varaus");
            System.out.println(" 5 - listaa varaukset");
            System.out.println(" 6 - tilastoja");
            System.out.println("");

            String komento = lukija.nextLine();
            System.out.println("");
            if (komento.equals("x")) {
                break;
            }

            if (komento.equals("1")) {
                varausjarjestelma.lisaaHuone(lukija);
            } else if (komento.equals("2")) {
                varausjarjestelma.listaaHuoneet();
            } else if (komento.equals("3")) {
                varausjarjestelma.haeHuoneita(lukija);
            } else if (komento.equals("4")) {
                varausjarjestelma.lisaaVaraus(lukija);
            } else if (komento.equals("5")) {
                varausjarjestelma.listaaVaraukset();
            } else if (komento.equals("6")) {
                varausjarjestelma.tilastoja(lukija);
            }
        }
    }
}
