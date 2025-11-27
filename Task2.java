import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.lang.management.ManagementFactory;
import java.util.Locale;

import com.sun.management.OperatingSystemMXBean;

public class Task2 extends JPanel {

    private final JLabel cpuLabel = new JLabel("CPU: N/A");
    private final JLabel ramLabel = new JLabel("RAM: N/A");
    private final OperatingSystemMXBean osBean;

    public Task2() {
        OperatingSystemMXBean bean;
        try {
            bean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        } catch (Exception e) {
            bean = null;
        }
        this.osBean = bean;

        buildUI();
        startMonitoringTimer();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder(BorderFactory.createEtchedBorder(),
                "2. Incarcare CPU si memorie RAM"));

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        cpuLabel.setFont(cpuLabel.getFont().deriveFont(Font.BOLD, 18f));
        ramLabel.setFont(ramLabel.getFont().deriveFont(Font.BOLD, 18f));

        cpuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ramLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalStrut(40));
        center.add(cpuLabel);
        center.add(Box.createVerticalStrut(20));
        center.add(ramLabel);
        center.add(Box.createVerticalGlue());

        JLabel info = new JLabel("Actualizare la fiecare 5 secunde (OperatingSystemMXBean)");
        info.setHorizontalAlignment(SwingConstants.CENTER);

        add(center, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);
    }

    private void startMonitoringTimer() {
        int delayMs = 5000; // 5 secunde
        Timer timer = new Timer(delayMs, e -> updateStats());
        timer.setInitialDelay(0); // prima actualizare imediat
        timer.start();
    }

    private void updateStats() {
        if (osBean == null) {
            cpuLabel.setText("CPU: N/A (OperatingSystemMXBean indisponibil)");
            ramLabel.setText("RAM: N/A");
            return;
        }

        double cpuLoad = osBean.getSystemCpuLoad();
        long freeMem = osBean.getFreePhysicalMemorySize();
        long totalMem = osBean.getTotalPhysicalMemorySize();

        String cpuText;
        if (cpuLoad < 0) {
            cpuText = "CPU: N/A";
        } else {
            double percent = cpuLoad * 100.0;
            cpuText = String.format(Locale.US, "CPU: %.1f %%", percent);
        }

        String ramText;
        if (totalMem <= 0) {
            ramText = "RAM: N/A";
        } else {
            double freeGB = freeMem / (1024.0 * 1024 * 1024);
            double totalGB = totalMem / (1024.0 * 1024 * 1024);
            ramText = String.format(Locale.US,
                    "RAM libera: %.2f GB din %.2f GB",
                    freeGB, totalGB
            );
        }

        cpuLabel.setText(cpuText);
        ramLabel.setText(ramText);
    }
}
