import controller.Controller;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

public class Main {
    @Test
    public void test1() {
        System.out.println(new Vector3f(4f, 5f, 6f).dot(new Vector3f(1f, 2f, 3f)));
    }

    @Test
    void test2() {
        Controller controller = new Controller();
        controller.run();
    }
}
