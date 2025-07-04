package globalquake.playground;

import globalquake.client.GlobalQuakeLocal;
import globalquake.core.GlobalQuake;
import globalquake.core.archive.EarthquakeArchive;
import globalquake.core.database.StationDatabaseManager;
import globalquake.core.earthquake.EarthquakeAnalysis;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.exception.ApplicationErrorHandler;
import globalquake.core.regions.Regions;
import globalquake.core.station.GlobalStationManager;
import globalquake.main.Main;
import globalquake.utils.Scale;
import globalquake.utils.monitorable.MonitorableCopyOnWriteArrayList;
import org.tinylog.Logger;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GlobalQuakePlayground extends GlobalQuakeLocal {

    public long createdAtMillis;
    private long timeOffset = 0;
    private final long playgroundStartMillis = LocalDate.of(2000, 1, 1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

    private final Collection<Earthquake> playgroundEarthquakes = new MonitorableCopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        GlobalQuake.prepare(Main.MAIN_FOLDER, new ApplicationErrorHandler(null, false));
        Regions.init();
        Scale.load();

        new GlobalQuakePlayground();
    }

    @Override
    public void startRuntime() {
        getGlobalQuakeRuntime().runThreads();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> playgroundEarthquakes.removeIf(earthquake -> EarthquakeAnalysis.shouldRemove(earthquake, -30)), 0, 1, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unused")
    public GlobalQuakePlayground(StationDatabaseManager stationDatabaseManager, GlobalStationManager globalStationManager) {
        super(stationDatabaseManager, globalStationManager);
    }

    public GlobalQuakePlayground() {
        super(new StationDatabaseManagerPlayground(), new GlobalStationManagerPlayground());
        new WaveformGenerator(this);
        createdAtMillis = System.currentTimeMillis();
        createFrame();
        startRuntime();
    }

    public GlobalQuakePlayground createFrame() {
        EventQueue.invokeLater(() -> {
            try {
                globalQuakeFrame = new GlobalQuakeFramePlayground();
                globalQuakeFrame.setVisible(true);

                Main.getErrorHandler().setParent(globalQuakeFrame);

// 为了确保 WindowAdapter 能被识别，需要添加相应的导入语句
                globalQuakeFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        for (Earthquake quake : getEarthquakeAnalysis().getEarthquakes()) {
                            getArchive().archiveQuake(quake);
                        }
                        getArchive().saveArchive();
                    }
                });
            } catch (Exception e) {
                Logger.error(e);
                System.exit(0);
            }
        });
        return this;
    }

    @Override
    public long currentTimeMillis() {
        return playgroundStartMillis + (System.currentTimeMillis() - createdAtMillis) + timeOffset;
    }

    public void setTimeOffset(long offset) {
        this.timeOffset = offset;
    }

    public long getTimeOffset() {
        return timeOffset;
    }

    public long getPlaygroundStartMillis() {
        return playgroundStartMillis;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    @Override
    public EarthquakeArchive createArchive() {
        return new EarthquakeArchive();
    }

    public Collection<Earthquake> getPlaygroundEarthquakes() {
        return playgroundEarthquakes;
    }

    public String getWatermark() {
        return "";
    }

    public boolean isSimulation() {
        return true;
    }
}
