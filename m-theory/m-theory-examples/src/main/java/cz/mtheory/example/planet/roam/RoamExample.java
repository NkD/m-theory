package cz.mtheory.example.planet.roam;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyHeldCondition;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.GeoSphere;
import com.ardor3d.scenegraph.shape.GeoSphere.TextureMode;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.geom.Debugger;

import cz.mtheory.core.tool.TimeMeter;
import cz.mtheory.example.ExampleBaseMtheory;
import cz.mtheory.helper.Tool;
import cz.mtheory.resources.ResourcesMtheory;
import cz.mtheory.roam.planet.Atmosphere;
import cz.mtheory.roam.planet.PlanetROAM;
import cz.mtheory.roam.planet.PlanetStatistic;
import cz.mtheory.roam.planet.config.PlanetConfigEarth;
import cz.mtheory.roam.planet.config.PlanetConfigMoon;

/**
 * @author Michal NkD Nikodim
 */
public class RoamExample extends ExampleBaseMtheory {

    public static void main(String[] args) {
        start(RoamExample.class);
    }

    private Skybox skyBox;

    private Node sunNode;
    private GeoSphere sun;
    private PointLight light1;
    private Atmosphere atmosphere;
    private PlanetROAM planet;
    private Node moonNode;
    private PlanetROAM moon;

    private boolean updatePlanet = true;
    private boolean updateStatistic = false;
    private boolean showText = true;

    private PlanetStatistic ps = new PlanetStatistic();
    private String planetStatistic = "";

    private int fps = 0;
    private long prevTime;

    private final BasicText info[] = new BasicText[7];
    private Node textNodes;

    private final Matrix3 rotateSun = new Matrix3();
    private double angleSun = 0;
    private final Vector3 axisSun = new Vector3(0, 1, 0).normalizeLocal();

    private final Matrix3 rotateMoon = new Matrix3();
    private double angleMoon = 0;
    private final Vector3 axisMoon = new Vector3(0, 1, 0).normalizeLocal();

    private BigDecimal rotateBD = BigDecimal.ZERO;

    private double distance;

    //** hack, because i have wrong object hieararchy. I have root -> sun,planet,moon and have to be (maybe) root -> sun-> planet->moon 
    private Vector3 initialLightPos = new Vector3(1000000, 0, 17000000);

    @Override
    protected void initExample() {
        ResourcesMtheory.touch();
        prepareCamera();
        prepareLight();
        prepareKeys();
        prepareTexts();

        skyBox = Tool.buildSkyBox();
        _root.attachChild(skyBox);

        sun = new GeoSphere("SUN", true, 100000, 3, TextureMode.Original);
        sun.setTranslation(initialLightPos);
        MaterialState ms = new MaterialState();
        ms.setEmissive(ColorRGBA.WHITE);
        ms.setColorMaterial(ColorMaterial.Emissive);
        sun.setRenderState(ms);
        sunNode = new Node();
        sunNode.attachChild(sun);
        _root.attachChild(sunNode);

        planet = new PlanetROAM(new PlanetConfigEarth(), _camera);
        _root.attachChild(planet);

        atmosphere = new Atmosphere(6600, planet.getPlanetConfig().getRadius(), _camera, sun);
        planet.setAtmosphere(atmosphere);

        moon = new PlanetROAM(new PlanetConfigMoon(), _camera);
        moon.setTranslation(37337, 0, 0);
        moonNode = new Node("Moon_node");
        moonNode.attachChild(moon);
        _root.attachChild(moonNode);
    }

    @Override
    protected void updateExample(ReadOnlyTimer timer) {

        if (updatePlanet) {
            if (BigDecimal.ZERO.compareTo(rotateBD) != 0) {
                angleSun += timer.getTimePerFrame() * rotateBD.doubleValue();
                angleSun %= 360;
                rotateSun.fromAngleNormalAxis(angleSun * MathUtils.DEG_TO_RAD, axisSun);
                sunNode.setRotation(rotateSun);
                Vector3 v = Vector3.fetchTempInstance();
                v.set(initialLightPos);
                sunNode.getWorldTransform().applyForward(v, v);
                light1.setLocation(v);
                Vector3.releaseTempInstance(v);

                angleMoon -= timer.getTimePerFrame() * rotateBD.doubleValue();
                angleMoon %= 360;
                rotateMoon.fromAngleNormalAxis(angleMoon * MathUtils.DEG_TO_RAD, axisMoon);
                moonNode.setRotation(rotateMoon);
                moon.setRotation(rotateMoon);
            }
        }

        double distance1 = planet.makeCameraCorrection(_camera);
        double distance2 = moon.makeCameraCorrection(_camera);
        distance = Math.min(distance1, distance2);
        double speed = distance;
        if (speed < 1) speed = 1;
        _controlHandle.setMoveSpeed(speed);

        skyBox.setTranslation(_camera.getLocation());
        _camera.update();

        if (updatePlanet) {
            planet.updateGeometry();
            planet.updateBuffers();
            moon.updateGeometry();
            moon.updateBuffers();
        }

        if (showText) {
            long current = System.currentTimeMillis();
            if (prevTime + 300 < current) {
                prevTime = current;
                fps = (int) timer.getFrameRate();
            }
            if (updateStatistic) {
                ps.reset();
                planet.takeStatistic(ps);
                planetStatistic = ps.getLevelsStatistic(-1).toString();
            } else {
                planetStatistic = "off";
            }
            updateText();
        }

    }

    @Override
    protected void renderExample(Renderer renderer) {
        super.renderExample(renderer);
        if (!updatePlanet) Debugger.drawCameraFrustum(renderer, planet.getPlanetCamera(), ColorRGBA.YELLOW, (short) 0xFFFF, false);
    }

    private void prepareCamera() {
        _canvas.setVSyncEnabled(false);
        _camera.setLocation(10000, 10000, 10000);
        _camera.lookAt(Vector3.ZERO, Vector3.UNIT_Y);
        _camera.setFrustumPerspective(70, (float) _camera.getWidth() / _camera.getHeight(), 0.005f, 600000);
    }

    private void prepareLight() {
        light1 = new PointLight();
        light1.setDiffuse(new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f));
        light1.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.1f));
        light1.setAmbient(new ColorRGBA(0.03f, 0.03f, 0.03f, 1.0f));
        light1.setAttenuate(false);
        light1.setEnabled(true);
        light1.setLocation(initialLightPos);

        LightState lightState = new LightState();
        lightState.setEnabled(true);
        lightState.setSeparateSpecular(false);
        lightState.attach(light1);
        _root.setRenderState(lightState);
    }

    private void prepareKeys() {
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.SPACE), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                updatePlanet = !updatePlanet;
                System.out.println("Update planet set to " + updatePlanet);
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.Z), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                updateStatistic = !updateStatistic;
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.H), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                showText = !showText;
                textNodes.getSceneHints().setCullHint(showText ? CullHint.Never : CullHint.Always);
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.ONE), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                rotateBD = rotateBD.add(new BigDecimal("0.01"));
            }
        }));
        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.TWO), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                rotateBD = BigDecimal.ZERO;
            }
        }));

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.P), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                TimeMeter.printAll();
                TimeMeter.reset();
            }
        }));

    }

    private void prepareTexts() {
        textNodes = new Node("Text");
        WireframeState wfs = new WireframeState();
        wfs.setEnabled(false);
        textNodes.setRenderState(wfs);
        _root.attachChild(textNodes);
        textNodes.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);
        textNodes.getSceneHints().setLightCombineMode(LightCombineMode.Off);

        final double infoStartY = info.length * 15 - 13;

        info[0] = BasicText.createDefaultTextLabel("Text", "", 20);
        info[0].setTranslation(new Vector3(2, infoStartY + 10, 0));
        textNodes.attachChild(info[0]);

        for (int i = 1; i < info.length; i++) {
            info[i] = BasicText.createDefaultTextLabel("Text", "", 14);
            info[i].setTranslation(new Vector3(2, infoStartY - i * 15, 0));
            textNodes.attachChild(info[i]);
        }

        textNodes.updateGeometricState(0.0);
        textNodes.getSceneHints().setCullHint(showText ? CullHint.Never : CullHint.Always);
        updateText();
    }

    private void updateText() {
        info[0].setText("FPS: " + fps);
        info[1].setText("DISTANCE TO PLANET: " + new BigDecimal(distance).setScale(2, RoundingMode.HALF_UP).toPlainString() + " km");
        info[2].setText("UPDATE PLANET CAMERA [SPACE]: " + updatePlanet);
        info[3].setText("CAMERA SPEED [NUM+-]: " + _controlHandle.getSpeed());
        info[4].setText("ROTATE PLANET [1/2]: " + rotateBD);
        info[5].setText("STATISTIC [Z]: " + planetStatistic);
        info[6].setText("HIDE/SHOW TEXT [H]");
    }

}
