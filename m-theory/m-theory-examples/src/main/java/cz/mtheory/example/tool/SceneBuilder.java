package cz.mtheory.example.tool;

import java.util.ArrayList;
import java.util.List;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Sphere;

public class SceneBuilder {

    public static void randomRotatingBoxes(final Node root, final int count) {
        List<Node> nodes = new ArrayList<Node>(count);
        Node parent = root;
        for (int i = 0; i < count; i++) {
            Node n = new Node("node_" + i);
            parent.attachChild(n);
            Box box = new Box("box_" + i, Vector3.ZERO, 0.5, 0.5, 0.5);
            box.setModelBound(new BoundingBox());
            n.attachChild(box);
            if (i != 0) {
                n.setTranslation(Rnd.nextInt(-5, 5), Rnd.nextInt(-5, 5), Rnd.nextInt(-5, 5));
                n.addController(new RandomRotateController());
            }
            nodes.add(n);
            parent = nodes.get(Rnd.nextInt(0, nodes.size() - 1));
        }
    }
    
    public static void randomRotatingSpheres(final Node root, final int count) {
        List<Node> nodes = new ArrayList<Node>(count);
        Node parent = root;
        for (int i = 0; i < count; i++) {
            Node n = new Node("node_" + i);
            parent.attachChild(n);
            Sphere sphere = new Sphere("sphere_" + i, 10, 10, 1);
            sphere.setModelBound(new BoundingBox());
            n.attachChild(sphere);
            if (i != 0) {
                n.setTranslation(Rnd.nextInt(-5, 5), Rnd.nextInt(-5, 5), Rnd.nextInt(-5, 5));
                n.addController(new RandomRotateController());
            }
            nodes.add(n);
            parent = nodes.get(Rnd.nextInt(0, nodes.size() - 1));
        }
    }
    

}
