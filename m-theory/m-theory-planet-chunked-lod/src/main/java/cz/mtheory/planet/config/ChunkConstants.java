/**
 * 
 */
package cz.mtheory.planet.config;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import com.ardor3d.math.MathUtils;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.util.geom.BufferUtils;

/** 
 * M-theory project
 *
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class ChunkConstants {

    public final int vertexesOnEdge;
    public final int vertexesOnEdgePlusOne;
    public final int vertexesOnHalfEdge;
    public final int vertexesOnHalfEdgePlusOne;
    public final int vertexesOnChunk;

    public final int indexesOnChunk;
    public final int indexesOnEdge;
    public final int indexesOnHalfEdge;

    public final int cubeEdgeSize;

    public final List<FloatBufferData> textureCoords = new ArrayList<FloatBufferData>(1);

    public final double splitThreshold;

    public final int splitMaxLevel;

    public final double[] splitByLevelValues;
    public final double[] mergeByLevelValues;
    
    public final int textureSize = 64;

    public ChunkConstants(int vertexesOnEdge, double splitThreshold, int maxSplitLevel, double radius) {
        this.vertexesOnEdge = vertexesOnEdge;
        this.splitThreshold = splitThreshold;
        this.splitMaxLevel = maxSplitLevel;

        this.vertexesOnEdgePlusOne = vertexesOnEdge + 1;
        this.vertexesOnHalfEdge = vertexesOnEdge / 2;
        this.vertexesOnHalfEdgePlusOne = this.vertexesOnHalfEdge + 1;
        this.vertexesOnChunk = this.vertexesOnEdgePlusOne * this.vertexesOnEdgePlusOne;
        this.indexesOnChunk = (int) Math.pow(vertexesOnEdgePlusOne, 2) * 3;
        this.indexesOnEdge = vertexesOnEdgePlusOne * 3;
        this.indexesOnHalfEdge = vertexesOnHalfEdge * 3;
        this.cubeEdgeSize = 2;

        FloatBuffer fb = BufferUtils.createFloatBuffer(vertexesOnEdgePlusOne * vertexesOnEdgePlusOne * 2);
        double ts = 1.0 / vertexesOnEdge;
        for (int y = 0; y < vertexesOnEdgePlusOne; y++) {
            for (int x = 0; x < vertexesOnEdgePlusOne; x++) {
                fb.put((float) (x * ts)).put((float) (y * ts));
            }
        }
        FloatBufferData fbd = new FloatBufferData(fb, 2);
        textureCoords.add(fbd);

        this.splitByLevelValues = new double[maxSplitLevel + 1];
        this.mergeByLevelValues = new double[maxSplitLevel + 1];
        double cubeInSphereEdgeSize = (radius * 2) / Math.sqrt(3);
        for (int level = 0; level <= splitMaxLevel; level++) {
            double edgeSize = cubeInSphereEdgeSize / Math.pow(2, level);
            splitByLevelValues[level] = edgeSize / Math.tan(splitThreshold * MathUtils.DEG_TO_RAD);
            splitByLevelValues[level] = splitByLevelValues[level] * splitByLevelValues[level];

            mergeByLevelValues[level] = edgeSize / Math.tan((splitThreshold - 0.1) * MathUtils.DEG_TO_RAD);
            mergeByLevelValues[level] = mergeByLevelValues[level] * mergeByLevelValues[level];

        }
    }

}
