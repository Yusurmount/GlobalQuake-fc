package globalquake.ui.settings;

import globalquake.core.GlobalQuake;
import java.awt.*;

public class ClientSettingsFrame extends SettingsFrame {

    private static ClientSettingsFrame instance;

    public static ClientSettingsFrame getInstance() {
        return instance;
    }

    public ClientSettingsFrame(Component parent, boolean isClient) {
        super(parent, isClient);
        if (instance == null) {
            instance = this;
        }
    }

    @Override
    protected void addPanels() {
        super.addPanels();
        // Only add Playground panel in playground mode
        if (GlobalQuake.getInstance() instanceof globalquake.playground.GlobalQuakePlayground) {
            addPanel(new PlaygroundSettingsPanel());
        }
    }
}