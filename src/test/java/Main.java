import controller.Controller;
import org.junit.jupiter.api.Test;

public class Main {
    @Test
    public void test1() {
        System.out.println(1);
    }

    @Test
    void test2() {
        Controller controller = new Controller();
        controller.run();
    }
}
