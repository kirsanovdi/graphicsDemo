package graphics;

import controller.Controller;
import controller.Mode;
import engine.Engine;
import graphics.translateObjects.DataTranslation;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Display {
    public final int width;
    public final int height;

    public final Camera camera;
    //public final Camera camera2;
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
        //this.camera2 = new Camera(width,height, new Vector3f(0.0f, 2.0f, 25.0f));
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

    public static float[] getSubWindowVertices(float centerX, float centerY, float rangeX, float rangeY) {
        return new float[]{
                centerX - rangeX, centerY + rangeY, 0.0f, 1.0f,
                centerX - rangeX, centerY - rangeY, 0.0f, 0.0f,
                centerX + rangeX, centerY - rangeY, 1.0f, 0.0f,

                centerX - rangeX, centerY + rangeY, 0.0f, 1.0f,
                centerX + rangeX, centerY - rangeY, 1.0f, 0.0f,
                centerX + rangeX, centerY + rangeY, 1.0f, 1.0f,
        };
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

        final Shader shader = new Shader("vertexShader", "fragmentShader", "geometryShader");
        final Shader mirrorShader = new Shader("vertexShader", "mirrorFragmentShader", "geometryShader");
        final Shader outlineShader = new Shader("outlineVertexShader", "outlineFragmentShader", "outlineGeometryShader");
        final Shader frameShader = new Shader("frameVertexShader", "frameFragmentShader");
        final Shader frameShader2 = new Shader("frameVertexShader", "frameFragmentShader");
        final Shader frameShader3 = new Shader("frameVertexShader", "frameFragmentShader");
        final Shader frameShader4 = new Shader("frameVertexShader", "frameFragmentShader");
        final Shader testFrameShader = new Shader("frameVertexShader", "testFrameFragmentShader");

        final Texture textureMap = new Texture("texturePack.png", 0, 4096, 4096);
        textureMap.texUnit(shader, "tex0");
        textureMap.texUnit(mirrorShader, "tex0");

        final Texture reflectMap = new Texture("texturePackSpecularMap.png", 1, 4096, 4096);
        reflectMap.texUnit(shader, "tex1");
        reflectMap.texUnit(mirrorShader, "tex1");

        final DataTranslation dataTranslation = new DataTranslation(engine);

        final FrameBuffer mainFrame = new FrameBuffer(frameShader, 2, width, height, getSubWindowVertices(0f, 0f, 1f, 1f));
        final FrameBuffer secondaryFrameUR = new FrameBuffer(testFrameShader, 3, width, height, getSubWindowVertices(0.75f, 0.75f, 0.25f, 0.25f));
        final FrameBuffer secondaryFrame = new FrameBuffer(frameShader2, 4, width, height, getSubWindowVertices(0f, 0f, 1f, 1f));
        final FrameBuffer secondaryFrame2 = new FrameBuffer(frameShader3, 5, width, height, getSubWindowVertices(0f, 0f, 1f, 1f));
        final FrameBuffer secondaryFrame3 = new FrameBuffer(frameShader4, 6, width, height, getSubWindowVertices(0f, 0f, 1f, 1f));

        final Vector3f mirror = new Vector3f(0.0f, 1.0f, 0.0f);
        final Vector3f mirrorNormal = new Vector3f(1.0f, 0.0f, 0.0f);

        while (!glfwWindowShouldClose(window) && controller.status == Mode.RUNNING) {
            controller.handleInput(window);

            Vector3f mirrorCameraVec = new Vector3f(mirror).sub(camera.position).reflect(mirrorNormal);
            Vector3f mirrorCameraPos = new Vector3f(mirror).sub(mirrorCameraVec);

            secondaryFrame.renderSubFrame(() ->{
                glClearColor(0.2f, 0.3f, 0.5f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer
                textureMap.bind();
                reflectMap.bind();

                dataTranslation.update(RenderingType.Texture);
                shader.activate();
                shader.transferCamera(new Camera(1900, 1000, mirrorCameraPos, mirrorCameraVec));
                shader.translateLightPoints(engine.lightPoints);
                shader.translate1f("ambient", ambLight);
                dataTranslation.setupVAO();

                glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(RenderingType.Texture), GL_UNSIGNED_INT, 0);
            });

            secondaryFrame2.renderSubWindow(() -> {
                glClearColor(0.2f, 0.3f, 0.5f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer
                textureMap.bind();
                reflectMap.bind();

                dataTranslation.update(RenderingType.Texture);
                mirrorShader.activate();
                glUniform1i(glGetUniformLocation(mirrorShader.getId(), "mirrorTex"), 4);
                mirrorShader.transferCamera(new Camera(1900, 1000, mirrorCameraPos, mirrorCameraVec));
                mirrorShader.translateLightPoints(engine.lightPoints);
                mirrorShader.translate1f("ambient", ambLight);
                dataTranslation.setupVAO();

                glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(RenderingType.Texture), GL_UNSIGNED_INT, 0);
            });

            secondaryFrame3.renderSubWindow(() -> {
                glClearColor(0.2f, 0.3f, 0.5f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer
                textureMap.bind();
                reflectMap.bind();

                dataTranslation.update(RenderingType.Texture);
                mirrorShader.activate();
                glUniform1i(glGetUniformLocation(mirrorShader.getId(), "mirrorTex"), 5);
                mirrorShader.transferCamera(new Camera(1900, 1000, mirrorCameraPos, mirrorCameraVec));
                mirrorShader.translateLightPoints(engine.lightPoints);
                mirrorShader.translate1f("ambient", ambLight);
                dataTranslation.setupVAO();

                glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(RenderingType.Texture), GL_UNSIGNED_INT, 0);
            });

            mainFrame.renderSubWindow(() -> {
                glClearColor(0.2f, 0.3f, 0.5f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer
                textureMap.bind();
                reflectMap.bind();

                dataTranslation.update(RenderingType.Texture);
                mirrorShader.activate();
                glUniform1i(glGetUniformLocation(mirrorShader.getId(), "mirrorTex"), 6);
                mirrorShader.transferCamera(camera);
                mirrorShader.translateLightPoints(engine.lightPoints);
                mirrorShader.translate1f("ambient", ambLight);
                dataTranslation.setupVAO();

                glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(RenderingType.Texture), GL_UNSIGNED_INT, 0);
            });

            secondaryFrameUR.renderSubWindow(() -> {
                glClearColor(0.7f, 0.7f, 0.7f, 1.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // clear the framebuffer
                textureMap.bind();
                reflectMap.bind();

                dataTranslation.update(RenderingType.Texture);
                shader.activate();
                shader.transferCamera(new Camera(1900,1000, mirrorCameraPos, mirrorCameraVec));
                shader.translateLightPoints(engine.lightPoints);
                shader.translate1f("ambient", 0.4f);
                dataTranslation.setupVAO();

                glDrawElements(GL_TRIANGLES, dataTranslation.indicesSize(RenderingType.Texture), GL_UNSIGNED_INT, 0);
            });

            glfwSwapBuffers(window);
            glfwPollEvents();
            printRenderTime();
        }

        dataTranslation.destroy();
        textureMap.delete();
        reflectMap.delete();
        shader.delete();

    }
}
