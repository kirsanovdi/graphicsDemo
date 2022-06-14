package engine.entities;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class LightPoint {
    /**
     * Координата источника света
     */
    public Vector3f cord;
    /**
     * Цвет источника света
     */
    public Vector4f color;

    /**
     * Конструктор источника света
     *
     * @param cord  координата источника света
     * @param color цвет источника света
     */
    public LightPoint(Vector3f cord, Vector4f color) {
        this.cord = cord;
        this.color = color;
    }
}
