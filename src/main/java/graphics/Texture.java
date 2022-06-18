package graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.opengl.GL46.glDeleteTextures;
import static org.lwjgl.opengl.GL46.glActiveTexture;
import static org.lwjgl.opengl.GL46.glGetUniformLocation;
import static org.lwjgl.opengl.GL46.glUniform1i;
import static org.lwjgl.opengl.GL46.glGenerateMipmap;

/**
 * Текстура(карта текстур) для отображения
 */
public class Texture {
    /**
     * Идентификатор текстуры
     */
    private final int texture;
    /**
     * Слот ячейки текстуры
     */
    private final int slot;

    /**
     * Конструктор текстуры
     *
     * @param path   путь к файлу .png с текстурой
     * @param slot   слот ячейки текстуры
     * @param width  ширина текстуры в пикселях
     * @param height высота текстуры в пикселях
     */
    Texture(String path, int slot, int width, int height) {
        this.slot = slot;
        texture = glGenTextures();
        //System.out.println(texture);
        glActiveTexture(GL_TEXTURE0 + slot);

        bind();

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        try {
            File imgPath = new File("src/main/resources/img/" + path);
            BufferedImage image = ImageIO.read(imgPath);

            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
            ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

            for (int h = 0; h < image.getHeight(); h++) {
                for (int w = 0; w < image.getWidth(); w++) {
                    int pixel = pixels[(image.getHeight() - 1 - h) * image.getWidth() + w];

                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            buffer.flip();
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
            System.out.println("Texture (capacity = " + buffer.capacity() + ") loaded");
        } catch (Exception e) {
            System.out.println("Unable to load " + path + " texture");
            //System.out.println(e);
        }

        glGenerateMipmap(GL_TEXTURE_2D);

        unbind();
    }

    /**
     * Активация шейдера и задание слота для используемой текстуры
     *
     * @param shader  шейдерная программв
     * @param uniform uniform для tex0Uni
     */
    public void texUnit(Shader shader, String uniform) {
        int tex0Uni = glGetUniformLocation(shader.getId(), uniform);
        shader.activate();
        glUniform1i(tex0Uni, slot);
    }

    /**
     * Активация текстуры и задание её как GL_TEXTURE_2D для шейдерной программы
     */
    public void bind() {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    /**
     * Задание GL_TEXTURE_2D как 0
     */
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Удаление текстуры
     */
    public void delete() {
        glDeleteTextures(texture);
    }
}

