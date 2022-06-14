package graphics.translateObjects;


import engine.Engine;
import engine.entities.Block;
import engine.entities.Line;
import graphics.translateObjects.EBO;
import graphics.translateObjects.VAO;
import graphics.translateObjects.VBO;
import org.joml.Vector3f;

public class DataTranslation {

    private final Engine engine;

    private final int[] indicesRaw;
    private final float[] cordsRaw;

    private int sizeI = 0, sizeC = 0, verticesCount = 0;


    private final VBO vertexBufferObject;
    private final VAO vertexArrayObject;
    private final EBO elementBufferObject;

    public DataTranslation(Engine engine) {
        this.engine = engine;
        indicesRaw = new int[20000000];
        cordsRaw = new float[20000000];

        vertexArrayObject = new VAO();
        vertexArrayObject.bind();
        vertexBufferObject = new VBO(this);
        elementBufferObject = new EBO(this);

        vertexBufferObject.bindRefresh();
        elementBufferObject.bindRefresh();

        vertexArrayObject.LinkAttrib(vertexBufferObject, 0, 32, 0);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 1, 32, 12);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 2, 32, 20);

        vertexArrayObject.unbind();
        vertexBufferObject.unbind();
        elementBufferObject.unbind();
    }

    public void update() {
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
        elementBufferObject.bindRefresh();

        vertexArrayObject.LinkAttrib(vertexBufferObject, 0, 32, 0);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 1, 32, 12);
        vertexArrayObject.LinkAttrib(vertexBufferObject, 2, 32, 20);

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

    public int[] getIndices() {
        final int[] indices = new int[sizeI];
        System.arraycopy(indicesRaw, 0, indices, 0, sizeI);
        return indices;
    }

    /**
     * Добавление нового пакета данных в массиы значений и индексов
     *
     * @param cords   массив добавляемых значений вершин
     * @param indices массив добавляемых индексов
     */
    private void transfer(float[] cords, int[] indices) {
        System.arraycopy(cords, 0, cordsRaw, sizeC, cords.length);
        System.arraycopy(indices, 0, indicesRaw, sizeI, indices.length);
        sizeC += cords.length;
        sizeI += indices.length;
    }

    private void reset() {
        sizeI = 0;
        sizeC = 0;
        verticesCount = 0;
    }

    public int indicesSize() {
        return sizeI;
    }

    /**
     * Преобразование и передача данных четырёхугольника(квадрата) в массивы индексов и значений вершин
     *
     * @param a  первая координата квадрата
     * @param b  вторая координата квадрата
     * @param c  третья координата квадрата
     * @param d  четвёртая координата квадрата
     * @param id id стороны
     */
    public void transferSquare(Vector3f a, Vector3f b, Vector3f c, Vector3f d, long id) {
        final float yId = (float) (id / 16L) / 16.0f;
        final float xId = (float) (id % 16L) / 16.0f;
        final float delta = 1.0f / 16.0f;

        final float kX = (b.y - a.y) * (c.z - a.z) - (c.y - a.y) * (b.z - a.z);
        final float kY = (c.x - a.x) * (b.z - a.z) - (b.x - a.x) * (c.z - a.z);
        final float kZ = (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y);

        final float[] tempCordsRaw = new float[]{
                a.x, a.y, a.z, xId, yId, kX, kY, kZ,
                b.x, b.y, b.z, xId, yId + delta, kX, kY, kZ,
                c.x, c.y, c.z, xId + delta, yId + delta, kX, kY, kZ,
                d.x, d.y, d.z, xId + delta, yId, kX, kY, kZ
        };
        final int[] tempIndicesRaw = new int[]{
                verticesCount, verticesCount + 2, verticesCount + 1,
                verticesCount, verticesCount + 3, verticesCount + 2
        };
        verticesCount += 4;
        transfer(tempCordsRaw, tempIndicesRaw);
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

