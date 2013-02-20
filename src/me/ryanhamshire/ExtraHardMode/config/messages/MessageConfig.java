package me.ryanhamshire.ExtraHardMode.config.messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.ConfigNode;
import me.ryanhamshire.ExtraHardMode.service.ModularConfig;

/**
 * Configuration handler for the messages.yml file.
 */
public class MessageConfig extends ModularConfig {
   /**
    * File reference.
    */
   private final File file;
   /**
    * Configuration object reference.
    */
   private final YamlConfiguration config;

   /**
    * Constructor.
    * @param plugin - Plugin instance.
    */
   public MessageConfig(ExtraHardMode plugin) {
      super(plugin);
      file = new File(plugin.getDataFolder().getAbsolutePath() + "/messages.yml");
      config = YamlConfiguration.loadConfiguration(file);
   }

   @Override
   public void starting() {
      loadDefaults(config);
      save();
      reload();
   }

   @Override
   public void closing() {
      reload();
      save();
   }

   @Override
   public void save() {
      try {
         config.save(file);
      } catch(IOException e) {
         plugin.getLogger().log(Level.SEVERE, "File I/O Exception on saving messages.yml", e);
      }
   }

   @Override
   public void set(String path, Object value) {
      config.set(path, value);
   }

   @Override
   public void reload() {
      // Reload config from file.
      try {
         config.load(file);
         loadSettings(config);
      } catch(FileNotFoundException e) {
         plugin.getLogger().log(Level.SEVERE, "File messages.yml not found.", e);
      } catch(IOException e) {
         plugin.getLogger().log(Level.SEVERE, "File I/O Exception on saving messages.yml", e);
      } catch(InvalidConfigurationException e) {
         plugin.getLogger().log(Level.SEVERE, "Invalid configuration for messages.yml", e);
      }
   }

   @Override
   public void loadSettings(ConfigurationSection config) {
      for(MessageNode node : MessageNode.values()) {
         updateOption(node);
      }
   }

   @Override
   public void loadDefaults(ConfigurationSection config) {
      for(MessageNode node : MessageNode.values()) {
         if(!config.contains(node.getPath())) {
            config.set(node.getPath(), node.getDefaultValue());
         }
      }
   }

   @Override
   public String getString(ConfigNode node) {
      return ChatColor.translateAlternateColorCodes('&', super.getString(node));
   }

   @Override
   public void boundsCheck() {
   }

   @Override
   public int getInt(ConfigNode node) {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<String> getStringList(ConfigNode node) {
      throw new UnsupportedOperationException();
   }

   @Override
   public double getDouble(ConfigNode node) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean getBoolean(ConfigNode node) {
      throw new UnsupportedOperationException();
   }
}
