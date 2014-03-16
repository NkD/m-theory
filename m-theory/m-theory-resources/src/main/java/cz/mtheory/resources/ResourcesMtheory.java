/**
 * 
 */
package cz.mtheory.resources;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import com.ardor3d.image.util.awt.AWTImageLoader;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;


/** 
 * M-theory project
 *
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class ResourcesMtheory {

    static {
        AWTImageLoader.registerLoader();
        try {
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL,new SimpleResourceLocator(ResourceLocatorTool.getClassPathResource(ResourcesMtheory.class, "data/model/")));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_SHADER, new SimpleResourceLocator(ResourceLocatorTool.getClassPathResource(ResourcesMtheory.class, "data/shader/")));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(ResourceLocatorTool.getClassPathResource(ResourcesMtheory.class, "data/texture/")));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private ResourcesMtheory() {
        //utility class
    }
    
    public static final void touch(){
        //nothing
    }
    
    public static final ByteBuffer loadShader(String path){
        InputStream is = null;
        try {
            is = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_SHADER, path).openStream();
            final DataInputStream dis = new DataInputStream(new BufferedInputStream(is));
            final byte shaderCode[] = new byte[dis.available()];
            dis.readFully(shaderCode);
            final ByteBuffer shaderByteBuffer = BufferUtils.createByteBuffer(shaderCode.length);
            shaderByteBuffer.put(shaderCode);
            shaderByteBuffer.rewind();
            return shaderByteBuffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            closeStream(is);
        }
    }
    
    private static final void closeStream(final InputStream is){
        if (is != null){
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
