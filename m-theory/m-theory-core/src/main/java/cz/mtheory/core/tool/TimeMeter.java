/**
 * 
 */
package cz.mtheory.core.tool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Michal NkD Nikodim
 */
public enum TimeMeter {
    TRY_SPLIT,
    TRY_JOIN,
    FRUSTRUM_TEST,
    NORMAL_TEST,
    LENGHT_TEST,
    SEGMENT_CONSTRUCTOR,
    PLANET_UPDATE,
    UPDATE_INDEX_BUFFER,
    UPDATE_VERTEX_BUFFER,
    SET_VERTEX,
    CREATE_NEW_POINT,
    SPLIT,
    JOIN,
    TRANSFORM,
    TEST_HASHMAP,
    TEST_FASTMAP,
    TEST_GMAP,
    ;

    private static final double nano = 1000000000.0d;
    private static boolean enabled = true;
    private long totalTime = 0;
    private long totalIterations = 0;
    private long startTime = -1;

    public void start() {
        if (enabled) {
            if (this.startTime != -1) throw new RuntimeException("Can not start twice - TimeUnit name: " + name());
            this.startTime = System.nanoTime();
        }
    }

    public void stop() {
        if (enabled) {
            long time = System.nanoTime();
            if (startTime == -1) throw new RuntimeException("Can not call stop before call start - TimeUnit name: " + name());
            totalTime += (time - startTime);
            totalIterations++;
            startTime = -1;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getFormmatedName());
        if (getTotalIterations() == 0) {
            sb.append(" - empty");
        } else {
            sb.append(" - total: ").append(getFormattedDouble(totalTime / nano)).append("s");
            long average = getOneIterationTime();
            sb.append(", oneIter: ").append(getFormattedDouble(average / nano)).append("s");
            sb.append(", totalIter: ").append(totalIterations);
            sb.append(", iterPerSec: ").append(getFormattedDouble(getIterationPerSec()));
        }
        return sb.toString();
    }

    private String getFormattedDouble(double number) {
        if (Double.isInfinite(number) || Double.isNaN(number)) return "0.000000000";
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(9, RoundingMode.HALF_UP);
        return bd.toPlainString();
    }

    private String getFormmatedName() {
        String name = name();
        if (name.length() > 20) {
            return name.substring(0, 17) + "...";
        } else if (name.length() < 20) {
            return name + "                    ".substring(0, 20 - name.length());
        }
        return name;
    }

    public double getTotalTimeInSecond() {
        return totalTime / nano;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getTotalIterations() {
        return totalIterations;
    }

    public long getOneIterationTime() {
        return totalIterations == 0 ? 0 : totalTime / totalIterations;
    }

    public double getIterationPerSec() {
        return nano / (totalTime / (double) totalIterations);
    }

    public static String getAllAsString() {
        StringBuilder sb = new StringBuilder();
        List<TimeMeter> list = new ArrayList<TimeMeter>(Arrays.asList(TimeMeter.values()));
        Collections.sort(list, new Comparator<TimeMeter>() {
            @Override
            public int compare(TimeMeter t1, TimeMeter t2) {
                int i = ((Double) t1.getIterationPerSec()).compareTo(t2.getIterationPerSec());
                if (i == 0) i = t1.name().compareTo(t2.name());
                return i;
            }
        });

        for (TimeMeter t : list) {
            if (t.getTotalIterations() != 0) {
                sb.append(t).append("\n");
            }
        }
        return sb.length() == 0 ? "TM is empty" : sb.toString();
    }

    public void print() {
        System.out.println(toString());
    }

    public static void printAll() {
        System.out.println(getAllAsString());
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnable(boolean enable) {
        TimeMeter.enabled = enable;
    }

    public static void reset() {
        TimeMeter[] vals = TimeMeter.values();
        for (TimeMeter tm2 : vals) {
            tm2.startTime = -1;
            tm2.totalIterations = 0;
            tm2.totalTime = 0;
        }
    }

}
