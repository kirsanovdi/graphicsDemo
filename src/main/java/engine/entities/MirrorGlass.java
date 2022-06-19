package engine.entities;

import graphics.Mirror;
import org.joml.Vector3f;

public class MirrorGlass {
    public final Vector3f position;
    public final Vector3f normal;
    public final Vector3f a, b, c, d;
    public Mirror mirror;
    public MirrorGlass (Vector3f position, Vector3f normal, Vector3f a, Vector3f b, Vector3f c, Vector3f d){
        this.position = position;
        this.normal = normal;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
}
