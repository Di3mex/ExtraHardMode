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


package com.extrahardmode.task;


import com.extrahardmode.ExtraHardMode;
import com.extrahardmode.events.EhmZombieRespawnEvent;
import com.extrahardmode.module.EntityHelper;
import com.extrahardmode.module.temporaryblock.TemporaryBlock;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

/**
 * Respawn a zombie for a given location with a target player.
 */
public class RespawnZombieTask implements Runnable
{

    /**
     * Plugin instance.
     */
    private final ExtraHardMode plugin;

    /**
     * Respawn location.
     */
    private final Location location;

    /**
     * Target player.
     */
    private final Player player;

    /**
     * Skull Block
     */
    private final TemporaryBlock block;


    /**
     * Constructor.
     *
     * @param plugin   - Plugin instance.
     * @param location - Respawn location.
     * @param target   - Target player.
     */
    public RespawnZombieTask(ExtraHardMode plugin, Location location, Player target)
    {
        this(plugin, location, target, null);
    }


    /**
     * Constructor.
     *
     * @param plugin         - Plugin instance.
     * @param location       - Respawn location.
     * @param target         - Target player.
     * @param temporaryBlock block where the skull is placed
     */
    public RespawnZombieTask(ExtraHardMode plugin, Location location, Player target, TemporaryBlock temporaryBlock)
    {
        this.plugin = plugin;
        this.location = location;
        this.player = target;
        this.block = temporaryBlock;
    }


    @Override
    public void run()
    {
        if (block == null || !block.isBroken())
        {
            Chunk chunk = location.getChunk();
            if (!chunk.isLoaded())
            {
                return;
            }
            Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
            // zombie has half normal zombie health
            zombie.setHealth(((Damageable)zombie).getHealth() / 2);
            // this zombie will not drop loot (again)
            EntityHelper.markLootLess(plugin, zombie);
            EntityHelper.markAsOurs(plugin, zombie);
            // zombie is still madat the same player
            if (this.player != null && this.player.isOnline())
            {
                zombie.setTarget(this.player);
            }
            EhmZombieRespawnEvent zombieEvent = new EhmZombieRespawnEvent(player, zombie, false);
            plugin.getServer().getPluginManager().callEvent(zombieEvent);
            if (block != null)
                block.getLoc().getBlock().setType(Material.AIR);
        }
    }
}
