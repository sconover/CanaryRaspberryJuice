import net.canarymod.Main;
import net.minecraft.server.MinecraftServer;
import org.junit.Test;

public class IntegrationTest {
  @Test
  public void runServer() throws Exception {
    System.setProperty("java.awt.headless", "true");
    MinecraftServer minecraftServer = Main.doMain(new String[] {});
    while (!minecraftServer.isRunning()) {
      System.out.printf("starting up, i think");
      Thread.sleep(1000);
    }
    while (minecraftServer.isRunning()) {
      System.out.printf("running, i think");
      Thread.sleep(1000);
    }
  }
}
