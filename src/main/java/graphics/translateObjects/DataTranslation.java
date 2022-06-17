package graphics.translateObjects;

import engine.Engine;
import engine.entities.Block;
import engine.entities.Line;
import graphics.RenderingType;
import org.joml.Vector3f;

public class DataTranslation {

    private final Engine engine;

    private final int[] indicesTextureRaw;
    private final int[] indicesOutlineRaw;
    private final float[] cordsRaw;

    private int sizeITexture = 0, sizeIOutline = 0, sizeC = 0, verticesCount = 0;


    private final VBO vertexBufferObject;
    private final VAO vertexArrayObject;
    private final EBO elementBufferObject;

    public DataTranslation(Engine engine) {
        this.engine = engine;
        indicesTextureRaw = new int[20000000];
        indicesOutlineRaw = new int[20000000];
        cordsRaw = new float[20000000];

        vertexArrayObject = new VAO();
        vertexBufferObject = new VBO(this);
        elementBufferObject = new EBO(this);
    }

    public void update(RenderingType renderingType) {
        reset();
        for (Block block : engine.blocks.values()) {
            transferBlock(block);
        }
        synchronized (engine.lines) {
            for (Line line : engine.lines) {
                transferLine(line);
            }
        }

        vertexArrayObject.bind();
        vertexBufferObject.bindRefresh();
        elementBufferObject.bindRefresh(renderingType);

        vertexArrayObject.LinkAttrib(vertexBufferObject, 0, 20, 0);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 1, 20, 12);

        vertexArrayObject.unbind();
        vertexBufferObject.unbind();
        elementBufferObject.unbind();
    }

    public void setupVAO() {
        vertexArrayObject.bind();
    }

    public void destroy() {
        vertexArrayObject.delete();
        vertexBufferObject.delete();
        elementBufferObject.delete();
    }

    public float[] getCords() {
        final float[] cords = new float[sizeC];
        System.arraycopy(cordsRaw, 0, cords, 0, sizeC);
        return cords;
    }

    public int[] getIndices(RenderingType type){
        return switch (type){
            case Texture -> getTextureIndices();
            case Outline -> getOutlineIndices();
            default -> null;
        };
    }

    private int[] getTextureIndices() {
        final int[] indices = new int[sizeITexture];
        System.arraycopy(indicesTextureRaw, 0, indices, 0, sizeITexture);
        return indices;
    }

    private int[] getOutlineIndices() {
        final int[] indices = new int[sizeIOutline];
        System.arraycopy(indicesOutlineRaw, 0, indices, 0, sizeIOutline);
        return indices;
    }

    /**
     * Добавление нового пакета данных в массиы значений и индексов
     *
     * @param cords   массив добавляемых значений вершин
     */
    private void transfer(float[] cords, int[] indicesTexture, int[] indicesOutline) {
        System.arraycopy(cords, 0, cordsRaw, sizeC, cords.length);
        System.arraycopy(indicesTexture, 0, indicesTextureRaw, sizeITexture, indicesTexture.length);
        System.arraycopy(indicesOutline, 0, indicesOutlineRaw, sizeIOutline, indicesOutline.length);
        sizeC += cords.length;
        sizeITexture += indicesTexture.length;
        sizeIOutline += indicesOutline.length;
    }

    private void reset() {
        sizeITexture = 0;
        sizeIOutline = 0;
        sizeC = 0;
        verticesCount = 0;
    }

    public int indicesSize(RenderingType type) {
        return switch (type){
            case Texture -> sizeITexture;
            case Outline -> sizeIOutline;
            default -> 0;
        };
    }

    /**
     * Преобразование и передача данных четырёхугольника(квадрата) в массивы индексов и значений вершин
     *
     * @param a  первая координата треугольника
     * @param b  вторая координата треугольника
     * @param c  третья координата треугольника
     * @param id id стороны
     */
    public void transferTriangle(Vector3f a, Vector3f b, Vector3f c, long id, boolean upper) {
        final float yId = (float) (id / 16L) / 16.0f;
        final float xId = (float) (id % 16L) / 16.0f;
        final float delta = 1.0f / 16.0f;

        final float[] tempCordsRaw = upper ? new float[]{
                a.x, a.y, a.z, xId, yId,
                b.x, b.y, b.z, xId, yId + delta,
                c.x, c.y, c.z, xId + delta, yId + delta,
        } : new float[]{
                a.x, a.y, a.z, xId + delta, yId + delta,
                b.x, b.y, b.z, xId + delta, yId,
                c.x, c.y, c.z, xId, yId,
        };
        final int[] tempIndicesTextureRaw = new int[]{
                verticesCount, verticesCount + 2, verticesCount + 1,
        };
        final int[] tempIndicesOutlineRaw = new int[]{
                verticesCount, verticesCount + 2, verticesCount + 1,
        };
        verticesCount += 3;
        transfer(tempCordsRaw, tempIndicesTextureRaw, tempIndicesOutlineRaw);
    }
    //1 3 2 a, b, c
    //1 4 3 a, d, b

    public void transferSquare(Vector3f a, Vector3f b, Vector3f c, Vector3f d, long id) {
        transferTriangle(a, b, c, id, true);
        transferTriangle(c, d, a, id, false);
    }

    /**
     * Преобразование и передача данных отрезка в массивы индексов и значений вершин
     *
     * @param line прямая(отрезок)
     */
    private void transferLine(Line line) {
        final float delta = 0.001f;
        Vector3f startUp = new Vector3f(line.start).add(0.0f, delta, 0.0f);
        Vector3f startDown = new Vector3f(line.start).add(0.0f, -delta, 0.0f);
        Vector3f endUp = new Vector3f(line.end).add(0.0f, delta, 0.0f);
        Vector3f endDown = new Vector3f(line.end).add(0.0f, -delta, 0.0f);
        transferSquare(startUp, startDown, endDown, endUp, 17);
        transferSquare(endUp, endDown, startDown, startUp, 17);
    }

    /**
     * Преобразование и передача данных блока в массивы индексов и значений вершин
     *
     * @param block блок(куб)
     */
    public void transferBlock(Block block) {
        final float delta = 1.0f / 2.0f;
        final Vector3f center = new Vector3f(block.cord).add(delta, delta, delta);
        final Vector3f[] vertex = new Vector3f[]{
                new Vector3f(center.x - delta, center.y - delta, center.z - delta),//0 - far down left
                new Vector3f(center.x + delta, center.y - delta, center.z - delta),//1 - far down right
                new Vector3f(center.x - delta, center.y + delta, center.z - delta),//2 - far up left
                new Vector3f(center.x + delta, center.y + delta, center.z - delta),//3 - far up right
                new Vector3f(center.x - delta, center.y - delta, center.z + delta),//4 - near down left
                new Vector3f(center.x + delta, center.y - delta, center.z + delta),//5 - near down right
                new Vector3f(center.x - delta, center.y + delta, center.z + delta),//6 - near up left
                new Vector3f(center.x + delta, center.y + delta, center.z + delta) //7 - near up right
        };
        if (block.sideRender[0]) transferSquare(vertex[4], vertex[6], vertex[7], vertex[5], block.sideIds[0]);//near
        if (block.sideRender[1]) transferSquare(vertex[6], vertex[2], vertex[3], vertex[7], block.sideIds[1]);//up
        if (block.sideRender[2]) transferSquare(vertex[0], vertex[4], vertex[5], vertex[1], block.sideIds[2]);//down
        if (block.sideRender[3]) transferSquare(vertex[1], vertex[3], vertex[2], vertex[0], block.sideIds[3]);//far
        if (block.sideRender[4]) transferSquare(vertex[0], vertex[2], vertex[6], vertex[4], block.sideIds[4]);//left
        if (block.sideRender[5]) transferSquare(vertex[5], vertex[7], vertex[3], vertex[1], block.sideIds[5]);//right
    }
}

