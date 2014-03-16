/**
 * 
 */
package cz.mtheory.core.tool;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.renderer.state.WireframeState.Face;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.NormalsMode;

/**
 * @author Michal NkD Nikodim
 */
public class Analyzer extends Mesh {

    private static Analyzer REF;

    public static void attachToScene(Node root, LogicalLayer logicalLayer) {
        detachFromScene();
        REF = new Analyzer(root, logicalLayer);
        root.attachChild(REF);
    }

    public static void detachFromScene() {
        if (REF != null) {
            REF.dispose();
            REF = null;
        }
    }
    
    public static Analyzer ref(){
        return REF;
    }

    private Node root;
    private LogicalLayer logicalLayer;
    private PickResults pickResults = new PrimitivePickResults();
    private ColorRGBA faceColor = new ColorRGBA(1, 0.6f, 0, 0.4f);
    private ColorRGBA wireColor = new ColorRGBA(0.2f, 0.2f, 0.2f, 1f);

    private Mesh selectedMesh;
    private BillBoardVertexNumbers bbvn;

    private SpatialController<Spatial> updater = new SpatialController<Spatial>() {
        @Override
        public void update(double time, Spatial caller) {
            setWorldTransform(caller.getWorldTransform());
            if (bbvn != null) bbvn.setWorldTransform(caller.getWorldTransform());
        }
    };
    private List<InputTrigger> inputTriggers = new ArrayList<InputTrigger>();

    private Analyzer(Node root, LogicalLayer logicalLayer) {
        super("Analyzer");

        this.root = root;
        this.logicalLayer = logicalLayer;
        this.pickResults.setCheckDistance(true);

        ZBufferState zs = new ZBufferState();
        zs.setEnabled(false);
        setRenderState(zs);

        getSceneHints().setNormalsMode(NormalsMode.Off);
        getSceneHints().setLightCombineMode(LightCombineMode.Off);
        getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
        getSceneHints().setAllPickingHints(false);

        registerControls();
    }

    private void registerControls() {
        inputTriggers.add(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                final Vector2 pos = Vector2.fetchTempInstance().set(inputStates.getCurrent().getMouseState().getX(), inputStates.getCurrent().getMouseState().getY());
                final Ray3 pickRay = Ray3.fetchTempInstance();
                source.getCanvasRenderer().getCamera().getPickRay(pos, false, pickRay);
                Vector2.releaseTempInstance(pos);
                pickResults.clear();
                CollisionTreeManager.getInstance().removeCollisionTree(root);
                PickingUtil.findPick(Analyzer.this.root, pickRay, pickResults);
                if (pickResults.getNumber() > 0) {
                    final PickData pick = pickResults.getPickData(0);
                    if (pick != null && pick.getTarget() instanceof Mesh) {
                        setSelection((Mesh) pick.getTarget());
                    } else {
                        setSelection(null);
                    }
                } else {
                    setSelection(null);
                }

            }
        }));

        inputTriggers.add(new InputTrigger(new KeyPressedCondition(Key.I), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                if (hasSelection() && bbvn == null) {
                    bbvn = new BillBoardVertexNumbers(selectedMesh);
                    bbvn.updateGeometricState(0);
                } else {
                    bbvn = null;
                }
            }
        }));

        for (InputTrigger it : inputTriggers) {
            logicalLayer.registerTrigger(it);
        }

    }

    @Override
    public void draw(Renderer r) {
        if (hasSelection()) {
            if (r.isProcessingQueue()) {
                {
                    setDefaultColor(faceColor);
                    WireframeState ws = new WireframeState();
                    ws.setEnabled(false);
                    setRenderState(ws);
                    CullState cs = new CullState();
                    cs.setCullFace(com.ardor3d.renderer.state.CullState.Face.None);
                    setRenderState(cs);
                    BlendState bs = new BlendState();
                    bs.setBlendEnabled(true);
                    bs.setSourceFunctionAlpha(SourceFunction.SourceAlpha);
                    bs.setDestinationFunction(DestinationFunction.One);
                    bs.setTestEnabled(false);
                    setRenderState(bs);
                    updateGeometricState(0);
                    super.draw(r);
                }
                {
                    setDefaultColor(wireColor);
                    WireframeState ws = new WireframeState();
                    ws.setEnabled(true);
                    ws.setFace(Face.Front);
                    setRenderState(ws);
                    CullState cs = new CullState();
                    cs.setCullFace(com.ardor3d.renderer.state.CullState.Face.Back);
                    setRenderState(cs);
                    BlendState bs = new BlendState();
                    bs.setBlendEnabled(false);
                    setRenderState(bs);
                    updateGeometricState(0);
                    super.draw(r);
                }
                if (bbvn != null) bbvn.draw(r);
            } else {
                super.draw(r);
            }
        }
    }

    public boolean hasSelection() {
        return getSelection() != null;
    }

    public Mesh getSelection() {
        return selectedMesh;
    }

    public void setSelection(Mesh mesh) {
        if (mesh == null) {
            if (selectedMesh != null) selectedMesh.removeController(updater);
            if (bbvn != null) bbvn = null;
            selectedMesh = null;
        } else if (selectedMesh != mesh) {
            if (selectedMesh != null) selectedMesh.removeController(updater);
            selectedMesh = mesh;
            _meshData.setVertexBuffer(selectedMesh.getMeshData().getVertexBuffer());
            Buffer indexBuffer = selectedMesh.getMeshData().getIndexBuffer();
            if (indexBuffer instanceof IntBuffer) {
                _meshData.setIndexBuffer((IntBuffer) indexBuffer);
            } else if (indexBuffer instanceof ByteBuffer) {
                _meshData.setIndexBuffer((ByteBuffer) indexBuffer);
            } else if (indexBuffer instanceof ShortBuffer) {
                _meshData.setIndexBuffer((ShortBuffer) indexBuffer);
            }
            _meshData.setIndexModes(selectedMesh.getMeshData().getIndexModes());
            _meshData.setIndexLengths(selectedMesh.getMeshData().getIndexLengths());
            if (bbvn != null) {
                bbvn = new BillBoardVertexNumbers(selectedMesh);
                bbvn.updateGeometricState(0);
            }
            selectedMesh.addController(updater);
        }
    }

    public void dispose() {
        if (selectedMesh != null) selectedMesh.removeController(updater);
        selectedMesh = null;
        bbvn = null;
        root.detachChild(this);
        for (InputTrigger it : inputTriggers) {
            logicalLayer.deregisterTrigger(it);
        }
    }
}
