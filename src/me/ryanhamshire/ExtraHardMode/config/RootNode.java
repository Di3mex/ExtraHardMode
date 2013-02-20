package me.ryanhamshire.ExtraHardMode.config;

import java.util.ArrayList;

import org.bukkit.Material;

import me.ryanhamshire.ExtraHardMode.service.ConfigNode;

/**
 * Configuration options of the root config.yml file.
 */
public enum RootNode implements ConfigNode {
   /**
    * list of worlds where extra hard mode rules apply
    */
   WORLDS("ExtraHardMode.Worlds", VarType.LIST, new ArrayList<String>()),
   /**
    * minimum y for placing standard torches
    */
   STANDARD_TORCH_MIN_Y("ExtraHardMode.PermanentFlameMinYCoord", VarType.INTEGER, 30),
   /**
    * whether stone is hardened to encourage cave exploration over tunneling
    */
   SUPER_HARD_STONE("ExtraHardMode.HardenedStone", VarType.BOOLEAN, true),
   /**
    * whether players take additional damage and/or debuffs from environmental
    * injuries
    */
   ENHANCED_ENVIRONMENTAL_DAMAGE("ExtraHardMode.EnhancedEnvironmentalInjuries", VarType.BOOLEAN, true),
   /**
    * whether players catch fire when extinguishing a fire up close
    */
   EXTINGUISHING_FIRE_IGNITES_PLAYERS("ExtraHardMode.ExtinguishingFiresIgnitesPlayers", VarType.BOOLEAN, true),
   /**
    * whether TNT should be more powerful and plentiful
    */
   BETTER_TNT("ExtraHardMode.BetterTNT", VarType.BOOLEAN, true),
   /**
    * whether monster grinders (or "farms") should be inhibited
    */
   INHIBIT_MONSTER_GRINDERS("ExtraHardMode.InhibitMonsterGrinders", VarType.BOOLEAN, true),
   /**
    * whether players may place blocks directly underneath themselves
    */
   LIMITED_BLOCK_PLACEMENT("ExtraHardMode.LimitedBlockPlacement", VarType.BOOLEAN, true),
   /**
    * whether players are limited to placing torches against specific materials
    */
   LIMITED_TORCH_PLACEMENT("ExtraHardMode.LimitedTorchPlacement", VarType.BOOLEAN, true),
   /**
    * whether rain should break torches
    */
   RAIN_BREAKS_TORCHES("ExtraHardMode.RainBreaksTorches", VarType.BOOLEAN, true),
   /**
    * percent chance for broken netherrack to start a fire
    */
   BROKEN_NETHERRACK_CATCHES_FIRE_PERCENT("ExtraHardMode.NetherrackCatchesFirePercent", VarType.INTEGER, 20),
   /**
    * max y value for extra monster spawns
    */
   MORE_MONSTERS_MAX_Y("ExtraHardMode.MoreMonsters.MaxYCoord", VarType.INTEGER, 55),
   /**
    * what to multiply monster spawns by
    */
   MORE_MONSTERS_MULTIPLIER("ExtraHardMode.MoreMonsters.Multiplier", VarType.INTEGER, 2),
   /**
    * max y value for monsters to spawn in the light
    */
   MONSTER_SPAWNS_IN_LIGHT_MAX_Y("ExtraHardMode.MonstersSpawnInLightMaxY", VarType.INTEGER, 50),
   /**
    * whether zombies apply a debuff to players on hit
    */
   ZOMBIES_DEBILITATE_PLAYERS("ExtraHardMode.Zombies.SlowPlayers", VarType.BOOLEAN, true),
   /**
    * percent chance for a zombie to reanimate after death
    */
   ZOMBIES_REANIMATE_PERCENT("ExtraHardMode.Zombies.ReanimatePercent", VarType.INTEGER, 50),
   /**
    * percent chance skeletons have a chance to knock back targets with arrows
    */
   SKELETONS_KNOCK_BACK_PERCENT("ExtraHardMode.Skeletons.ArrowsKnockBackPercent", VarType.INTEGER, 30),
   /**
    * percent chance skeletons will release silverfish instead of firing arrows
    */
   SKELETONS_RELEASE_SILVERFISH("ExtraHardMode.Skeletons.ReleaseSilverfishPercent", VarType.INTEGER, 30),
   /**
    * whether or not arrows will pass harmlessly through skeletons
    */
   SKELETONS_DEFLECT_ARROWS("ExtraHardMode.Skeletons.DeflectArrowsPercent", VarType.INTEGER, 100),
   /**
    * percentage of zombies which will be replaced with spiders under sea level
    */
   BONUS_UNDERGROUND_SPIDER_SPAWN_PERCENT("ExtraHardMode.Spiders.BonusUndergroundSpawnPercent", VarType.INTEGER, 20),
   /**
    * whether spiders drop webbing when they die
    */
   SPIDERS_DROP_WEB_ON_DEATH("ExtraHardMode.Spiders.DropWebOnDeath", VarType.BOOLEAN, true),
   /**
    * percentage of surface zombies which spawn as witches
    */
   BONUS_WITCH_SPAWN_PERCENT("ExtraHardMode.Witches.BonusSpawnPercent", VarType.INTEGER, 5),
   /**
    * percentage of creepers which will spawn charged
    */
   CHARGED_CREEPER_SPAWN_PERCENT("ExtraHardMode.Creepers.ChargedCreeperSpawnPercent", VarType.INTEGER, 5),
   /**
    * percentage of creepers which spawn activated TNT on death
    */
   CREEPERS_DROP_TNT_ON_DEATH_PERCENT("ExtraHardMode.Creepers.DropTNTOnDeathPercent", VarType.INTEGER, 20),
   /**
    * whether charged creepers explode when damaged
    */
   CHARGED_CREEPERS_EXPLODE_ON_HIT("ExtraHardMode.Creepers.ChargedCreepersExplodeOnDamage", VarType.BOOLEAN, true),
   /**
    * whether creepers explode when caught on fire
    */
   FLAMING_CREEPERS_EXPLODE("ExtraHardMode.Creepers.FireTriggersExplosion", VarType.BOOLEAN, true),
   /**
    * percentage of skeletons near bedrock which will be replaced with blazes
    */
   NEAR_BEDROCK_BLAZE_SPAWN_PERCENT("ExtraHardMode.Blazes.NearBedrockSpawnPercent", VarType.INTEGER, 50),
   /**
    * percentage of pig zombies which will be replaced with blazes
    */
   BONUS_NETHER_BLAZE_SPAWN_PERCENT("ExtraHardMode.Blazes.BonusNetherSpawnPercent", VarType.INTEGER, 20),
   /**
    * percentage chance that a blaze spawn will trigger a flame slime spawn as
    * well
    */
   FLAME_SLIMES_SPAWN_WITH_NETHER_BLAZE_PRESENT("ExtraHardMode.MagmaCubes.SpawnWithNetherBlazePercent", VarType.INTEGER, 100),
   /**
    * whether damaging a magma cube turns it into a blaze
    */
   MAGMA_CUBES_BECOME_BLAZES_ON_DAMAGE("ExtraHardMode.MagmaCubes.GrowIntoBlazesOnDamage", VarType.BOOLEAN, true),
   /**
    * whether blazes explode and spread fire when they die
    */
   BLAZES_EXPLODE_ON_DEATH("ExtraHardMode.Blazes.ExplodeOnDeath", VarType.BOOLEAN, true),
   /**
    * whether blazes drop fire when damaged
    */
   BLAZES_DROP_FIRE_ON_DAMAGE("ExtraHardMode.Blazes.DropFireOnDamage", VarType.BOOLEAN, true),
   /**
    * whether blazes drop extra loot
    */
   BLAZES_DROP_BONUS_LOOT("ExtraHardMode.Blazes.BonusLoot", VarType.BOOLEAN, true),
   /**
    * percentage chance that a blaze slain in the nether will split into two
    * blazes
    */
   NETHER_BLAZES_SPLIT_ON_DEATH_PERCENT("ExtraHardMode.Blazes.NetherSplitOnDeathPercent", VarType.INTEGER, 25),
   /**
    * whether pig zombies are always hostile
    */
   ALWAYS_ANGRY_PIG_ZOMBIES("ExtraHardMode.PigZombies.AlwaysAngry", VarType.BOOLEAN, true),
   /**
    * whether pig zombies drop nether wart in nether fortresses
    */
   FORTRESS_PIGS_DROP_WART("ExtraHardMode.PigZombies.DropWartInFortresses", VarType.BOOLEAN, true),
   /**
    * whether ghasts should deflect arrows and drop extra loot TODO make this a
    * percentage like skeleton deflect
    */
   GHASTS_DEFLECT_ARROWS("ExtraHardMode.Ghasts.DeflectArrows", VarType.BOOLEAN, true),
   /**
    * whether endermen may teleport players
    */
   IMPROVED_ENDERMAN_TELEPORTATION("ExtraHardMode.Endermen.MayTeleportPlayers", VarType.BOOLEAN, true),
   /**
    * whether the ender dragon respawns
    */
   RESPAWN_ENDER_DRAGON("ExtraHardMode.EnderDragon.Respawns", VarType.BOOLEAN, true),
   /**
    * whether it drops an egg when slain
    */
   ENDER_DRAGON_DROPS_EGG("ExtraHardMode.EnderDragon.DropsEgg", VarType.BOOLEAN, true),
   /**
    * whether it drops a pair of villager eggs when slain
    */
   ENDER_DRAGON_DROPS_VILLAGER_EGGS("ExtraHardMode.EnderDragon.DropsVillagerEggs", VarType.BOOLEAN, true),
   /**
    * whether the dragon spits fireballs and summons minions
    */
   ENDER_DRAGON_ADDITIONAL_ATTACKS("ExtraHardMode.EnderDragon.HarderBattle", VarType.BOOLEAN, true),
   /**
    * whether server wide messages will broadcast player victories and defeats
    */
   ENDER_DRAGON_COMBAT_ANNOUNCEMENTS("ExtraHardMode.EnderDragon.BattleAnnouncements", VarType.BOOLEAN, true),
   /**
    * whether players will be allowed to build in the end
    */
   ENDER_DRAGON_NO_BUILDING("ExtraHardMode.EnderDragon.NoBuildingAllowed", VarType.BOOLEAN, true),
   /**
    * whether food crops die more easily
    */
   WEAK_FOOD_CROPS("ExtraHardMode.Farming.WeakCrops", VarType.BOOLEAN, true),
   /**
    * whether bonemeal may be used on mushrooms
    */
   NO_BONEMEAL_ON_MUSHROOMS("ExtraHardMode.Farming.NoBonemealOnMushrooms", VarType.BOOLEAN, true),
   /**
    * whether nether wart will ever drop more than 1 wart when broken
    */
   NO_FARMING_NETHER_WART("ExtraHardMode.Farming.NoFarmingNetherWart", VarType.BOOLEAN, true),
   /**
    * whether sheep will always regrow white wool
    */
   SHEEP_REGROW_WHITE_WOOL("ExtraHardMode.Farming.SheepGrowOnlyWhiteWool", VarType.BOOLEAN, true),
   /**
    * whether players may move water source blocks
    */
   DONT_MOVE_WATER_SOURCE_BLOCKS("ExtraHardMode.Farming.BucketsDontMoveWaterSources", VarType.BOOLEAN, true),
   /**
    * whether players may swim while wearing armor
    */
   NO_SWIMMING_IN_ARMOR("ExtraHardMode.NoSwimmingWhenHeavy", VarType.BOOLEAN, true),
   /**
    * percentage of item stacks lost on death
    */
   PLAYER_DEATH_ITEM_STACKS_FORFEIT_PERCENT("ExtraHardMode.PlayerDeath.ItemStacksForfeitPercent", VarType.INTEGER, 10),
   /**
    * how much health after respawn
    */
   PLAYER_RESPAWN_HEALTH("ExtraHardMode.PlayerDeath.RespawnHealth", VarType.INTEGER, 15),
   /**
    * how much food bar after respawn
    */
   PLAYER_RESPAWN_FOOD_LEVEL("ExtraHardMode.PlayerDeath.RespawnFoodLevel", VarType.INTEGER, 15),
   /**
    * whether tree logs respect gravity
    */
   BETTER_TREE_CHOPPING("ExtraHardMode.BetterTreeFelling", VarType.BOOLEAN, true),
   /**
    * explosions disable option, needed to dodge bugs in popular plugins
    */
   WORK_AROUND_EXPLOSION_BUGS("ExtraHardMode.WorkAroundOtherPluginsExplosionBugs", VarType.BOOLEAN, false),
   /**
    * which materials beyond sand and gravel should be subject to gravity
    */
   MORE_FALLING_BLOCKS("ExtraHardMode.AdditionalFallingBlocks", VarType.LIST, new DefaultFallingBlocks());

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
    * @param path
    *           - Configuration path.
    * @param type
    *           - Variable type.
    * @param def
    *           - Default value.
    */
   private RootNode(String path, VarType type, Object def) {
      this.path = path;
      this.type = type;
      this.defaultValue = def;
   }

   @Override
   public String getPath() {
      return path;
   }

   @Override
   public VarType getVarType() {
      return type;
   }

   @Override
   public Object getDefaultValue() {
      return defaultValue;
   }

   /**
    * Default list of falling blocks.
    */
   private static class DefaultFallingBlocks extends ArrayList<String> {

      /**
       * Serial Version UID.
       */
      private static final long serialVersionUID = 1L;

      /**
       * Constructor.
       */
      public DefaultFallingBlocks() {
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
