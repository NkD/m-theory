package cz.mtheory.helper;

import java.nio.FloatBuffer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Michal NkD Nikodim
 *
 */
public class PathTracker extends Line {

    private Spatial spatial;
    private Segment[] segment;
    private int segmentsCount;
    private int actualTopSegmentIndex = 0;
    private long segmentTiming = 300;
    private ColorRGBA beginColor = new ColorRGBA(0, 1, 0, 1);
    private ColorRGBA endColor = new ColorRGBA(1, 0, 0, 0);
    private float colorDelta;
    private Vector3 p1 = new Vector3(5, 0, 0);
    private Vector3 p2 = new Vector3(-5, 0, 0);
    private FloatBuffer fbPoints;
    private FloatBuffer fbColors;
    private boolean useZBuffer = false;
    private ZBufferState zBufferState;

    public PathTracker(String name, Spatial spatial, int segmentsCount) {
        this(name, spatial, segmentsCount, false);
    }

    public PathTracker(String name, Spatial spatial, int segmentsCount, boolean useZBuffer) {
        super(name);

        this.useZBuffer = useZBuffer;
        this.segmentsCount = segmentsCount;

        initRenderStates();
        attach(spatial);

        colorDelta = (1.0f / (segmentsCount * segmentTiming));
        actualTopSegmentIndex = 0;
        segment = new Segment[segmentsCount];
        for (int i = 0; i < segmentsCount; i++) {
            segment[i] = new Segment();
        }
        fbPoints = BufferUtils.createFloatBuffer(3 * segmentsCount * 6);
        fbColors = BufferUtils.createFloatBuffer(4 * segmentsCount * 6);
        fillFloatBufferPoints(fbPoints);
        fillFloatBufferColors(fbColors);
        _meshData.setVertexBuffer(fbPoints);
        _meshData.setColorBuffer(fbColors);
        _meshData.setIndexMode(IndexMode.Lines);
        //generateIndices();
        updateWorldRenderStates(true);
        updateWorldTransform(true);
        setModelBound(new BoundingBox());
        updateModelBound();
    }

    public final void setEmitorPoints(Vector3 p1, Vector3 p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public final void setColors(ColorRGBA beginColor, ColorRGBA endColor) {
        this.beginColor = beginColor;
        this.endColor = endColor;
    }

    public final void setTiming(long segmentTiming) {
        this.segmentTiming = segmentTiming;
        colorDelta = (1.0f / (segmentsCount * segmentTiming));
    }

    public final void setZbuffer(boolean useZBuffer) {
        this.useZBuffer = useZBuffer;
        zBufferState.setFunction(useZBuffer ? ZBufferState.TestFunction.LessThanOrEqualTo : ZBufferState.TestFunction.Always);
    }

    public final void attach(Spatial spatial) {
        if (this.spatial != null) detach();
        this.spatial = spatial;
        if (spatial != null) {
            Node node = spatial.getParent();
            if (node == null) throw new RuntimeException("Spatial is root of scene");
            node.attachChild(this);
            
            actualTopSegmentIndex = 0;
            segment = new Segment[segmentsCount];
            for (int i = 0; i < segmentsCount; i++) {
                segment[i] = new Segment();
            }
        }
    }
    
    @SuppressWarnings("unused")
    private Node findRoot(Node node){
        if (node.getParent() == null) return node;
        return findRoot(node.getParent());
    }

    public final void detach() {
        spatial.getParent().detachChild(this);
        spatial = null;
    }

    @Override
    public void draw(final Renderer r) {
        if (spatial != null) {
            update();
            super.draw(r);
        }
    }

    private void update() {
        if (segment[actualTopSegmentIndex].lifeTime() > segmentTiming || !segment[actualTopSegmentIndex].initialized) {
            int prevPointer = actualTopSegmentIndex;
            actualTopSegmentIndex = (actualTopSegmentIndex + 1) % segmentsCount;
            if (segment[prevPointer].initialized) {
                segment[actualTopSegmentIndex].init(segment[prevPointer].points[0], segment[prevPointer].points[1]);
            } else {
                segment[actualTopSegmentIndex].init(null, null);
            }
        }
        segment[actualTopSegmentIndex].updatePoints();
        for (int i = 0; i < segment.length; i++) {
            segment[i].updateColors();
        }
        fillFloatBufferPoints(fbPoints);
        fillFloatBufferColors(fbColors);
        updateModelBound();
    }

    private void fillFloatBufferPoints(FloatBuffer fb) {
        fb.rewind();
        for (int i = 0; i < segmentsCount; i++) {
            for (int j = 0; j < 6; j++) {
                fb.put(segment[i].points[j].getXf()).put(segment[i].points[j].getYf()).put(segment[i].points[j].getZf());
            }
        }
        fb.flip();
    }

    private void fillFloatBufferColors(FloatBuffer fb) {
        fb.rewind();
        for (int i = 0; i < segmentsCount; i++) {
            for (int j = 0; j < 6; j++) {
                fb.put(segment[i].colors[j].getRed()).put(segment[i].colors[j].getGreen()).put(segment[i].colors[j].getBlue()).put(segment[i].colors[j].getAlpha());
            }
        }
        fb.flip();
    }

    private void initRenderStates() {
        BlendState blendState = new BlendState();
        blendState.setBlendEnabled(true);
        blendState.setSourceFunction(SourceFunction.SourceAlpha);
        blendState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
        zBufferState = new ZBufferState();
        zBufferState.setEnabled(true);
        zBufferState.setWritable(false);
        setZbuffer(this.useZBuffer);
        this.setRenderState(blendState);
        this.setRenderState(zBufferState);
        this.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        this.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
    }

    private class Segment {

        private Vector3[] points = new Vector3[6];
        private ColorRGBA[] colors = new ColorRGBA[6];
        private long segmentStartTime = 0;
        private float localColorDelta;
        private boolean initialized = false;
        private ColorRGBA localBeginColor;
        private ColorRGBA localEndColor;

        private Segment() {
            for (int i = 0; i < 6; i++) {
                points[i] = new Vector3();
                colors[i] = new ColorRGBA();
            }
        }

        private void init(Vector3 v0Prev, Vector3 v1Prev) {
            updatePoints();
            points[3].set(v0Prev == null ? points[2] : v0Prev);
            points[5].set(v1Prev == null ? points[4] : v1Prev);
            localBeginColor = beginColor;
            localEndColor = endColor;
            for (int i = 0; i < 6; i++) {
                colors[i].set(localBeginColor);
            }
            segmentStartTime = System.currentTimeMillis();
            localColorDelta = colorDelta;
            initialized = true;
        }

        private void updatePoints() {
            spatial.getRotation().applyPost(p1, points[0]);
            spatial.getTranslation().add(points[0], points[0]);
            points[2] = points[0];
            spatial.getRotation().applyPost(p2, points[1]);
            spatial.getTranslation().add(points[1], points[1]);
            points[4] = points[1];

        }

        private void updateColors() {
            if (!initialized) return;
            float scalar = lifeTime() * localColorDelta;
            if (scalar > 1) scalar = 1;
            for (int i = 0; i < 6; i++) {
                ColorRGBA.lerp(localBeginColor, localEndColor, scalar, colors[i]);
            }
        }

        private long lifeTime() {
            return System.currentTimeMillis() - segmentStartTime;
        }
    }

}
