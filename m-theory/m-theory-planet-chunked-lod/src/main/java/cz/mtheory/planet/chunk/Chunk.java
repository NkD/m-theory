/**
 * 
 */
package cz.mtheory.planet.chunk;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture.ApplyMode;
import com.ardor3d.image.Texture.MagnificationFilter;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

import cz.mtheory.planet.DEBUG;
import cz.mtheory.planet.config.IConfig;
import cz.mtheory.planet.enums.Quadrant;
import cz.mtheory.planet.pool.IPoolable;
import cz.mtheory.planet.pool.Pool;
import cz.mtheory.planet.threads.TextureGenerator;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class Chunk implements IPoolable {

    private static final Pool<Chunk> pool = Pool.create(Chunk.class, 1000);

    public static Pool<Chunk> getPool() {
        return pool;
    }

    private double[] cubeXYZ;
    private double[] sphereXYZ;
    //private double[] normalXYZ;
    private double[] quad = new double[4];
    private FloatBufferData vertexBufferData;
    private FloatBufferData normalBufferData;
    private final FloatBufferData interleavedData = new FloatBufferData(FloatBuffer.allocate(0), 1);
    private ChunkNode chunkNode;
    private final BoundingSphere boundingSphere = new BoundingSphere();
    private final ColorRGBA color = new ColorRGBA(MathUtils.nextRandomFloat() * 0.1f, MathUtils.nextRandomFloat(), MathUtils.nextRandomFloat(), 1.0f);
    private IConfig config;

    private volatile Texture2D texture;
    private volatile ByteBuffer textureBuffer;
    private volatile boolean stopGenerate = false;
    private volatile boolean haveTexture = false;
    private volatile boolean textureUpdated = false;
    private volatile boolean inQueue = false;
    private Lock lock = new ReentrantLock(false);

    private volatile int priority = Integer.MAX_VALUE;
    private volatile boolean visible = false;

    public Chunk() {
        //empty constructor;
    }

    @Override
    public void afterCreateNewInstance(Object initiator) {
        this.config = (IConfig) initiator;
        cubeXYZ = new double[config.getConstants().indexesOnChunk];
        sphereXYZ = new double[config.getConstants().indexesOnChunk];
        //normalXYZ = new double[config.getConstants().indexesOnChunk];
        vertexBufferData = new FloatBufferData(BufferUtils.createFloatBuffer(config.getConstants().indexesOnChunk), 3);
        normalBufferData = new FloatBufferData(BufferUtils.createFloatBuffer(config.getConstants().indexesOnChunk), 3);
        int size = config.getConstants().textureSize;
        texture = new Texture2D();
        Image image = new Image(ImageDataFormat.RGB, PixelDataType.UnsignedByte, size, size, BufferUtils.createByteBuffer(size * size * 3), null);
        texture.setImage(image);
        texture.setTextureKey(TextureKey.getRTTKey(MinificationFilter.NearestNeighborNearestMipMap));
        texture.setApply(ApplyMode.Replace);
        texture.setWrap(WrapMode.EdgeClamp);
        texture.setMinificationFilter(MinificationFilter.NearestNeighborNoMipMaps);
        texture.setMagnificationFilter(MagnificationFilter.NearestNeighbor);
        textureBuffer = image.getData(0);

    }

    public final void setChunkNode(ChunkNode chunkNode) {
        this.chunkNode = chunkNode;
    }

    public ChunkNode getChunkNode() {
        return chunkNode;
    }

    public Chunk initRoot() {
        double step = config.getConstants().cubeEdgeSize / (double) config.getConstants().vertexesOnEdge;
        double offset = config.getConstants().vertexesOnEdge * 0.5 * step;
        Vector3 v = Vector3.fetchTempInstance();
        for (int y = 0; y < config.getConstants().vertexesOnEdgePlusOne; y++) {
            for (int x = 0; x < config.getConstants().vertexesOnEdgePlusOne; x++) {
                v.set(x * step - offset, y * step - offset, 1);
                setCubeVector(x, y, v, true);
            }
        }
        Vector3.releaseTempInstance(v);
        fillBuffers();
        return this;
    }

    protected Chunk initSmaller(Chunk chunk, Quadrant q) {
        Vector3 v1 = Vector3.fetchTempInstance();
        Vector3 v2 = Vector3.fetchTempInstance();
        Vector3 v = Vector3.fetchTempInstance();
        int xx = 0, yy = 0;
        for (int y = q.getOffsetTop(); y < q.getOffsetBottom(); y++) {
            xx = 0;
            for (int x = q.getOffsetLeft(); x < q.getOffsetRight(); x++) {
                chunk.getCubeVector(x, y, v1);
                setCubeVector(xx, yy, v1, false);
                chunk.getSphereVector(x, y, v);
                setSphereVector(xx, yy, v);
                xx++;
                if (x != q.getOffsetRight() - 1) {
                    chunk.getCubeVector(x + 1, y, v2);
                    v1.setX((v1.getX() + v2.getX()) * 0.5);
                    setCubeVector(xx, yy, v1, true);
                    xx++;
                }
            }
            yy++;
            if (y != q.getOffsetBottom() - 1) {
                xx = 0;
                for (int x = q.getOffsetLeft(); x < q.getOffsetRight(); x++) {
                    chunk.getCubeVector(x, y, v1);
                    chunk.getCubeVector(x, y + 1, v2);
                    v1.setY((v1.getY() + v2.getY()) * 0.5);
                    setCubeVector(xx, yy, v1, true);
                    xx++;
                    if (x != q.getOffsetRight() - 1) {
                        chunk.getCubeVector(x + 1, y, v2);
                        v1.setX((v1.getX() + v2.getX()) * 0.5);
                        setCubeVector(xx, yy, v1, true);
                        xx++;
                    }
                }
                yy++;
            }
        }
        Vector3.releaseTempInstance(v);
        Vector3.releaseTempInstance(v2);
        Vector3.releaseTempInstance(v1);

        fillBuffers();
        return this;
    }

    protected Chunk initBigger(Chunk leftTop, Chunk rightTop, Chunk leftBottom, Chunk rightBottom) {
        Vector3 vLeftTop = Vector3.fetchTempInstance();
        Vector3 vRightTop = Vector3.fetchTempInstance();
        Vector3 vLeftBottom = Vector3.fetchTempInstance();
        Vector3 vRightBottom = Vector3.fetchTempInstance();
        int xx = 0, yy = 0;
        int vohe = config.getConstants().vertexesOnHalfEdge;
        for (int y = 0; y < config.getConstants().vertexesOnHalfEdgePlusOne; y++) {
            xx = 0;
            for (int x = 0; x < config.getConstants().vertexesOnHalfEdgePlusOne; x++) {

                leftTop.getCubeVector(xx, yy, vLeftTop);
                rightTop.getCubeVector(xx, yy, vRightTop);
                leftBottom.getCubeVector(xx, yy, vLeftBottom);
                rightBottom.getCubeVector(xx, yy, vRightBottom);
                setCubeVector(x, y, vLeftTop, false);
                setCubeVector(x + vohe, y, vRightTop, false);
                setCubeVector(x, y + vohe, vLeftBottom, false);
                setCubeVector(x + vohe, y + vohe, vRightBottom, false);

                leftTop.getSphereVector(xx, yy, vLeftTop);
                rightTop.getSphereVector(xx, yy, vRightTop);
                leftBottom.getSphereVector(xx, yy, vLeftBottom);
                rightBottom.getSphereVector(xx, yy, vRightBottom);
                setSphereVector(x, y, vLeftTop);
                setSphereVector(x + vohe, y, vRightTop);
                setSphereVector(x, y + vohe, vLeftBottom);
                setSphereVector(x + vohe, y + vohe, vRightBottom);

                xx = xx + 2;
            }
            yy = yy + 2;
        }
        Vector3.releaseTempInstance(vRightBottom);
        Vector3.releaseTempInstance(vLeftBottom);
        Vector3.releaseTempInstance(vRightTop);
        Vector3.releaseTempInstance(vLeftTop);

        fillBuffers();
        return this;
    }

    private void fillBuffers() {
        Vector3 v = Vector3.fetchTempInstance();
        vertexBufferData.getBuffer().rewind();
        for (int y = 0; y < config.getConstants().vertexesOnEdgePlusOne; y++) {
            for (int x = 0; x < config.getConstants().vertexesOnEdgePlusOne; x++) {
                getSphereVector(x, y, v);
                vertexBufferData.getBuffer().put(v.getXf()).put(v.getYf()).put(v.getZf());
            }
        }
        interleavedData.setNeedsRefresh(true);
        Vector3.releaseTempInstance(v);
        boundingSphere.computeFromPoints(vertexBufferData.getBuffer());
        chunkNode.updateBoundingSphere();
        initQuad();
    }

    private void initQuad() {
        Vector3 v = Vector3.fetchTempInstance();
        getCubeVector(0, 0, v);
        quad[0] = v.getX();
        quad[1] = v.getY();
        getCubeVector(config.getConstants().vertexesOnEdge, config.getConstants().vertexesOnEdge, v);
        quad[2] = v.getX();
        quad[3] = v.getY();
        Vector3.releaseTempInstance(v);

        //generateTexture();

    }

    private int getCubeVector(int x, int y, Vector3 v) {
        int index = x * 3 + y * config.getConstants().indexesOnEdge;
        v.set(cubeXYZ[index], cubeXYZ[index + 1], cubeXYZ[index + 2]);
        return index;
    }

    private int setCubeVector(int x, int y, Vector3 v, boolean updateSphere) {
        int index = x * 3 + y * config.getConstants().indexesOnEdge;
        cubeXYZ[index] = v.getX();
        cubeXYZ[index + 1] = v.getY();
        cubeXYZ[index + 2] = v.getZ();

        if (updateSphere) updateSphere(index, v);
        return index;
    }

    private void updateSphere(int index, Vector3 v) {
        Vector3 vs = Vector3.fetchTempInstance();
        vs.set(v);
        chunkNode.getCubeFace().vectorTurn(vs);
        spherify(vs);
        sphereXYZ[index] = vs.getX();
        sphereXYZ[index + 1] = vs.getY();
        sphereXYZ[index + 2] = vs.getZ();
        Vector3.releaseTempInstance(vs);
    }

    private int getSphereVector(int x, int y, Vector3 v) {
        int index = x * 3 + y * config.getConstants().indexesOnEdge;
        v.set(sphereXYZ[index], sphereXYZ[index + 1], sphereXYZ[index + 2]);
        return index;
    }

    private int setSphereVector(int x, int y, Vector3 v) {
        int index = x * 3 + y * config.getConstants().indexesOnEdge;
        sphereXYZ[index] = v.getX();
        sphereXYZ[index + 1] = v.getY();
        sphereXYZ[index + 2] = v.getZ();
        return index;
    }

    private void spherify(Vector3 v) {
        //v.normalize(n);
        //config.terrainPoint(n, v);

        double squareX = v.getX() * v.getX();
        double squareY = v.getY() * v.getY();
        double squareZ = v.getZ() * v.getZ();

        double nx = v.getX() * Math.sqrt(1 - (squareY / 2.0) - (squareZ / 2.0) + ((squareY * squareZ) / 3.0));
        double ny = v.getY() * Math.sqrt(1 - (squareZ / 2.0) - (squareX / 2.0) + ((squareZ * squareX) / 3.0));
        double nz = v.getZ() * Math.sqrt(1 - (squareX / 2.0) - (squareY / 2.0) + ((squareX * squareY) / 3.0));

        double terrain = config.computeTerrainHeight(nx, ny, nz);
        double x = nx * config.getPlanetRadius() + nx * terrain;
        double y = ny * config.getPlanetRadius() + ny * terrain;
        double z = nz * config.getPlanetRadius() + nz * terrain;

        v.set(x, y, z);
    }

    public FloatBufferData getVertexBufferData() {
        return vertexBufferData;
    }
    
    public FloatBufferData getNormalBufferData() {
        return normalBufferData;
    }

    public FloatBufferData getInterleavedData() {
        return interleavedData;
    }

    public ColorRGBA getColor() {
        return color;
    }

    public BoundingSphere getBoundingSphere() {
        return boundingSphere;
    }

    public int getIndex() {
        return chunkNode.getIndexOfIndexBuffer();
    }

    public final Texture2D getTexture() {
        return texture;
    }

    public final ByteBuffer getTextureBuffer() {
        return textureBuffer;
    }

    @Override
    public void afterGetFromPool() {
        //nothing
    }

    @Override
    public void beforeReturnToPool() {
        TextureGenerator.remove(this);
        stopGenerate = true;
        haveTexture = false;
        textureUpdated = false;
        chunkNode = null;
        priority = Integer.MAX_VALUE;
        visible = false;
        inQueue = false;
    }

    public boolean haveTexture() {
        return haveTexture;
    }

    public boolean isTextureUpdated() {
        return textureUpdated;
    }

    public void setTextureUpdated(boolean textureUpdated) {
        this.textureUpdated = textureUpdated;
    }

    public void generateTexture() {
        try {
            stopGenerate = false;
            while (!getLock().tryLock()) {
                //
            }
            int size = config.getConstants().textureSize;
            double dd = Math.abs(quad[2] - quad[0]) / (size);
            Vector3 v = Vector3.fetchTempInstance();
            textureBuffer.rewind();
            for (double y = quad[1]; y < quad[3]; y += dd) {
                for (double x = quad[0]; x < quad[2]; x += dd) {
                    if (chunkNode == null) return;
                    chunkNode.getCubeFace().vectorTurn(v.set(x, y, 1));
                    double squareX = v.getX() * v.getX();
                    double squareY = v.getY() * v.getY();
                    double squareZ = v.getZ() * v.getZ();
                    double nx = v.getX() * Math.sqrt(1 - (squareY / 2.0) - (squareZ / 2.0) + ((squareY * squareZ) / 3.0));
                    double ny = v.getY() * Math.sqrt(1 - (squareZ / 2.0) - (squareX / 2.0) + ((squareZ * squareX) / 3.0));
                    double nz = v.getZ() * Math.sqrt(1 - (squareX / 2.0) - (squareY / 2.0) + ((squareX * squareY) / 3.0));
                    double terrain = config.computeTerrainHeight(nx, ny, nz);
                    int index = (int) ((terrain / 50) * 255.0);
                    if (index > 255) index = 255;
                    ReadOnlyColorRGBA c = DEBUG.COLORS[index];
                    byte r = (byte) (c.getRed() * 255);
                    byte g = (byte) (c.getGreen() * 255);
                    byte b = (byte) (c.getBlue() * 255);
                    textureBuffer.put(r).put(g).put(b);
                    if (stopGenerate) {
                        stopGenerate = false;
                        return;
                    }
                }
            }
            Vector3.releaseTempInstance(v);
            textureBuffer.rewind();
            inQueue = false;
            haveTexture = true;
        } finally {
            getLock().unlock();
        }
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getPriority() {
        return priority;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public final boolean isInQueue() {
        return inQueue;
    }

    public final void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public final Lock getLock() {
        return lock;
    }

}
