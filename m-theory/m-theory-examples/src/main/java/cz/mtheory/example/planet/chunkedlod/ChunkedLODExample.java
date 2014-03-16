/**
 * 
 */
package cz.mtheory.example.planet.chunkedlod;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.geom.Debugger;

import cz.mtheory.core.tool.TimeMeter;
import cz.mtheory.example.ExampleBaseMtheory;
import cz.mtheory.planet.DEBUG;
import cz.mtheory.planet.Planet;
import cz.mtheory.planet.chunk.Chunk;
import cz.mtheory.planet.config.TestPlanetConfig;
import cz.mtheory.planet.util.Probe;

/**
 * @author Michal NkD Nikodim
 */
public class ChunkedLODExample extends ExampleBaseMtheory {

    private Probe probe = new Probe();
    private Planet planet;

    public static void main(String[] args) {
        start(ChunkedLODExample.class);
    }

    @Override
    protected void initExample() {
        prepareCamera();
        prepareKeys();
        planet = new Planet("Planet", _camera, new TestPlanetConfig(8, 18, 15, 6368));
        _root.attachChild(planet);

        //  Analyzer.attachToScene(_root, _logicalLayer);
    }

    @Override
    protected void updateExample(ReadOnlyTimer timer) {
        double distance = planet.makeCameraCorrection(_camera);
        double speed = distance;
        if (speed < 1) speed = 1;
        _controlHandle.setMoveSpeed(speed);
        //_controlHandle.setMoveSpeed(10000);

        probe.reset();
        if (DEBUG.updatePlanetCamera) planet.update(probe);
        if (probe.mergeCount != 0 || probe.splitCount != 0) System.out.println(probe + " - chunkPoolSize = " +
                Chunk.getPool().getActualSize());
    }

    @Override
    protected void renderExample(Renderer renderer) {
        super.renderExample(renderer);
        if (!DEBUG.updatePlanetCamera) Debugger.drawCameraFrustum(renderer, planet.getPlanetCamera(), ColorRGBA.YELLOW, (short) 0xFFFF, false);
    }

    private void prepareCamera() {
        _canvas.getCanvasRenderer().getRenderer().setBackgroundColor(ColorRGBA.RED);

        _camera.setFrustumPerspective(45, (float) _camera.getWidth() / _camera.getHeight(), 0.01f, 6000000);
        _camera.setLocation(new Vector3(0, 0, 6378 * 3));

        double d = 6378 * 2;
        PointLight light1 = new PointLight();
        light1.setDiffuse(new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
        light1.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.1f));
        light1.setAmbient(new ColorRGBA(0.03f, 0.03f, 0.03f, 1.0f));
        light1.setAttenuate(false);
        light1.setEnabled(true);
        light1.setLocation(new Vector3(d, -d, 0));

        PointLight light2 = new PointLight();
        light2.setDiffuse(new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
        light2.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.1f));
        light2.setAmbient(new ColorRGBA(0.03f, 0.03f, 0.03f, 1.0f));
        light2.setAttenuate(false);
        light2.setEnabled(true);
        light2.setLocation(new Vector3(-d, d, -d));

        PointLight light3 = new PointLight();
        light3.setDiffuse(new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
        light3.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.1f));
        light3.setAmbient(new ColorRGBA(0.03f, 0.03f, 0.03f, 1.0f));
        light3.setAttenuate(false);
        light3.setEnabled(true);
        light3.setLocation(new Vector3(-d, 0, d));

        LightState lightState = new LightState();
        lightState.setEnabled(true);
        lightState.setSeparateSpecular(true);

        lightState.attach(light1);
        lightState.attach(light2);
        lightState.attach(light3);
        _root.setRenderState(lightState);
    }

    private void prepareKeys() {
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.SPACE), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                DEBUG.updatePlanetCamera = !DEBUG.updatePlanetCamera;
                System.out.println("DEBUG.updatePlanetCamera = " + DEBUG.updatePlanetCamera);
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.H), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                DEBUG.horizontCulling = !DEBUG.horizontCulling;
                System.out.println("DEBUG.horizontCulling = " + DEBUG.horizontCulling);
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.J), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                DEBUG.showDrawedChunksCount = true;
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.N), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                DEBUG.solveNeighbors = !DEBUG.solveNeighbors;
                System.out.println("DEBUG.solveNeighbors = " + DEBUG.solveNeighbors);
            }
        }));
        
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.M), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                DEBUG.foo = (DEBUG.foo + 1) % 9;
                System.out.println("DEBUG.foo = " + DEBUG.foo);
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.INSERT), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                TimeMeter.printAll();
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.DELETE), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                TimeMeter.reset();
            }
        }));

    }

}
