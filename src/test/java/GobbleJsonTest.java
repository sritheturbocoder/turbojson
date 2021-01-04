import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GobbleJsonTest {

    @Test
    public void Test_if_code_coverage_badge_works(){
        GobbleJson gobbleJson = new GobbleJson();
        String input = "{ Name: Hello }";
        String output = gobbleJson.fromJson(input);
        Assertions.assertEquals(input, output);
    }
}