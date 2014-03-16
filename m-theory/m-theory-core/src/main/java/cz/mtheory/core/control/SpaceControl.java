/**
 * 
 */
package cz.mtheory.core.control;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * M-theory project
 * 
 * @author Michal NkD Nikodim
 * @email michal.nikodim@gmail.com
 * @url http://code.google.com/p/m-theory/
 */
public class SpaceControl {

    private static final double[] speeds = new double[] { 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50 };

    private Camera _camera;
    private InputTrigger _keyTrigger;
    private InputTrigger _keySpeedUpTrigger;
    private InputTrigger _keySpeedDownTrigger;
    private InputTrigger _mouseTrigger;

    private double _mouseRotateSpeed = .005;
    private double _moveSpeed = 50;

    private int speedIndex = 18;
    private double _keyRotateSpeed = 2.25;
    private final Vector3 _v = new Vector3();
    private final Vector3 _vLeft = new Vector3();
    private final Vector3 _vUp = new Vector3();
    private final Vector3 _vDir = new Vector3();
    private final Matrix3 _m = new Matrix3();
    private MouseManager _mouseManager;

    private SpaceControl() {
        //
    }

    public double getMouseRotateSpeed() {
        return _mouseRotateSpeed;
    }

    public void setMouseRotateSpeed(final double speed) {
        _mouseRotateSpeed = speed;
    }

    public double getSpeed(){
        return speeds[speedIndex];
    }
    
    public double getActualMoveSpeed(){
        return _moveSpeed * speeds[speedIndex];
    }
    
    public double getMoveSpeed() {
        return _moveSpeed;
    }

    public void setMoveSpeed(final double speed) {
        _moveSpeed = speed;
    }

    public double getKeyRotateSpeed() {
        return _keyRotateSpeed;
    }

    public void setKeyRotateSpeed(final double speed) {
        _keyRotateSpeed = speed;
    }

    protected void rotateDir(final double r) {
        if (r != 0) {
            _m.fromAngleNormalAxis(r, _camera.getDirection());
            _m.applyPost(_camera.getLeft(), _vLeft);
            _m.applyPost(_camera.getUp(), _vUp);
            _camera.setAxes(_vLeft, _vUp, _camera.getDirection());
            _camera.normalize();
        }
    }

    protected void rotateLeft(final double r) {
        if (r != 0) {
            _m.fromAngleNormalAxis(r, _camera.getLeft());
            _m.applyPost(_camera.getDirection(), _vDir);
            _m.applyPost(_camera.getUp(), _vUp);
            _camera.setAxes(_camera.getLeft(), _vUp, _vDir);
            _camera.normalize();
        }
    }

    protected void rotateUp(final double r) {
        if (r != 0) {
            _m.fromAngleNormalAxis(r, _camera.getUp());
            _m.applyPost(_camera.getDirection(), _vDir);
            _m.applyPost(_camera.getLeft(), _vLeft);
            _camera.setAxes(_vLeft, _camera.getUp(), _vDir);
            _camera.normalize();
        }
    }

    private void move(final KeyboardState kb, final double tpf) {
        double moveFB = 0, moveLR = 0, moveUD = 0, rotDir = 0;
        if (kb.isDown(Key.W)) moveFB += 1;
        if (kb.isDown(Key.S)) moveFB -= 1;
        if (kb.isDown(Key.A)) moveLR += 1;
        if (kb.isDown(Key.D)) moveLR -= 1;
        if (kb.isDown(Key.R)) moveUD += 1;
        if (kb.isDown(Key.F)) moveUD -= 1;
        if (kb.isDown(Key.E)) rotDir += 1;
        if (kb.isDown(Key.Q)) rotDir -= 1;
        
        if (moveFB != 0 || moveLR != 0 || moveUD != 0) {
            _v.zero();
            double speed = _moveSpeed * speeds[speedIndex];
            _v.addLocal(_vDir.set(_camera.getDirection()).multiplyLocal(moveFB * speed * tpf));
            _v.addLocal(_vLeft.set(_camera.getLeft()).multiplyLocal(moveLR * speed * tpf));
            _v.addLocal(_vUp.set(_camera.getUp()).multiplyLocal(moveUD * speed * tpf));
            _v.addLocal(_camera.getLocation());
            _camera.setLocation(_v);
        }
        if (rotDir != 0) {
            rotateDir(rotDir * _keyRotateSpeed * speeds[speedIndex] * tpf);
        }
    }

    public static SpaceControl setupTriggers(final LogicalLayer layer, final MouseManager mouseManager, final Camera camera) {
        final SpaceControl control = new SpaceControl();
        control.setupKeyboardTriggers(layer);
        control.setupMouseTriggers(layer);
        control._camera = camera;
        control._mouseManager = mouseManager;
        return control;
    }

    public static void removeTriggers(final LogicalLayer layer, final SpaceControl control) {
        if (control._mouseTrigger != null) layer.deregisterTrigger(control._mouseTrigger);
        if (control._keyTrigger != null) layer.deregisterTrigger(control._keyTrigger);
        if (control._keySpeedUpTrigger != null) layer.deregisterTrigger(control._keySpeedUpTrigger);
        if (control._keySpeedDownTrigger != null) layer.deregisterTrigger(control._keySpeedDownTrigger);
    }

    public void setupKeyboardTriggers(final LogicalLayer layer) {
        
        final Predicate<TwoInputStates> keysNotHeld = new Predicate<TwoInputStates>() {
            Key[] keys = new Key[] { Key.LCONTROL, Key.LSHIFT, Key.LMENU, Key.RCONTROL, Key.RSHIFT, Key.RMENU};
            @Override
            public boolean apply(final TwoInputStates states) {
                for (final Key k : keys) {
                    if (states.getCurrent() != null && states.getCurrent().getKeyboardState().isDown(k)) {
                        return false;
                    }
                }
                return true;
            }
        };
        final Predicate<TwoInputStates> keysHeld = new Predicate<TwoInputStates>() {
            Key[] keys = new Key[] { Key.W, Key.A, Key.S, Key.D, Key.R, Key.F, Key.Q, Key.E};

            @Override
            public boolean apply(final TwoInputStates states) {
                for (final Key k : keys) {
                    if (states.getCurrent() != null && states.getCurrent().getKeyboardState().isDown(k)) {
                        return true;
                    }
                }
                return false;
            }
        };
        final TriggerAction moveAction = new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                SpaceControl.this.move(inputStates.getCurrent().getKeyboardState(), tpf);
            }
        };
        
        
        _keyTrigger = new InputTrigger(Predicates.and(keysNotHeld, keysHeld), moveAction);

        _keySpeedUpTrigger = new InputTrigger(new KeyPressedCondition(Key.NUMPADADD), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                speedIndex++;
                if (speedIndex == speeds.length) speedIndex = speeds.length - 1;
                System.out.println("SpaceControl - speed multiplicator = " + speeds[speedIndex]);
            }
        });
        _keySpeedDownTrigger = new InputTrigger(new KeyPressedCondition(Key.NUMPADSUBTRACT), new TriggerAction() {
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                speedIndex--;
                if (speedIndex == -1) speedIndex = 0;
                System.out.println("SpaceControl - speed multiplicator = " + speeds[speedIndex]);
            }
        });

        layer.registerTrigger(_keyTrigger);
        layer.registerTrigger(_keySpeedUpTrigger);
        layer.registerTrigger(_keySpeedDownTrigger);
    }

    public void setupMouseTriggers(final LogicalLayer layer) {
        final Predicate<TwoInputStates> dragged = Predicates.and(TriggerConditions.mouseMoved(), TriggerConditions.rightButtonDown());
        final TriggerAction dragAction = new TriggerAction() {
            // Test boolean to allow us to ignore first mouse event. First event can wildly vary based on platform.
            private boolean firstPing = true;
            @Override
            public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
                final MouseState mouse = inputStates.getCurrent().getMouseState();
                if (mouse.getDx() != 0 || mouse.getDy() != 0) {
                    if (!firstPing) {
                        SpaceControl.this.rotateUp(-mouse.getDx() * _mouseRotateSpeed);
                        SpaceControl.this.rotateLeft(-mouse.getDy() * _mouseRotateSpeed);
                        
                        _mouseManager.setPosition(mouse.getX() - mouse.getDx(), mouse.getY() - mouse.getDy());
                    } else {
                        firstPing = false;
                    }

                }
            }
        };
        _mouseTrigger = new InputTrigger(dragged, dragAction);
        layer.registerTrigger(_mouseTrigger);
    }

}
