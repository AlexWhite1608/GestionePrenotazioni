import views.HomePage;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new HomePage();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
