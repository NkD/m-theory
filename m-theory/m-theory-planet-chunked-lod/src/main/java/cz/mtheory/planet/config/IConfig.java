/**
 * 
 */
package cz.mtheory.planet.config;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.IntBufferData;

/** 
 * M-theory project
 *
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public interface IConfig {

    public void terrainPoint(ReadOnlyVector3 unitSphereVector, Vector3 store);
    
    /**  
     * @return !Must return only positive values!
     */
    public double computeTerrainHeight(double unitX, double unitY, double unitZ);
    
    public double getPlanetRadius();
    
    public ChunkConstants getConstants();
    
    /** @param index 0 - full, 1 - left, 2 - left top, 3 - top, 4 - top right, 5 - right, 6 - right bottom, 7 - bottom, 8 - bottom left */
    public IntBufferData getChunkIndexBufferData(int index);
}
