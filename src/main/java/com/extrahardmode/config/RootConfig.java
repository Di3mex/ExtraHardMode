/*
 * This file is part of
 * ExtraHardMode Server Plugin for Minecraft
 *
 * Copyright (C) 2012 Ryan Hamshire
 * Copyright (C) 2013 Diemex
 *
 * ExtraHardMode is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExtraHardMode is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero Public License
 * along with ExtraHardMode.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.extrahardmode.config;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.service.Response;
import com.extrahardmode.service.SpecialParsers;
import com.extrahardmode.service.config.*;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RootConfig extends MultiWorldConfig
{
    /**
     * Contains Info about Blocks which fall and their Metadata
     */
    private final Table<ConfigNode, String/*world name*/, Map<Integer/*Block id*/, List<Byte>/*Block Id* (-1 for no id)*/>> extraSettings = HashBasedTable.create();


    /**
     * Constructor
     */
    public RootConfig(ExtraHardMode plugin)
    {
        super(plugin);
    }


    @Override
    public void starting()
    {
        load();
    }


    @Override
    public void closing()
    {
        extraSettings.clear();
    }


    /**
     * Get a node which has to parsed from a StringList prior to be usable
     *
     * @param node  special node
     * @param world the world where this is activated
     *
     * @return a List of Block Ids and MetaData values if found
     */
    public Map<Integer, List<Byte>> getMappedNode(ConfigNode node, String world)
    {
        return extraSettings.contains(node, world) ? extraSettings.get(node, world) : enabledForAll ? extraSettings.get(node, ALL_WORLDS) : Collections.EMPTY_MAP;
    }


    @Override
    public void load()
    {
        init();
        File[] configFiles = getConfigFiles(plugin.getDataFolder());
        List<Config> configs = loadFilesFromDisk(configFiles);
        load(configs);
    }


    /**
     * Loads all FileConfigurations into memory Insures that there is always a main config.yml loads all other Config's
     * based on the Mode specified in the ConfigFile
     *
     * @param configs FileName + respective FileConfiguration
     */
    void load(List<Config> configs)
    {
        configs = loadMain(configs);

        Config defaults = null;
        for (Config config : configs)
        {   //loadMain insures that there is always a config.yml
            if (config.getFileName().equals("config.yml") && config.getStatus() == Status.PROCESSED && config.getMode() == Mode.MAIN)
            {
                defaults = config;
                configs.remove(config);
                break;
            }
        }

        for (Config config : configs)
        {
            {//Check if Mode is specified
                Response<String> response = (Response<String>) loadNode(config.getConfig(), RootNode.MODE, false);
                try
                {
                    if (response.getStatusCode() != Status.NOT_FOUND)
                        config.setMode(Mode.valueOf(response.getContent().toUpperCase()));
                } catch (IllegalArgumentException ignored)
                {
                } finally
                {
                    if (config.getMode() == null || config.getMode() == Mode.NOT_SET)
                        config.setMode(Mode.INHERIT);
                }
            }

            switch (config.getMode())
            {
                case MAIN:
                {
                    //Inherit is the default mode
                    config.setMode(Mode.INHERIT); //fallthrough
                }
                case DISABLE:
                case INHERIT:
                {
                    config = loadConfigToMem(config, defaults);
                    break;
                }
                case NOT_SET:
                default:
                {
                    config.setMode(config.getFileName().equals("config.yml") ? Mode.MAIN : Mode.INHERIT);
                    config = loadConfigToMem(config, defaults);
                    break;
                }
            }

            //Check if all values in the config are the same as in the master config and mark them as "inheritent" if they are the same, this makes
            //it easier to see what the admin has overriden. Or if a value in disable mode is already disabled.
            if (config.getMode() == Mode.INHERIT || config.getMode() == Mode.DISABLE)
            {
                for (RootNode node : RootNode.values())
                {
                    Object thisValue = loadNode(config.getConfig(), node, false).getContent();
                    Object thatValue = loadNode(defaults.getConfig(), node, false).getContent();

                    switch (config.getMode())
                    {
                        case INHERIT:
                        {   //floating point arithmetic is inaccurate...
                            if ((thisValue == thatValue) || ((thisValue instanceof Double && thatValue instanceof Double)
                                    && (BigDecimal.valueOf((Double) thisValue).equals(BigDecimal.valueOf((Double) thatValue)))))
                            {
                                config.getConfig().set(node.getPath(), config.getMode().name().toLowerCase());
                                config.setStatus(Status.ADJUSTED);
                            }
                            break;
                        }
                        case DISABLE:
                        {
                            if ((thisValue == node.getValueToDisable()) || ((thisValue instanceof Double && node.getValueToDisable() instanceof Double)
                                    && (BigDecimal.valueOf((Double) thisValue).equals(BigDecimal.valueOf((Double) node.getValueToDisable())))))
                            {
                                config.getConfig().set(node.getPath(), config.getMode().name().toLowerCase());
                                config.setStatus(Status.ADJUSTED);
                            }
                            break;
                        }
                        default:
                            throw new IllegalArgumentException(config.getMode().name() + " not implemented");
                    }

                }
            }

            if (config.getStatus() == Status.ADJUSTED)
            {
                saveConfig(config);
            }
        }
    }


    /**
     * Loads the main config.yml
     *
     * @param configs to process
     *
     * @return all configs and the main config marked as processed
     */
    List<Config> loadMain(List<Config> configs)
    {
        Config main = null;

        boolean contains = false;
        for (Config config : configs)
        {
            if (config.getFileName().equals("config.yml"))
            {
                contains = true;
                main = config;
                configs.remove(config);
                break;
            }
        }
        if (!contains)
        {
            main = new Config(new YamlConfiguration(), plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml");
        }

        main.setMode(Mode.MAIN);
        main = loadConfigToMem(main, null);
        if (main.getStatus() == Status.ADJUSTED)
        {
            saveConfig(main);
        }

        main.setStatus(Status.PROCESSED);
        configs.add(main);
        return configs;
    }


    /**
     * Store the Options from the FileConfiguration into memory
     *
     * @param config Config, to load the values from, according to the Mode specified. The Mode determines how not found
     *               or same values are treated
     * @param main   main file to use for reference values, can be null if we are loading with Mode.MAIN
     *
     * @return the passed in config, is marked as Status.ADJUSTED if the Config has been changed
     */
    private Config loadConfigToMem(Config config, Config main)
    {
        Response<List<String>> myWorlds = loadNode(config.getConfig(), RootNode.WORLDS, false);
        List<String> worlds = myWorlds.getContent();
        //Check for * placeholder = Enables plugin for all worlds
        if (worlds.contains(ALL_WORLDS))
            enabledForAll = true;

        for (RootNode node : RootNode.values())
        {
            Response response = loadNode(config.getConfig(), node, false);

            if (node.getVarType().equals(ConfigNode.VarType.INTEGER) && response.getStatusCode() == Status.OK)
            {
                response = validateInt(node, response.getContent());
            }

            switch (config.getMode()) //special actions regarding default values
            {
                case MAIN:
                {
                    if (response.getStatusCode() == Status.NOT_FOUND)
                    {
                        //get with defaults on
                        response = loadNode(config.getConfig(), node, true);
                        config.setStatus(Status.ADJUSTED);
                    }
                    break;
                }
                case DISABLE:
                {
                    if (response.getStatusCode() == Status.NOT_FOUND || response.getStatusCode() == Status.INHERITS) //Status = Disable: nothing needs to be done
                    {
                        if ((!response.getContent().equals(Mode.DISABLE.name()) && response.getStatusCode() == Status.INHERITS)
                                || response.getStatusCode() == Status.NOT_FOUND)
                        {   //mark it only adjusted if it has been changed, we rewrite the option nevertheless
                            config.setStatus(Status.ADJUSTED);
                        }

                        response.setContent(Mode.DISABLE.name().toLowerCase());
                        response.setStatus(Status.DISABLES);
                    }
                    break;
                }
                case INHERIT: //mark non found nodes as inherits
                {
                    if (response.getStatusCode() == Status.NOT_FOUND || response.getStatusCode() == Status.DISABLES)
                    {
                        if ((!response.getContent().equals(Mode.INHERIT.name()) && response.getStatusCode() == Status.DISABLES)
                                || response.getStatusCode() == Status.NOT_FOUND)
                        {   //mark it only adjusted if it has been changed, we rewrite the option nevertheless
                            config.setStatus(Status.ADJUSTED);
                        }
                        response.setContent(Mode.INHERIT.name().toLowerCase());
                        response.setStatus(Status.INHERITS);
                    }
                    break;
                }
                default:
                {
                    throw new UnsupportedOperationException("Mode: " + config.getMode().name() + " isn't handled");
                }
            }

            //the Mode should always reflect in the yml file
            /* Special node handling */
            switch (node)
            {
                case MODE:
                {
                    /* Make sure that the mode we used is the same in the config */
                    if ((response.getContent() instanceof String) && !((String) response.getContent()).equalsIgnoreCase(config.getMode().name()))
                    {
                        response.setContent(config.getMode().name());
                        config.setStatus(Status.ADJUSTED);
                    }
                    break;
                }
                case MORE_FALLING_BLOCKS:
                case BREAKABLE_BY_FALLING_BLOCKS:
                case SUPER_HARD_STONE_TOOLS:
                case SUPER_HARD_STONE_PHYSICS_BLOCKS:
                {
                    if (response.getContent() instanceof List)
                    {
                        Response parsedBlocks = SpecialParsers.parseMaterials((List<String>) response.getContent());
                        if (parsedBlocks.getStatusCode() == Status.NEEDS_TO_BE_ADJUSTED)
                            config.setStatus(Status.ADJUSTED);
                        for (String world : worlds)
                        {
                            extraSettings.put(node, world, (Map<Integer, List<Byte>>) parsedBlocks.getContent());
                        }
                    }
                    break;
                }
            }

            config.getConfig().set(node.getPath(), response.getContent()); //has to be before we get the actual values

            //the actual values that need to be loaded into memory for the two modes to work
            if (response.getStatusCode() == Status.INHERITS || response.getStatusCode() == Status.DISABLES)
            {
                switch (config.getMode())
                {
                    case INHERIT: //load the value from the main config
                        response.setContent(loadNode(main.getConfig(), node, false).getContent());
                        break;
                    case DISABLE: //get the value to disable this option
                        response.setContent(node.getValueToDisable());
                        break;
                }
            }

            if (myWorlds.getStatusCode() != Status.NOT_FOUND)
            {
                for (String world : worlds)
                {
                    set(world, node, response.getContent());
                }
            }
        }
        return config;
    }


    /**
     * Reorders and saves the config. Reorders the Config to the order specified by the enum in RootNode. This assumes
     * that the Config only has valid Entries.
     *
     * @param config Config to save
     */
    void saveConfig(Config config)
    {
        //Reorder
        FileConfiguration reorderedConfig = new YamlConfiguration();
        for (RootNode node : RootNode.values())
        {
            switch (node)
            {
            	case MORE_FALLING_BLOCKS:
            	case BREAKABLE_BY_FALLING_BLOCKS:
                {
                    reorderedConfig.set(node.getPath(),
                            SpecialParsers.convertToStringList(
                                    SpecialParsers.parseMaterials(config.getConfig().getStringList(node.getPath())).getContent()));
                    break;
                }
                default:
                    reorderedConfig.set(node.getPath(), config.getConfig().get(node.getPath()));
            }
        }
        config.setConfig(reorderedConfig);

        //save the reordered config
        try
        {
            config.getConfig().save(config.getConfigFile());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Get a value to be used by metrics
     * <pre>
     * Boolean values:
     * 0 = completely disabled
     * 1 = enabled in all worlds
     * 2 = Enabled in some worlds
     *
     * Integers:
     * -----------
     * Percentages:
     * 0 = 0%
     * 1 = 1-20%
     * 2 = 21-40%
     * 3 = 41-60%
     * 4 = 61-80%
     * 5 = 81-100%
     * Health:
     * 0 = 0
     * 1 = 1-5
     * 2 = 6-10
     * 3 = 11-15
     * 4 = 16-19
     * 5 = 20
     * Y-Value:
     * 0 = 0
     * 1 = 1-50
     * 2 = 51-100
     * 3 = 101-150
     * 4 = 151-200
     * 5 = 201-255
     * </pre>
     *
     * @param node to get the value for
     */
    public int getMetricsValue(ConfigNode node)
    {
        switch (node.getVarType())
        {
            case BOOLEAN:
            {
                //Add up how often it's enabled
                int value = 0;
                for (String world : getEnabledWorlds())
                    value += getBoolean(node, world) ? 1 : 0;
                return value == 0 ? 0 : value == getEnabledWorlds().length ? 1 : 2;
            }
            default:
                throw new UnsupportedOperationException(node.getPath() + " " + node.getVarType().name() + " not supported yet!");
        }
    }
}