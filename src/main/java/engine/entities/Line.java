package engine.entities;

import org.joml.Vector3f;

/**
 * Прямая(отрезок), заданная точками начала и конца
 */
public class Line {

    /**
     * Точка начала
     */
    public Vector3f start;

    /**
     * Точка конца
     */
    public Vector3f end;

    /**
     * Конструктор прямой(отрезка)
     *
     * @param start точка начало
     * @param end   точка конца
     */
    public Line(Vector3f start, Vector3f end) {
        this.start = start;
        this.end = end;
    }
}
