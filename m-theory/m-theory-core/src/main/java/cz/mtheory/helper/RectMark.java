package cz.mtheory.helper;

import java.nio.FloatBuffer;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.NormalsMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Michal NkD Nikodim
 * Free for any use (copy&paste, rewrite, reuse etc.)
 */
public class RectMark extends Line {

    private final Mesh mesh;
    private final Camera camera;
    private final int pointsCount = 12;
    private final FloatBuffer fbPoints = BufferUtils.createFloatBuffer(pointsCount * 3);
    private final FloatBuffer fbColors = BufferUtils.createFloatBuffer(pointsCount * 4);
    private final FloatBuffer fbMesh;
    private final Vector3 tmpVec = new Vector3();
    private long lastTime = System.currentTimeMillis();

    private long updateEvery = 5; // in milliseconds

    public RectMark(String name, Mesh mesh, Camera camera) {
        super(name);
        initRenderStates();
        this.mesh = mesh;
        fbMesh = BufferUtils.createFloatBuffer(mesh.getMeshData().getVertexCount() * 3);
        this.camera = camera;
        ColorRGBA color = ColorRGBA.fetchTempInstance();
        color.set(1, 0.4f, 0, 0.7f);
        setColor(color);
        ColorRGBA.releaseTempInstance(color);
        _meshData.setVertexBuffer(fbPoints);
        _meshData.setColorBuffer(fbColors);
        _meshData.setIndexMode(IndexMode.LineStrip);
        _meshData.setIndexLengths(new int[] { 3, 3, 3, 3 });
        setLineWidth(3);
        //generateIndices();
        updateWorldRenderStates(true);
        updateWorldTransform(true);
        setModelBound(null);
        getSceneHints().setNormalsMode(NormalsMode.Off);
        updateModelBound();
    }

    public final void setColor(ColorRGBA color) {
        fbColors.rewind();
        for (int i = 0; i < pointsCount; i++) {
            fbColors.put(color.getRed()).put(color.getGreen()).put(color.getBlue()).put(color.getAlpha());
        }
    }

    public final void setUpdateEvery(long inMilis) {
        updateEvery = inMilis;
    }

    @Override
    public final void draw(Renderer r) {
        if (mesh != null) {
            if (shouldDraw(mesh)) {
                computeBoundingRect();
                r.setOrtho();
                super.draw(r);
                r.unsetOrtho();
            }
        }
    }

    private boolean shouldDraw(Spatial node) {
        if (Camera.FrustumIntersect.Outside.equals(node.getLastFrustumIntersection())) return false;
        if (node.getParent() != null) return shouldDraw(node.getParent());
        return true;
    }

    private void computeBoundingRect() {
        if ((System.currentTimeMillis() - lastTime) < updateEvery) return;
        lastTime = System.currentTimeMillis();
        mesh.getWorldVectors(fbMesh);
        BufferUtils.populateFromBuffer(tmpVec, fbMesh, 0);
        camera.getScreenCoordinates(tmpVec, tmpVec);
        float minX = tmpVec.getXf(), minY = tmpVec.getYf();
        float maxX = tmpVec.getXf(), maxY = tmpVec.getYf();
        int len = fbMesh.remaining() / 3;
        int step = 1;// len / 100;
        if (step < 1) step = 1;
        for (int i = 1; i < len; i = i + step) {
            BufferUtils.populateFromBuffer(tmpVec, fbMesh, i);
            camera.getScreenCoordinates(tmpVec, tmpVec);
            if (tmpVec.getXf() < minX) minX = tmpVec.getXf();
            if (tmpVec.getXf() > maxX) maxX = tmpVec.getXf();
            if (tmpVec.getYf() < minY) minY = tmpVec.getYf();
            if (tmpVec.getYf() > maxY) maxY = tmpVec.getYf();
        }
        float lengthX = (maxX - minX) / 5.0f;
        float lengthY = (maxY - minY) / 5.0f;
        fbPoints.rewind();
        fbPoints.put(minX + lengthX).put(minY).put(0);
        fbPoints.put(minX).put(minY).put(0);
        fbPoints.put(minX).put(minY + lengthY).put(0);
        fbPoints.put(maxX - lengthX).put(maxY).put(0);
        fbPoints.put(maxX).put(maxY).put(0);
        fbPoints.put(maxX).put(maxY - lengthY).put(0);
        fbPoints.put(maxX - lengthX).put(minY).put(0);
        fbPoints.put(maxX).put(minY).put(0);
        fbPoints.put(maxX).put(minY + lengthY).put(0);
        fbPoints.put(minX + lengthX).put(maxY).put(0);
        fbPoints.put(minX).put(maxY).put(0);
        fbPoints.put(minX).put(maxY - lengthY).put(0);
    }

    private void initRenderStates() {
        BlendState blendState = new BlendState();
        blendState.setBlendEnabled(true);
        blendState.setSourceFunction(SourceFunction.SourceAlpha);
        blendState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
        ZBufferState zBufferState = new ZBufferState();
        zBufferState.setEnabled(true);
        zBufferState.setWritable(false);
        zBufferState.setFunction(ZBufferState.TestFunction.Always);
        this.setRenderState(blendState);
        this.setRenderState(zBufferState);
        this.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        this.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
    }
}
