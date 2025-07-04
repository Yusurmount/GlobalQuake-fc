package globalquake.ui.settings;

import globalquake.core.GlobalQuake;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import globalquake.core.station.AbstractStation;
import globalquake.playground.GlobalQuakePlayground;
import globalquake.playground.PlaygroundStation;
import globalquake.playground.StationWaveformGenerator;

public class PlaygroundSettingsPanel extends SettingsPanel {

    @Override
    public String getTitle() {
        return "Debug";
    }

    private JSpinner sensitivitySpinner;
    private JSpinner delaySpinner;
    private JSpinner simulationTimeSpinner;

    public PlaygroundSettingsPanel() {
        super();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Simulation Time Setting
        JPanel simulationTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        simulationTimePanel.add(new JLabel("模拟时间:"));
        simulationTimeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(simulationTimeSpinner, "yyyy-MM-dd HH:mm:ss");
        simulationTimeSpinner.setEditor(dateEditor);
        simulationTimeSpinner.setPreferredSize(new Dimension(200, 25));
        simulationTimePanel.add(simulationTimeSpinner);
        add(simulationTimePanel);

        // Sensitivity Setting
        JPanel sensitivityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sensitivityPanel.add(new JLabel("台站灵敏度乘数:"));
        sensitivitySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));
        sensitivitySpinner.setPreferredSize(new Dimension(100, 25));
        sensitivityPanel.add(sensitivitySpinner);
        add(sensitivityPanel);

        // Delay Setting
        JPanel delayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel delayLabel = new JLabel("随机延迟偏差 (毫秒):");
        delayLabel.setToolTipText("使用高斯分布控制随机延迟变化（值越高变化越大）");
        delayPanel.add(delayLabel);
        delaySpinner = new JSpinner(new SpinnerNumberModel(8000.0, 0, 30000, 100.0));
        delaySpinner.setPreferredSize(new Dimension(100, 25));
        delayPanel.add(delaySpinner);
        add(delayPanel);

        // Load current values
        loadCurrentValues();
    }

    private void loadCurrentValues() {
        GlobalQuake instance = GlobalQuake.getInstance();
        if (instance instanceof GlobalQuakePlayground) {
            GlobalQuakePlayground playground = (GlobalQuakePlayground) instance;
            // Set simulation time
            long currentSimulationTime = playground.currentTimeMillis();
            simulationTimeSpinner.setValue(new Date(currentSimulationTime));
            
            // Load sensitivity and delay from first playground station
            for (AbstractStation station : GlobalQuake.instance.getStationManager().getStations()) {
                if (station instanceof PlaygroundStation) {
                    StationWaveformGenerator generator = ((PlaygroundStation) station).getGenerator();
                    sensitivitySpinner.setValue(generator.getSensMul());
                    delaySpinner.setValue(generator.getDelayBias());
                    break; // Take first station's values
                }
            }
        } else {
            // Set default values if not in playground mode
            sensitivitySpinner.setValue(1.0);
            delaySpinner.setValue(8000.0);
            simulationTimeSpinner.setValue(new Date());
        }
    }

    @Override
    public void save() {
        GlobalQuake instance = GlobalQuake.getInstance();
        if (!(instance instanceof GlobalQuakePlayground)) {
            return;
        }

        GlobalQuakePlayground playground = (GlobalQuakePlayground) instance;

        // Save simulation time
        Date selectedDate = (Date) simulationTimeSpinner.getValue();
        long desiredTime = selectedDate.getTime();
        long currentElapsed = System.currentTimeMillis() - playground.getCreatedAtMillis();
        long newOffset = desiredTime - playground.getPlaygroundStartMillis() - currentElapsed;
        playground.setTimeOffset(newOffset);

        // Save sensitivity
        double sensitivity = (Double) sensitivitySpinner.getValue();
        instance.getStationManager().getStations().forEach(station -> {
            if (station instanceof PlaygroundStation) {
                ((PlaygroundStation) station).getGenerator().setSensMul(sensitivity);
            }
        });

        // Save delay
        double delayBias = (Double) delaySpinner.getValue();
        instance.getStationManager().getStations().forEach(station -> {
            if (station instanceof PlaygroundStation) {
                ((PlaygroundStation) station).getGenerator().setDelayBias(delayBias);
            }
        });
    }

    @Override
    public void refreshUI() {
        loadCurrentValues();
    }
}