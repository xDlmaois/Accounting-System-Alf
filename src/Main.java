import data.DataStore;
import ui.LoginWindow;

import javax.swing.SwingUtilities;

public class Main {
    // Starts the application on Swing's event thread.
    public static void main(String[] args) throws Exception {
        DataStore dataStore = new DataStore();
        SwingUtilities.invokeLater(() -> new LoginWindow(dataStore).setVisible(true));
    }
}
