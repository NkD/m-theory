/**
 * 
 */
package cz.mtheory.roam.planet.config;

import com.ardor3d.image.util.GeneratedImageFactory;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.functions.FbmFunction3D;
import com.ardor3d.math.functions.Function3D;
import com.ardor3d.math.functions.Functions;
import com.ardor3d.math.functions.RidgeFunction3D;
import com.ardor3d.math.type.ReadOnlyColorRGBA;



/**
 * @author Michal NkD Nikodim
 */
public class PlanetConfigEarth extends PlanetConfig{

    private static final Function3D noiseFunction = Functions.simplexNoise();
    private static final Function3D fbmFunction = new FbmFunction3D(noiseFunction, 5, 0.7, 0.35, 3.14);
    private static final Function3D ridgeFunction = new RidgeFunction3D(fbmFunction, 8, 0.3, 1.5);

    public PlanetConfigEarth() {
        super("Planet_earth", 6378, 100000);
    }
   
    @Override
    public double computeTerrainDelta(double nx, double ny, double nz) {
        double terrainDelta = ridgeFunction.eval(nx * 150, ny * 150, nz * 150) * 4;
        terrainDelta = terrainDelta + (noiseFunction.eval(nx * 3, ny, nz * 3) * 40);
        if (terrainDelta < -5) terrainDelta = -5;
        return terrainDelta;
    }

    @Override
    protected int computeColorIndex(double terrainDelta) {
        return (int) ((terrainDelta / 50) * 255.0);
    }


    @Override
    protected ReadOnlyColorRGBA[] prepareColorTable() {
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

    @Override
    protected double getTerrainErrorMetricAngle() {
        return 0.2;
    }

}
