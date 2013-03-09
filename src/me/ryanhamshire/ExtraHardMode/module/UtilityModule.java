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
package me.ryanhamshire.ExtraHardMode.module;

import me.ryanhamshire.ExtraHardMode.ExtraHardMode;
import me.ryanhamshire.ExtraHardMode.service.EHMModule;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Put all the Utility Stuff here that doesn't fit into the other modules
 */
public class UtilityModule extends EHMModule
{
    /**
     * Constructor.
     *
     * @param plugin - Plugin instance.
     */
    public UtilityModule(ExtraHardMode plugin)
    {
        super(plugin);
    }

    /**
     * Generates a Firework with random colors/velocity and the given Firework Type
     *
     * @param type The type of firework
     * @return nothing
     */
    public void fireWorkRandomColors(FireworkEffect.Type type, Location location)
    {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();

        //Generate the colors
        int rdmInt1 = plugin.getRandom().nextInt(255);
        int rdmInt2 = plugin.getRandom().nextInt(255);
        int rdmInt3 = plugin.getRandom().nextInt(255);
        Color mainColor = Color.fromRGB(rdmInt1, rdmInt2, rdmInt3);

        FireworkEffect fwEffect = FireworkEffect.builder().withColor(mainColor).with(type).build();
        fireworkMeta.addEffect(fwEffect);
        fireworkMeta.setPower(1);
        firework.setFireworkMeta(fireworkMeta);
    }

    /**
     * Returns if Material is a plant that should be affected by the farming Rules
     */
    public boolean isPlant(Material material)
    {
        return material.equals(Material.CROPS)
                || material.equals(Material.POTATO)
                || material.equals(Material.CARROT)
                || material.equals(Material.MELON_STEM)
                || material.equals(Material.PUMPKIN_STEM);
    }

    @Override
    public void starting()
    {
    }

    @Override
    public void closing()
    {
    }
}
