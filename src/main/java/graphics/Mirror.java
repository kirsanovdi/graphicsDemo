package graphics;

import org.joml.Vector3f;

import static graphics.Display.getSubWindowVertices;

public class Mirror {
    private final Camera camera;
    private final FrameBuffer frameBuffer;
    private final Shader shader;
    private final Vector3f position, normal;

    private final int slot;

    public Mirror(Shader shader, int slot, Camera camera, Vector3f position, Vector3f normal, int width, int height){
        this.camera = camera;
        this.shader = shader;
        this.position = position;
        this.normal = normal;
        this.slot = slot;
        frameBuffer = new FrameBuffer(shader, slot, width, height, getSubWindowVertices(0f, 0f, 1f, 1f));
    }
}
