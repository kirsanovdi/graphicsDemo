package graphics;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.opengl.GL46.glDeleteProgram;

/**
 * Шейдерная программа
 */
public class Shader {
    /**
     * id шейдерной программы
     */
    private int id;

    /**
     * Конструктор шейдера, в котором происходит компилящия шейдерной программы
     *
     * @param vertexString   GLSL код для вершинного шейдера
     * @param fragmentString GLSL код для фрагментного шейдера
     */
    Shader(String vertexString, String fragmentString) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexString);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentString);
        glCompileShader(fragmentShader);

        id = glCreateProgram();
        //System.out.println(id);
        glAttachShader(id, vertexShader);
        glAttachShader(id, fragmentShader);
        glLinkProgram(id);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    /**
     * Getter для id шейдера
     *
     * @return id шейдера
     */
    public int getId() {
        return id;
    }

    /**
     * Активация шейдерной программы
     */
    void activate() {
        glUseProgram(id);
    }

    /**
     * Удаление шейдерной программы
     */
    void delete() {
        glDeleteProgram(id);
    }
}
