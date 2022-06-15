package graphics;

import engine.entities.LightPoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

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

    protected void transferCamera(Camera camera){
        camera.Matrix(45.0f, 0.1f, 10000.0f, this, "camMatrix");

        int camPos = glGetUniformLocation(getId(), "camPos");
        glUniform3fv(camPos, new float[]{camera.position.x, camera.position.y, camera.position.z});
    }

    protected void translateLightPoints(Set<LightPoint> lightPoints) {
        int lightSize = glGetUniformLocation(getId(), "lightSize");
        int lightColor = glGetUniformLocation(getId(), "lightColor");
        int lightPos = glGetUniformLocation(getId(), "lightPos");

        final int size = lightPoints.size();
        float[] cords = new float[size * 3], colors = new float[size * 4];
        int count = 0;
        for (LightPoint point : lightPoints) {
            colors[count * 4] = point.color.x;
            colors[count * 4 + 1] = point.color.y;
            colors[count * 4 + 2] = point.color.z;
            colors[count * 4 + 3] = point.color.w;
            cords[count * 3] = point.cord.x;
            cords[count * 3 + 1] = point.cord.y;
            cords[count * 3 + 2] = point.cord.z;
            count++;
        }

        glUniform1i(lightSize, size);
        glUniform4fv(lightColor, colors);
        glUniform3fv(lightPos, cords);
    }

    protected void translateAmbientLight(float ambLight) {
        int ambLightPos = glGetUniformLocation(getId(), "ambient");
        glUniform1f(ambLightPos, ambLight);
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
