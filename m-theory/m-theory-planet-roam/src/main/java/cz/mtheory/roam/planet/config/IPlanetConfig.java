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
public interface IPlanetConfig {

    public String getPlanetName();
    
    public double generateTerrainPoint(ReadOnlyVector3 point, Vector3 result);
    
    public ReadOnlyColorRGBA getColor(double terrainDelta);

    public int getRadius();
    
    public int getVertexCount();
    
    public double getAngleForTerrainErrorMetric();
}
