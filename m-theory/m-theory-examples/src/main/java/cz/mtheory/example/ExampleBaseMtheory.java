/**
 * 
 */
package cz.mtheory.example;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.example.PropertiesDialog;
import com.ardor3d.example.PropertiesGameSettings;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.lwjgl.LwjglCanvas;
import com.ardor3d.framework.lwjgl.LwjglCanvasRenderer;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.logical.DummyControllerWrapper;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyHeldCondition;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.input.lwjgl.LwjglControllerWrapper;
import com.ardor3d.input.lwjgl.LwjglKeyboardWrapper;
import com.ardor3d.input.lwjgl.LwjglMouseManager;
import com.ardor3d.input.lwjgl.LwjglMouseWrapper;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;
import com.ardor3d.renderer.lwjgl.LwjglTextureRendererProvider;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.NormalsMode;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import com.ardor3d.util.export.xml.XMLExporter;
import com.ardor3d.util.export.xml.XMLImporter;
import com.google.common.base.Predicates;

import cz.mtheory.core.control.SpaceControl;
import cz.mtheory.resources.ResourcesMtheory;

/**
 * @author Michal NkD Nikodim
 */
public abstract class ExampleBaseMtheory implements Runnable, Updater, Scene {

    protected final Timer _timer = new Timer();
    protected final FrameHandler _frameHandler = new FrameHandler(_timer);
    protected NativeCanvas _canvas;
    protected volatile boolean _exit = false;
    protected MouseManager _mouseManager;
    protected SpaceControl _controlHandle;
    protected final Node _root = new Node();
    protected PhysicalLayer _physicalLayer;
    protected final LogicalLayer _logicalLayer = new LogicalLayer();
    protected Camera _camera;

    public static final void start(final Class<? extends ExampleBaseMtheory> clazz) {
    	final PropertiesGameSettings pgs = new PropertiesGameSettings("settings.prop", null);
        if (pgs.isNew()) showPropertiesDialog(pgs);

        ExampleBaseMtheory example;
        try {
            example = clazz.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final DisplaySettings settings = new DisplaySettings(pgs.getWidth(), pgs.getHeight(), pgs.getDepth(), pgs.getFrequency(), pgs.getAlphaBits(), pgs.getDepthBits(), pgs.getStencilBits(), pgs.getSamples(), pgs.isFullscreen(), false);

        if (pgs.getRenderer().startsWith("LWJGL")) {
            final LwjglCanvasRenderer canvasRenderer = new LwjglCanvasRenderer(example);
            example._canvas = new LwjglCanvas(settings, canvasRenderer);
            example._physicalLayer = new PhysicalLayer(new LwjglKeyboardWrapper(), new LwjglMouseWrapper(), new LwjglControllerWrapper(), (LwjglCanvas) example._canvas);
            example._mouseManager = new LwjglMouseManager();
            TextureRendererFactory.INSTANCE.setProvider(new LwjglTextureRendererProvider());
        } else if (pgs.getRenderer().startsWith("JOGL")) {
            final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(example);
            example._canvas = new JoglCanvas(canvasRenderer, settings);
            final JoglCanvas canvas = (JoglCanvas) example._canvas;
            example._mouseManager = new AwtMouseManager(canvas);
            example._physicalLayer = new PhysicalLayer(new AwtKeyboardWrapper(canvas), new AwtMouseWrapper(canvas, example._mouseManager), DummyControllerWrapper.INSTANCE, new AwtFocusWrapper(canvas));
            TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
        }
        example._logicalLayer.registerInput(example._canvas, example._physicalLayer);
        example._frameHandler.addUpdater(example);
        example._frameHandler.addCanvas(example._canvas);
        new Thread(example).start();
    }
    
    @MainThread
    @Override
    public final void init() {
        ResourcesMtheory.touch();

        _canvas.setVSyncEnabled(false);
        _camera = _canvas.getCanvasRenderer().getCamera();
        registerInputTriggers();

        final ZBufferState buf = new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        _root.setRenderState(buf);

        final PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
        light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        light.setLocation(new Vector3(10000, 10000, 10000));
        light.setAttenuate(false);
        light.setEnabled(true);

        final LightState lightState = new LightState();
        lightState.setEnabled(true);
        lightState.attach(light);
        _root.setRenderState(lightState);

        WireframeState wireframeState = new WireframeState();
        wireframeState.setEnabled(false);
        _root.setRenderState(wireframeState);

        _root.getSceneHints().setRenderBucketType(RenderBucketType.Opaque);
        _root.getSceneHints().setCullHint(CullHint.Dynamic);
        _root.getSceneHints().setNormalsMode(NormalsMode.UseProvided);
        _root.getSceneHints().setLightCombineMode(LightCombineMode.CombineClosest);

        initExample();

        _root.updateGeometricState(0);
    }
    
    @MainThread
    @Override
    public final void update(ReadOnlyTimer timer) {
        _exit = _canvas.isClosing();
        _logicalLayer.checkTriggers(timer.getTimePerFrame());
        GameTaskQueueManager.getManager(_canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.UPDATE).execute();
        updateExample(timer);
        _root.updateGeometricState(timer.getTimePerFrame(), true);
    }

    protected abstract void initExample();

    protected abstract void updateExample(ReadOnlyTimer timer);

    protected void renderExample(Renderer renderer) {
        _root.onDraw(renderer);
    }

    @Override
    public final boolean renderUnto(Renderer renderer) {
        GameTaskQueueManager.getManager(_canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER).execute(renderer);
        ContextGarbageCollector.doRuntimeCleanup(renderer);
        if (!_canvas.isClosing()) {
            renderExample(renderer);
            return true;
        }
        return false;
    }

    @MainThread
    @Override
    public final void run() {
        try {
            _frameHandler.init();
            while (!_exit) {
                _frameHandler.updateFrame();
                Thread.yield();
            }
            final CanvasRenderer cr = _canvas.getCanvasRenderer();
            cr.makeCurrentContext();
            ContextGarbageCollector.doFinalCleanup(cr.getRenderer());
            _canvas.close();
            cr.releaseCurrentContext();
            System.exit(0);
        } catch (final Throwable t) {
            System.err.println("Throwable caught in MainThread - exiting");
            t.printStackTrace(System.err);
        }
    }

    @Override
    public final PickResults doPick(Ray3 pickRay) {
        return null;
    }

    protected void registerInputTriggers() {
        _controlHandle = SpaceControl.setupTriggers(_logicalLayer, _mouseManager, _camera);

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                _exit = true;
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.T), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                WireframeState wfs = (WireframeState) _root.getLocalRenderState(StateType.Wireframe);
                wfs.setEnabled(!wfs.isEnabled());
                _root.markDirty(DirtyType.RenderState);
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.RIGHT), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                if (_mouseManager.isSetGrabbedSupported()) {
                    _mouseManager.setGrabbed(GrabbedState.GRABBED);
                }
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.RIGHT), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                if (_mouseManager.isSetGrabbedSupported()) {
                    _mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                }
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(Predicates.and(new KeyHeldCondition(Key.LSHIFT), new KeyPressedCondition(Key.C)), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                File f = new File("camera.snapshot");
                try {
                    new XMLExporter().save(_camera, f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Camera snapshot created");
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(Predicates.and(new KeyHeldCondition(Key.LMENU), new KeyPressedCondition(Key.C)), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                File f = new File("camera.snapshot");
                if (f.isFile()) {
                    try {
                        Camera c = (Camera) new XMLImporter().load(f);
                        c.resize(_camera.getWidth(), _camera.getHeight());
                        _camera.set(c);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Camera snapshot restored");
                }
            }
        }));

    }

    private static void showPropertiesDialog(final PropertiesGameSettings pgs) {
        final AtomicReference<PropertiesDialog> dialogRef = new AtomicReference<PropertiesDialog>();
        final Stack<Runnable> mainThreadTasks = new Stack<Runnable>();
        try {
            if (EventQueue.isDispatchThread()) {
                dialogRef.set(new PropertiesDialog(pgs, (URL) null, mainThreadTasks));
            } else {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        dialogRef.set(new PropertiesDialog(pgs, (URL) null, mainThreadTasks));
                    }
                });
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        PropertiesDialog dialogCheck = dialogRef.get();
        while (dialogCheck == null || dialogCheck.isVisible()) {
            try {
                while (!mainThreadTasks.isEmpty()) {
                    mainThreadTasks.pop().run();
                }
                Thread.sleep(50);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            dialogCheck = dialogRef.get();
        }
        if (dialogCheck.isCancelled()) System.exit(0);
    }
}
