/**
 * 
 */
package cz.mtheory.planet.config;

import com.ardor3d.math.MathUtils;
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
public abstract class AConfig implements IConfig {

    private final double planetRadius;
    private final ChunkConstants chunkConstants;
    private final IntBufferData[] chunkIndexBufferData;
    
    public AConfig(int vertexesOnEdge, int splitThreshold, int splitMaxLevel, double planetRadius) {
        this.planetRadius = planetRadius;
        if (vertexesOnEdge < 4) throw new RuntimeException("VertextesOnEdge must be equal or greater then 4");
        if (!MathUtils.isPowerOfTwo(vertexesOnEdge)) throw new RuntimeException("VertexesOnEdge must be power of two");
        chunkConstants = new ChunkConstants(vertexesOnEdge, splitThreshold, splitMaxLevel, planetRadius);
        chunkIndexBufferData = ChunkBuffers.createIndexBuffers(chunkConstants.vertexesOnEdge);
    }

    @Override
    public final void terrainPoint(ReadOnlyVector3 unitSphereVector, Vector3 store) {
        double uX = unitSphereVector.getX();
        double uY = unitSphereVector.getY();
        double uZ = unitSphereVector.getZ();
        double terrainHeight = computeTerrainHeight(uX, uY, uZ);
        double r = getPlanetRadius();
        store.set(uX * r + uX * terrainHeight, uY * r + uY * terrainHeight, uZ * r + uZ * terrainHeight);
    }

    @Override
    public final double getPlanetRadius() {
        return planetRadius;
    }

    @Override
    public final ChunkConstants getConstants() {
        return chunkConstants;
    }

    @Override
    public final IntBufferData getChunkIndexBufferData(int index) {
        return chunkIndexBufferData[index];
    }
    
    @Override
    public abstract double computeTerrainHeight(double unitX, double unitY, double unitZ);

}
