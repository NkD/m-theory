package cz.mtheory.example.test;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.lwjgl.LwjglCanvas;
import com.ardor3d.framework.lwjgl.LwjglCanvasRenderer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.ContextGarbageCollector;

public class ShortestExample implements Scene {

    private LwjglCanvas _canvas;
    private Node _root;

    public static void main(String[] args) {
        new ShortestExample().start();
    }

    public void start() {
        LwjglCanvasRenderer canvasRenderer = new LwjglCanvasRenderer(this);
        DisplaySettings settings = new DisplaySettings(640, 480, 24, 0, 0, 8, 0, 0, false, false);
        _canvas = new LwjglCanvas(settings, canvasRenderer);
        _root = new Node("root");
        Box box = new Box("box", Vector3.ZERO, 1, 1, 1);
        _root.attachChild(box);
        while (!_canvas.isClosing()) {
            _canvas.draw(null);
            Thread.yield();
        }
        ContextGarbageCollector.doFinalCleanup(_canvas.getCanvasRenderer().getRenderer());
        _canvas.close();
    }

    @Override
    public boolean renderUnto(Renderer renderer) {
        if (!_canvas.isClosing()) {
            renderer.draw(_root);
            return true;
        }
        return false;
    }

    @Override
    public PickResults doPick(Ray3 pickRay) {
        return null;
    }

}
