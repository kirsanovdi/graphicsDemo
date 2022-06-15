package graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class FrameBuffer {
    public final int slot;
    private final int FBO, framebufferTexture;
    private final int rectVAO, rectVBO;
    private final Shader shader;

    public FrameBuffer(Shader shader, int slot, int width, int height){
        this.slot = slot;
        this.shader = shader;

        FBO = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, FBO);

        framebufferTexture = glGenTextures();
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, framebufferTexture);


        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, (double[]) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, framebufferTexture, 0);

        int RBO = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, RBO);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, RBO);

        final float[] rectangleVertices = new float[]{
                -1.0f,  1.0f,  0.0f,  1.0f,
                -1.0f, -1.0f,  0.0f,  0.0f,
                1.0f, -1.0f,  1.0f,  0.0f,

                -1.0f,  1.0f,  0.0f,  1.0f,
                1.0f, -1.0f,  1.0f,  0.0f,
                1.0f,  1.0f,  1.0f,  1.0f,
        };

        rectVAO = glGenVertexArrays();
        rectVBO = glGenBuffers();
        glBindVertexArray(rectVAO);
        glBindBuffer(GL_ARRAY_BUFFER, rectVBO);
        glBufferData(GL_ARRAY_BUFFER, rectangleVertices, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 16, 8);

        shader.activate();
        glUniform1i(glGetUniformLocation(shader.getId(), "screenTexture"), slot);
    }

    public void hookOutput(){
        glBindFramebuffer(GL_FRAMEBUFFER, FBO);
    }

    public void releaseOutput(){
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void render(){
        shader.activate();
        glBindVertexArray(rectVAO);
        glDisable(GL_DEPTH_TEST);
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, framebufferTexture);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glEnable(GL_DEPTH_TEST);
    }
}
