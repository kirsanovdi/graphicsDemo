package graphics;

import engine.entities.MirrorGlass;
import org.joml.Vector3f;

import static graphics.Display.getSubWindowVertices;

public class Mirror {
    public final Camera camera;
    private final FrameBuffer frameBuffer;
    public final Shader shader;
    private final MirrorGlass mirrorGlass;

    private final int slot;
    public final float val;

    public Mirror(Shader shader, int slot, Camera mainCamera, MirrorGlass mirrorGlass, int width, int height){
        Vector3f mirrorCameraVec = new Vector3f(mirrorGlass.position).sub(mainCamera.position).reflect(mirrorGlass.normal);
        Vector3f mirrorCameraPos = new Vector3f(mirrorGlass.position).sub(mirrorCameraVec);

        this.camera = new Camera(1900, 1000, mirrorCameraPos, mirrorCameraVec);
        this.shader = shader;
        this.mirrorGlass = mirrorGlass;
        mirrorGlass.mirror = this;
        this.slot = slot;
        this.val = -getCenter().dot(getNormal());
        frameBuffer = new FrameBuffer(shader, slot, width, height, getSubWindowVertices(0f, 0f, 1f, 1f));
    }

    public Vector3f getCenter(){
        return new Vector3f(mirrorGlass.position);
    }

    public Vector3f getNormal(){
        return new Vector3f(mirrorGlass.normal);
    }

    public int getTVal(Camera mainCamera){
        return mainCamera.position.dot(mirrorGlass.normal) > val ? 1 : 0;
    }

    public void updateCamera(Camera mainCamera){
        Vector3f mirrorCameraVec = new Vector3f(mirrorGlass.position).sub(mainCamera.position).reflect(mirrorGlass.normal);
        Vector3f mirrorCameraPos = new Vector3f(mirrorGlass.position).sub(mirrorCameraVec);
        camera.setPos(mirrorCameraPos);
        camera.setVec(mirrorCameraVec);
    }

    public void render(Camera mainCamera, Runnable runnable){
        updateCamera(mainCamera);
        frameBuffer.renderSubFrame(runnable);
    }
}
