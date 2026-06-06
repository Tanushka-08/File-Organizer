import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileOrganizerGUI gui = new FileOrganizerGUI();
            gui.setVisible(true);
        });
    }
}