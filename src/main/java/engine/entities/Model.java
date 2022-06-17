package engine.entities;

import controller.Commands;
import graphics.Camera;
import org.joml.Vector3f;

import java.util.Set;

import static controller.Commands.*;

/**
 * Модель для движка
 */
public class Model {
    /**
     * Камера
     */
    private Camera camera;
    /**
     * Координаты модели
     */
    private Vector3f position;

    private final float g = 0.00098f;
    private float speed;
    private final Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
    private final float modelHeight = 1.5f;

    /**
     * Констуктор модели для движка
     *
     * @param position координаты позиции
     * @param speed    изначальная скорость передвижения
     * @param camera   камера
     */
    public Model(Vector3f position, float speed, Camera camera) {
        this.position = new Vector3f(position);
        this.speed = speed;
        this.camera = camera;
    }


    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public Vector3f getCameraPosition() {
        return new Vector3f(position).add(0f, modelHeight, 0f);
    }

    public Vector3f getOrientation() {
        return new Vector3f(camera.orientation).normalize();
    }

    public void handleInput(Set<Commands> commandsSet) {
        Vector3f orientation = getOrientation();
        //orientation.y = 0f;
        orientation.normalize();

        if (commandsSet.contains(FORWARD)) moveForward(orientation);
        if (commandsSet.contains(BACKWARD)) moveBackward(orientation);
        if (commandsSet.contains(LEFT)) moveLeft(orientation);
        if (commandsSet.contains(RIGHT)) moveRight(orientation);

        if (commandsSet.contains(SPEED_1)) speed = 0.5f;
        if (commandsSet.contains(SPEED_01)) speed = 0.05f;
        if (commandsSet.contains(SPEED_0025)) speed = 0.0125f;
        camera.setPos(new Vector3f(position).add(0f, modelHeight, 0f));
    }


    private void moveForward(Vector3f movingOrientation) {
        final Vector3f delta = new Vector3f(movingOrientation).mul(speed);
        position.add(delta);
    }

    private void moveBackward(Vector3f movingOrientation) {
        final Vector3f delta = new Vector3f(movingOrientation).mul(-speed);
        position.add(delta);
    }

    private void moveRight(Vector3f movingOrientation) {
        final Vector3f delta = new Vector3f(movingOrientation).cross(up).normalize().mul(speed);
        position.add(delta);
    }

    private void moveLeft(Vector3f movingOrientation) {
        final Vector3f delta = new Vector3f(movingOrientation).cross(up).normalize().mul(-speed);
        position.add(delta);
    }

}
