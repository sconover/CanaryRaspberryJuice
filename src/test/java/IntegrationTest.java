import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.canarymod.Main;
import net.canarymod.exceptions.InvalidPluginException;
import net.canarymod.exceptions.PluginLoadFailedException;
import net.canarymod.plugin.DefaultPluginManager;
import net.canarymod.plugin.Plugin;
import net.canarymod.plugin.PluginDescriptor;
import net.canarymod.plugin.PluginLifecycle;
import net.canarymod.plugin.PluginManager;
import net.canarymod.plugin.PluginState;
import net.canarymod.plugin.lifecycle.JavaPluginLifecycle;
import net.canarymod.plugin.lifecycle.PluginLifecycleBase;
import net.canarymod.plugin.lifecycle.PluginLifecycleFactory;
import net.minecraft.server.MinecraftServer;
import net.visualillusionsent.utils.PropertiesFile;
import net.visualillusionsent.utils.UtilityException;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class IntegrationTest {
  @Test
  public void runServer() throws Exception {
    //check current working dir and make sure it's not the project root

    System.setProperty("java.awt.headless", "true");
    PluginLifecycleFactory.registerLifecycle("java", JavaPluginLifecycle.class);

    TestPluginDescriptor pluginDescriptor =
        new TestPluginDescriptor(
            new File("../../classes/production").getAbsolutePath(),
            new LinkedHashMap<String, String>(){{
              put("main-class", "mctest.hello.HelloPlugin");
              put("name", "HelloPlugin");
              put("author", "Steve Conover");
              put("version", "0.0.1");
              // put("enable-early", "true");
            }});

    TestPluginManager pluginManager = new TestPluginManager();
    pluginManager.putPluginDescriptor(pluginDescriptor);

    MinecraftServer minecraftServer = Main.doMain(new String[] {}, pluginManager);
    while (!minecraftServer.isRunning()) {
      System.out.printf("starting up, i think");
      Thread.sleep(1000);
    }
    //PluginLifecycleFactory.createLifecycle(new TestPluginDescriptor(null, null));
    while (minecraftServer.isRunning()) {

      // System.out.printf("running, i think");
      Thread.sleep(1000);
    }
  }

  // TODO: use a DefaultPluginManager, but override scanForPLugins, and reflectively add plugins to the (private) Map



  public static class TestPluginManager implements PluginManager {
    private final DefaultPluginManager inner;

    public TestPluginManager() {
      this.inner = new DefaultPluginManager();
    }

    @Override public void scanForPlugins() {
      // do nothing
    }

    public void putPluginDescriptor(PluginDescriptor pluginDescriptor) {
      try {
        Field field = DefaultPluginManager.class.getDeclaredField("plugins");
        field.setAccessible(true);
        Map<String, PluginDescriptor> plugins =
            (Map<String, PluginDescriptor>) field.get(inner);
        plugins.put(pluginDescriptor.getName(), pluginDescriptor);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override public boolean enablePlugin(String name) throws PluginLoadFailedException {
      return inner.enablePlugin(name);
    }

    @Override public void enableLatePlugins() {
      inner.enableLatePlugins();
    }

    @Override public void enableEarlyPlugins() {
      inner.enableEarlyPlugins();
    }

    @Override public boolean disablePlugin(String name) {
      return inner.disablePlugin(name);
    }

    @Override public void disableAllPlugins() {
      inner.disableAllPlugins();
    }

    @Override public void disableAllPlugins(Logger log) {
      inner.disableAllPlugins(log);
    }

    @Override public boolean reloadPlugin(String name)
        throws PluginLoadFailedException, InvalidPluginException {
      return inner.reloadPlugin(name);
    }

    @Override public Plugin getPlugin(String name) {
      return inner.getPlugin(name);
    }

    @Override public Collection<Plugin> getPlugins() {
      return inner.getPlugins();
    }

    @Override public Collection<String> getPluginNames() {
      return inner.getPluginNames();
    }

    @Override public Collection<PluginDescriptor> getPluginDescriptors() {
      return inner.getPluginDescriptors();
    }

    @Override public PluginDescriptor getPluginDescriptor(Plugin plugin) {
      return inner.getPluginDescriptor(plugin);
    }

    @Override public PluginDescriptor getPluginDescriptor(String plugin) {
      return inner.getPluginDescriptor(plugin);
    }
  }

  public static class TestPluginLifecycle extends PluginLifecycleBase {
    public TestPluginLifecycle(PluginDescriptor desc) {
      super(desc);
    }

    @Override public boolean enable() {
      System.out.println("enable!");
      return true;
    }

    @Override public boolean disable() {
      System.out.println("disable!");
      return true;
    }

    @Override public Plugin load() throws PluginLoadFailedException {
      System.out.println("load!");
      return null;
      // return super.load();
    }

    @Override public void unload() {
      // super.unload();
    }

    @Override protected void _load() throws PluginLoadFailedException {
      System.out.printf("LOAD");
      // throw new UnsupportedOperationException();
    }

    @Override protected void _unload() {
      // throw new UnsupportedOperationException();
    }
  }

  /**
   * Contents largely copied from PluginDescriptor. We only want slightly different behavior,
   * but there are scoping issues.
   *
   * Note that because all state and behavior is managed in this subclass, PluginDescriptor
   * basically becomes an interface.
   */
  public static class TestPluginDescriptor extends PluginDescriptor {
    private PropertiesFile canaryInf;
    private String path;
    private String name;
    private String version;
    private String author;
    private String language;
    private boolean enableEarly;
    private Plugin plugin;
    private PluginLifecycle pluginLifecycle;
    private String[] dependencies;
    private PluginState currentState;
    private int priority;
    private Map<String, String> canaryInfProperties;

    public TestPluginDescriptor(
        String targetDirectory,
        Map<String, String> canaryInfProperties) throws InvalidPluginException {
      super(targetDirectory);
      this.canaryInfProperties = canaryInfProperties;

      if (! new File(targetDirectory).isDirectory()) {
        throw new InvalidPluginException(
            String.format("%s must be a directory, which contains the plugin class files.",
                targetDirectory));
      }

      this.path = targetDirectory;
      try {
        reloadInf2();
      }
      catch (UtilityException uex) {
        throw new InvalidPluginException("Unable to load INF file", uex);
      }
      currentState = PluginState.KNOWN;
    }

    //TestPluginDescriptor() {
    //  // Used for the PluginLangLoader initializing a Plugin Lang
    //}

    protected void reloadInf() throws InvalidPluginException {
      //superclass constructor will call this. do nothing.
    }

    protected void reloadInf2() throws InvalidPluginException {
      findAndLoadCanaryInf();
      name = canaryInf.getString("name", "");
      if (name.equals("")) {
        name = canaryInf.getString("main-class", "");
      }
      if (name.equals("")) {
        name = new File(path).getName();
      }
      version = canaryInf.getString("version", "UNKNOWN");
      author = canaryInf.getString("author", "UNKNOWN");
      language = canaryInf.getString("language", "java");
      enableEarly = canaryInf.getBoolean("enable-early", false); // Enable before subsystems are initialized and before the first world gets loaded
      if (canaryInf.containsKey("dependencies")) {
        dependencies = canaryInf.getStringArray("dependencies", ",");
      }
      else {
        dependencies = new String[0];
      }
      // this:
      // PluginLifecycleFactory.registerLifecycle("java", JavaPluginLifecycle.class);
      // must have happening prior to now or the attempt to createLifecycle below will blow up.
      pluginLifecycle = PluginLifecycleFactory.createLifecycle(this);
    }

    private void findAndLoadCanaryInf() throws InvalidPluginException {
      try {
        StringBuffer sb = new StringBuffer();
        canaryInfProperties.forEach((key, value) -> {
          sb.append(String.format("%s=%s\n", key, value));
        });
        File canaryInfFile = new File(path, "Canary.inf");
        Files.write(canaryInfFile.toPath(), sb.toString().getBytes());
        canaryInf = new PropertiesFile(canaryInfFile.getAbsolutePath());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public PropertiesFile getCanaryInf() {
      return canaryInf;
    }

    public String getName() {
      return name;
    }

    public String getAuthor() {
      return author;
    }

    public String getVersion() {
      return version;
    }

    public String getPath() {
      return path;
    }

    public String getLanguage() {
      return language;
    }

    public boolean enableEarly() {
      return enableEarly;
    }

    public PluginLifecycle getPluginLifecycle() {
      return pluginLifecycle;
    }

    public Plugin getPlugin() {
      return plugin;
    }

    /**
     * DO NOT CALL THIS METHOD. It is for internal use only.
     *
     * @param plugin
     *         Current plugin object
     */
    public void setPlugin(Plugin plugin) {
      this.plugin = plugin;
    }

    public PluginState getCurrentState() {
      return currentState;
    }

    /**
     * DO NOT CALL THIS METHOD. It is for internal use only.
     *
     * @param state
     *         New plugin state
     */
    public void setCurrentState(PluginState state) {
      this.currentState = state;
    }

    public String[] getDependencies() {
      return dependencies;
    }

    public int getPriority() {
      return priority;
    }

    public void setPriority(int priority) {
      this.priority = priority;
    }

  }
}
