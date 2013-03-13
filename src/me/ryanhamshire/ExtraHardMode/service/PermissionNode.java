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
package me.ryanhamshire.ExtraHardMode.service;

/**
 * All known permission nodes.
 */
public enum PermissionNode
{
    /**
     * Bypass. TODO individual bypass nodes.
     */
    BYPASS("bypass.bypass"),
    /**
     * Bypasses all Creeper features
     */
    BYPASS_CREEPERS("bypass.creepers"),
    /**
     * Inventory
     */
    BYPASS_INVENTORY("bypass.inventory"),
    /**
     * Admin.
     */
    ADMIN("admin"),
    /**
     * Silent.
     */
    SILENT_STONE_MINING_HELP("silent.stone_mining_help"),
    SILENT_NO_PLACING_ORE_AGAINST_STONE("silent.no_placing_ore_against_stone"),
    SILENT_REALISTIC_BUILDING("silent.realistic_building"),
    SILENT_LIMITED_TORCH_PLACEMENT("silent.limited_torch_placement"),
    SILENT_NO_TORCHES_HERE("silent.no_torches_here");

    /**
     * Prefix for all permission nodes.
     */
    private static final String PREFIX = "ExtraHardMode.";

    /**
     * Resulting permission node path.
     */
    private final String node;

    /**
     * Constructor.
     *
     * @param subperm - specific permission path.
     */
    private PermissionNode(String subperm)
    {
        this.node = PREFIX + subperm;
    }

    /**
     * Get the full permission node path.
     *
     * @return Permission node path.
     */
    public String getNode()
    {
        return node;
    }
}
