package cz.mtheory.roam.planet.buffer;

import java.nio.IntBuffer;

import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Michal NkD Nikodim
 */
public class IndexBuffer {

    private int maxIndex;
    private int[] indexBufferSize;
    private int[] indexArray;
    private IntBuffer indexBuffer;
    private int actualSize = 0;

    public IndexBuffer(int vertexCount) {
        this.maxIndex = ((vertexCount - 8) * 6) + 36;
        this.indexArray = new int[this.maxIndex];
        indexBuffer = BufferUtils.createIntBuffer(this.maxIndex);
        indexBufferSize = new int[1];
    }

    public void addIndexes(int index0, int index1, int index2) {
        if (actualSize >= maxIndex) throw new RuntimeException("Can not add index");
        indexArray[actualSize++] = index0;
        indexArray[actualSize++] = index1;
        indexArray[actualSize++] = index2;
    }

    public void update() {
        if (actualSize != 0) {
            indexBuffer.limit(actualSize);
            indexBuffer.rewind();
            indexBuffer.put(indexArray, 0, actualSize);
            indexBufferSize[0] = actualSize;
            actualSize = 0;
        }
    }

    public int[] getIndexBufferSize() {
        return indexBufferSize;
    }

    public IntBuffer getIndexBuffer() {
        return indexBuffer;
    }

    public int[] getIndexArray() {
        return indexArray;
    }

}
