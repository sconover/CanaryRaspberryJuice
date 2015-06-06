import com.sun.nio.file.SensitivityWatchEventModifier;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.canarymod.Main;
import net.canarymod.exceptions.InvalidPluginException;
import net.canarymod.exceptions.PluginLoadFailedException;
import net.canarymod.plugin.DefaultPluginManager;
import net.canarymod.plugin.Plugin;
import net.canarymod.plugin.PluginDescriptor;
import net.canarymod.plugin.PluginLifecycle;
import net.canarymod.plugin.PluginManager;
import net.canarymod.plugin.PluginState;
import net.canarymod.plugin.lifecycle.PluginLifecycleBase;
import net.canarymod.plugin.lifecycle.PluginLifecycleFactory;
import net.minecraft.server.MinecraftServer;
import net.visualillusionsent.utils.PropertiesFile;
import net.visualillusionsent.utils.UtilityException;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class IntegrationTest {
  @Test
  public void runServer() throws Exception {
    //check current working dir and make sure it's not the project root

    System.setProperty("java.awt.headless", "true");
    PluginLifecycleFactory.registerLifecycle("java", AutoReloadingPluginLifecycle.class);

    TestPluginDescriptor pluginDescriptor =
        new TestPluginDescriptor(
            new File("../../classes/production/CanaryRaspberryJuice").getAbsolutePath(),
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

  public static class Reloader extends ClassLoader {

    private final String dir;
    private final ClassLoader parentClassLoader;

    public Reloader(String dir, ClassLoader parentClassLoader) {
      this.dir = dir;
      this.parentClassLoader = parentClassLoader;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
      if (classFileFor(className).exists()) {
        return findClass(className);
      } else {
        return parentClassLoader.loadClass(className);
      }
    }

    @Override
    public Class<?> findClass(String className) {
      try {
        byte[] bytes = loadClassData(className);
        return defineClass(className, bytes, 0, bytes.length);
      } catch (IOException ioe) {
        try {
          return super.loadClass(className);
        } catch (ClassNotFoundException ignore) { }
        ioe.printStackTrace(System.out);
        return null;
      }
    }

    private byte[] loadClassData(String className) throws IOException {
      File f = classFileFor(className);
      int size = (int) f.length();
      byte buff[] = new byte[size];
      FileInputStream fis = new FileInputStream(f);
      DataInputStream dis = new DataInputStream(fis);
      dis.readFully(buff);
      dis.close();
      return buff;
    }

    private File classFileFor(String className) {
      return new File(dir, className.replaceAll("\\.", "/") + ".class");
    }
  }

  public static class AutoReloadingPluginLifecycle extends PluginLifecycleBase {
    private Thread watchThread;
    private ClassLoader ploader;

    public AutoReloadingPluginLifecycle(PluginDescriptor desc) {
      super(desc);

      this.watchThread = null;
    }

    @Override protected void _load() throws PluginLoadFailedException {
      try {
        ploader = new Reloader(desc.getPath(), getClass().getClassLoader());
        Class<?> cls = ploader.loadClass(desc.getCanaryInf().getString("main-class"));
        //A hacky way of getting the name in during the constructor/initializer
        Plugin.threadLocalName.set(desc.getName());
        Plugin p = (Plugin)cls.newInstance();
        //If it isn't called in initializer, gotta set it here.
        p.setName(desc.getName());
        p.setPriority(desc.getPriority());
        desc.setPlugin(p);
      }
      catch (Throwable thrown) {
        throw new PluginLoadFailedException("Failed to load plugin", thrown);
      }


      watchThread = new Thread(new Runnable() {
        @Override public void run() {
          try {
            Path path = new File(desc.getPath()).toPath();
            WatchService watchService =
                path.getFileSystem().newWatchService();

            // see http://stackoverflow.com/questions/9588737/is-java-7-watchservice-slow-for-anyone-else
            path.register(watchService,
                new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_MODIFY},
                SensitivityWatchEventModifier.HIGH);

            while (!Thread.currentThread().isInterrupted()) {
              WatchKey key = watchService.poll(50, TimeUnit.MILLISECONDS);
              if (key != null && !key.pollEvents().isEmpty()) {
                System.out.println("Plugin change detected, auto-reloading...");
                System.out.flush();
                disable();
                unload();
                load();
                enable();
                System.out.println("...plugin reloaded.");
                System.out.flush();
              }
            }
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      });
      watchThread.start();
    }

    @Override protected void _unload() {
      if (watchThread != null && watchThread.isAlive()) {
        watchThread.interrupt();
      }
      ploader = null;
    }
  }

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
