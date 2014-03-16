/**
 * 
 */
package cz.mtheory.roam.planet;

import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.CullState.Face;
import com.ardor3d.renderer.state.CullState.PolygonWind;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.renderer.state.ZBufferState.TestFunction;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.GeoSphere;
import com.ardor3d.scenegraph.shape.GeoSphere.TextureMode;

import cz.mtheory.helper.Tool;

/** 
 * M-theory project
 *
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class Atmosphere {

    private final GLSLShaderObjectsState skyFromSpace;
    private final GLSLShaderObjectsState skyFromAtm;
    private GLSLShaderObjectsState skyShader;

    private final Mesh mesh;

    private final double atmosphereRadius;
    private final double planetRadius;
    private final Camera camera;
    private final Spatial sun;

    private CullState cullState;
    private boolean inside = false;

    private Vector3 lightPos = new Vector3();
    private Vector3 lightDirection = new Vector3();
    private Vector3 cameraPositionObjectSpace = new Vector3();
    private Vector3 cameraPosition = new Vector3();
    
    private int nSamples;
    private float kR;
    private float kR4PI;
    private float kM;
    private float kM4PI;
    private float eSun;
    private float g;
    private float scale;
    private float[] waveLength;
    private float[] waveLength4inv;
    private float rayleighScaleDepth;

    public Atmosphere(double atmosphereRadius, double planetRadius, Camera camera, Spatial sun) {
        this.atmosphereRadius = atmosphereRadius;
        this.planetRadius = planetRadius;
        this.camera = camera;
        this.sun = sun;

        mesh = new GeoSphere("", false, atmosphereRadius, 6, TextureMode.Original);

        WireframeState wfs = new WireframeState();
        wfs.setEnabled(false);
        mesh.setRenderState(wfs);

        BlendState atmoBlendState = new BlendState();
        atmoBlendState.setBlendEnabled(true);
        atmoBlendState.setSourceFunction(BlendState.SourceFunction.One);
        atmoBlendState.setDestinationFunction(BlendState.DestinationFunction.One);
        mesh.setRenderState(atmoBlendState);

        cullState = new CullState();
        cullState.setPolygonWind(PolygonWind.ClockWise);
        cullState.setCullFace(Face.Back);
        mesh.setRenderState(cullState);
        
        ZBufferState zstate = new ZBufferState();
        zstate.setEnabled(true);
        zstate.setWritable(false);
        zstate.setFunction(TestFunction.LessThanOrEqualTo);
        mesh.setRenderState(zstate);

        mesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);

        skyFromSpace = Tool.createShaderState("atmospherescattering/oneil/SkyFromSpaceVert.glsl", "atmospherescattering/oneil/SkyFromSpaceFrag.glsl");
        skyFromAtm = Tool.createShaderState("atmospherescattering/oneil/SkyFromAtmosphereVert.glsl", "atmospherescattering/oneil/SkyFromAtmosphereFrag.glsl");
        skyShader = skyFromSpace;
        inside = true;
        init();
        
        
    }

    private void init() {
        nSamples = 4;
        kR = 0.0035f;
        kR4PI = kR * 4.0f * (float) Math.PI;
        kM = 0.0010f;
        kM4PI = kM * 4.0f * (float) Math.PI;
        eSun = 20f;
        g = -0.95f;
        scale = (float) (1 / (atmosphereRadius - planetRadius));
        waveLength = new float[3];
        waveLength[0] = 0.650f;
        waveLength[1] = 0.570f;
        waveLength[2] = 0.475f;
        waveLength4inv = new float[3];
        waveLength4inv[0] = 1f / (float) Math.pow(waveLength[0], 4.0f);
        waveLength4inv[1] = 1f / (float) Math.pow(waveLength[1], 4.0f);
        waveLength4inv[2] = 1f / (float) Math.pow(waveLength[2], 4.0f);
        rayleighScaleDepth = 0.25f;
    }
    
    public void render(Renderer renderer){
        mesh.render(renderer);
    }

    public void update() {

        cameraPosition.set(camera.getLocation());
        cameraPosition.subtract(mesh.getWorldTranslation(), cameraPositionObjectSpace);
        float cameraHeight = (float) Math.sqrt(cameraPositionObjectSpace.lengthSquared());
        float cameraHeight2 = (float) cameraPositionObjectSpace.lengthSquared();

        checkForShaderSwitch(cameraHeight);

        sun.getWorldTranslation().subtract(mesh.getWorldTranslation(), lightPos);

        lightDirection.set(lightPos).normalizeLocal();

        skyShader.setUniform("v3CameraPos", cameraPositionObjectSpace);
        skyShader.setUniform("v3LightPos", lightDirection);
        skyShader.setUniform("v3InvWavelength", waveLength4inv[0], waveLength4inv[1], waveLength4inv[2]);
        skyShader.setUniform("fInnerRadius", (float) planetRadius);
        if (inside) {
            skyShader.setUniform("fCameraHeight", cameraHeight);
        } else {
            skyShader.setUniform("fCameraHeight2", cameraHeight2);
            skyShader.setUniform("fOuterRadius", (float) atmosphereRadius);
            skyShader.setUniform("fOuterRadius2", (float) (atmosphereRadius * atmosphereRadius));
        }
        skyShader.setUniform("fKrESun", kR * eSun);
        skyShader.setUniform("fKmESun", kM * eSun);
        skyShader.setUniform("fKr4PI", kR4PI);
        skyShader.setUniform("fKm4PI", kM4PI);
        skyShader.setUniform("fScale", scale);
        skyShader.setUniform("fScaleDepth", rayleighScaleDepth);
        skyShader.setUniform("fScaleOverScaleDepth", scale / rayleighScaleDepth);
        skyShader.setUniform("g", g);
        skyShader.setUniform("g2", g * g);
        skyShader.setUniform("nSamples", nSamples);
        skyShader.setUniform("fSamples", (float) nSamples);
        
        mesh.updateGeometricState(0);
    }

    private void checkForShaderSwitch(float distanceToOrigin) {
        if (distanceToOrigin <= atmosphereRadius && !inside) {
            skyShader = skyFromAtm;
            inside = true;
            cullState.setPolygonWind(PolygonWind.ClockWise);
            cullState.setCullFace(Face.Back);
            mesh.setRenderState(skyShader);
        } else if (distanceToOrigin > atmosphereRadius && inside) {
            skyShader = skyFromSpace;
            inside = false;
            cullState.setPolygonWind(PolygonWind.CounterClockWise);
            cullState.setCullFace(Face.Front);
            mesh.setRenderState(skyShader);
        }
    }

}
