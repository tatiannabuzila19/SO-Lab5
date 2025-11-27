import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Task1 extends JPanel {

    private final JTextArea messageArea = new JTextArea(5, 25);

    public Task1() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
                "1. Reporniere si inchidere sistem"));

        JPanel textPanel = new JPanel(new BorderLayout(5, 5));
        javax.swing.JLabel label =
                new javax.swing.JLabel("Mesaj ce va fi salvat pe desktop inainte de actiune:");
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(messageArea);
        textPanel.add(label, BorderLayout.NORTH);
        textPanel.add(scroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton shutdownButton = new JButton("Inchide sistemul");
        JButton restartButton = new JButton("Reporneste sistemul");

        shutdownButton.addActionListener(this::handleShutdown);
        restartButton.addActionListener(this::handleRestart);

        buttonPanel.add(shutdownButton);
        buttonPanel.add(restartButton);

        add(textPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleShutdown(ActionEvent e) {
        performSystemAction(false);
    }

    private void handleRestart(ActionEvent e) {
        performSystemAction(true);
    }

    private void performSystemAction(boolean restart) {
        String actionName = restart ? "REPORNIERE" : "INCHIDERE";
        int result = JOptionPane.showConfirmDialog(
                this,
                "Sigur doriti " + actionName.toLowerCase(Locale.ROOT) +
                        " sistemul?\nActiunea poate necesita drepturi de administrator.",
                actionName + " sistem",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        String userMessage = messageArea.getText().trim();
        if (userMessage.isEmpty()) {
            userMessage = "(fara mesaj)";
        }

        final String finalMessage = userMessage;
        new Thread(() -> {
            try {
                logMessageToDesktop(actionName, finalMessage);
                runOSCommand(restart);
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                        this,
                        "Eroare la executarea actiunii: " + ex.getMessage(),
                        "Eroare",
                        JOptionPane.ERROR_MESSAGE
                ));
            }
        }, "SystemActionThread").start();
    }

    private void logMessageToDesktop(String action, String message) throws IOException {
        String userHome = System.getProperty("user.home");
        Path desktopDir = Paths.get(userHome, "Desktop");
        if (!Files.exists(desktopDir)) {
            desktopDir = Paths.get(userHome);
        }

        Path logFile = desktopDir.resolve("system_actions_log.txt");

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = String.format("[%s] %s: %s%n", timestamp, action, message);

        try (BufferedWriter writer = Files.newBufferedWriter(
                logFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write(line);
        }
    }

    private void runOSCommand(boolean restart) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        java.lang.ProcessBuilder pb;

        if (osName.contains("win")) {
            String cmd = restart ? "shutdown /r /t 0" : "shutdown /s /t 0";
            pb = new java.lang.ProcessBuilder("cmd", "/c", cmd);
        } else if (osName.contains("linux")) {
            String cmd = restart ? "reboot" : "shutdown -h now";
            pb = new java.lang.ProcessBuilder("bash", "-c", cmd);
        } else {
            throw new IOException("Sistem de operare neacceptat (numai Windows sau Linux).");
        }

        pb.inheritIO();
        pb.start();
    }
}
