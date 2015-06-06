import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.junit.Test;

public class MinecraftPiProtocolTest {

  @Test
  public void testChat() throws Exception {
    Socket s = new Socket("127.0.0.1", 4711);
    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    bufferedWriter.write(String.format("chat.post(my-first-message-%d)\n", System.currentTimeMillis()));
    bufferedWriter.flush();
    s.close();
  }
}
