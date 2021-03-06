package graphics.translateObjects;

import graphics.RenderingType;

import static org.lwjgl.opengl.GL46.*;

/**
 * Element Buffer Object, буфер для индексов
 */
public class EBO {
    private final int id;
    private final DataTranslation dataTranslation;

    protected EBO(DataTranslation dataTranslation) {
        id = glGenBuffers();
        this.dataTranslation = dataTranslation;
    }

    protected void bindRefresh(RenderingType type) {
        bind();
        refresh(type);
    }

    protected void refresh(RenderingType type) {
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, dataTranslation.getIndices(type), GL_DYNAMIC_DRAW);
    }

    protected void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    protected void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    protected void delete() {
        glDeleteBuffers(id);
    }
}
