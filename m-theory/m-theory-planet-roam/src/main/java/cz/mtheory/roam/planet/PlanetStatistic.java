package cz.mtheory.roam.planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michal NkD Nikodim
 */
public class PlanetStatistic {

    private LevelStatistic root = new LevelStatistic();

    private Map<Integer, LevelStatistic> levels = new HashMap<Integer, LevelStatistic>();
    private List<Integer> list = new ArrayList<Integer>();
    private int levelCount = 0; 

    public PlanetStatistic() {
        //nothing
    }

    public LevelStatistic getRoot() {
        return root;
    }

    public LevelStatistic getLevelsStatistic(int level) {
        if (level == -1) return root;
        LevelStatistic ls = levels.get(level);
        if (ls == null) {
            ls = new LevelStatistic();
            ls.level = level;
            levels.put(level, ls);
        }
        if (levelCount < level) levelCount = level;
        return ls;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PlanetStatistic : \n");
        list.clear();
        list.addAll(levels.keySet());
        Collections.sort(list);
        for (Integer level : list) {
            LevelStatistic ls = levels.get(level);
            if (ls.active) {
                sb.append(ls).append("\n");
            }
        }
        sb.append(root).append("\n");
        return sb.toString();
    }

    public void reset() {
        root.reset();
        for (LevelStatistic ls : levels.values()) {
            ls.reset();
        }
        list.clear();
        levelCount = 0;
    }

    public class LevelStatistic {
        private int level = -1;
        private boolean active = false;
        private int planetTriangles = 0;
        private int inFrustrum = 0;
        private int correctNormal = 0;
        private int lastPlanetTriangles = 0;
        private int drawPlanetTriangles = 0;
        private StringBuilder sb = new StringBuilder();

        public void incPlanetTriangles() {
            planetTriangles++;
            root.planetTriangles++;
            active = true;
        }

        public void incInFrustrum() {
            inFrustrum++;
            root.inFrustrum++;
            active = true;
        }

        public void incCorrectNormal() {
            correctNormal++;
            root.correctNormal++;
            active = true;
        }

        public void incLastPlanetTriangle() {
            lastPlanetTriangles++;
            root.lastPlanetTriangles++;
            active = true;
        }

        public void incDrawPlanetTriangle() {
            drawPlanetTriangles++;
            root.drawPlanetTriangles++;
            active = true;
        }

        public void reset() {
            active = false;
            planetTriangles = 0;
            inFrustrum = 0;
            correctNormal = 0;
            lastPlanetTriangles = 0;
            drawPlanetTriangles = 0;
        }

        @Override
        public String toString() {
            sb.setLength(0);
            if (level != -1) sb.append("LEVEL ").append(level).append(" - ");
            if (level == -1) sb.append("Levels=").append(levelCount).append(",");
            sb.append("PT=").append(planetTriangles);
            sb.append(",lastPT=").append(lastPlanetTriangles);
            sb.append(",drawPT=").append(drawPlanetTriangles);
            sb.append(",inFrust=").append(inFrustrum);
            sb.append(",corrNorm=").append(correctNormal);
            
            return sb.toString();
        }

        public int getLevel() {
            return level;
        }

        public boolean isActive() {
            return active;
        }

        public int getPlanetTriangles() {
            return planetTriangles;
        }

        public int getInFrustrum() {
            return inFrustrum;
        }

        public int getDrawPlanetTriangles() {
            return drawPlanetTriangles;
        }

        public StringBuilder getSb() {
            return sb;
        }
    }

}
