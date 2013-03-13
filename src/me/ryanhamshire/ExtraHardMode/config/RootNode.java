/*
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.ryanhamshire.ExtraHardMode.config;

import me.ryanhamshire.ExtraHardMode.service.ConfigNode;
import org.bukkit.Material;

import java.util.ArrayList;

/**
 * Configuration options of the root config.yml file.
 */
//Please keep the codestyle, it makes it easier to grasp the structure of the config
public enum RootNode implements ConfigNode
{
    /**
     * list of worlds where extra hard mode rules apply
     */
    WORLDS
            ("ExtraHardMode.Enabled Worlds", VarType.LIST, new ArrayList<String>()),


    /**
     * Should explosions be disabled? They don't get recognized by a lot of plugins
     */
    DISABLE_EXPLOSIONS
            ("ExtraHardMode.Plugin.Disable Explosions", VarType.BOOLEAN, false),


    /**
     * whether stone is hardened to encourage cave exploration over tunneling
     */
    SUPER_HARD_STONE
            ("ExtraHardMode.World Rules.Mining.Inhibit Tunneling", VarType.BOOLEAN, true),
    /**
     * The next two entries configure how much extra durability loss iron and diamond picks take when SUPER_HARD_STONE is TRUE
     */
    IRON_DURABILITY_PENALTY
            ("ExtraHardMode.World Rules.Mining.If Tunneling Is Inhibited.Number of Stone Iron Pickaxe Can Mine", VarType.INTEGER, 32),
    DIAMOND_DURABILITY_PENALTY
            ("ExtraHardMode.World Rules.Mining.If Tunneling Is Inhibited.Number of Stone Diamond Pickaxe Can Mine", VarType.INTEGER, 71),
    /**
     * Breaking an ore will cause surrounding stone to turn to cobble and fall
     */
    SUPER_HARD_STONE_PHYSICS
            ("ExtraHardMode.World Rules.Mining.Breaking Ore Softens Adjacent Stone", VarType.BOOLEAN, true),
    /**
     * When true, and when used with SUPER_HARD_STONE, mining stone will cause adjacent stone to turn to cobble, as if mining ore
     */
    STONE_LIKE_ORE
            ("ExtraHardMode.World Rules.Mining.If Breaking Ore Softens Stone.Breaking Stone Also Softens Adjacent Stone", VarType.BOOLEAN, true),
    /**
     * maximum y for placing standard torches
     */
    STANDARD_TORCH_MIN_Y
            ("ExtraHardMode.World Rules.Torches.Max Y", VarType.INTEGER, 30),
    /**
     * whether players are limited to placing torches against specific materials
     */
    LIMITED_TORCH_PLACEMENT
            ("ExtraHardMode.World Rules.Torches.No Placement On Soft Materials", VarType.BOOLEAN, true),
    /**
     * whether rain should break torches
     */
    RAIN_BREAKS_TORCHES
            ("ExtraHardMode.World Rules.Torches.Rain Breaks Torches", VarType.BOOLEAN, true),
    /**
     * whether TNT should be more powerful and plentiful
     */
    BETTER_TNT
            ("ExtraHardMode.World Rules.Better Tnt.Bigger Explosions", VarType.BOOLEAN, true),
    /**
     * wheter the crafting recipe should give more tnt
     */
    MORE_TNT_NUMBER
            ("ExtraHardMode.World Rules.Better Tnt.Tnt Per Recipe", VarType.INTEGER, 3),
    /**
     * Sound when torch placing fails
     */
    SOUNDS_TORCH_FIZZ
            ("ExtraHardMode.World Rules.Play Sounds.Torch Fizzing", VarType.BOOLEAN, true),
    /**
     * Warning Sound when a creeper drops tnt
     */
    SOUND_CREEPER_TNT
            ("ExtraHardMode.World Rules.Play Sounds.Creeper Tnt Warning", VarType.BOOLEAN, true),
    /**
     * percent chance for broken netherrack to start a fire
     */
    BROKEN_NETHERRACK_CATCHES_FIRE_PERCENT
            ("ExtraHardMode.World Rules.Breaking Netherrack Starts Fire Percent", VarType.INTEGER, 20),
    /**
     * whether players may place blocks directly underneath themselves
     */
    LIMITED_BLOCK_PLACEMENT
            ("ExtraHardMode.World Rules.Limited Block Placement", VarType.BOOLEAN, true),
    /**
     * whether tree logs respect gravity
     */
    BETTER_TREE_CHOPPING
            ("ExtraHardMode.World Rules.Better Tree Felling", VarType.BOOLEAN, true),
    /**
     * whether players take additional damage and/or debuffs from environmental injuries
     */


    ENHANCED_ENVIRONMENTAL_DAMAGE
            ("ExtraHardMode.Player.Enhanced Environmental Injuries", VarType.BOOLEAN, true),
    /**
     * whether players catch fire when extinguishing a fire up close
     */
    EXTINGUISHING_FIRE_IGNITES_PLAYERS
            ("ExtraHardMode.Player.Extinguishing Fires Ignites Player", VarType.BOOLEAN, true),
    /**
     * percentage of item stacks lost on death
     */
    PLAYER_DEATH_ITEM_STACKS_FORFEIT_PERCENT
            ("ExtraHardMode.Player.Death.Item Stacks Forfeit Percent", VarType.INTEGER, 10),
    /**
     * how much health after respawn
     */
    PLAYER_RESPAWN_HEALTH
            ("ExtraHardMode.Player.Death.Respawn Health", VarType.INTEGER, 15),
    /**
     * how much food bar after respawn
     */
    PLAYER_RESPAWN_FOOD_LEVEL
            ("ExtraHardMode.Player.Death.Respawn Foodlevel", VarType.INTEGER, 15),
    /**
     * whether players may swim while wearing armor
     */
    NO_SWIMMING_IN_ARMOR("ExtraHardMode.Player.No Swimming When Too Heavy.Enable", VarType.BOOLEAN, true),
    /**
     * Block Swimming Up WaterFalls/WaterElevators
     */
    NO_SWIMMING_IN_ARMOR_BLOCK_ELEVATORS("ExtraHardMode.Player.No Swimming When Too Heavy.Block Elevators/Waterfalls", VarType.BOOLEAN, true),
    /**
     * The maximum amount of points you can have before being too heavy
     */
    NO_SWIMMING_IN_ARMOR_MAX_POINTS("ExtraHardMode.Player.No Swimming When Too Heavy.Max Points", VarType.DOUBLE, 18.0),
    /**
     * The amount of points a piece of armor adds to the max
     */
    NO_SWIMMING_IN_ARMOR_ARMOR_POINTS("ExtraHardMode.Player.No Swimming When Too Heavy.One Piece Of Worn Armor Adds", VarType.DOUBLE, 2.0),
    /**
     * The amount of points that stuff in your inventory adds to the max
     */
    NO_SWIMMING_IN_ARMOR_INV_POINTS("ExtraHardMode.Player.No Swimming When Too Heavy.One Stack Adds", VarType.DOUBLE, 1.0),
    /**
     * How much a tool or item which doesn't stack adds to the max
     */
    NO_SWIMMING_IN_ARMOR_TOOL_POINTS("ExtraHardMode.Player.No Swimming When Too Heavy.One Tool Adds", VarType.DOUBLE, 0.5),
    /**
     * How fast do you drown, 100 (percent) = you drown no chance, 25 there is a chance you'll drown
     */
    NO_SWIMMING_IN_ARMOR_DROWN_RATE("ExtraHardMode.Player.No Swimming When Too Heavy.Drown Rate", VarType.INTEGER, 25),
    /**
     * How much do you drown faster per weight over the max
     */
    NO_SWIMMING_IN_ARMOR_ENCUMBRANCE_EXTRA("ExtraHardMode.Player.No Swimming When Too Heavy.Overencumbrance Adds To Drown Rate", VarType.INTEGER, 2),

    /**
     * whether monster grinders (or "farms") should be inhibited
     */
    INHIBIT_MONSTER_GRINDERS
            ("ExtraHardMode.General Monster Rules.Inhibit Monster Grinders", VarType.BOOLEAN, true),
    /**
     * max y value for extra monster spawns
     */
    MORE_MONSTERS_MAX_Y
            ("ExtraHardMode.General Monster Rules.More Monsters.Max Y", VarType.INTEGER, 55),
    /**
     * what to multiply monster spawns by
     */
    MORE_MONSTERS_MULTIPLIER
            ("ExtraHardMode.General Monster Rules.More Monsters.Multiplier", VarType.INTEGER, 2),
    /**
     * max y value for monsters to spawn in the light
     */
    MONSTER_SPAWNS_IN_LIGHT_MAX_Y
            ("ExtraHardMode.General Monster Rules.Monsters Spawn In Light Max Y", VarType.INTEGER, 50),

    /**
     * whether zombies apply a debuff to players on hit
     */
    ZOMBIES_DEBILITATE_PLAYERS
            ("ExtraHardMode.Zombies.Slow Players", VarType.BOOLEAN, true),
    /**
     * percent chance for a zombie to reanimate after death
     */
    ZOMBIES_REANIMATE_PERCENT
            ("ExtraHardMode.Zombies.Reanimate Percent", VarType.INTEGER, 50),

    /**
     * percent chance skeletons have a chance to knock back targets with arrows
     */
    SKELETONS_KNOCK_BACK_PERCENT
            ("ExtraHardMode.Skeletons.Arrows Knockback Percent", VarType.INTEGER, 30),
    /**
     * percent chance skeletons will release silverfish instead of firing arrows
     */
    SKELETONS_RELEASE_SILVERFISH
            ("ExtraHardMode.Skeletons.Shoot Silverfish Percent", VarType.INTEGER, 30),
    /**
     * whether or not arrows will pass harmlessly through skeletons
     */
    SKELETONS_DEFLECT_ARROWS
            ("ExtraHardMode.Skeletons.Deflect Arrows Percent", VarType.INTEGER, 100),
    /**
     * If Silverfish cant enter stone etc and turn it into a silverfish block
     */
    SILVERFISH_CANT_ENTER_BLOCKS
            ("ExtraHardMode.Silverfish.Cant enter blocks", VarType.BOOLEAN, true),

    /**
     * percentage of zombies which will be replaced with spiders under sea level
     */
    BONUS_UNDERGROUND_SPIDER_SPAWN_PERCENT
            ("ExtraHardMode.Spiders.Bonus Underground Spawn Percent", VarType.INTEGER, 20),
    /**
     * whether spiders drop webbing when they die
     */
    SPIDERS_DROP_WEB_ON_DEATH
            ("ExtraHardMode.Spiders.Drop Web On Death", VarType.BOOLEAN, true),

    /**
     * percentage of creepers which will spawn charged
     */
    CHARGED_CREEPER_SPAWN_PERCENT
            ("ExtraHardMode.Creepers.Charged Creeper Spawn Percent", VarType.INTEGER, 10),
    /**
     * percentage of creepers which spawn activated TNT on death
     */
    CREEPERS_DROP_TNT_ON_DEATH_PERCENT
            ("ExtraHardMode.Creepers.Drop Tnt On Death.Percent", VarType.INTEGER, 20),
    /**
     * max y for creepers to drop tnt, to restrict them to caves
     */
    CREEPERS_DROP_TNT_ON_DEATH_MAX_Y
            ("ExtraHardMode.Creepers.Drop Tnt On Death.Max Y", VarType.INTEGER, 50),
    /**
     * whether charged creepers explode when damaged
     */
    CHARGED_CREEPERS_EXPLODE_ON_HIT
            ("ExtraHardMode.Creepers.Charged Creepers Explode On Damage", VarType.BOOLEAN, true),
    /**
     * whether creepers explode when caught on fire
     */
    FLAMING_CREEPERS_EXPLODE
            ("ExtraHardMode.Creepers.Fire Triggers Explosion.Enable", VarType.BOOLEAN, true),
    /**
     * Number of Fireworks to show when creeper launches
     */
    FLAMING_CREEPERS_FIREWORK
            ("ExtraHardMode.Creepers.Fire Triggers Explosion.Firework Count", VarType.INTEGER, 3),
    /**
     * Speed at which a creeper ascends
     */
    FLAMING_CREEPERS_ROCKET
            ("ExtraHardMode.Creepers.Fire Triggers Explosion.Launch In Air Speed", VarType.DOUBLE, 0.5),

    /**
     * percentage of skeletons near bedrock which will be replaced with blazes
     */
    NEAR_BEDROCK_BLAZE_SPAWN_PERCENT
            ("ExtraHardMode.Blazes.Near Bedrock Spawn Percent", VarType.INTEGER, 50),
    /**
     * percentage of pig zombies which will be replaced with blazes
     */
    BONUS_NETHER_BLAZE_SPAWN_PERCENT
            ("ExtraHardMode.Blazes.Bonus Nether Spawn Percent", VarType.INTEGER, 20),
    /**
     * whether blazes explode and spread fire when they die
     */
    BLAZES_EXPLODE_ON_DEATH
            ("ExtraHardMode.Blazes.Explode On Death", VarType.BOOLEAN, true),
    /**
     * whether blazes drop fire when damaged
     */
    BLAZES_DROP_FIRE_ON_DAMAGE
            ("ExtraHardMode.Blazes.Drop Fire On Damage", VarType.BOOLEAN, true),
    /**
     * whether blazes drop extra loot
     */
    BLAZES_DROP_BONUS_LOOT
            ("ExtraHardMode.Blazes.Bonus Loot", VarType.BOOLEAN, true),
    /**
     * percentage chance that a blaze slain in the nether will split into two
     * blazes
     */
    NETHER_BLAZES_SPLIT_ON_DEATH_PERCENT
            ("ExtraHardMode.Blazes.Nether Split On Death Percent", VarType.INTEGER, 25),

    /**
     * percentage chance that a blaze spawn will trigger a flame slime spawn as
     * well
     */
    FLAME_SLIMES_SPAWN_WITH_NETHER_BLAZE_PERCENT
            ("ExtraHardMode.MagmaCubes.Spawn With Nether Blaze Percent", VarType.INTEGER, 100),
    /**
     * whether damaging a magma cube turns it into a blaze
     */
    MAGMA_CUBES_BECOME_BLAZES_ON_DAMAGE
            ("ExtraHardMode.MagmaCubes.Grow Into Blazes On Damage", VarType.BOOLEAN, true),


    /**
     * whether pig zombies are always hostile
     */
    ALWAYS_ANGRY_PIG_ZOMBIES
            ("ExtraHardMode.PigZombies.Always Angry", VarType.BOOLEAN, true),
    /**
     * whether pig zombies always drop nether wart in nether fortresses
     */
    FORTRESS_PIGS_DROP_WART
            ("ExtraHardMode.PigZombies.Always Drop Netherwart In Fortresses", VarType.BOOLEAN, true),
    /**
     * Whether pig zombies should drop netherwart occasionally elsewhere in Nether
     */
    NETHER_PIGS_DROP_WART
            ("ExtraHardMode.PigZombies.Percent Chance to Drop Netherwart Elsewhere In Nether", VarType.INTEGER, 0),
    
    /**
     * whether ghasts should deflect arrows and drop extra loot
     * percentage like skeleton deflect
     */
    GHASTS_DEFLECT_ARROWS
            ("ExtraHardMode.Ghasts.Immune To Arrows", VarType.BOOLEAN, true),
    /**
     * whether endermen may teleport players
     */
    IMPROVED_ENDERMAN_TELEPORTATION
            ("ExtraHardMode.Endermen.May Teleport Players", VarType.BOOLEAN, true),
    /**
     * percentage of surface zombies which spawn as witches
     */
    BONUS_WITCH_SPAWN_PERCENT
            ("ExtraHardMode.Witches.Bonus Spawn Percent", VarType.INTEGER, 5),

    /**
     * whether the ender dragon respawns
     */
    RESPAWN_ENDER_DRAGON
            ("ExtraHardMode.EnderDragon.Respawns", VarType.BOOLEAN, true),
    /**
     * whether it drops an egg when slain
     */
    ENDER_DRAGON_DROPS_EGG
            ("ExtraHardMode.EnderDragon.Drops Dragonegg", VarType.BOOLEAN, true),
    /**
     * whether it drops a pair of villager eggs when slain
     */
    ENDER_DRAGON_DROPS_VILLAGER_EGGS
            ("ExtraHardMode.EnderDragon.Drops 2 Villager Eggs", VarType.BOOLEAN, true),
    /**
     * whether the dragon spits fireballs and summons minions
     */
    ENDER_DRAGON_ADDITIONAL_ATTACKS
            ("ExtraHardMode.EnderDragon.Harder Battle", VarType.BOOLEAN, true),
    /**
     * whether server wide messages will broadcast player victories and defeats
     */
    ENDER_DRAGON_COMBAT_ANNOUNCEMENTS
            ("ExtraHardMode.EnderDragon.Battle Announcements", VarType.BOOLEAN, true),
    /**
     * whether players will be allowed to build in the end
     */
    ENDER_DRAGON_NO_BUILDING
            ("ExtraHardMode.EnderDragon.No Building Allowed", VarType.BOOLEAN, true),

    /**
     * whether food crops die more easily
     */
    WEAK_FOOD_CROPS
            ("ExtraHardMode.Farming.Weak Crops.Enable", VarType.BOOLEAN, true),
    /**
     * How much percent of plants you loose
     */
    WEAK_FOOD_CROPS_LOSS_RATE
            ("ExtraHardMode.Farming.Weak Crops.Loss Rate", VarType.INTEGER, 25),
    /**
     * Should desserts be really empty and hostile towards plants
     */
    ARID_DESSERTS
            ("ExtraHardMode.Farming.Weak Crops.Infertile Deserts", VarType.BOOLEAN, true),
    /**
     * Weather Snow should break crops
     */
    SNOW_BREAKS_CROPS
            ("ExtraHardMode.Farming.Weak Crops.Snow Breaks Crops", VarType.BOOLEAN, true),
    /**
     * Should you be able to craft melonseeds
     */
    CANT_CRAFT_MELONSEEDS
            ("ExtraHardMode.Farming.Cant Craft Melonseeds", VarType.BOOLEAN, true),
    /**
     * whether bonemeal may be used on mushrooms
     */
    NO_BONEMEAL_ON_MUSHROOMS
            ("ExtraHardMode.Farming.No Bonemeal On Mushrooms", VarType.BOOLEAN, true),
    /**
     * whether nether wart will ever drop more than 1 wart when broken
     */
    NO_FARMING_NETHER_WART
            ("ExtraHardMode.Farming.No Farming Nether Wart", VarType.BOOLEAN, true),
    /**
     * whether sheep will always regrow white wool
     */
    SHEEP_REGROW_WHITE_WOOL
            ("ExtraHardMode.Farming.Sheep Grow Only White Wool", VarType.BOOLEAN, true),
    /**
     * whether players may move water source blocks
     */
    DONT_MOVE_WATER_SOURCE_BLOCKS
            ("ExtraHardMode.Farming.Buckets Dont Move Water Sources", VarType.BOOLEAN, true),
    /**
     * wheter animals should drop exp
     */
    ANIMAL_EXP_NERF
            ("ExtraHardMode.Farming.Animal Experience Nerf", VarType.BOOLEAN, true),
    /**
     * Wheter More Falling blocks should be enabled
     */
    MORE_FALLING_BLOCKS_ENABLE
            ("ExtraHardMode.Additional Falling Blocks.Enable", VarType.BOOLEAN, true),
    /**
     * wheter falling grass/mycel turns into dirt
     */
    MORE_FALLING_BLOCKS_TURN_TO_DIRT
            ("ExtraHardMode.Additional Falling Blocks.Turn Mycel/Grass To Dirt", VarType.BOOLEAN, true),
    /**
     * which materials beyond sand and gravel should be subject to gravity
     */
    MORE_FALLING_BLOCKS
            ("ExtraHardMode.Additional Falling Blocks.Enabled Blocks", VarType.LIST, new DefaultFallingBlocks());


    /**
     * Path.
     */
    private final String path;
    /**
     * Variable type.
     */
    private final VarType type;
    /**
     * Default value.
     */
    private final Object defaultValue;

    /**
     * Constructor.
     *
     * @param path - Configuration path.
     * @param type - Variable type.
     * @param def  - Default value.
     */
    private RootNode(String path, VarType type, Object def)
    {
        this.path = path;
        this.type = type;
        this.defaultValue = def;
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public VarType getVarType()
    {
        return type;
    }

    @Override
    public Object getDefaultValue()
    {
        return defaultValue;
    }

    /**
     * Default list of falling blocks.
     */
    private static class DefaultFallingBlocks extends ArrayList<String>
    {

        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public DefaultFallingBlocks()
        {
            super();
            this.add(Material.DIRT.toString());
            this.add(Material.GRASS.toString());
            this.add(Material.COBBLESTONE.toString());
            this.add(Material.MOSSY_COBBLESTONE.toString());
            this.add(Material.MYCEL.toString());
            this.add(Material.JACK_O_LANTERN.toString());
        }
    }

}
