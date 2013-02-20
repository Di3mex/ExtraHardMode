package me.ryanhamshire.ExtraHardMode.config;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * public static = global config option
 * private static final = min/max values, only set this if its not a percentage (max 100) or ends with _Y (max = 255)
 * transient = variables that aren't configs, important! All variables that aren't configs must be transient
 * variables prefixed Max_variableName are maximum allowed values for variableName
 * This class is designed to make it very easy to add configoptions
 * There is a problem with the "excessive" use of static variables. On a reload of the server the vals stay. This class doesn't get
 * reinitialised, therefore we save the default values in a HashMap the first time the server starts up, on a reload the values
 * from the last "instance" of the plugin get replaced by the default values. Class takes around 30-100ms to perform load(). Storing
 * the default values takes up an additional 2,5k bytes ~ 0,0025mb. If there is a way to use something else than the fieldName
 * as a "reference" to the field then one could save a bit of memory.
 * Sadly this class has become a lot more complicated than I had originally envisioned, but imo it's still pretty okay for what it does.
 */
@SuppressWarnings("unused")
public class Config {

   private final static String dataLayerFolderPath = "plugins" + File.separator + "ExtraHardMode";
   
    private static transient Plugin plugin = null;
    private static transient Logger logger;
    private static transient boolean logVerbose = false;

    private static transient FileConfiguration config;
    private static transient String configPath = dataLayerFolderPath + File.separator + "config.yml";
    private static transient File configFile = new File (configPath);

    /**This saves the defaults and restores them on a reload, static variables don't get cleared on a reload**/
    private static transient HashMap <String,Object> defaults;

    /**The topmost Node in the YamlFile **/
    private static transient final String MAIN_NODE= "Extra Hard Mode",
    /**prefix to define a max value for a variable	**/ MAX = "MAX_",
    /** 		""		  min			""			**/ MIN = "MIN_";

    private final static transient int
            WORLD_HEIGHT = 255, //This is default max for all variables ending with _Y
            MAX_PERCENTAGE = 100, //Max Value for all variables containing percentage
		    MIN_INT = 0; //All int's from the config must be bigger than 0

    /**list of worlds where extra hard mode rules apply**/
    public static ArrayList <String> Enabled_Worlds;


    /**Plugin General Stuff**/
    public static boolean
            Plugin__Ops_Bypass_By_Default = true,
		    Plugin__Creative_Mode_Bypasses_Most_Limitations = true,
		    Plugin__Enable_Advanced_Support_For_Vanilla_Mods = false;


    /**world rules**/
    public static int                                               /**maximum y for placing torches**/
            World__Torches__Torch_Max_Y = 30, 				        /**percent chance for broken netherrack to start a fire**/
            World__Broken_Netherrack_Catches_Fire_Percent = 20;     /**whether stone is hardened to encourage cave exploration over tunneling**/
    public static boolean
			World__Mining__Breaking_Ores_Softens_Surrounding_Stone = true,         /**whether players may place blocks directly underneath themselves**/
            World__Mining__Prevent_Tunneling_To_Encourage_Cave_Exploration = true, /**whether TNT should be more powerful and plentiful**/
	        World__Limited_Block_Placement = true,				                   /**whether players are limited to placing torches against specific materials**/
            World__Better_Tnt = true,							                   /**whether rain should break torches**/
            World__Torches__Limited_Torch_Placement = true,				           /**which materials beyond sand and gravel should be subject to gravity**/
            World__Torches__Rain_Breaks_Torches = true,					           /**Play Fizzing Sound when Torches get put out**/
            World__Play_Sounds__Torch_Fizzing = true,                              /**Play Ghast Sound when Creeper drops Tnt**/
	        World__Play_Sounds__Creeper_Tnt_Warning = false,                       /**Tree Logs fall realistically**/
	        World__Better_Tree_Chopping = true;

    /**water**/
    public static boolean                                                          /**whether players may move water source blocks**/
			World__Water__Dont_Move_Source_Blocks = true,                          /**Limited Swimming in Armor**/
	        World__Water__No_Swimming_In_Armor = true;


    /**general monster rules**/															/**max y-level for extra monster spawns**/
    public static int
            General_Monster_Rules__More_Monsters__Max_Y = 55,			/**what to multiply monster spawns by**/
            General_Monster_Rules__More_Monsters__Multiplier = 2,		/**max y-level for monsters to spawn in the light**/
            General_Monster_Rules__Monsters_Spawn_In_Light_Max_Y = 50;
    private final static int
            MAX_General_Monster_Rules__More_Monsters__Multiplier = 25;


    /**monster grinder fix rules**/											/**whether monster grinders (or "farms") should be inhibited**/
    public static  boolean 	General_Monster_Rules__Inhibit_Monster_Grinders = true;


    /**zombie rules**/
    /**whether zombies apply a debuff to players on hit**/
    public static boolean 	Zombies__Debilitate_Players = true;				/**percent chance for a zombie to reanimate after death**/
    public static int 		Zombies__Reanimate_Percent = 50;


    /**skeleton rules**/													/**percent chance skeletons have a chance to knock back targets with arrows**/
    public static int
            Skeletons__Knock_Back_Percent = 30,				/**percent chance skeletons will release silverfish instead of firing arrows**/
	        Skeletons__Shoot_Silverfish_Percent = 30,		/**whether or not arrows will pass harmlessly through skeletons**/
            Skeletons__Deflect_Arrows_Percent = 100;


    /**creeper rules**/
    public static int                                       /**percentage of creepers which will spawn charged**/
            Creepers__Charged_Spawn_Percent = 20,			/**percentage of creepers which spawn activated TNT on death**/
            Creepers__Drop_Tnt_On_Death_Percent = 10,		/**Max Y to potentially only allow them to explode in caves**/
            Creepers__Drop_Tnt_On_Death_Max_Y = 50;
    public static boolean                                           /**whether charged creepers explode when damaged**/
            Creepers__Charged_Creepers_Explode_On_Hit = true,		/**whether creepers explode when caught on fire**/
            Creepers__Flaming_Creepers_Explode = true;


    /**pig zombie rules**/													/**whether pig zombies are always hostile**/
    public static boolean
            PigZombie__Always_Angry = true,					/**whether pig zombies drop nether wart in nether fortresses**/
            PigZombie__Drop_Warts_In_Fortresses = true;


    /**ghast rules**/														/**whether ghasts should deflect arrows and drop extra loot**/
    public static boolean 	Ghasts__Deflect_Arrows = true;


    /**magma cube rules**/													/**whether damaging a magma cube turns it into a blaze**/
    public static boolean 	MagmaCubes__Become_Blazes_On_Damage = true;		/**percentage chance that a blaze spawn will trigger a flame slime spawn as well**/
    public static int
			MagmaCubes__Spawn_Blaze_On_Death_Percent = 100,
			MagmaCubes__Spawn_A_MagmaCube_With_A_Blaze_Percent = 50;


    /**blaze rules**/														/**whether blazes explode and spread fire when they die**/
    public static boolean
            Blazes__Explode_On_Death =  true,				/**whether blazes drop fire when damaged**/
            Blazes__Drop_Fire_On_Damage = true,				/**whether blazes drop extra loot**/
            Blazes__Drop_Bonus_Loot = true;					/**percentage of pig zombies which will be replaced with blazes**/
    public static int
            Blazes__Bonus_Nether_Spawn_Percent = 20,		/**percentage of skeletons near bedrock which will be replaced with blazes**/
            Blazes__Near_Bedrock_Spawn_Percent = 50,		/**percentage chance that a blaze slain in the nether will split into two blazes**/
            Blazes__Nether_Blazes_Split_On_Death_Percent = 25;


    /**spider rules**/														/**percentage of zombies which will be replaced with spiders under sea level**/
    public static int 		Spiders__Bonus_Underground_Spawn_Percent = 20;	/**whether spiders drop webbing when they die**/
    public static boolean 	Spiders__Drop_Web_On_Death = true;


    /**enderman rules**/													/**whether endermen may teleport players**/
    public static boolean 	Enderman__Improved_Teleportation = true;


    /**witch rules**/														/**percentage of surface zombies which spawn as witches**/
    public static int 		Witches__Bonus_Spawn_Percent = 5;


    /**ender dragon rules**/												/**whether the ender dragon respawns**/
    public static boolean
            Enderdragon__Respawn = true,					/**whether it drops an egg when slain**/
            Enderdragon__Drops_Dragon_Egg = true,			/**whether it drops a pair of villager eggs when slain**/
            Enderdragon__Drops_Villager_Eggs = true,		/**whether the dragon spits fireballs and summons minions**/
            Enderdragon__Additional_Attacks = true,			/**whether server wide messages will broadcast player victories and defeats**/
            Enderdragon__Combat_Announcements = true,		/**whether players will be allowed to build in the end**/
            Enderdragon__No_Building_In_End = true;


	/**Farming**/
    public static boolean                                   /**whether food crops die more easily**/
			Farming__Weak_Food_Crops__Enable = true,        /**plants don't grow in desserts**/
			Farming__Weak_Food_Crops__Arid_Infertile_Desserts= true, /**whether bonemeal may be used on mushrooms**/
            Farming__No_Bonemeal_On_Mushrooms = true,		/**whether nether wart will ever drop more than 1 wart when broken**/
            Farming__No_Farming_Nether_Wart = true,			/**whether sheep will always regrow white wool**/
            Farming__Sheep_Only_Regrow_White_Wool = true,   /**whether crafting melonsseeds from melonds should be blocked**/
			Farming__No_Crafting_Melon_Seeds = true;
	public static int
			Farming__Weak_Food_Crops__Vegetation_Loss_Percentage = 25;


    /**Player Death**/
    public static int                                       /**how much health after respawn**/
            Player__Respawn_Health = 15,					/**how much food bar after respawn**/
            Player__Respawn_Food_Level = 15, 				/**percentage of item stacks lost on death**/
            Player__Death_Item_Stacks_Forfeit_Percent = 10;
    private final static int
            MAX_Player__Respawn_Health = 20,
            MIN_Player__Respawn_Health = 1,
            MAX_Player__Respawn_Food_Level = 20;


    /**player damage**/
    public static boolean                                   /**whether players take additional damage and/or debuffs from environmental injuries**/
            Player__Enhanced_Environmental_Damage = true,	/**whether players catch fire when extinguishing a fire up close**/
            Player__Extinguishing_Fire_Ignites_Players = true;


    /**More Falling Blocks**/								/**which materials beyond sand and gravel should be subject to gravity**/
    public static ArrayList <String> More_Falling_Blocks;
    private final static transient Material [] Defaults_More_Falling_Blocks = new Material[] {
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE,
            Material.DIRT,
            Material.GRASS,
            Material.MYCEL,
            Material.JACK_O_LANTERN };

    /**explosions disable option, needed to dodge bugs in popular plugins**/
    public static boolean 	Work_Around_Explosions_Bugs = false;


    /**
     * When the server starts up and initializes the fields, we save them. This approach allows us to initialize the variables
     * in one line when we declare the fields. With this approach we don't have fields that we declared and forgot to initialize.
     * Also it makes it easier to understand the config, because initialization, defaultValue and description are in one place.
     */
    public static void saveDefaults ()
    {
        defaults = new HashMap <String, Object>();
        for (Field field: Config.class.getDeclaredFields())
        {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) &! Modifier.isTransient(field.getModifiers()))
            {
                String fieldName = field.getName();
                Object obj = null;
                try {
                    obj = field.get(null);
                } catch (IllegalAccessException e) {
                    logInfo("saveDefaults: IllegalAccess to Field: " + fieldName);
                }
                if (obj != null)defaults.put(fieldName, obj);
            }
        }
    }

    /**
     * Load the defaultValues when the server reloads
     */
    public static void loadDefaults ()
    {
        if (defaults == null) return;
        for (Field field: Config.class.getDeclaredFields()){
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) &! Modifier.isTransient(field.getModifiers()))
            {
                String fieldName = field.getName();
                Object defaultValue = defaults.get(fieldName);
                if (defaultValue != null)
                {
                    try {
                        field.set(null, defaultValue);
                    } catch (IllegalAccessException e) {
                        logVerbose("IllegalAccessException while accessing Field: " + fieldName + " in loadDefaults()");
                    }
                }
            }
        }
    }

    /**
     * Only public static non transient variables are considered to be a global configOption. Gets the variableNames via
     * reflection and converts them for yaml like so:
     * My_Category__Option -> "Extra Hard Mode.My Category.Option
     * It prefixes everything with the global MainNode, converts double underscores to dots and underscores to spaces
     * It uses this generated name as the path for yaml
     *
     * booleans get no specialTreatment. However ints can have min and max values, which are checked and set to the min
     * or max values if they exceed the limits. Max and Min values can be defined by using the same VariableName and
     * prefixing it either with MIN_ or MAX_. But only private final variables are considered to be possible candidates
     * for min/max values. VariableNames containing "percent" have a default max of 100, names ending with _Y default to
     * the max worldheight.
     * String ArrayLists get parsed if they exist in the config , normally these are a bit more complex and you should
     * initialize them before the parsing starts. If you need to validate your Lists you can add another else if to check
     * for it.
     *
     * When the parsing has finished the config gets written back to disk. If logVerbose is true it will throw an error
     * everytime there is no maxValue or minValue defined, so you should set that to false before deploying.
     * @param plugin
     */
    public static void load (Plugin plugin)
    {
        if (defaults != null)
            loadDefaults();

        long time = System.currentTimeMillis();
        Config.plugin = plugin;
        logger = plugin.getLogger();
        config = YamlConfiguration.loadConfiguration(configFile);
        /**Second config to automatically remove clutter and mainly variables that won't get used anymore**/
        FileConfiguration outConfig = new YamlConfiguration();

        /*Clear everything which isn't a primitive.*/
        Enabled_Worlds = new ArrayList<String>();
        More_Falling_Blocks = new ArrayList<String>();

        for (World loadedWorld: plugin.getServer().getWorlds())
        {   //All loaded Worlds
            Enabled_Worlds.add(loadedWorld.getName());
        }

        for (Material material: Defaults_More_Falling_Blocks)
        {   //default Falling Blocks
            More_Falling_Blocks.add(material.name());
        }

        saveDefaults();

        for (Field field : Config.class.getDeclaredFields())
        {
	        if (   Modifier.isStatic 	    (field.getModifiers())
                && Modifier.isPublic 		(field.getModifiers())
                &! Modifier.isTransient 	(field.getModifiers())
                &! Modifier.isFinal 		(field.getModifiers())
                )
            {
                String fieldName = field.getName();
                String path = varNameToYamlPath(fieldName);

                try
                {
                    if (config.isSet(path))
                    {
                        String fieldType = field.getType().getName();


                        if (fieldType.equals("int"))
                        {
	                        int maxValue = 0;

	                        if (StringUtils.containsIgnoreCase(fieldName, "percent"))
		                        maxValue = MAX_PERCENTAGE;
	                        else if (fieldName.toLowerCase().endsWith("_y"))
		                        maxValue = WORLD_HEIGHT;
	                        else
		                        maxValue = getSpecialInt(fieldName, MAX, 100);

	                        int minValue = getSpecialInt(fieldName, MIN, MIN_INT);
                            int configInt = config.getInt(path);

                            if (maxValue != 0 && configInt >= maxValue)
                            {	//value in config exceeds the maximum for this value
                                field.setInt(null, maxValue);
                            } else if (minValue > configInt)
                            {	//value in config is too small, may even be negative
                                field.setInt(null, minValue);
                            } else
                            {	//admin didn't derp, we can use the config value
                                field.setInt(null, configInt);
                            }
                        }


                        else if (fieldType.equals("boolean"))
                        {	//booleans return false if admin puts invalid characters
                            boolean configBoolean = config.getBoolean(path);
                            field.setBoolean(null, configBoolean);
                        }


                        else if (fieldType.equals("java.util.ArrayList"))
                        {
                            ArrayList <String> cfgList = (ArrayList<String>) config.getStringList(path);


                            if (fieldName.equalsIgnoreCase("Enabled_Worlds"))
                            {
                                ArrayList <String> loadedWorlds = Enabled_Worlds;
                                Enabled_Worlds = new ArrayList<String>();
                                for (String cfgWorld: cfgList)
                                {
                                    if (loadedWorlds.contains(cfgWorld))
                                    {
                                        Enabled_Worlds.add(cfgWorld);
                                    }
                                    else
                                    {
                                        logInfo(cfgWorld + " doesn't exist, update your config");
                                    }
                                }
                                if (Enabled_Worlds.isEmpty())
                                {
                                    for (String defaultWorld: loadedWorlds)
                                    {
                                        Enabled_Worlds.add(defaultWorld);
                                    }
                                }
                            }


                            else if (fieldName.equalsIgnoreCase("More_Falling_Blocks"))
                            {
                                //Write the validated values back to MoreFallingBlocks
                                More_Falling_Blocks = new ArrayList<String>();
                                for (String strMaterial: cfgList)
                                {
                                    Material material = Material.getMaterial(strMaterial);
                                    if (material == null)
                                    {
                                        logInfo("There is no block called: " + strMaterial);
                                    }
                                    else
                                    {
                                        if (!More_Falling_Blocks.contains(strMaterial))//remove duplicates
                                        More_Falling_Blocks.add(strMaterial);
                                    }
                                }

                            }
                        }

                        else
                        {
                            logVerbose("Unprocessed field: " + fieldType);
                        }
                    }
                    outConfig.set(path, field.get(null));
                }
                catch (IllegalAccessException ignored) {}
            }
        }
        try
        {
            outConfig.save(configFile);
            time = System.currentTimeMillis() - time;
            logVerbose ("Took " + time + " ms to initialize");
        } catch (IOException e) {
            logException("Error: There was a problem while saving config to " + configPath);
            e.printStackTrace();
        }
    }

    /**
     * My_Super_Category__My_Super_Option
     * ->
     * MAIN_NODE.My Super Category.My Super Option
     *
     * MAIN_NODE:
     *   My Super Category:
     *     My Super Option:
     *
     * @param fieldName field to convert
     * @return
     */
    private static String varNameToYamlPath (String fieldName) {
        fieldName = fieldName.replaceAll("__", ".");
        fieldName = fieldName.replaceAll("_", " ");
        fieldName = MAIN_NODE + "." + fieldName;
        return fieldName;
    }

    /**
     * Used for Exceptions which are common and would otherwise spam the console.
     * Put verboseLogging = false before deploying, to disable the messages
     * @param msg message
     */
    private static void logVerbose (String msg)
    {
        if (!logVerbose)return;
        logger.info(msg);
    }

    /**
     * Used when informing about errors which aren't common
     * @param msg
     */
    private static void logInfo (String msg)
    {
        logger.info(msg);
    }

    /**
     * This is for real Errors
     * @param msg
     */
    private static void logException (String msg)
    {
        logger.severe(msg);
    }


    /**
     * Gets a special value, e.g. max/min value of a field with the given fieldName
     * @param fieldName name of field that should be tested
     * @param prefix the special prefix for the field
     * @param defaultVal this gets returned if the field doesn't exist
     */
    private static Integer getSpecialInt (String fieldName, String prefix, int defaultVal)
    {
        String specialName = prefix + fieldName;
        try
        {
            Field specialField = Config.class.getDeclaredField(specialName);
            if (Modifier.isPrivate(specialField.getModifiers()))
                return specialField.getInt(Config.class);
        }
        catch (NoSuchFieldException e)
        {	//This Exception gets thrown often, log verbose only
            logVerbose("No Such Field: " + specialName );
        } catch (SecurityException e)
        {
            logException("SecurityException while accessing: " + specialName);
        } catch (IllegalArgumentException e)
        {
            logException("IllegalArgumentException while accessing: " + specialName);
        }
        catch (IllegalAccessException e)
        {
            logException("IllegalAccessException while accessing: " + specialName);
        }
        return defaultVal;
    }

}