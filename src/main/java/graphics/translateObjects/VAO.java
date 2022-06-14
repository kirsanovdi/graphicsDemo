package graphics.translateObjects;

import static org.lwjgl.opengl.GL46.*;

/**
 * Vertex Array Object, массив вершин
 */
public class VAO {
    private int id;

    protected VAO() {
        id = glGenVertexArrays();
    }

    protected void LinkAttrib(VBO vbo, int layout, int offset, int pointer) {
        vbo.bind();
        glVertexAttribPointer(layout, 4, GL_FLOAT, false, offset, pointer);
        glEnableVertexAttribArray(layout);
        vbo.unbind();
    }

    protected void bind() {
        glBindVertexArray(id);
    }

    protected void unbind() {
        glBindVertexArray(0);
    }

    protected void delete() {
        glDeleteVertexArrays(id);
    }
}
