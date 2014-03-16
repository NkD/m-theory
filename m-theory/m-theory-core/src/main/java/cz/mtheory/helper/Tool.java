package cz.mtheory.helper;

import java.util.ArrayList;
import java.util.List;

import com.ardor3d.image.Texture;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.util.TextureManager;

import cz.mtheory.resources.ResourcesMtheory;

public class Tool {

    public static List<Mesh> findAllMeshes(Spatial spatial) {
        List<Mesh> meshes = new ArrayList<Mesh>();
        findMeshesRecursion(spatial, meshes);
        return meshes;
    }

    private static void findMeshesRecursion(Spatial spatial, List<Mesh> meshes) {
        if (spatial instanceof Mesh) {
            meshes.add((Mesh) spatial);
        } else if (spatial instanceof Node) {
            List<Spatial> children = ((Node) spatial).getChildren();
            for (Spatial child : children) {
                findMeshesRecursion(child, meshes);
            }
        }
    }

    public static GLSLShaderObjectsState createShaderState(String vertPath, String fragPath) {
        GLSLShaderObjectsState shader = new GLSLShaderObjectsState();
        shader.setVertexShader(ResourcesMtheory.loadShader(vertPath));
        shader.setFragmentShader(ResourcesMtheory.loadShader(fragPath));
        return shader;
    }

    public static Skybox buildSkyBox() {

        final Skybox skybox = new Skybox("skybox", 10, 10, 10);

        final String dir = "skybox/";
        final Texture north = TextureManager.load(dir + "front.png", Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture south = TextureManager.load(dir + "back.png", Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture east = TextureManager.load(dir + "right.png", Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture west = TextureManager.load(dir + "left.png", Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture up = TextureManager.load(dir + "top.png", Texture.MinificationFilter.BilinearNearestMipMap, true);
        final Texture down = TextureManager.load(dir + "bottom.png", Texture.MinificationFilter.BilinearNearestMipMap, true);

        skybox.setTexture(Skybox.Face.North, north);
        skybox.setTexture(Skybox.Face.West, west);
        skybox.setTexture(Skybox.Face.South, south);
        skybox.setTexture(Skybox.Face.East, east);
        skybox.setTexture(Skybox.Face.Up, up);
        skybox.setTexture(Skybox.Face.Down, down);

        WireframeState wfs = new WireframeState();
        wfs.setEnabled(false);
        skybox.setRenderState(wfs);
        
        return skybox;
    }

}
