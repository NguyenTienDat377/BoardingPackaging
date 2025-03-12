package board.packaging;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AppTest 
{
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue(true);
    }

    @Test
    public static void main() {
        String input = "3 3 2\n" + // m, n, r
                      "1 2 3\n" +  // board row 1
                      "4 5 6\n" +  // board row 2
                      "7 8 9\n" +  // board row 3
                      "2 2 10\n" + // rectangle 1
                      "1 1 5\n";   // rectangle 2
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        // Run the main method
        String[] args = new String[]{};
        App.main(args);
    }
}
