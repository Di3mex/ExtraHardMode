package me.ryanhamshire.ExtraHardMode.features;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

//TODO world check
//TODO netherrack breaks tools
//TODO netherrack slow to break
public class Antigrinder implements Listener
{
    ExtraHardMode plugin;
    RootConfig CFG;
    MessageConfig messages;
    EntityModule entityModule;
    BlockModule blockModule;
    UtilityModule utils;

    public Antigrinder (ExtraHardMode plugin)
    {
        this.plugin = plugin;
        CFG = plugin.getModuleForClass(RootConfig.class);
        messages = plugin.getModuleForClass(MessageConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
        blockModule = plugin.getModuleForClass(BlockModule.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
    }

    /**
     * When an Animal/Monster spawns check if the Location is "natural"
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        LivingEntity entity = event.getEntity();
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        final boolean inhibitMonsterGrindersEnabled = CFG.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS, world.getName());
        final int blazeBonusSpawnPercent = CFG.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT, world.getName());

        // FEATURE: inhibited monster grinders/farms
        if (inhibitMonsterGrindersEnabled)
        {
            // spawners and spawn eggs always spawn a monster, but the monster doesn't drop any loot
            if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER && !((entity instanceof Blaze)) && blazeBonusSpawnPercent <= 0)
            {
                entityModule.markLootLess(entity);
            }

            // otherwise, consider environment to stop monsters from spawning in non-natural places
            else if ((reason == CreatureSpawnEvent.SpawnReason.NATURAL || reason == CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION) && entity instanceof Monster)
            {
                World.Environment environment = location.getWorld().getEnvironment();

                Material underBlockType = location.getBlock().getRelative(BlockFace.DOWN).getType();
                switch (environment)
                {
                    case NORMAL:
                        if (utils.isNaturalSpawnMaterial(underBlockType))
                        {
                            event.setCancelled(true);
                        }
                        break;
                    case NETHER:
                        if (utils.isNaturalNetherSpawnMaterial(underBlockType))
                        {
                            event.setCancelled(true);
                        }
                        break;
                    case THE_END:
                        if (underBlockType != Material.ENDER_STONE && underBlockType != Material.OBSIDIAN && underBlockType != Material.AIR)
                        {
                            // ender dragon
                            event.setCancelled(true);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * When an entity dies check if loot should be blocked due to AntiGrinder
     * @param event
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean inhibitMonsterGrindersEnabled = CFG.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS, world.getName());

        // FEATURE: monsters which take environmental damage or spawn from spawners don't drop loot and exp (monster grinder inhibitor)
        if (inhibitMonsterGrindersEnabled && entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.SQUID)
        {
            boolean noLoot = false;

            //animals aren't affected by antigrinder
            if (!entityModule.isCattle(entity))
            {
                if (entityModule.isLootLess(entity))
                {
                    noLoot = true;
                }
                else if (entity instanceof Skeleton)
                {
                    Skeleton skeleton = (Skeleton) entity;
                    //Stuck Wither Skeletons
                    if (skeleton.getSkeletonType() == Skeleton.SkeletonType.WITHER && skeleton.getEyeLocation().getBlock().getType() != Material.AIR)
                    {
                        noLoot = true;
                    }
                }
                else if (entity instanceof Enderman)
                {
                    //Enderman spawned in block
                    if (entity.getEyeLocation().getBlock().getType() != Material.AIR)
                    {
                        noLoot = true;
                    }
                }
                else
                {
                    // also no loot for monsters which die standing in water
                    Block block = entity.getLocation().getBlock();
                    Block underBlock = block.getRelative(BlockFace.DOWN);
                    BlockFace[] adjacentFaces = blockModule.getHorizontalAdjacentFaces();
                    //All the adjacentBlocks
                    Block[] adjacentBlocks = new Block[adjacentFaces.length * 2 + 1];
                    adjacentBlocks[0] = block;
                    for (int i = 0; i < adjacentFaces.length; i++)
                    {
                        adjacentBlocks[i+1] = block.getRelative(adjacentFaces[i]);
                    }
                    for (int i = 0; i < adjacentFaces.length; i++)
                    {
                        adjacentBlocks[i+adjacentFaces.length] = underBlock.getRelative(adjacentFaces[i]);
                    }
                    for (Block adjacentBlock : adjacentBlocks)
                    {
                        if (adjacentBlock!=null && (adjacentBlock.getType() == Material.WATER || adjacentBlock.getType() == Material.STATIONARY_WATER))
                        {
                            noLoot = true;
                            break;
                        }
                    }

                    // also no loot for monsters who can't reach their (melee) killers
                    Player killer = entity.getKiller();
                    if (killer != null)
                    {
                        Location monsterEyeLocation = entity.getEyeLocation();
                        Location playerEyeLocation = killer.getEyeLocation();

                        // interpolate locations
                        Location[] locations = new Location[]{
                                new Location(monsterEyeLocation.getWorld(), .2 * monsterEyeLocation.getX() + .8 * playerEyeLocation.getX(),
                                        monsterEyeLocation.getY(), .2 * monsterEyeLocation.getZ() + .8 * playerEyeLocation.getZ()),
                                new Location(monsterEyeLocation.getWorld(), .5 * monsterEyeLocation.getX() + .5 * playerEyeLocation.getX(),
                                        monsterEyeLocation.getY(), .5 * monsterEyeLocation.getZ() + .5 * playerEyeLocation.getZ()),
                                new Location(monsterEyeLocation.getWorld(), .8 * monsterEyeLocation.getX() + .2 * playerEyeLocation.getX(),
                                        monsterEyeLocation.getY(), .8 * monsterEyeLocation.getZ() + .2 * playerEyeLocation.getZ()),};

                        for (Location middleLocation : locations)
                        {
                            // monster is blocked at eye level, unable to advance toward killer
                            if (middleLocation.getBlock().getType() != Material.AIR)
                            {
                                noLoot = true;
                            }

                            // monster doesn't have room above to hurdle a foot level block, unable to advance toward killer
                            else
                            {
                                Block bottom = middleLocation.getBlock().getRelative(BlockFace.DOWN);
                                Block top = middleLocation.getBlock().getRelative(BlockFace.UP);
                                if (top.getType() != Material.AIR && bottom.getType() != Material.AIR || bottom.getType() == Material.FENCE
                                        || bottom.getType() == Material.FENCE_GATE || bottom.getType() == Material.COBBLE_WALL
                                        || bottom.getType() == Material.NETHER_FENCE)
                                {
                                    noLoot = true;
                                }
                            }
                        }
                    }
                }

                if (noLoot)
                {
                    event.setDroppedExp(0);
                    event.getDrops().clear();
                }
            }
        }
    }

    /**
     * When an entity takes damage
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        final boolean inhibitMonsterGrindersEnabled = CFG.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS, world.getName());

        // FEATURE: monsters which take environmental damage don't drop loot or experience (monster grinder inhibitor)
        if (inhibitMonsterGrindersEnabled && entity instanceof LivingEntity)
        {
            EntityDamageEvent.DamageCause damageCause = event.getCause();
            if (damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK && damageCause != EntityDamageEvent.DamageCause.PROJECTILE && damageCause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
            {
                entityModule.addEnvironmentalDamage((LivingEntity) entity, event.getDamage());
            }
        }
    }

}
