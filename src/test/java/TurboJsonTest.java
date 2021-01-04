import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TurboJsonTest {

    @Test
    public void Test_code_coverage() {
        TurboJson turboJson = new TurboJson();
        String input = "json test";
        String output = turboJson.fromJson(input);
        Assertions.assertEquals(input,output);
    }
}
