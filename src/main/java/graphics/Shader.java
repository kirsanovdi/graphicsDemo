package graphics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
     * @param vertexSource   путь к файлу с GLSL кодом для вершинного шейдера
     * @param fragmentSource путь к файлу с GLSL кодом для фрагментного шейдера
     */
    Shader(String vertexSource, String fragmentSource) {

        String vertexCode = "", fragmentCode = "";
        try {
            vertexCode = Files.readString(new File(vertexSource).toPath());
            fragmentCode = Files.readString(new File(fragmentSource).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexCode);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentCode);
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
