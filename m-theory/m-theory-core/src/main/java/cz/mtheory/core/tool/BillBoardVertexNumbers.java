/**
 * 
 */
package cz.mtheory.core.tool;

import java.nio.FloatBuffer;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Michal NkD Nikodim
 */
public class BillBoardVertexNumbers extends Node{

    public BillBoardVertexNumbers(Mesh mesh){
        Vector3 v = Vector3.fetchTempInstance();
        FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
        int size = buf.limit() / 3;
        ZBufferState zs = new ZBufferState();
        zs.setEnabled(false);
        setRenderState(zs);
        WireframeState ws = new WireframeState();
        ws.setEnabled(false);
        setRenderState(ws);
        getSceneHints().setRenderBucketType(RenderBucketType.PostBucket);
        for (int index = 0; index < size; index++) {
            BufferUtils.populateFromBuffer(v, buf, index);
            BMText bt = new BMText("", String.valueOf(index), BasicText.DEFAULT_FONT,Align.Center);
            bt.setFontScale(0.7);
            
            bt.setSolidColor(ColorRGBA.WHITE);
            bt.setTranslation(v);
            bt.setRenderState(zs);
            attachChild(bt);
        }
        Vector3.releaseTempInstance(v);
    }
    
}
