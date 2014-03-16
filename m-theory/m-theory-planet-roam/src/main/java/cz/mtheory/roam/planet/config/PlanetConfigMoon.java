/**
 * 
 */
package cz.mtheory.roam.planet.config;

import com.ardor3d.image.util.GeneratedImageFactory;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.functions.FbmFunction3D;
import com.ardor3d.math.functions.Function3D;
import com.ardor3d.math.functions.Functions;
import com.ardor3d.math.type.ReadOnlyColorRGBA;


/**
 * @author Michal NkD Nikodim
 */
public class PlanetConfigMoon extends PlanetConfig {

    private static final Function3D noiseFunction = Functions.simplexNoise();
    private static final Function3D fbmFunction = new FbmFunction3D(noiseFunction, 5, 0.9, 0.1, 3.14);
   
    public PlanetConfigMoon() {
        super("Planet_moon", 1737, 100000);
    }

    @Override
    public double computeTerrainDelta(double nx, double ny, double nz) {
        double f = fbmFunction.eval(nx * 50, ny * 100, nz * 100) * 5;
        if (f > 0) f=f*0.1;
        return f;
    }

    @Override
    protected int computeColorIndex(double terrainDelta) {
        return (int) (((terrainDelta +5) * 255.0) / 7.0);
    }

    @Override
    protected ReadOnlyColorRGBA[] prepareColorTable() {
        ReadOnlyColorRGBA[] terrainColors = new ReadOnlyColorRGBA[256];
        terrainColors[0] = ColorRGBA.DARK_GRAY;
        terrainColors[120] = ColorRGBA.GRAY;
        terrainColors[180] = ColorRGBA.DARK_GRAY;
        terrainColors[185] = ColorRGBA.GRAY;
        GeneratedImageFactory.fillInColorTable(terrainColors);
        return terrainColors;
    }

    @Override
    protected double getTerrainErrorMetricAngle() {
        return 0.15;
    }

}
