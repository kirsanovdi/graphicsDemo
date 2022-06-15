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

        Shader shader = new Shader("src/main/resources/code/vertexShader", "src/main/resources/code/fragmentShader", "src/main/resources/code/geometryShader");
        Shader outlineShader = new Shader("src/main/resources/code/outlineVertexShader", "src/main/resources/code/outlineFragmentShader", "src/main/resources/code/outlineGeometryShader");
        Shader frameShader = new Shader("src/main/resources/code/frameVertexShader", "src/main/resources/code/frameFragmentShader");

        Texture textureMap = new Texture("src/main/resources/img/texturePack.png", 0, 4096, 4096);
        textureMap.texUnit(shader, "tex0");

        Texture reflectMap = new Texture("src/main/resources/img/texturePackSpecularMap.png", 1, 4096, 4096);
        reflectMap.texUnit(shader, "tex1");

        DataTranslation dataTranslation = new DataTranslation(engine);

        FrameBuffer frameBuffer = new FrameBuffer(frameShader, 2, width, height);

        while (!glfwWindowShouldClose(window) && controller.status == Mode.RUNNING) {
            controller.handleInput(window);

            frameBuffer.hookOutput();

            glClearColor(0.07f, 0.13f, 0.17f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer
            textureMap.bind();
            reflectMap.bind();

            dataTranslation.update(RenderingType.Texture);
            shader.activate();
            shader.transferCamera(camera);
            shader.translateLightPoints(engine.lightPoints);
            shader.translateAmbientLight(ambLight);
            dataTranslation.setupVAO();

            glStencilFunc(GL_ALWAYS, 1, 0xFF);
            glStencilMask(0xFF);

            glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(RenderingType.Texture), GL_UNSIGNED_INT, 0);

            glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
            glStencilMask(0x00);
            glDisable(GL_DEPTH_TEST);


            dataTranslation.update(RenderingType.Outline);
            outlineShader.activate();
            outlineShader.transferCamera(camera);
            glUniform1f(glGetUniformLocation(outlineShader.getId(), "outlining"), 0.05f);
            dataTranslation.setupVAO();

            glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(RenderingType.Outline), GL_UNSIGNED_INT, 0);

            glStencilMask(0xFF);
            glStencilFunc(GL_ALWAYS, 0, 0xFF);
            glEnable(GL_DEPTH_TEST);

            frameBuffer.releaseOutput();
            frameBuffer.render();

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
