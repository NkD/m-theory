/**
 * 
 */
package cz.mtheory.roam.planet.config;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * @author Michal NkD Nikodim
 */
public abstract class PlanetConfig implements IPlanetConfig {

    private String name;
    private int radius;
    private int vertexCount;
    private ReadOnlyColorRGBA[] terrainColors;
    
    private final double angleForTerrainErrorMetric;
    
    @SuppressWarnings("unused")
    private double minTerrainDelta = Integer.MAX_VALUE;
    @SuppressWarnings("unused")
    private double maxTerrainDelta = Integer.MIN_VALUE;

    public PlanetConfig(String name, int radius, int vertexCount) {
        this.name = name;
        this.radius = radius;
        this.vertexCount = vertexCount;
        terrainColors = prepareColorTable();
        angleForTerrainErrorMetric = prepareTerrainErrorMetricAngle();
    }

    private double prepareTerrainErrorMetricAngle() {
        return Math.PI * (getTerrainErrorMetricAngle() / 180);
    }

    public abstract double computeTerrainDelta(double nx, double ny, double nz);

    protected abstract int computeColorIndex(double terrainDelta);

    protected abstract ReadOnlyColorRGBA[] prepareColorTable();
    
    protected abstract double getTerrainErrorMetricAngle();

    @Override
    public final double generateTerrainPoint(ReadOnlyVector3 point, Vector3 result) {
        Vector3 n = Vector3.fetchTempInstance();
        n.set(point).normalizeLocal();
        double terrainDelta = computeTerrainDelta(n.getX(), n.getY(), n.getZ());
        double x = n.getX() * getRadius() + (n.getX() * terrainDelta);
        double y = n.getY() * getRadius() + (n.getY() * terrainDelta);
        double z = n.getZ() * getRadius() + (n.getZ() * terrainDelta);
        result.set(x, y, z);
        
       /* if (minTerrainDelta > terrainDelta){
            minTerrainDelta = terrainDelta;
            System.out.println(getPlanetName() + ": terrainDelta min = " + minTerrainDelta + ", max = " + maxTerrainDelta);
        }
        if (maxTerrainDelta < terrainDelta) {
            maxTerrainDelta = terrainDelta;
            System.out.println(getPlanetName() + ": terrainDelta min = " + minTerrainDelta + ", max = " + maxTerrainDelta);
        }*/
        return terrainDelta;
    }

    @Override
    public final ReadOnlyColorRGBA getColor(double terrainDelta) {
        int index = computeColorIndex(terrainDelta);
        if (index < 0) index = 0;
        if (index > 255) index = 255;
        return terrainColors[index];
    }

    @Override
    public final String getPlanetName() {
        return name;
    }

    @Override
    public final int getRadius() {
        return radius;
    }

    @Override
    public final int getVertexCount() {
        return vertexCount;
    }

    @Override
    public double getAngleForTerrainErrorMetric() {
        return angleForTerrainErrorMetric;
    }
}
