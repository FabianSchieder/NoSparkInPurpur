package me.dreamvoid.some.user.dont.need;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.util.*;

public class spark extends JavaPlugin {
    private final spark INSTANCE = this;

    @Override
    public void onEnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Plugin plugin = Bukkit.getPluginManager().getPlugin("spark");
                if(plugin != null) {
                    getLogger().info("Found spark loaded, now unload it and myself.");
                    unloadPlugin(plugin);
                }

                try {
                    YamlConfiguration y = YamlConfiguration.loadConfiguration(new File("commands.yml"));
                    List<String> timings = new ArrayList<>();
                    timings.add("bukkit:timings $1-");
                    y.set("aliases.timings", timings);

                    List<String> tps = new ArrayList<>();
                    tps.add("spigot:tps $1-");
                    y.set("aliases.tps", tps);
                    y.save(new File("commands.yml"));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload commands");
                } catch (IOException e) {
                    getLogger().warning("Unable to save command.yml: " + e);
                }

                unloadPlugin(INSTANCE);
            }
        }.runTask(this);
    }

    private static void unloadPlugin(Plugin plugin){
        // PlugMan source, MIT License, https://github.com/TheBlackEntity/PlugMan
        PluginManager pluginManager = Bukkit.getPluginManager();

        SimpleCommandMap commandMap = null;

        List<Plugin> plugins = null;

        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Event, SortedSet<RegisteredListener>> listeners = null;

        boolean reloadlisteners = true;

        pluginManager.disablePlugin(plugin);

        try {

            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            try {
                Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                listenersField.setAccessible(true);
                listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
            } catch (Exception e) {
                reloadlisteners = false;
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            commands = (Map<String, Command>) knownCommandsField.get(commandMap);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        pluginManager.disablePlugin(plugin);

        if (listeners != null && reloadlisteners)
            for (SortedSet<RegisteredListener> set : listeners.values())
                set.removeIf(value -> value.getPlugin() == plugin);

        if (commandMap != null)
            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Command> entry = it.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand c = (PluginCommand) entry.getValue();
                    if (c.getPlugin() == plugin) {
                        c.unregister(commandMap);
                        it.remove();
                    }
                } else try {
                    Field pluginField = Arrays.stream(entry.getValue().getClass().getDeclaredFields()).filter(field -> Plugin.class.isAssignableFrom(field.getType())).findFirst().orElse(null);
                    if (pluginField != null) {
                        Plugin owningPlugin;
                        try {
                            pluginField.setAccessible(true);
                            owningPlugin = (Plugin) pluginField.get(entry.getValue());
                            if (owningPlugin.getName().equalsIgnoreCase(plugin.getName())) {
                                entry.getValue().unregister(commandMap);
                                it.remove();
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IllegalStateException e) {
                    if (e.getMessage().equalsIgnoreCase("zip file closed")) {
                        //if (PlugMan.getInstance().isNotifyOnBrokenCommandRemoval())
                        //    Logger.getLogger(PluginUtil.class.getName()).info("Removing broken command '" + entry.getValue().getName() + "'!");
                        entry.getValue().unregister(commandMap);
                        it.remove();
                    }
                }
            }

        if (plugins != null && plugins.contains(plugin))
            plugins.remove(plugin);

        if (names != null && names.containsKey(plugin.getName()))
            names.remove(plugin.getName());

        ClassLoader cl = plugin.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {

                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }

            try {

                ((URLClassLoader) cl).close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
