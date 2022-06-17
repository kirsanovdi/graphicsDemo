package engine.entities;

import graphics.Camera;
import graphics.Shader;

public interface Entity {
    void drawIndividually(Shader shader, Camera camera);

    void draw();
}
