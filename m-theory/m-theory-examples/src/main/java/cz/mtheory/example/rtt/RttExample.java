/**
 * 
 */
package cz.mtheory.example.rtt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.util.awt.AWTImageUtil;
import com.ardor3d.input.Key;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.TextureState.CorrectionType;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureKey;

import cz.mtheory.example.ExampleBaseMtheory;
import cz.mtheory.helper.Tool;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class RttExample extends ExampleBaseMtheory {

    public static void main(String[] args) {
        start(RttExample.class);
    }

    private boolean inited = false;
    private int rttSize = 256;
    private TextureRenderer tr;
    private Quad rttQuad;
    private Texture2D rttTexture;
    private Box box;

    @Override
    protected void initExample() {

        box = new Box("Box", Vector3.ZERO, 100, 100, 100);

        _root.attachChild(box);

        _camera.setLocation(50, 50, 350);
        _camera.lookAt(0, 0, 0, Vector3.UNIT_Y);
        _controlHandle.setMoveSpeed(100);

        _logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.SPACE), new TriggerAction() {
            @Override
            @MainThread
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                try {
                    
                    Display.makeCurrent();
                    
                    
                } catch (LWJGLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //GL11.glCopyTexSubImage2D(rttTexture.getTextureIdForContext(ContextManager.getCurrentContext()), 0, 0, 0, 0, 0, rttSize, rttSize);
                Texture2D tex = new Texture2D();
                tex.setTextureKey(TextureKey.getRTTKey(tex.getMinificationFilter()));
                tr.copyToTexture(tex, 0, 0, rttSize, rttSize, 0, 0);
                final List<BufferedImage> img = AWTImageUtil.convertToAWT(tex.getImage());

                try {
                    final File file = new File("1.png");
                    ImageIO.write(img.get(0), "PNG", file);
                    System.err.println(file.getAbsolutePath());
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            }
        }));
    }

    @Override
    protected void updateExample(ReadOnlyTimer timer) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void renderExample(Renderer renderer) {
        if (!inited) initRtt(renderer);
        tr.setBackgroundColor(ColorRGBA.RED);
        tr.render(rttQuad, rttTexture, 1);
        _canvas.getCanvasRenderer().getRenderer().setBackgroundColor(ColorRGBA.BLUE);
        super.renderExample(renderer);
    }

    private void initRtt(final Renderer renderer) {
        inited = true;
        DisplaySettings ds = new DisplaySettings(rttSize, rttSize, 24, 0);
        tr = TextureRendererFactory.INSTANCE.createTextureRenderer(ds, false, renderer, ContextManager.getCurrentContext().getCapabilities());
        tr.getCamera().setProjectionMode(ProjectionMode.Parallel);
        tr.getCamera().setLocation(0, 0, 100);
        tr.getCamera().setFrustum(1.0f, 1000.0f, -rttSize / 2, rttSize / 2, rttSize / 2, -rttSize / 2);
        tr.getCamera().update();
        rttQuad = new Quad("quad", rttSize, rttSize);
        rttQuad.setDefaultColor(ColorRGBA.GREEN);
        rttQuad.setRenderState(Tool.createShaderState("examples/atest.vert", "examples/atest.frag"));

        rttQuad.updateGeometricState(0);
        
        rttTexture = new Texture2D();
        //TextureManager.addToCache(rttTexture);
        rttTexture.setWrap(WrapMode.Clamp);
        rttTexture.setMagnificationFilter(Texture.MagnificationFilter.NearestNeighbor);
        rttTexture.setMinificationFilter(Texture.MinificationFilter.NearestNeighborNoMipMaps);

        tr.setBackgroundColor(ColorRGBA.RED);
        rttTexture.setStoreImage(true);
        tr.setupTexture(rttTexture);
        
        
        final TextureState ts = new TextureState();
        ts.setTexture(rttTexture);
        ts.setCorrectionType(CorrectionType.Affine);
        ts.setEnabled(true);
        box.setRenderState(ts);
    }
}
