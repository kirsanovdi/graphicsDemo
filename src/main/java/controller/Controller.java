package controller;

import engine.Engine;
import engine.entities.Model;
import graphics.Camera;
import graphics.Display;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static controller.Commands.*;
import static org.lwjgl.glfw.GLFW.*;

public class Controller {
    private final Engine engine;
    private final Display display;
    private final Camera camera;
    private final Model model;
    private final Set<Commands> commandsSet;
    private final Map<Commands, Boolean> commandsHashSet;
    public Mode status;

    public Controller() {
        engine = new Engine(this, Engine.generateBlockLayer(new Vector3i(0, -2, 0), 25));
        camera = new Camera(1900, 1000, new Vector3f(0.0f, 0.0f, 2.0f));
        display = new Display(this, engine, camera, 1900, 1000, "test");
        model = new Model(new Vector3f(0f, 0f, 0f), 0.5f, camera);
        commandsSet = new HashSet<>();
        commandsHashSet = new HashMap<>();
        status = Mode.BEFORELAUNCH;

        commandsHashSet.put(REMOVE, true);
        commandsHashSet.put(ADD, true);
        commandsHashSet.put(JUMP, true);
        commandsHashSet.put(START_DEBUG, true);
        commandsHashSet.put(END_DEBUG, true);
        commandsHashSet.put(PLACE_LIGHT, true);
        commandsHashSet.put(REMOVE_LIGHT, true);
        commandsHashSet.put(CREATE_LEVEL, true);
        commandsHashSet.put(ID0, true);
        commandsHashSet.put(ID1, true);
        commandsHashSet.put(ID2, true);
        commandsHashSet.put(ID3, true);
    }

    public void run() {
        status = Mode.RUNNING;
        Thread thread = new Thread(engine::run);
        thread.start();
        display.run();
    }

    private void lockKey(Commands command) {
        commandsHashSet.put(command, false);
    }

    private void unlockKey(Commands command) {
        commandsHashSet.put(command, true);
    }

    private boolean getKeyValue(Commands command) {
        return commandsHashSet.get(command);
    }

    private void keyHandler(long window, Commands command, int key) {
        if (getKeyValue(command) && glfwGetKey(window, key) == GLFW_PRESS) {
            commandsSet.add(command);
            lockKey(command);
        }
        if (!getKeyValue(command) && glfwGetKey(window, key) == GLFW_RELEASE) unlockKey(command);
    }

    public void handleInput(long window) {
        commandsSet.clear();

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) commandsSet.add(FORWARD);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) commandsSet.add(BACKWARD);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) commandsSet.add(LEFT);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) commandsSet.add(RIGHT);

        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) commandsSet.add(SPEED_1);
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_RELEASE) commandsSet.add(SPEED_01);
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) commandsSet.add(SPEED_0025);

        if (glfwGetKey(window, GLFW_KEY_EQUAL) == GLFW_PRESS) display.increaseAmbLight();
        if (glfwGetKey(window, GLFW_KEY_MINUS) == GLFW_PRESS) display.decreaseAmbLight();

        camera.mouseInput(window);
        model.handleInput(commandsSet);

        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) status = Mode.FINISHED;
    }
}
