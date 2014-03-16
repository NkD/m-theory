/**
 * 
 */
package cz.mtheory.planet.config;

import com.ardor3d.math.functions.FbmFunction3D;
import com.ardor3d.math.functions.Function3D;
import com.ardor3d.math.functions.Functions;
import com.ardor3d.math.functions.RidgeFunction3D;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class TestPlanetConfig extends AConfig {

    private static final Function3D noiseFunction = Functions.simplexNoise();
    private static final Function3D fbmFunction = new FbmFunction3D(noiseFunction, 2, 0.01, 0.1, 30.14);
    private static final Function3D ridgeFunction = new RidgeFunction3D(noiseFunction, 3, 0.01, 1.8);
    private static final Function3D fbmFunction2 = new FbmFunction3D(noiseFunction, 5, 0.7, 0.4, 3.14);
    private static final Function3D ridgeFunction2 = new RidgeFunction3D(fbmFunction2, 3, 0.7, 1.5);

    public TestPlanetConfig(int vertexesOnEdge, int splitThreshold, int splitMaxLevel, double planetRadius) {
        super(vertexesOnEdge, splitThreshold, splitMaxLevel, planetRadius);
    }

    @Override
    public double computeTerrainHeight(double unitX, double unitY, double unitZ) {
        double terrainDelta1 = fbmFunction.eval(unitX * 100, unitY * 100, unitZ * 100) * 40;
        double terrainDelta2 = ridgeFunction.eval(unitX * 100, unitY * 100, unitZ * 100) * 40;
        double terrainDelta3 = ridgeFunction2.eval(unitX * 100, unitY * 100, unitZ * 100) * 2 -10;
        double terrainDelta = terrainDelta1 + terrainDelta2 + terrainDelta3;
        if (terrainDelta < -5) terrainDelta = -5;
        return terrainDelta + 5.1;
    }

}
