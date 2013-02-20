package me.ryanhamshire.ExtraHardMode.service;

/**
 * All known permission nodes.
 */
public enum PermissionNode {
   /**
    * Bypass. TODO individual bypass nodes.
    */
   BYPASS("bypass"),
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
    * @param subperm
    *           - specific permission path.
    */
   private PermissionNode(String subperm) {
      this.node = PREFIX + subperm;
   }

   /**
    * Get the full permission node path.
    * 
    * @return Permission node path.
    */
   public String getNode() {
      return node;
   }
}
