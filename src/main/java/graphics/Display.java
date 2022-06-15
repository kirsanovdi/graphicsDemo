package graphics;

import controller.Controller;
import controller.Mode;
import engine.Engine;
import graphics.translateObjects.DataTranslation;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Set;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import engine.entities.LightPoint;


public class Display {
    public final int width;
    public final int height;

    public final Camera camera;
    public final Engine engine;
    public final Controller controller;

    private final String name;
    private long window;
    private double frames, lastTime;
    private float ambLight;

    private void printRenderTime() {
        frames++;
        final double currentTime = glfwGetTime();
        if (currentTime - lastTime > 2.0) {
            System.out.println("gui \t" + frames / 2.0);
            lastTime = currentTime;
            frames = 0;
        }
    }

    public void increaseAmbLight() {
        if (ambLight <= 0.1f) {
            ambLight += 0.001;
        }
    }

    public void decreaseAmbLight() {
        if (ambLight >= 0.0f) {
            ambLight -= 0.001;
        }
    }

    public Display(Controller controller, Engine engine, Camera camera, int width, int height, String name) {
        this.height = height;
        this.width = width;
        this.name = name;
        this.camera = camera;
        this.engine = engine;
        this.controller = controller;
    }

    public void run() {
        System.out.println("GraphicsDisplay has launched with LWJGL " + Version.getVersion());

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {

        // Set up an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(width, height, name, NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");


        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (videoMode == null) throw new RuntimeException("Failed ti get resolution of the primary monitor");

            // Center the window
            glfwSetWindowPos(
                    window,
                    (videoMode.width() - pWidth.get(0)) / 2,
                    (videoMode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void translateLightPoints(Shader shader) {
        int lightSize = glGetUniformLocation(shader.getId(), "lightSize");
        int lightColor = glGetUniformLocation(shader.getId(), "lightColor");
        int lightPos = glGetUniformLocation(shader.getId(), "lightPos");

        Set<LightPoint> lightPoints = engine.lightPoints;
        final int size = lightPoints.size();
        float[] cords = new float[size * 3], colors = new float[size * 4];
        int count = 0;
        for (LightPoint point : lightPoints) {
            colors[count * 4] = point.color.x;
            colors[count * 4 + 1] = point.color.y;
            colors[count * 4 + 2] = point.color.z;
            colors[count * 4 + 3] = point.color.w;
            cords[count * 3] = point.cord.x;
            cords[count * 3 + 1] = point.cord.y;
            cords[count * 3 + 2] = point.cord.z;
            count++;
        }

        glUniform1i(lightSize, size);
        glUniform4fv(lightColor, colors);
        glUniform3fv(lightPos, cords);
    }

    private void translateAmbientLight(Shader shader) {
        int ambLightPos = glGetUniformLocation(shader.getId(), "ambient");
        glUniform1f(ambLightPos, ambLight);
    }

    /**
     * Метод, содержащий цикл отрисовки окна
     */
    private void loop() {
        GL.createCapabilities();
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_STENCIL_TEST);
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

        Shader shader = new Shader("src/main/resources/code/vertexShader", "src/main/resources/code/fragmentShader");
        Shader outline = new Shader("src/main/resources/code/outlineVertexShader", "src/main/resources/code/outlineFragmentShader");

        Texture textureMap = new Texture("src/main/resources/img/texturePack.png", 0, 4096, 4096);
        textureMap.texUnit(shader, "tex0");

        Texture reflectMap = new Texture("src/main/resources/img/texturePackSpecularMap.png", 1, 4096, 4096);
        reflectMap.texUnit(shader, "tex1");


        DataTranslation dataTranslation = new DataTranslation(engine);

        while (!glfwWindowShouldClose(window) && controller.status == Mode.RUNNING) {

            dataTranslation.update();


            glClearColor(0.07f, 0.13f, 0.17f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer

            shader.activate();

            controller.handleInput(window);
            camera.Matrix(45.0f, 0.1f, 10000.0f, shader, "camMatrix");

            translateLightPoints(shader);
            translateAmbientLight(shader);

            int camPos = glGetUniformLocation(shader.getId(), "camPos");
            glUniform3fv(camPos, new float[]{camera.position.x, camera.position.y, camera.position.z});


            textureMap.bind();
            reflectMap.bind();
            dataTranslation.setupVAO();

            glStencilFunc(GL_ALWAYS, 1, 0xFF);
            glStencilMask(0xFF);
            glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(), GL_UNSIGNED_INT, 0);

            glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
            glStencilMask(0x00);
            glDisable(GL_DEPTH_TEST);

            outline.activate();
            dataTranslation.setupVAO();

            camera.Matrix(45.0f, 0.1f, 10000.0f, outline, "camMatrix");
            glUniform3fv(glGetUniformLocation(outline.getId(), "camPos"), new float[]{camera.position.x, camera.position.y, camera.position.z});
            glUniform1f(glGetUniformLocation(outline.getId(), "outlining"), 0.05f);

            glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(), GL_UNSIGNED_INT, 0);

            glStencilMask(0xFF);
            glStencilFunc(GL_ALWAYS, 0, 0xFF);
            glEnable(GL_DEPTH_TEST);

            glfwSwapBuffers(window);
            glfwPollEvents();
            //printRenderTime();
        }

        dataTranslation.destroy();
        textureMap.delete();
        reflectMap.delete();
        shader.delete();

    }
}
