package graphics.translateObjects;

import static org.lwjgl.opengl.GL46.*;

/**
 * Vertex Buffer Object, буфер вершин
 */
public class VBO {
    private final int id;
    private final DataTranslation dataTranslation;

    protected VBO(DataTranslation dataTranslation) {
        id = glGenBuffers();
        this.dataTranslation = dataTranslation;
    }

    protected void bindRefresh() {
        bind();
        refresh();
    }

    protected void refresh() {
        glBufferData(GL_ARRAY_BUFFER, dataTranslation.getCords(), GL_DYNAMIC_DRAW);
    }

    protected void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    protected void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    protected void delete() {
        glDeleteBuffers(id);
    }
}
