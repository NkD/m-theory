/**
 * 
 */
package cz.mtheory.planet;

import com.ardor3d.image.util.GeneratedImageFactory;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

/**
 * @author Michal NkD Nikodim
 */
public class DEBUG {

    public static boolean updatePlanetCamera = true;
    
    public static boolean horizontCulling = true;
    
    public static boolean showDrawedChunksCount = false;

    public static boolean solveNeighbors = true;
    
    public static int foo =6;
    
    public static final ReadOnlyColorRGBA[] COLORS = prepareColorTable();
    
    private static ReadOnlyColorRGBA[] prepareColorTable() {
        ReadOnlyColorRGBA[] terrainColors = new ReadOnlyColorRGBA[256];
        terrainColors[0] = new ColorRGBA(0.0f, 0.0f, 0.7f, 1);
        terrainColors[9] = new ColorRGBA(0.0f, 0.0f, 1.0f, 1);
        terrainColors[17] = new ColorRGBA(0.0f, 0.5f, 1.0f, 1);
        terrainColors[37] = new ColorRGBA(240 / 255f, 240 / 255f, 64 / 255f, 1);
        terrainColors[133] = new ColorRGBA(32 / 255f, 160 / 255f, 0, 1);
        terrainColors[185] = new ColorRGBA(178 / 255f, 154 / 255f, 134 / 255f, 1);
        terrainColors[193] = new ColorRGBA(128 / 255f, 128 / 255f, 128 / 255f, 1);
        terrainColors[225] = ColorRGBA.WHITE;
        GeneratedImageFactory.fillInColorTable(terrainColors);
        return terrainColors;
    }
}
