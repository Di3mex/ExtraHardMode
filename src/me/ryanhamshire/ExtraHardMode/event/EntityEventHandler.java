/*
    ExtraHardMode Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

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

package me.ryanhamshire.ExtraHardMode.event;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.config.RootConfig;
import me.ryanhamshire.ExtraHardMode.config.RootNode;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageConfig;
import me.ryanhamshire.ExtraHardMode.config.messages.MessageNode;
import me.ryanhamshire.ExtraHardMode.module.BlockModule;
import me.ryanhamshire.ExtraHardMode.module.EntityModule;
import me.ryanhamshire.ExtraHardMode.module.UtilityModule;
import me.ryanhamshire.ExtraHardMode.service.PermissionNode;
import me.ryanhamshire.ExtraHardMode.task.*;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//TODO create variables that hold all the materials, like an array for all natural blocks, etc. Makes it easy if a new block is introduced.
/**
 * Handles events related to entities.
 */
public class EntityEventHandler implements Listener
{
    /**
     * Plugin instance.
     */
    private ExtraHardMode plugin;
    /**
     * Config instance
     */
    private RootConfig rootC;
    /**
     * Awesome modules
     */
    EntityModule entityModule;
    UtilityModule utils;
    /**
     * List of players fighting a dragon.
     */
    private final List<Player> playersFightingDragon = new ArrayList<Player>();

    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public EntityEventHandler(ExtraHardMode plugin)
    {
        this.plugin = plugin;
        rootC = plugin.getModuleForClass(RootConfig.class);
        entityModule = plugin.getModuleForClass(EntityModule.class);
        utils = plugin.getModuleForClass(UtilityModule.class);
    }

    /**
     * Handles all of EHM's custom explosions,
     * this includes bigger random tnt explosions,
     * TODO fix too extreme ghasts
     * bigger ghast explosion
     * turn stone into cobble in hardened stone mode
     * the fireball-event of the dragon is used to spawn monsters
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onExplosion(EntityExplodeEvent event)
    {
        World world = event.getLocation().getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName())) {
            return;
        }
        
        // Check on the y coordinate limit in the config.
        int maxY = rootC.getInt(RootNode.EXPLOSION_MAX_HEIGHT);
        if(event.getLocation().getY() >= maxY) {
           event.setCancelled(true);
           return;
        }

        EntityModule module = plugin.getModuleForClass(EntityModule.class);

        Entity entity = event.getEntity();

        // FEATURE: bigger TNT booms, all explosions have 100% block yield
        if (rootC.getBoolean(RootNode.BETTER_TNT))
        {
            event.setYield(1);

            if (entity != null && entity.getType() == EntityType.PRIMED_TNT && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
            {
                // create more explosions nearby
                long serverTime = world.getFullTime();
                int random1 = (int) (serverTime + entity.getLocation().getBlockZ()) % 8;
                int random2 = (int) (serverTime + entity.getLocation().getBlockX()) % 8;

                Location[] locations = new Location[4];

                locations[0] = entity.getLocation().add(random1, 1, random2);
                locations[1] = entity.getLocation().add(-random2, 0, random1 / 2);
                locations[2] = entity.getLocation().add(-random1 / 2, -1, -random2);
                locations[3] = entity.getLocation().add(random1 / 2, 0, -random2 / 2);

                for (int i = 0; i < locations.length; i++)
                {
                    CreateExplosionTask task = new CreateExplosionTask(locations[i], 6F);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 3L * (i + 1));
                }
            }
        }

        // FEATURE: ender dragon fireballs may summon minions and/or set fires
        if (entity != null && entity.getType() == EntityType.FIREBALL)
        {
            Fireball fireball = (Fireball) entity;
            Entity spawnedMonster = null;
            if (fireball.getShooter() != null && fireball.getShooter().getType() == EntityType.ENDER_DRAGON)
            {
                int random = plugin.getRandom().nextInt(100);
                if (random < 40)
                {
                    spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.BLAZE);

                    for (int x1 = -2; x1 <= 2; x1++)
                    {
                        for (int z1 = -2; z1 <= 2; z1++)
                        {
                            for (int y1 = 2; y1 >= -2; y1--)
                            {
                                Block block = fireball.getLocation().add(x1, y1, z1).getBlock();
                                Material underType = block.getRelative(BlockFace.DOWN).getType();
                                if (block.getType() == Material.AIR && underType != Material.AIR && underType != Material.FIRE)
                                {
                                    block.setType(Material.FIRE);
                                }
                            }
                        }
                    }

                    Location location = fireball.getLocation().add(0, 1, 0);
                    for (int i = 0; i < 10; i++)
                    {
                        FallingBlock fire = world.spawnFallingBlock(location, Material.FIRE, (byte) 0);
                        Vector velocity = Vector.getRandom();
                        if (velocity.getY() < 0)
                        {
                            velocity.setY(velocity.getY() * -1);
                        }
                        if (plugin.getRandom().nextBoolean())
                        {
                            velocity.setZ(velocity.getZ() * -1);
                        }
                        if (plugin.getRandom().nextBoolean())
                        {
                            velocity.setX(velocity.getX() * -1);
                        }
                        fire.setVelocity(velocity);
                    }
                }
                else if (random < 70)
                {
                    for (int i = 0; i < 2; i++)
                    {
                        spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ZOMBIE);
                        module.markLootLess((LivingEntity) spawnedMonster);
                        Zombie zombie = (Zombie) spawnedMonster;
                        zombie.setVillager(true);
                    }
                }
                else
                {
                    spawnedMonster = entity.getWorld().spawnEntity(entity.getLocation(), EntityType.ENDERMAN);
                }
            }

            if (spawnedMonster != null)
            {
                module.markLootLess((LivingEntity) spawnedMonster);
            }
        }

        // FEATURE: in hardened stone mode, TNT only softens stone to cobble
        if (rootC.getBoolean(RootNode.SUPER_HARD_STONE))
        {
            List<Block> blocks = event.blockList();
            for (int i = 0; i < blocks.size(); i++)
            {
                Block block = blocks.get(i);
                if (block.getType() == Material.STONE)
                {
                    block.setType(Material.COBBLESTONE);
                    blocks.remove(i--);
                }

                // FEATURE: more falling blocks
                BlockModule physics = plugin.getModuleForClass(BlockModule.class);
                physics.physicsCheck(block, 0, true);
            }
        }
        
        //Protect blocks if enabled
        if(rootC.getBoolean(RootNode.EXPLOSION_PROTECT_ABOVE_MAX)) {
           Iterator<Block> iterator = event.blockList().iterator();
           while(iterator.hasNext()) {
              if(iterator.next().getLocation().getY() >= maxY) {
                 iterator.remove();
              }
           }
        }

        // FEATURE: more powerful ghast fireballs
        if (entity != null && entity instanceof Fireball && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            Fireball fireball = (Fireball) entity;
            if (fireball.getShooter() != null && fireball.getShooter().getType() == EntityType.GHAST)
            {
                event.setCancelled(true);
                // same as vanilla TNT, plus fire
                entity.getWorld().createExplosion(entity.getLocation(), 4F, true);
            }
        }

        // FEATURE: bigger creeper explosions (for more-frequent cave-ins)
        if (entity != null && entity instanceof Creeper && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            event.setCancelled(true);
            // same as vanilla TNT
            entity.getWorld().createExplosion(entity.getLocation(), 3F, false);
        }
    }

    /**
     * When a potion breaks
     * atm this is used for witches. When they throw a potion we sometimes spawn explosions or monsters
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPotionSplash(PotionSplashEvent event)
    {
        ThrownPotion potion = event.getPotion();
        Location location = potion.getLocation();
        World world = location.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;
        EntityModule module = plugin.getModuleForClass(EntityModule.class);
        // FEATURE: enhanced witches. they throw wolf spawner and teleport potions
        // as well as poison potions
        LivingEntity shooter = potion.getShooter();
        if (shooter.getType() == EntityType.WITCH)
        {
            Witch witch = (Witch) shooter;

            int random = plugin.getRandom().nextInt(100);

            boolean makeExplosion = false;

            // 30% summon zombie
            if (random < 30)
            {
                event.setCancelled(true);

                boolean zombieNearby = false;
                for (Entity entity : location.getChunk().getEntities())
                {
                    if (entity.getType() == EntityType.ZOMBIE)
                    {
                        Zombie zombie = (Zombie) entity;
                        if (zombie.isVillager() && zombie.isBaby())
                        {
                            zombieNearby = true;
                            break;
                        }
                    }
                }

                if (!zombieNearby)
                {
                    Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
                    zombie.setVillager(true);
                    zombie.setBaby(true);
                    if (zombie.getTarget() != null)
                    {
                        zombie.setTarget(witch.getTarget());
                    }

                    module.markLootLess(zombie);
                }
                else
                {
                    makeExplosion = true;
                }
            }
            else if (random < 60)
            {
                // 30% teleport
                event.setCancelled(true);
                witch.teleport(location);
            }
            else if (random < 90)
            {
                // 30% explosion
                event.setCancelled(true);
                makeExplosion = true;
            }
            else
            {
                // otherwise poison potion (selective target)
                for (LivingEntity target : event.getAffectedEntities())
                {
                    if (target.getType() != EntityType.PLAYER)
                    {
                        event.setIntensity(target, 0);
                    }
                }
            }

            // if explosive potion, direct damage to players in the area
            if (makeExplosion)
            {
                // explosion just for show, no damage
                location.getWorld().createExplosion(location, 0F);

                for (LivingEntity target : event.getAffectedEntities())
                {
                    if (target.getType() == EntityType.PLAYER)
                    {
                        target.damage(3);
                    }
                }
            }
        }

    }

    /**
     * when a creature spawns...
     * More Monsters underground
     * Charged Creepers
     * More spiders underground
     * Blazes on bedrocklevel
     * Inhibited Grinders
     * more Blazes in Nether
     * always angry pigzombies
     *
     * @param event - Event that occurred.
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onEntitySpawn(CreatureSpawnEvent event)
    {
        Location location = event.getLocation();
        World world = location.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // avoid infinite loops
        if (event.getSpawnReason() == SpawnReason.CUSTOM)
            return;

        EntityModule module = plugin.getModuleForClass(EntityModule.class);

        LivingEntity entity = event.getEntity();

        EntityType entityType = entity.getType();

        SpawnReason reason = event.getSpawnReason();

        //We don't know how to handle ghosts. (Mo Creatures)
        if (entityType.equals(EntityType.UNKNOWN))
            return;

        // FEATURE: inhibited monster grinders/farms
        if (rootC.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS))
        {

            // spawners and spawn eggs always spawn a monster, but the monster
            // doesn't drop any loot
            if (reason == SpawnReason.SPAWNER && (rootC.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT) > 0 || !(entity instanceof Blaze)))
            {
                module.markLootLess(entity);
            }

            // otherwise, consider environment to stop monsters from spawning in
            // non-natural places
            else if ((reason == SpawnReason.NATURAL || reason == SpawnReason.VILLAGE_INVASION) && entity instanceof Monster)
            {
                Environment environment = location.getWorld().getEnvironment();

                Material underBlockType = location.getBlock().getRelative(BlockFace.DOWN).getType();
                if (environment == Environment.NORMAL)
                {              //natural blocks
                    if (underBlockType != Material.GRASS && underBlockType != Material.STONE
                            && underBlockType != Material.SAND && underBlockType != Material.GRAVEL
                            && underBlockType != Material.MOSSY_COBBLESTONE && underBlockType != Material.OBSIDIAN
                            && underBlockType != Material.COBBLESTONE && underBlockType != Material.BEDROCK
                            && underBlockType != Material.AIR && underBlockType != Material.WATER) //bats, squid
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
                else if (environment == Environment.NETHER)
                {
                    if (underBlockType != Material.NETHERRACK && underBlockType != Material.NETHER_BRICK
                            && underBlockType != Material.SOUL_SAND && underBlockType != Material.AIR)
                    {
                        // ghasts
                        event.setCancelled(true);
                        return;
                    }
                }
                else
                {
                    if (underBlockType != Material.ENDER_STONE && underBlockType != Material.OBSIDIAN && underBlockType != Material.AIR)
                    {
                        // ender dragon
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        //Breed Sheep spawn white, natural sheep are more likely to have a random color
        if (rootC.getBoolean(RootNode.SHEEP_REGROW_WHITE_WOOL) && entityType == EntityType.SHEEP)
        {
            Sheep sheep = (Sheep) entity;
            if (reason.equals(SpawnReason.BREEDING))
            {
                sheep.setColor(DyeColor.WHITE);
                return;
            }
        }

        // FEATURE: charged creeper spawns
        if (entityType == EntityType.CREEPER)
        {
            if (plugin.random(rootC.getInt(RootNode.CHARGED_CREEPER_SPAWN_PERCENT)))
            {
                ((Creeper) entity).setPowered(true);
            }
        }

        // FEATURE: more witches above ground (on grass)
        if (entityType == EntityType.ZOMBIE && world.getEnvironment() == Environment.NORMAL
                && entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRASS)
        {
            if (plugin.random(rootC.getInt(RootNode.BONUS_WITCH_SPAWN_PERCENT)))
            {
                event.setCancelled(true);
                entityType = EntityType.WITCH;
                world.spawnEntity(location, entityType);
            }
        }

        // FEATURE: more spiders underground
        if (entityType == EntityType.ZOMBIE && world.getEnvironment() == Environment.NORMAL && location.getBlockY() < world.getSeaLevel() - 5)
        {
            if (plugin.random(rootC.getInt(RootNode.BONUS_UNDERGROUND_SPIDER_SPAWN_PERCENT)))
            {
                event.setCancelled(true);
                entityType = EntityType.SPIDER;
                world.spawnEntity(location, entityType);
            }
        }

        // FEATURE: blazes near bedrock
        else if (entityType == EntityType.SKELETON && world.getEnvironment() == Environment.NORMAL && location.getBlockY() < 20)
        {
            if (plugin.random(rootC.getInt(RootNode.NEAR_BEDROCK_BLAZE_SPAWN_PERCENT)))
            {
                event.setCancelled(true);
                entityType = EntityType.BLAZE;
                world.spawnEntity(location, entityType);
            }
        }

        // FEATURE: more blazes
        else if (entityType == EntityType.PIG_ZOMBIE)
        {
            if (plugin.random(rootC.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT)))
            {
                event.setCancelled(true);
                entityType = EntityType.BLAZE;

                // FEATURE: magma cubes spawn with blazes
                if (plugin.random(rootC.getInt(RootNode.BONUS_NETHER_BLAZE_SPAWN_PERCENT)))
                {
                    MagmaCube cube = (MagmaCube) (world.spawnEntity(location, EntityType.MAGMA_CUBE));
                    cube.setSize(1);
                }
                world.spawnEntity(location, entityType);
            }
        }

        // FEATURE: extra monster spawns underground
        final int maxY = rootC.getInt(RootNode.MONSTER_SPAWNS_IN_LIGHT_MAX_Y);
        final int multiplier = rootC.getInt(RootNode.MORE_MONSTERS_MULTIPLIER);
        if (maxY > 0)
        {
            if (world.getEnvironment() == Environment.NORMAL && event.getLocation() != null
                    && event.getLocation().getBlockY() < maxY && entityType != null && entity instanceof Monster)
            {
                if (!entityType.equals(EntityType.SILVERFISH)) //no multiple silverfish per block
                {
                    for (int i = 1; i < multiplier; i++)
                    {
                        Entity newEntity = world.spawnEntity(event.getLocation(), entityType);
                        if (module.isLootLess(entity))
                        {
                            module.markLootLess((LivingEntity) newEntity);
                        }
                    }
                }
            }
        }

        // FEATURE: always-angry pig zombies
        if (rootC.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES))
        {
            if (entity instanceof PigZombie)
            {
                PigZombie pigZombie = (PigZombie) entity;
                pigZombie.setAnger(Integer.MAX_VALUE);
            }
        }
    }

    /**
     * when an entity shoots a bow...
     * Skeletons: Knockback-arrows, silverfish
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onShootProjectile(ProjectileLaunchEvent event)
    {
        Location location = event.getEntity().getLocation();
        World world = location.getWorld();
        EntityType entityType = event.getEntityType();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        if (event.getEntity() == null)
            return;

        // FEATURE: skeletons sometimes release silverfish to attack their targets
        if (entityType != EntityType.ARROW)
            return;

        Arrow arrow = (Arrow) event.getEntity();

        LivingEntity shooter = arrow.getShooter();
        if (shooter != null && shooter.getType() == EntityType.SKELETON && plugin.random(rootC.getInt(RootNode.SKELETONS_RELEASE_SILVERFISH)))
        {
            Skeleton skeleton = (Skeleton) shooter;
            EntityModule module = plugin.getModuleForClass(EntityModule.class);
            // cancel arrow fire
            event.setCancelled(true);

            // replace with silverfish, quarter velocity of arrow, wants to attack
            // same target as skeleton
            Creature silverFish = (Creature) world.spawnEntity(skeleton.getLocation().add(0, 1.5, 0), EntityType.SILVERFISH);
            silverFish.setVelocity(arrow.getVelocity().multiply(.25));
            silverFish.setTarget(skeleton.getTarget());
            module.markLootLess(silverFish); // this silverfish doesn't
            // drop loot
        }
    }

    /**
     * when a chunk loads...
     * Always angry pigzombies
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // FEATURE: always-angry pig zombies
        if (rootC.getBoolean(RootNode.ALWAYS_ANGRY_PIG_ZOMBIES))
        {
            for (Entity entity : chunk.getEntities())
            {
                if (entity instanceof PigZombie)
                {
                    PigZombie pigZombie = (PigZombie) entity;
                    pigZombie.setAnger(Integer.MAX_VALUE);
                }
            }
        }
    }

    /**
     * when an entity dies...
     * <h3>Features</h3>
     * <ul><li>Players loose certain percentage of inventory</li>
     * <li>Creepers drop tnt, y-level configurable</li>
     * <li>Silverfish drop cobble TODO config
     * <li>Zombies reanimate
     * <li>Pigzombies drop wart
     * <li>blazes drop extra loot (glowstone, gunpowder)
     * <li>Enderdragon drops villagereggs and dragonegg TODO completely configurable with a list
     * <li>checks if monster is from a grinder, no loot if from grinder
     * <li>no exp for animals TODO config + very little exp, no exp is not cool
     * <li>Ghasts drop more loot
     * <li>nether blazes may multiple
     * <li>spiders drop web as a combat obstacle
     * </ul>
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
        {
            return;
        }

        EntityModule module = plugin.getModuleForClass(EntityModule.class);
        MessageConfig messages = plugin.getModuleForClass(MessageConfig.class);
        // FEATURE: some portion of player inventory is permanently lost on death
        if (entity instanceof Player)
        {
            Player player = (Player) entity;
            if (!player.hasPermission(PermissionNode.BYPASS.getNode()))
            {
                List<ItemStack> drops = event.getDrops();
                int numberOfStacksToRemove = (int) (drops.size() * (rootC.getInt(RootNode.PLAYER_DEATH_ITEM_STACKS_FORFEIT_PERCENT) / 100f));
                for (int i = 0; i < numberOfStacksToRemove && drops.size() > 0; i++)
                {
                    int indexOfStackToRemove = plugin.getRandom().nextInt(drops.size());
                    drops.remove(indexOfStackToRemove);
                }
            }
        }

        // FEATURE: silverfish drop cobblestone
        if (entity.getType() == EntityType.SILVERFISH)
        {
            event.getDrops().add(new ItemStack(Material.COBBLESTONE));
        }

        // FEATURE: zombies may reanimate if not on fire when they die
        final int zombiesReanimatePercent = rootC.getInt(RootNode.ZOMBIES_REANIMATE_PERCENT);
        if (zombiesReanimatePercent > 0)
        {
            if (entity.getType() == EntityType.ZOMBIE)
            {
                Zombie zombie = (Zombie) entity;

                if (!zombie.isVillager() && entity.getFireTicks() < 1 && plugin.random(zombiesReanimatePercent))
                {
                    Player playerTarget = null;
                    Entity target = zombie.getTarget();
                    if (target instanceof Player)
                    {
                        playerTarget = (Player) target;
                    }

                    RespawnZombieTask task = new RespawnZombieTask(plugin, entity.getLocation(), playerTarget);
                    int respawnSeconds = plugin.getRandom().nextInt(6) + 3; // 3-8
                    // seconds
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * respawnSeconds); // /20L
                    // ~
                    // 1
                    // second
                }
            }
        }

        // FEATURE: creepers may drop activated TNT when they die
        final int creeperDropTNTPercent = rootC.getInt(RootNode.CREEPERS_DROP_TNT_ON_DEATH_PERCENT);
        final int creeperDropTntMaxY = rootC.getInt(RootNode.CREEPERS_DROP_TNT_ON_DEATH_MAX_Y);
        if (creeperDropTNTPercent > 0)
        {
            if (entity.getType() == EntityType.CREEPER && plugin.random(creeperDropTNTPercent)
                    && creeperDropTntMaxY > entity.getLocation().getBlockY())
            {
                world.spawnEntity(entity.getLocation(), EntityType.PRIMED_TNT);
                if (rootC.getBoolean(RootNode.SOUND_CREEPER_TNT))
                    world.playEffect(entity.getLocation(), Effect.GHAST_SHRIEK, 1, 35);
            }
        }

        // FEATURE: pig zombies drop nether wart when slain in nether fortresses
        if (rootC.getBoolean(RootNode.FORTRESS_PIGS_DROP_WART) && world.getEnvironment().equals(Environment.NETHER) && entity instanceof PigZombie)
        {
            Block underBlock = entity.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (underBlock.getType() == Material.NETHER_BRICK)
            {
                event.getDrops().add(new ItemStack(Material.NETHER_STALK));
            }
        }

        // FEATURE: nether blazes drop extra loot (glowstone and gunpowder)
        if (rootC.getBoolean(RootNode.BLAZES_DROP_BONUS_LOOT) && entity instanceof Blaze)
        {
            if (world.getEnvironment() == Environment.NETHER)
            {
                // 50% chance of each
                if (plugin.getRandom().nextInt(2) == 0)
                {
                    event.getDrops().add(new ItemStack(Material.SULPHUR, 2));
                }
                else
                {
                    event.getDrops().add(new ItemStack(Material.GLOWSTONE_DUST, 2));
                }
            }
            else // no drops in the normal world (restricting blaze rods to the
            // nether)
            {
                event.getDrops().clear();
            }
        }

        // FEATURE: ender dragon drops prizes on death
        if (entity instanceof EnderDragon)
        {
            if (rootC.getBoolean(RootNode.ENDER_DRAGON_DROPS_VILLAGER_EGGS))
            {
                ItemStack itemStack = new ItemStack(Material.MONSTER_EGG, 2, (short) 120);
                world.dropItemNaturally(entity.getLocation().add(10, 0, 0), itemStack);
            }

            if (rootC.getBoolean(RootNode.ENDER_DRAGON_DROPS_EGG))
            {
                world.dropItemNaturally(entity.getLocation().add(10, 0, 0), new ItemStack(Material.DRAGON_EGG));
            }

            if (rootC.getBoolean(RootNode.ENDER_DRAGON_COMBAT_ANNOUNCEMENTS))
            {
                StringBuilder builder = new StringBuilder("The dragon has been defeated!  ( By: ");
                for (Player player : this.playersFightingDragon)
                {
                    builder.append(player.getName()).append(" ");
                }
                builder.append(")");

                plugin.getServer().broadcastMessage(builder.toString());
            }

            if (rootC.getBoolean(RootNode.ENDER_DRAGON_NO_BUILDING))
            {
                for (Player player : this.playersFightingDragon)
                {
                    plugin.sendMessage(player, messages.getString(MessageNode.DRAGON_FOUNTAIN_TIP));
                }
            }

            this.playersFightingDragon.clear();
        }

        // FEATURE: monsters which take environmental damage or spawn from
        // spawners don't drop loot and exp (monster grinder inhibitor)
        if (rootC.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS) && entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.SQUID)
        {
            boolean noLoot = false;

            //animals aren't affected by antigrinder
            if (entity instanceof Chicken || entity instanceof Cow || entity instanceof Pig)
                return;
            if (module.isLootLess(entity))
            {
                noLoot = true;
            }
            else if (entity instanceof Skeleton)
            {
                Skeleton skeleton = (Skeleton) entity;
                if (skeleton.getSkeletonType() == SkeletonType.WITHER && skeleton.getEyeLocation().getBlock().getType() != Material.AIR)
                {
                    noLoot = true;
                }
            }
            else if (entity instanceof Enderman)
            {
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
                Block[] adjacentBlocks = new Block[]{block, block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST),
                        block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH), block.getRelative(BlockFace.NORTH_EAST),
                        block.getRelative(BlockFace.SOUTH_EAST), block.getRelative(BlockFace.NORTH_WEST), block.getRelative(BlockFace.SOUTH_WEST),
                        underBlock, underBlock.getRelative(BlockFace.EAST), underBlock.getRelative(BlockFace.WEST),
                        underBlock.getRelative(BlockFace.NORTH), underBlock.getRelative(BlockFace.SOUTH), underBlock.getRelative(BlockFace.NORTH_EAST),
                        underBlock.getRelative(BlockFace.SOUTH_EAST), underBlock.getRelative(BlockFace.NORTH_WEST),
                        underBlock.getRelative(BlockFace.SOUTH_WEST)};

                for (Block adjacentBlock : adjacentBlocks)
                {
                    block = adjacentBlock;
                    if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
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
                        // monster is blocked at eye level, unable to advance toward
                        // killer
                        if (middleLocation.getBlock().getType() != Material.AIR)
                        {
                            noLoot = true;
                        }

                        // monster doesn't have room above to hurdle a foot level
                        // block, unable to advance toward killer
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

        // FEATURE: animals don't drop experience (because they're easy to "farm")
        if (rootC.getBoolean(RootNode.ANIMAL_EXP_NERF) && entity instanceof Animals)
        {
            event.setDroppedExp(0);
        }

        // FEATURE: ghasts deflect arrows and drop extra loot and exp
        if (rootC.getBoolean(RootNode.GHASTS_DEFLECT_ARROWS))
        {
            if (entity instanceof Ghast)
            {
                event.setDroppedExp(event.getDroppedExp() * 10);
                List<ItemStack> itemDrops = event.getDrops();
                for (ItemStack itemDrop : itemDrops)
                {
                    itemDrop.setAmount(itemDrop.getAmount() * 10);
                }
            }
        }

        // FEATURE: blazes explode on death in normal world
        if (rootC.getBoolean(RootNode.BLAZES_EXPLODE_ON_DEATH) && entity instanceof Blaze && world.getEnvironment() == Environment.NORMAL
                && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            // create explosion
            world.createExplosion(entity.getLocation(), 2F, true); // equal to a
            // TNT blast,
            // sets fires

            // fire a fireball straight up in normal worlds
            Fireball fireball = (Fireball) world.spawnEntity(entity.getLocation(), EntityType.FIREBALL);
            fireball.setDirection(new Vector(0, 10, 0));
            fireball.setYield(1);
        }

        // FEATURE: nether blazes may multiply on death
        final int blazeSplitPercent = rootC.getInt(RootNode.NETHER_BLAZES_SPLIT_ON_DEATH_PERCENT);
        if (blazeSplitPercent > 0 && world.getEnvironment() == Environment.NETHER && entity instanceof Blaze)
        {
            if (plugin.random(blazeSplitPercent))
            {
                Entity firstNewBlaze = world.spawnEntity(entity.getLocation(), EntityType.BLAZE);
                firstNewBlaze.setVelocity(new Vector(1, 0, 1));

                Entity secondNewBlaze = world.spawnEntity(entity.getLocation(), EntityType.BLAZE);
                secondNewBlaze.setVelocity(new Vector(-1, 0, -1));

                // if this blaze was marked lootless, mark the new blazes the same
                if (module.isLootLess(entity))
                {
                    module.markLootLess((LivingEntity) firstNewBlaze);
                    module.markLootLess((LivingEntity) secondNewBlaze);
                }
            }
        }

        // FEATURE: spiders drop web on death
        if (rootC.getBoolean(RootNode.SPIDERS_DROP_WEB_ON_DEATH))
        {
            if (entity instanceof Spider)
            {
                // random web placement
                long serverTime = world.getFullTime();
                int random1 = (int) (serverTime + entity.getLocation().getBlockZ()) % 9;
                int random2 = (int) (serverTime + entity.getLocation().getBlockX()) % 9;

                Location[] locations = new Location[4];

                locations[0] = entity.getLocation().add(random1, 0, random2);
                locations[1] = entity.getLocation().add(-random2, 0, random1 / 2);
                locations[2] = entity.getLocation().add(-random1 / 2, 0, -random2);
                locations[3] = entity.getLocation().add(random1 / 2, 0, -random2 / 2);

                List<Block> changedBlocks = new ArrayList<Block>();
                for (Location location : locations)
                {
                    Block block = location.getBlock();

                    // don't replace anything solid with web
                    if (block.getType() != Material.AIR)
                        continue;

                    // only place web on the ground, not hanging up in the air
                    do
                    {
                        block = block.getRelative(BlockFace.DOWN);
                    } while (block.getType() == Material.AIR);

                    // don't place web over fluids or stack webs
                    if (!block.isLiquid() && block.getType() != Material.WEB)
                    {
                        block = block.getRelative(BlockFace.UP);

                        // don't place next to cactus, because it will break the
                        // cactus
                        Block[] adjacentBlocks = new Block[]{block.getRelative(BlockFace.EAST), block.getRelative(BlockFace.WEST),
                                block.getRelative(BlockFace.NORTH), block.getRelative(BlockFace.SOUTH)};

                        boolean nextToCactus = false;
                        for (Block adjacentBlock : adjacentBlocks)
                        {
                            if (adjacentBlock.getType() == Material.CACTUS)
                            {
                                nextToCactus = true;
                                break;
                            }
                        }

                        if (!nextToCactus)
                        {
                            block.setType(Material.WEB);
                            changedBlocks.add(block);
                        }
                    }
                }

                // any webs placed above sea level will be automatically cleaned up
                // after a short time
                if (entity.getLocation().getBlockY() >= entity.getLocation().getWorld().getSeaLevel() - 5)
                {
                    WebCleanupTask task = new WebCleanupTask(changedBlocks);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * 30);
                }
            }
        }
    }

    /**
     * when an entity is damaged
     * handles
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        EntityType entityType = entity.getType();
        World world = entity.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // is this an entity damaged by entity event?
        EntityDamageByEntityEvent damageByEntityEvent = null;
        if (event instanceof EntityDamageByEntityEvent)
        {
            damageByEntityEvent = (EntityDamageByEntityEvent) event;
        }

        // FEATURE: don't allow explosions to destroy items on the ground
        // REASONS: charged creepers explode twice, enhanced TNT explodes 5 times
        if (entityType == EntityType.DROPPED_ITEM)
        {
            event.setCancelled(true);
        }

        // FEATURE: the dragon has new attacks
        if (damageByEntityEvent != null && entity.getType() == EntityType.ENDER_DRAGON && rootC.getBoolean(RootNode.ENDER_DRAGON_ADDITIONAL_ATTACKS))
        {
            Player damager = null;
            if (damageByEntityEvent.getDamager() instanceof Player)
            {
                damager = (Player) damageByEntityEvent.getDamager();
            }
            else if (damageByEntityEvent.getDamager() instanceof Projectile)
            {
                Projectile projectile = (Projectile) damageByEntityEvent.getDamager();
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Player)
                {
                    damager = (Player) projectile.getShooter();
                }
            }

            if (damager != null)
            {
                if (!this.playersFightingDragon.contains(damager))
                {
                    this.playersFightingDragon.add(damager);

                    DragonAttackPatternTask task = new DragonAttackPatternTask(plugin, (LivingEntity) entity, damager, this.playersFightingDragon);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 1L);

                    if (rootC.getBoolean(RootNode.ENDER_DRAGON_COMBAT_ANNOUNCEMENTS))
                    {
                        plugin.getServer().broadcastMessage(damager.getName() + " is challenging the dragon!");
                    }
                }

                for (int i = 0; i < 5; i++)
                {
                    DragonAttackTask task = new DragonAttackTask(plugin, entity, damager);
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20L * (plugin.getRandom().nextInt(15)));
                }

                Chunk chunk = damager.getLocation().getChunk();
                Entity[] entities = chunk.getEntities();
                for (Entity entity1 : entities)
                {
                    if (entity1.getType() == EntityType.ENDERMAN)
                    {
                        Enderman enderman = (Enderman) entity1;
                        enderman.setTarget(damager);
                    }
                }
            }
        }

        // FEATURE: zombies can apply a debilitating effect
        if (rootC.getBoolean(RootNode.ZOMBIES_DEBILITATE_PLAYERS))
        {
            if (damageByEntityEvent != null && damageByEntityEvent.getDamager() instanceof Zombie)
            {
                if (entity instanceof Player)
                {
                    Player player = (Player) entity;
                    if (!player.hasPermission(PermissionNode.BYPASS.getNode()))
                    {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 10, 3));
                    }
                }
            }
        }

        // FEATURE: magma cubes become blazes when they take damage
        if (entityType == EntityType.MAGMA_CUBE && rootC.getBoolean(RootNode.MAGMA_CUBES_BECOME_BLAZES_ON_DAMAGE) && !entity.isDead() && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            entity.remove(); // remove magma cube
            entity.getWorld().spawnEntity(entity.getLocation().add(0, 2, 0), EntityType.BLAZE); // replace with blaze
            entity.getWorld().createExplosion(entity.getLocation(), 2F, true); // fiery explosion for effect
        }

        // FEATURE: arrows pass through skeletons
        final int deflect = rootC.getInt(RootNode.SKELETONS_DEFLECT_ARROWS);
        if (entityType == EntityType.SKELETON && damageByEntityEvent != null && deflect > 0)
        {
            Entity damageSource = damageByEntityEvent.getDamager();

            // only arrows
            if (damageSource instanceof Arrow)
            {
                Arrow arrow = (Arrow) damageSource;

                // percent chance
                if (plugin.random(deflect))
                {

                    // cancel the damage
                    event.setCancelled(true);

                    // teleport the arrow a single block farther along its flight
                    // path
                    // note that .6 and 12 were the unexplained recommended values
                    // for speed and spread, reflectively, in the bukkit wiki
                    arrow.remove();
                    world.spawnArrow(arrow.getLocation().add((arrow.getVelocity().normalize()).multiply(2)), arrow.getVelocity(), .6f, 12f);
                }
            }
        }

        // FEATURE: extra damage and effects from environmental damage
        if (rootC.getBoolean(RootNode.ENHANCED_ENVIRONMENTAL_DAMAGE))
        {
            Player player = null;
            if (entity instanceof Player)
            {
                player = (Player) entity;
            }

            if (player != null && !player.hasPermission(PermissionNode.BYPASS.getNode()))
            {
                DamageCause cause = event.getCause();

                if (event.getDamage() > 2 && (cause == DamageCause.BLOCK_EXPLOSION || cause == DamageCause.ENTITY_EXPLOSION))
                {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 15, 3));
                }
                else if (cause == DamageCause.FALL)
                {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * event.getDamage(), 4));
                    event.setDamage(event.getDamage() * 2);
                }
                else if (cause == DamageCause.SUFFOCATION)
                {
                    event.setDamage(event.getDamage() * 5);
                }
                else if (cause == DamageCause.LAVA)
                {
                    event.setDamage(event.getDamage() * 2);
                }
                else if (cause == DamageCause.FIRE_TICK)
                {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
                }
            }
        }

        // FEATURE: skeletons can knock back
        final int knockBackPercent = rootC.getInt(RootNode.SKELETONS_KNOCK_BACK_PERCENT);
        if (knockBackPercent > 0)
        {
            if (damageByEntityEvent != null)
            {
                if (damageByEntityEvent.getDamager() instanceof Arrow)
                {
                    Arrow arrow = (Arrow) (damageByEntityEvent.getDamager());
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Skeleton)
                    {
                        if (plugin.random(knockBackPercent))
                        {
                            // cut damage in half
                            event.setDamage(event.getDamage() / 2);

                            // knock back target with half the arrow's velocity
                            entity.setVelocity(arrow.getVelocity());
                        }
                    }
                }
            }
        }

        // FEATURE: monsters trapped in webbing break out of the webbing when hit
        if (entity instanceof Monster)
        {
            entityModule.clearWebbing(entity);
        }

        // FEATURE: blazes drop fire on hit
        if (rootC.getBoolean(RootNode.BLAZES_DROP_FIRE_ON_DAMAGE))
        {
            if (entityType == EntityType.BLAZE)
            {
                Blaze blaze = (Blaze) entity;

                if (blaze.getHealth() > blaze.getMaxHealth() / 2)
                {

                    Block block = entity.getLocation().getBlock();

                    Block underBlock = block.getRelative(BlockFace.DOWN);
                    while (underBlock.getType() == Material.AIR)
                        underBlock = underBlock.getRelative(BlockFace.DOWN);

                    block = underBlock.getRelative(BlockFace.UP);
                    if (block.getType() == Material.AIR && underBlock.getType() != Material.AIR && !underBlock.isLiquid() && underBlock.getY() > 0)
                    {
                        block.setType(Material.FIRE);
                    }
                }
            }
        }

        // FEATURE: charged creepers explode on hit
        if (rootC.getBoolean(RootNode.CHARGED_CREEPERS_EXPLODE_ON_HIT) && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            if (entityType == EntityType.CREEPER && !entity.isDead())
            {
                Creeper creeper = (Creeper) entity;
                if (creeper.isPowered())
                {
                    Player damager = null;
                    //Always explode when damaged by a player
                    if (damageByEntityEvent != null)
                    {
                        if (damageByEntityEvent.getDamager() != null && damageByEntityEvent.getDamager() instanceof Player)
                        {   //Normal Damage from a player
                            damager = (Player) damageByEntityEvent.getDamager();
                            if (damager != null && damager.hasPermission(PermissionNode.BYPASS_CREEPERS.getNode()))
                                return;
                        }
                        else if (damageByEntityEvent.getDamager() != null && damageByEntityEvent.getDamager() instanceof Arrow)
                        {   //Damaged by an arrow shot by a player
                            Arrow arrow = (Arrow) damageByEntityEvent.getDamager();
                            damager = (Player) arrow.getShooter();
                            if (damager != null && damager.hasPermission(PermissionNode.BYPASS_CREEPERS.getNode()))
                                return;
                        }
                    }
                    if (event != null && creeper.getTarget() == null && damager == null)
                    {   //If not targetting a player this is an explosion we don't need. Trying to prevent unecessary world damage
                        return;
                    }
                    entityModule.markLootLess((LivingEntity) entity);
                    entity.remove();
                    world.createExplosion(entity.getLocation(), 4F); // equal to a TNT blast
                }
            }
        }



        //FEATURE: a burning creeper will create a nice explosion + fireworks and will fly in the air
        //Will only trigger if creeper died from fire not from a sword with fireaspect or bow
        if (rootC.getBoolean(RootNode.FLAMING_CREEPERS_EXPLODE))
        {
            if (event != null && entity != null && entityType.equals(EntityType.CREEPER))
            {
                if (!entityModule.hasFlagIgnore(entity))
                {
                    if (event.getCause().equals(DamageCause.FIRE)
                            || event.getCause().equals(DamageCause.FIRE_TICK)
                            || event.getCause().equals(DamageCause.LAVA))
                    {
                        Creeper creeper = (Creeper) entity;
                        entityModule.flagIgnore(entity);
                        CoolCreeperExplosion bigBoom = new CoolCreeperExplosion(creeper, plugin);
                        bigBoom.run();
                    }
                }
            }
        }

        // FEATURE: flaming creepers explode
        /*if (rootC.getInt(RootNode.FLAMING_CREEPERS_EXPLODE) && !rootC.getBoolean(RootNode.DISABLE_EXPLOSIONS))
        {
            if (entityType == EntityType.CREEPER && !entity.isDead())
            {
                Creeper creeper = (Creeper) entity;
                entityModule.addFireDamage(entity);
                if (entityModule.getFireDamage(entity) >= 1 && 2 >= entityModule.getFireDamage(entity))
                {
                    Vector vec = creeper.getVelocity();
                    vec.setY(0.5);
                    creeper.setVelocity(vec);
                }
                else if (entityModule.getFireDamage(entity) > 3)
                {
                    utils.fireWorkRandomColors(FireworkEffect.Type.CREEPER, creeper.getLocation());
                }
                else if (entityModule.getFireDamage(entity) < 0)
                {
                    entityModule.markLootLess((LivingEntity) entity);
                    entity.remove();
                    world.createExplosion(entity.getLocation(), 4F); // equal to a TNT blast
                }
            }
        }*/

        // FEATURE: ghasts deflect arrows and drop extra loot
        if (rootC.getBoolean(RootNode.GHASTS_DEFLECT_ARROWS))
        {
            // only ghasts, and only if damaged by another entity (as opposed to
            // environmental damage)
            if (entity instanceof Ghast && event instanceof EntityDamageByEntityEvent)
            {
                Entity damageSource = damageByEntityEvent.getDamager();

                // only arrows
                if (damageSource instanceof Arrow)
                {
                    // who shot it?
                    Arrow arrow = (Arrow) damageSource;
                    if (arrow.getShooter() != null && arrow.getShooter() instanceof Player)
                    {
                        // check permissions when it's shot by a player
                        Player player = (Player) arrow.getShooter();
                        event.setCancelled(!player.hasPermission(PermissionNode.BYPASS.getNode()));
                    }
                    else
                    {
                        // otherwise always deflect
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // FEATURE: monsters which take environmental damage don't drop loot or
        // experience (monster grinder inhibitor)
        if (rootC.getBoolean(RootNode.INHIBIT_MONSTER_GRINDERS) && entity instanceof LivingEntity)
        {
            DamageCause damageCause = event.getCause();
            if (damageCause != DamageCause.ENTITY_ATTACK && damageCause != DamageCause.PROJECTILE && damageCause != DamageCause.BLOCK_EXPLOSION)
            {
                entityModule.addEnvironmentalDamage((LivingEntity) entity, event.getDamage());
            }
        }
    }

    /**
     * when a sheep regrows its wool...
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onSheepRegrowWool(SheepRegrowWoolEvent event)
    {
        World world = event.getEntity().getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        // FEATURE: sheep are all white, and may be dyed only temporarily
        if (rootC.getBoolean(RootNode.SHEEP_REGROW_WHITE_WOOL))
        {
            Sheep sheep = event.getEntity();
            if (sheep.isSheared())
            sheep.setColor(DyeColor.WHITE);
        }
    }

    /**
     * when an entity (not a player) teleports...
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onEntityTeleport(EntityTeleportEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;
        if (world.getEnvironment() != Environment.NORMAL)
            return;

        if (entity instanceof Enderman && rootC.getBoolean(RootNode.IMPROVED_ENDERMAN_TELEPORTATION))
        {
            Enderman enderman = (Enderman) entity;

            // ignore endermen which aren't fighting players
            if (enderman.getTarget() == null || !(enderman.getTarget() instanceof Player))
                return;

            // ignore endermen which are taking damage from the environment (to
            // avoid rapid teleportation due to rain or suffocation)
            if (enderman.getLastDamageCause() != null && enderman.getLastDamageCause().getCause() != DamageCause.ENTITY_ATTACK)
                return;

            // ignore endermen which are in caves (standing on stone)
            if (enderman.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.STONE)
                return;

            Player player = (Player) enderman.getTarget();

            // ignore when player is in a different world from the enderman
            if (!player.getWorld().equals(enderman.getWorld()))
                return;

            // half the time, teleport the player instead
            if (plugin.random(50))
            {
                event.setCancelled(true);
                int distanceSquared = (int) player.getLocation().distanceSquared(enderman.getLocation());

                // play sound at old location
                world.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
                Block destinationBlock;

                // if the player is far away
                if (distanceSquared > 75)
                {
                    // have the enderman swap places with the player
                    destinationBlock = enderman.getLocation().getBlock();
                    enderman.teleport(player.getLocation());
                }

                // otherwise if the player is close
                else
                {
                    // teleport the player to the enderman's destination
                    destinationBlock = event.getTo().getBlock();
                }

                while (destinationBlock.getType() != Material.AIR || destinationBlock.getRelative(BlockFace.UP).getType() != Material.AIR)
                {
                    destinationBlock = destinationBlock.getRelative(BlockFace.UP);
                }

                player.teleport(destinationBlock.getLocation(), TeleportCause.ENDER_PEARL);

                // play sound at new location
                world.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1, 1);
            }
        }
    }

    /**
     * when an entity targets something (as in to attack it)...
     *
     * @param event - Event that occurred.
     */
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event)
    {
        Entity entity = event.getEntity();
        World world = entity.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
            return;

        EntityModule module = plugin.getModuleForClass(EntityModule.class);
        // FEATURE: a monster which gains a target breaks out of any webbing it
        // might have been trapped within
        if (entity instanceof Monster)
        {
            module.clearWebbing(entity);
        }

        // FEATURE: monsters don't target the ender dragon
        if (event.getTarget() != null && event.getTarget() instanceof EnderDragon)
        {
            event.setCancelled(true);
        }
    }

    /**
     * when a player crafts something...
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemCrafted(CraftItemEvent event)
    {
        HumanEntity entity = event.getWhoClicked();
        if (entity == null || !(entity instanceof Player))
            return;

        Player player = (Player) entity;
        World world = player.getWorld();

        MessageConfig messages = plugin.getModuleForClass(MessageConfig.class);

        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()) || player.hasPermission(PermissionNode.BYPASS.getNode()))
            return;

        Material result = event.getRecipe().getResult().getType();

        // FEATURE: no crafting melon seeds
        if (rootC.getBoolean(RootNode.CANT_CRAFT_MELONSEEDS) && result == Material.MELON_SEEDS || result == Material.PUMPKIN_SEEDS)
        {
            event.setCancelled(true);
            plugin.sendMessage(player, messages.getString(MessageNode.NO_CRAFTING_MELON_SEEDS));
            return;
        }

        // FEATURE: extra TNT from the TNT recipe
        if (rootC.getInt(RootNode.MORE_TNT_NUMBER) > 1 && event.getRecipe().getResult().getType() == Material.TNT)
        {
            player.getInventory().addItem(new ItemStack(Material.TNT, rootC.getInt(RootNode.MORE_TNT_NUMBER)-1));
        }
    }

    /**
     * when a player teleports BUG HERE: last i checked, this event didn't fire
     * from bukkit. so this code is incomplete (i stopped working on it)
     *
     * @param event - Event that occurred.
     */
    /*TODO Implement Feature RoadMap: 3.2/3.3
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (event.getCause() != TeleportCause.END_PORTAL || !rootC.getStringList(RootNode.WORLDS).contains(world.getName())
                || player.hasPermission(PermissionNode.BYPASS.getNode()) || world.getEnvironment() == Environment.THE_END)
            return;
    }*/

    /**
     * when an item spawns
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemSpawn(ItemSpawnEvent event)
    {
        // FEATURE: fountain effect from dragon fireball explosions sometimes
        // causes fire to drop as an item. this is the fix for that.
        Item item = event.getEntity();
        World world = item.getWorld();
        if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()) || world.getEnvironment() != Environment.THE_END)
            return;

        if (item.getItemStack().getType() == Material.FIRE)
        {
            event.setCancelled(true);
        }
    }

    /**
     * when an entity tries to change a block (does not include player block
     * changes) don't allow endermen to change blocks
     *
     * @param event - Event that occurred.
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        //Prevent Silverfish from entering blocks?
        if (!rootC.getBoolean(RootNode.SILVERFISH_CANT_ENTER_BLOCKS))
        {
            Block block = event.getBlock();
            World world = block.getWorld();
            if (!rootC.getStringList(RootNode.WORLDS).contains(world.getName()))
                return;

            if (event.getEntity().getType() == EntityType.SILVERFISH && event.getTo() == Material.MONSTER_EGGS)
            {
                event.setCancelled(true);
            }
        }
    }
}
