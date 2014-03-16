package cz.mtheory.roam.planet.buffer;

import java.nio.FloatBuffer;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Michal NkD Nikodim
 */
public class VertexBuffer {

    private int vertexCount;
    private boolean[] active;

    private float[] vertex;
    private float[] normal;
    private float[] color;
    private FloatBuffer floatBufferVertex;
    private FloatBuffer floatBufferNormal;
    private FloatBuffer floatBufferColor;

    private int freeVertex;
    private int minChangedFloatIndex = Integer.MAX_VALUE;
    private int maxChangedFloatIndex = Integer.MIN_VALUE;
    private boolean vertexSetMark = false;

    public VertexBuffer(int vertexCount) {
        this.vertexCount = vertexCount;
        this.vertex = new float[this.vertexCount * 3];
        this.normal = new float[this.vertexCount * 3];
        this.color = new float[this.vertexCount * 4];
        this.floatBufferVertex = BufferUtils.createFloatBuffer(this.vertexCount * 3);
        this.floatBufferNormal = BufferUtils.createFloatBuffer(this.vertexCount * 3);
        this.floatBufferColor = BufferUtils.createFloatBuffer(this.vertexCount * 4);
        this.active = new boolean[vertexCount];
        this.freeVertex = 0;
    }

    public int setVertex(Vector3 vertex) {
        return setVertex(vertex.getXf(), vertex.getYf(), vertex.getZf());
    }

    public void setNormal(int index, Vector3 normal) {
        setNormal(index, normal.getXf(), normal.getYf(), normal.getZf());
    }
    
    public void setNormalSpecial(int index, Vector3 normal) {
        setNormal(index, normal.getXf(), normal.getYf(), normal.getZf());
        int i = index * 3;
        if (i < minChangedFloatIndex) minChangedFloatIndex = i;
        if (i > maxChangedFloatIndex) maxChangedFloatIndex = i;
        vertexSetMark = true;
    }

    public void setColor(int index, ReadOnlyColorRGBA color) {
        setColor(index, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public void setNormal(int index, float nx, float ny, float nz) {
        int i = index * 3;
        normal[i++] = nx;
        normal[i++] = ny;
        normal[i] = nz;
    }

    public void setColor(int index, float r, float g, float b, float a) {
        int i = index * 4;
        color[i++] = r;
        color[i++] = g;
        color[i++] = b;
        color[i] = a;
    }

    public int setVertex(float x, float y, float z) {
        if (freeVertex == Integer.MAX_VALUE) throw new RuntimeException("VertexBuffer is full");
        active[freeVertex] = true;
        int resultIndex = freeVertex;
        int i = freeVertex * 3;
        if (i < minChangedFloatIndex) minChangedFloatIndex = i;
        if (i > maxChangedFloatIndex) maxChangedFloatIndex = i;
        vertex[i++] = x;
        vertex[i++] = y;
        vertex[i] = z;
        freeVertex = Integer.MAX_VALUE;
        for (int j = resultIndex + 1; j < vertexCount; j++) {
            if (!active[j]) {
                freeVertex = j;
                break;
            }
        }
        vertexSetMark = true;
        return resultIndex;
    }

    public void getVertex(int index, Vector3 v) {
        if (!active[index]) throw new RuntimeException("Vertex on index " + index + " is not active");
        int i = index * 3;
        v.set(vertex[i++], vertex[i++], vertex[i]);
    }

    public void getNormal(int index, Vector3 n) {
        if (!active[index]) throw new RuntimeException("Vertex on index " + index + " is not active");
        int i = index * 3;
        n.set(normal[i++], normal[i++], normal[i]);
    }

    public void releaseVertex(int index) {
        active[index] = false;
        if (index < freeVertex) freeVertex = index;
    }

    public void update() {
        if (vertexSetMark) {
            int length = (maxChangedFloatIndex + 3) - minChangedFloatIndex;
            floatBufferVertex.position(minChangedFloatIndex);
            floatBufferVertex.put(vertex, minChangedFloatIndex, length);
            floatBufferNormal.position(minChangedFloatIndex);
            floatBufferNormal.put(normal, minChangedFloatIndex, length);

            int cStart = (minChangedFloatIndex / 3) * 4;
            int cLength = (length / 3) * 4;
            floatBufferColor.position(cStart);
            floatBufferColor.put(color, cStart, cLength);

            minChangedFloatIndex = Integer.MAX_VALUE;
            maxChangedFloatIndex = Integer.MIN_VALUE;
            vertexSetMark = false;
        }
    }

    public FloatBuffer getFloatBufferVertex() {
        return floatBufferVertex;
    }

    public FloatBuffer getFloatBufferNormal() {
        return floatBufferNormal;
    }

    public FloatBuffer getFloatBufferColor() {
        return floatBufferColor;
    }

    /**
     * @return
     */
    public int getStatisticVertexCount() {
        int result = 0;
        for (Boolean b : active) {
            if (b) result++;
        }
        return result;
    }

}
