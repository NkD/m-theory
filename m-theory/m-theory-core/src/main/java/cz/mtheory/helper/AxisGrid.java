package cz.mtheory.helper;

import java.util.ArrayList;

import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.hint.DataMode;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.NormalsMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * AxisGrid is helper for showing axis of node in space AxisGrid is represented
 * by Line (AxisGrid extends Line)
 * 
 * @author Michal NkD Nikodim Free for any use (copy&paste, rewrite, reuse etc.)
 */
public class AxisGrid extends Line {

    private static final ColorRGBA X_AXIS_RED = new ColorRGBA(1, 0, 0, 0.3f);
    private static final ColorRGBA Y_AXIS_GREEN = new ColorRGBA(0, 1, 0, 0.3f);
    private static final ColorRGBA Z_AXIS_BLUE = new ColorRGBA(0, 0, 1, 0.3f);

    private static final ColorRGBA NORMAL = new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f);
    private static final ColorRGBA MARKER = new ColorRGBA(1, 1, 1, 0.1f);

    private Spatial spatial;
    private SpatialController<Spatial> sc = new SpatialController<Spatial>() {
        @Override
        public void update(double time, Spatial caller) {
            setWorldTransform(caller.getWorldTransform());
        }
    };
    /**
     * AxisGrid is helper for showing axis of node in space AxisGrid is
     * represented by Line (AxisGrid extends Line)
     * 
     * @param name
     *            - name for identification
     * @param size
     *            - AxisGrid is square and parameter size is side size of square
     * @param spacing
     *            - space between lines
     * @param marker
     *            - count of lines between highlight one
     * @param zbuffer
     *            - use zbuffer (true) or AxisGrid is always visible (false)
     */
    public AxisGrid(Spatial spatial, float size, float spacing, int marker, boolean zbuffer) {
        super(spatial == null ? "AxisGrid" : "AxisGrid_" + spatial.getName());
        initRenderStates(zbuffer);
        if (spatial != null) spatial.addController(sc);

        ArrayList<Vector3> points = new ArrayList<Vector3>();
        ArrayList<ColorRGBA> colors = new ArrayList<ColorRGBA>();
        float halfSize = size * 0.5f;
        float pos = 0;
        int spc = 0;
        points.add(new Vector3(-halfSize, 0, pos));
        points.add(new Vector3(0, 0, pos));
        points.add(new Vector3(pos, 0, -halfSize));
        points.add(new Vector3(pos, 0, 0));
        for (int i = 0; i < 4; i++)
            colors.add(MARKER);
        do {
            pos = pos + spacing;
            if (pos > halfSize) break;
            points.add(new Vector3(-halfSize, 0, pos));
            points.add(new Vector3(halfSize, 0, pos));
            points.add(new Vector3(pos, 0, -halfSize));
            points.add(new Vector3(pos, 0, halfSize));
            points.add(new Vector3(-halfSize, 0, -pos));
            points.add(new Vector3(halfSize, 0, -pos));
            points.add(new Vector3(-pos, 0, -halfSize));
            points.add(new Vector3(-pos, 0, halfSize));
            spc = (spc + 1) % marker;
            if (spc == 0) {
                for (int i = 0; i < 8; i++)
                    colors.add(MARKER);
            } else {
                for (int i = 0; i < 8; i++)
                    colors.add(NORMAL);
            }
        } while (true);
        float axisSize = halfSize + (marker * spacing);
        points.add(new Vector3(0, 0, 0));
        points.add(new Vector3(axisSize, 0, 0));
        colors.add(X_AXIS_RED);
        colors.add(X_AXIS_RED);
        points.add(new Vector3(0, 0, 0));
        points.add(new Vector3(0, axisSize, 0));
        colors.add(Y_AXIS_GREEN);
        colors.add(Y_AXIS_GREEN);
        points.add(new Vector3(0, 0, 0));
        points.add(new Vector3(0, 0, axisSize));
        colors.add(Z_AXIS_BLUE);
        colors.add(Z_AXIS_BLUE);

        _meshData.setVertexBuffer(BufferUtils.createFloatBuffer(points.toArray(new Vector3[points.size()])));
        _meshData.setColorBuffer(BufferUtils.createFloatBuffer(colors.toArray(new ColorRGBA[colors.size()])));
        _meshData.setIndexMode(IndexMode.Lines);
        updateWorldRenderStates(true);
        setModelBound(new BoundingSphere());
    }

    private void initRenderStates(boolean zbuffer) {
        BlendState blendState = new BlendState();
        blendState.setBlendEnabled(true);
        blendState.setSourceFunction(SourceFunction.SourceAlpha);
        blendState.setDestinationFunction(DestinationFunction.OneMinusSourceAlpha);
        ZBufferState zBufferState = new ZBufferState();
        zBufferState.setEnabled(true);
        zBufferState.setWritable(false);
        zBufferState.setFunction(zbuffer ? ZBufferState.TestFunction.LessThanOrEqualTo : ZBufferState.TestFunction.Always);
        this.setRenderState(blendState);
        this.setRenderState(zBufferState);
        this.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        this.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
        this.getSceneHints().setNormalsMode(NormalsMode.Off);
        this.getSceneHints().setDataMode(DataMode.VBO);
    }
    
    public void detach(){
        if (spatial != null){
            spatial.removeController(sc);
            spatial = null;
        }
    }

}
