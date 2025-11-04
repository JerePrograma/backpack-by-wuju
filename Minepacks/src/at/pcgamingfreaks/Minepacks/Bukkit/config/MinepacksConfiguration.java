package at.pcgamingfreaks.Minepacks.Bukkit.config;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.MinecraftMaterial;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Config.Configuration;
import at.pcgamingfreaks.Config.ILanguageConfiguration;
import at.pcgamingfreaks.Config.YamlFileManager;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Database.DatabaseConnectionConfiguration;
import at.pcgamingfreaks.Minepacks.Bukkit.API.WorldBlacklistMode;
import at.pcgamingfreaks.Minepacks.Bukkit.Database.Helper.OldFileUpdater;
import at.pcgamingfreaks.Minepacks.Bukkit.Minepacks;
import at.pcgamingfreaks.Minepacks.Bukkit.ShrinkApproach;
import at.pcgamingfreaks.Minepacks.MagicValues;
import at.pcgamingfreaks.Reflection;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

/**
 * Centralised configuration access for Minepacks.
 *
 * <p>The previous implementation exposed every config lookup directly in this class, which made it hard to follow and
 * even harder to maintain.  This refactor splits the configuration into dedicated sections and keeps the legacy
 * getters for backwards compatibility.  New code should favour the section based accessors to avoid repeated lookups
 * and to make intent explicit.</p>
 */
public class MinepacksConfiguration extends Configuration implements DatabaseConnectionConfiguration, ILanguageConfiguration
{
        private static final Version CONFIG_VERSION = new Version(MagicValues.CONFIG_VERSION);
        private static final Version PRE_V2_VERSION = new Version(20);

        private final DatabaseSettings database = new DatabaseSettings();
        private final BackpackSettings backpack = new BackpackSettings();
        private final MiscSettings misc = new MiscSettings();
        private final CooldownSettings cooldown = new CooldownSettings();
        private final GameplaySettings gameplay = new GameplaySettings();
        private final FullInventorySettings fullInventory = new FullInventorySettings();
        private final ShulkerboxSettings shulkerboxes = new ShulkerboxSettings();
        private final ItemFilterSettings itemFilter = new ItemFilterSettings();
        private final WorldSettings worldSettings = new WorldSettings();
        private final ItemShortcutSettings itemShortcut = new ItemShortcutSettings();
        private final SoundSettings soundSettings = new SoundSettings();
        private final InventoryManagementSettings inventoryManagement = new InventoryManagementSettings();

        public MinepacksConfiguration(@NotNull Minepacks plugin)
        {
                super(plugin, CONFIG_VERSION);
                reloadSections();
        }

        //region Lifecycle
        @Override
        protected void doUpdate()
        {
                // Nothing to update yet
        }

        @Override
        protected void doUpgrade(@NotNull YamlFileManager oldConfig)
        {
                if(oldConfig.getVersion().olderThan(PRE_V2_VERSION))
                {
                        OldFileUpdater.updateConfig(oldConfig.getYamlE(), getConfigE());
                }
                else
                {
                        Map<String, String> remappedKeys = new HashMap<>();
                        if(oldConfig.getVersion().olderOrEqualThan(new Version(23))) remappedKeys.put("ItemFilter.Materials", "ItemFilter.Blacklist");
                        if(oldConfig.getVersion().olderOrEqualThan(new Version(28))) remappedKeys.put("Misc.AutoUpdate.Enabled", "Misc.AutoUpdate");
                        if(oldConfig.getVersion().olderOrEqualThan(new Version(30)))
                        {
                                remappedKeys.put("WorldSettings.FilteredWorlds", "WorldSettings.Blacklist");
                                remappedKeys.put("WorldSettings.BockMode", "WorldSettings.BlacklistMode");
                        }
                        if(oldConfig.getVersion().olderOrEqualThan(new Version(33))) remappedKeys.put("Database.Cache.UnCache.Strategy", "Database.Cache.UnCache.Strategie");
                        Collection<String> keysToKeep = oldConfig.getYamlE().getKeysFiltered("Database\\.SQL\\.(MaxLifetime|IdleTimeout)");
                        keysToKeep.addAll(oldConfig.getYamlE().getKeysFiltered("Database\\.Tables\\.Fields\\..+"));
                        doUpgrade(oldConfig, remappedKeys, keysToKeep);
                }
        }

        @Override
        public void reload() throws FileNotFoundException
        {
                super.reload();
                reloadSections();
        }

        private void reloadSections()
        {
                YamlFileManager yaml = getConfigE();
                database.reload(yaml);
                backpack.reload(yaml);
                misc.reload(yaml);
                cooldown.reload(yaml);
                gameplay.reload(yaml);
                fullInventory.reload(yaml);
                shulkerboxes.reload(yaml);
                itemFilter.reload(yaml);
                worldSettings.reload(yaml);
                itemShortcut.reload(yaml);
                soundSettings.reload(yaml);
                inventoryManagement.reload(yaml);
        }
        //endregion

        //region Section accessors
        public DatabaseSettings database()
        {
                return database;
        }

        public BackpackSettings backpack()
        {
                return backpack;
        }

        public MiscSettings misc()
        {
                return misc;
        }

        public CooldownSettings cooldown()
        {
                return cooldown;
        }

        public GameplaySettings gameplay()
        {
                return gameplay;
        }

        public FullInventorySettings fullInventory()
        {
                return fullInventory;
        }

        public ShulkerboxSettings shulkerboxes()
        {
                return shulkerboxes;
        }

        public ItemFilterSettings itemFilter()
        {
                return itemFilter;
        }

        public WorldSettings worldSettings()
        {
                return worldSettings;
        }

        public ItemShortcutSettings itemShortcut()
        {
                return itemShortcut;
        }

        public SoundSettings soundSettings()
        {
                return soundSettings;
        }

        public InventoryManagementSettings inventoryManagement()
        {
                return inventoryManagement;
        }
        //endregion

        //region Legacy delegate getters
        public int getAutoCleanupMaxInactiveDays()
        {
                return database.getAutoCleanupMaxInactiveDays();
        }

        public String getDatabaseType()
        {
                return database.getType();
        }

        public void setDatabaseType(String type)
        {
                database.setType(type);
        }

        public String getUserTable()
        {
                return database.getUserTable();
        }

        public String getBackpackTable()
        {
                return database.getBackpackTable();
        }

        public String getCooldownTable()
        {
                return database.getCooldownTable();
        }

        public String getDBFields(String sub, String def)
        {
                return database.getField(sub, def);
        }

        public boolean useOnlineUUIDs()
        {
                return database.useOnlineUUIDs(isBungeeCordModeEnabled());
        }

        public boolean getUseUUIDSeparators()
        {
                return database.isUseUuidSeparators();
        }

        public boolean isForceSaveOnUnloadEnabled()
        {
                return database.isForceSaveOnUnload();
        }

        public String getUnCacheStrategy()
        {
                return database.getUnCacheStrategy();
        }

        public long getUnCacheInterval()
        {
                return database.getUnCacheInterval();
        }

        public long getUnCacheDelay()
        {
                return database.getUnCacheDelay();
        }

        public String getBPTitleOther()
        {
                return backpack.getTitleOther();
        }

        public String getBPTitle()
        {
                return backpack.getTitleSelf();
        }

        public boolean useDynamicBPTitle()
        {
                return backpack.useDynamicTitle();
        }

        public boolean getDropOnDeath()
        {
                return backpack.getDropOnDeath();
        }

        public boolean getHonorKeepInventoryOnDeath()
        {
                return backpack.getHonorKeepInventoryOnDeath();
        }

        public int getBackpackMaxSize()
        {
                return backpack.getMaxSize();
        }

        public ShrinkApproach getShrinkApproach()
        {
                return backpack.getShrinkApproach();
        }

        public boolean useUpdater()
        {
                return misc.useUpdater();
        }

        public String getUpdateChannel()
        {
                return misc.getUpdateChannel();
        }

        public boolean isBungeeCordModeEnabled()
        {
                return misc.isBungeeCordModeEnabled(getDatabaseType());
        }

        public long getCommandCooldown()
        {
                return cooldown.getCommandCooldown();
        }

        public boolean isCommandCooldownSyncEnabled()
        {
                return cooldown.isSyncEnabled();
        }

        public boolean isCommandCooldownClearOnLeaveEnabled()
        {
                return cooldown.isClearOnLeaveEnabled();
        }

        public boolean isCommandCooldownAddOnJoinEnabled()
        {
                return cooldown.isAddOnJoinEnabled();
        }

        public long getCommandCooldownCleanupInterval()
        {
                return cooldown.getCleanupInterval();
        }

        public Collection<GameMode> getAllowedGameModes()
        {
                return gameplay.getAllowedGameModes();
        }

        public boolean getFullInvCollect()
        {
                return fullInventory.isCollectEnabled();
        }

        public long getFullInvCheckInterval()
        {
                return fullInventory.getCheckInterval();
        }

        public double getFullInvRadius()
        {
                return fullInventory.getCollectRadius();
        }

        public boolean isFullInvToggleAllowed()
        {
                return fullInventory.isToggleAllowed();
        }

        public boolean isFullInvEnabledOnJoin()
        {
                return fullInventory.isEnabledOnJoin();
        }

        public boolean isShulkerboxesPreventInBackpackEnabled()
        {
                return shulkerboxes.isPreventInBackpack();
        }

        public boolean isShulkerboxesDisable()
        {
                return shulkerboxes.isDisableEntirely();
        }

        public boolean isShulkerboxesExistingDropEnabled()
        {
                return shulkerboxes.isExistingDropEnabled();
        }

        public boolean isShulkerboxesExistingDestroyEnabled()
        {
                return shulkerboxes.isExistingDestroyEnabled();
        }

        public boolean isItemFilterEnabledNoShulker()
        {
                return itemFilter.isEnabledWithoutShulkerRequirement();
        }

        public boolean isItemFilterEnabled()
        {
                return itemFilter.isEnabled(shulkerboxes.isPreventInBackpack());
        }

        public Collection<MinecraftMaterial> getItemFilterMaterials()
        {
                return itemFilter.getFilteredMaterials();
        }

        public Set<String> getItemFilterNames()
        {
                return itemFilter.getFilteredNames();
        }

        public Set<String> getItemFilterLore()
        {
                return itemFilter.getFilteredLore();
        }

        public boolean isItemFilterModeWhitelist()
        {
                return itemFilter.isWhitelistMode();
        }

        public boolean isWorldWhitelistMode()
        {
                return worldSettings.isWhitelistMode();
        }

        public Set<String> getWorldFilteredList()
        {
                return worldSettings.getFilteredWorlds();
        }

        public Set<String> getWorldBlacklist()
        {
                return worldSettings.getWorldBlacklist();
        }

        public WorldBlacklistMode getWorldBlockMode()
        {
                return worldSettings.getBlockMode();
        }

        public boolean isItemShortcutEnabled()
        {
                return itemShortcut.isEnabled();
        }

        public String getItemShortcutItemName()
        {
                return itemShortcut.getItemName();
        }

        public String getItemShortcutHeadValue()
        {
                return itemShortcut.getHeadTextureValue();
        }

        public boolean isItemShortcutImproveDeathChestCompatibilityEnabled()
        {
                return itemShortcut.isImproveDeathChestCompatibilityEnabled();
        }

        public boolean isItemShortcutBlockAsHatEnabled()
        {
                return itemShortcut.isBlockAsHatEnabled();
        }

        public boolean isItemShortcutRightClickOnContainerAllowed()
        {
                return itemShortcut.isRightClickOnContainerAllowed();
        }

        public int getItemShortcutPreferredSlotId()
        {
                return itemShortcut.getPreferredSlotId();
        }

        public boolean getItemShortcutBlockItemFromMoving()
        {
                return itemShortcut.isBlockItemFromMoving();
        }

        public Sound getOpenSound()
        {
                return soundSettings.getOpenSound();
        }

        public Sound getCloseSound()
        {
                return soundSettings.getCloseSound();
        }

        public boolean isInventoryManagementClearCommandEnabled()
        {
                return inventoryManagement.isClearCommandEnabled();
        }
        //endregion

        private void saveConfig(String message)
        {
                try
                {
                        save();
                }
                catch(FileNotFoundException e)
                {
                        logger.log(Level.SEVERE, message, e);
                }
        }

        //region Nested settings classes
        public final class DatabaseSettings
        {
                private YamlFileManager yaml;
                private int autoCleanupMaxInactiveDays;
                private String type;
                private String uuidType;
                private String userTable;
                private String backpackTable;
                private String cooldownTable;
                private boolean useUuidSeparators;
                private boolean forceSaveOnUnload;
                private String unCacheStrategy;
                private long unCacheInterval;
                private long unCacheDelay;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        this.yaml = yaml;
                        autoCleanupMaxInactiveDays = yaml.getInt("Database.AutoCleanup.MaxInactiveDays", -1);
                        type = yaml.getString("Database.Type", "sqlite").toLowerCase(Locale.ENGLISH);
                        uuidType = yaml.getString("Database.UUID_Type", "auto").toLowerCase(Locale.ENGLISH);
                        userTable = yaml.getString("Database.Tables.User", "backpack_players");
                        backpackTable = yaml.getString("Database.Tables.Backpack", "backpacks");
                        cooldownTable = yaml.getString("Database.Tables.Cooldown", "backpack_cooldowns");
                        useUuidSeparators = yaml.getBoolean("Database.UseUUIDSeparators", false);
                        forceSaveOnUnload = yaml.getBoolean("Database.ForceSaveOnUnload", false);
                        unCacheStrategy = yaml.getString("Database.Cache.UnCache.Strategy", "interval").toLowerCase(Locale.ENGLISH);
                        unCacheInterval = yaml.getLong("Database.Cache.UnCache.Interval", 600) * 20L;
                        unCacheDelay = yaml.getLong("Database.Cache.UnCache.Delay", 600) * 20L;
                }

                public int getAutoCleanupMaxInactiveDays()
                {
                        return autoCleanupMaxInactiveDays;
                }

                public String getType()
                {
                        return type;
                }

                public void setType(String type)
                {
                        yaml.set("Database.Type", type);
                        this.type = type.toLowerCase(Locale.ENGLISH);
                        saveConfig("Failed to set database type");
                }

                public String getUserTable()
                {
                        return userTable;
                }

                public String getBackpackTable()
                {
                        return backpackTable;
                }

                public String getCooldownTable()
                {
                        return cooldownTable;
                }

                public String getField(String sub, String def)
                {
                        return yaml.getString("Database.Tables.Fields." + sub, def);
                }

                public boolean useOnlineUUIDs(boolean bungeeCordModeEnabled)
                {
                        if(uuidType.equals("auto"))
                        {
                                if(bungeeCordModeEnabled)
                                {
                                        Boolean detectedOnlineMode = Utils.getBungeeOrVelocityOnlineMode();
                                        if(detectedOnlineMode != null)
                                        {
                                                logger.log(Level.INFO, "Detected online mode in paper config: {0}", detectedOnlineMode);
                                                return detectedOnlineMode;
                                        }
                                        logger.warning("When using BungeeCord please make sure to set the UUID_Type config option explicitly!");
                                }
                                return Bukkit.getServer().getOnlineMode();
                        }
                        return uuidType.equals("online");
                }

                public boolean isUseUuidSeparators()
                {
                        return useUuidSeparators;
                }

                public boolean isForceSaveOnUnload()
                {
                        return forceSaveOnUnload;
                }

                public String getUnCacheStrategy()
                {
                        return unCacheStrategy;
                }

                public long getUnCacheInterval()
                {
                        return unCacheInterval;
                }

                public long getUnCacheDelay()
                {
                        return unCacheDelay;
                }
        }

        public final class BackpackSettings
        {
                private String titleOther;
                private String titleSelf;
                private boolean useDynamicTitle;
                private boolean dropOnDeath;
                private boolean honorKeepInventoryOnDeath;
                private int maxSize;
                private ShrinkApproach shrinkApproach;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        titleOther = translateTitle(yaml, "BackpackTitleOther", "{OwnerName} Backpack");
                        titleSelf = translateTitle(yaml, "BackpackTitle", "Backpack");
                        useDynamicTitle = yaml.getBoolean("Database.UseDynamicTitle", true);
                        dropOnDeath = yaml.getBoolean("DropOnDeath", true);
                        honorKeepInventoryOnDeath = yaml.getBoolean("HonorKeepInventoryOnDeath", false);
                        int configuredSize = yaml.getInt("MaxSize", 6);
                        if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_14)) configuredSize = Math.min(6, configuredSize);
                        if(configuredSize > 6)
                        {
                                logger.info("Starting with MC 1.14 backpacks with more than 6 rows will no longer be possible. A feature to allow bigger backpacks through multiple pages is currently in development.");
                        }
                        maxSize = Math.max(1, configuredSize);
                        shrinkApproach = determineShrinkApproach(yaml.getString("ShrinkApproach", "SORT"));
                }

                private String translateTitle(@NotNull YamlFileManager yaml, String key, String defaultValue)
                {
                        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(yaml.getString(key, defaultValue)).replace("%", "%%").replace("{OwnerName}", "%s"));
                }

                private ShrinkApproach determineShrinkApproach(String configured)
                {
                        if(MCVersion.isOlderThan(MCVersion.MC_1_8)) return ShrinkApproach.FAST;
                        try
                        {
                                return ShrinkApproach.valueOf(configured.toUpperCase(Locale.ENGLISH));
                        }
                        catch(IllegalArgumentException ignored)
                        {
                                logger.log(Level.WARNING, "Unknown ShrinkApproach \"{0}\"!", configured);
                                return ShrinkApproach.SORT;
                        }
                }

                public String getTitleOther()
                {
                        return titleOther;
                }

                public String getTitleSelf()
                {
                        return titleSelf;
                }

                public boolean useDynamicTitle()
                {
                        return useDynamicTitle;
                }

                public boolean getDropOnDeath()
                {
                        return dropOnDeath;
                }

                public boolean getHonorKeepInventoryOnDeath()
                {
                        return honorKeepInventoryOnDeath;
                }

                public int getMaxSize()
                {
                        return maxSize;
                }

                public ShrinkApproach getShrinkApproach()
                {
                        return shrinkApproach;
                }
        }

        public final class MiscSettings
        {
                private boolean autoUpdateEnabled;
                private String updateChannelRaw;
                private boolean useBungeeCord;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        autoUpdateEnabled = yaml.getBoolean("Misc.AutoUpdate.Enabled", yaml.getBoolean("Misc.AutoUpdate", true));
                        updateChannelRaw = yaml.getString("Misc.AutoUpdate.Channel", "Release");
                        useBungeeCord = yaml.getBoolean("Misc.UseBungeeCord", false);
                }

                public boolean useUpdater()
                {
                        return MCVersion.isNewerOrEqualThan(MCVersion.getFromVersionName(MagicValues.MIN_MC_VERSION_FOR_UPDATES)) && autoUpdateEnabled;
                }

                public String getUpdateChannel()
                {
                        if("Release".equals(updateChannelRaw) || "Master".equals(updateChannelRaw) || "Dev".equals(updateChannelRaw))
                        {
                                return updateChannelRaw;
                        }
                        logger.log(Level.INFO, "Unknown update Channel: {0}", updateChannelRaw);
                        return null;
                }

                public boolean isBungeeCordModeEnabled(String databaseType)
                {
                        boolean runsProxy = Utils.detectBungeeCord() || Utils.detectVelocity();
                        boolean shareableDB = databaseType.equals("mysql") || databaseType.equals("global");
                        if(useBungeeCord && !runsProxy)
                        {
                                logger.warning("You have BungeeCord enabled for the plugin, but it looks like you have not enabled it in your spigot.yml! You probably should check your configuration.");
                        }
                        else if(!useBungeeCord && runsProxy && shareableDB)
                        {
                                logger.warning("Your server is running behind a BungeeCord server. If you are using the plugin on more than one server with a shared database, please make sure to also enable the 'UseBungeeCord' config option.");
                        }
                        else if(useBungeeCord && !shareableDB)
                        {
                                logger.info("You have enabled BungeeCord mode for the plugin, but are not using a shared MySQL database.");
                                return false;
                        }
                        return useBungeeCord;
                }
        }

        public final class CooldownSettings
        {
                private long commandCooldown;
                private boolean syncEnabled;
                private boolean clearOnLeaveEnabled;
                private boolean addOnJoinEnabled;
                private long cleanupInterval;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        commandCooldown = yaml.getInt("Cooldown.Command", -1) * 1000L;
                        syncEnabled = yaml.getBoolean("Cooldown.Sync", false);
                        clearOnLeaveEnabled = yaml.getBoolean("Cooldown.ClearOnLeave", false);
                        addOnJoinEnabled = yaml.getBoolean("Cooldown.AddOnJoin", true);
                        cleanupInterval = yaml.getInt("Cooldown.CleanupInterval", 600) * 20L;
                }

                public long getCommandCooldown()
                {
                        return commandCooldown;
                }

                public boolean isSyncEnabled()
                {
                        return syncEnabled;
                }

                public boolean isClearOnLeaveEnabled()
                {
                        return clearOnLeaveEnabled;
                }

                public boolean isAddOnJoinEnabled()
                {
                        return addOnJoinEnabled;
                }

                public long getCleanupInterval()
                {
                        return cleanupInterval;
                }
        }

        public final class GameplaySettings
        {
                private Collection<GameMode> allowedGameModes = Collections.emptySet();

                private void reload(@NotNull YamlFileManager yaml)
                {
                        Collection<GameMode> modes = new HashSet<>();
                        for(String string : yaml.getStringList("AllowedGameModes", new LinkedList<>()))
                        {
                                GameMode gm = null;
                                try
                                {
                                        //noinspection deprecation
                                        gm = GameMode.getByValue(Integer.parseInt(string));
                                }
                                catch(NumberFormatException ignored) {}
                                if(gm == null)
                                {
                                        try
                                        {
                                                gm = GameMode.valueOf(string.toUpperCase(Locale.ROOT));
                                        }
                                        catch(IllegalArgumentException ignored)
                                        {
                                                logger.warning("Unknown game-mode '" + string + "'");
                                        }
                                }
                                if(gm != null)
                                {
                                        modes.add(gm);
                                }
                        }
                        if(modes.isEmpty()) modes.add(GameMode.SURVIVAL);
                        allowedGameModes = modes;
                }

                public Collection<GameMode> getAllowedGameModes()
                {
                        return allowedGameModes;
                }
        }

        public final class FullInventorySettings
        {
                private boolean collect;
                private long checkInterval;
                private double collectRadius;
                private boolean toggleAllowed;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        collect = yaml.getBoolean("FullInventory.CollectItems", false);
                        checkInterval = yaml.getInt("FullInventory.CheckInterval", 1) * 20L;
                        collectRadius = yaml.getDouble("FullInventory.CollectRadius", 1.5);
                        toggleAllowed = yaml.getBoolean("FullInventory.IsToggleAllowed", false);
                }

                public boolean isCollectEnabled()
                {
                        return collect;
                }

                public long getCheckInterval()
                {
                        return checkInterval;
                }

                public double getCollectRadius()
                {
                        return collectRadius;
                }

                public boolean isToggleAllowed()
                {
                        return toggleAllowed;
                }

                public boolean isEnabledOnJoin()
                {
                        return collect;
                }
        }

        public final class ShulkerboxSettings
        {
                private boolean preventInBackpack;
                private boolean disableEntirely;
                private String existingMode;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        preventInBackpack = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) && yaml.getBoolean("Shulkerboxes.PreventInBackpack", true);
                        disableEntirely = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) && yaml.getBoolean("Shulkerboxes.DisableShulkerboxes", false);
                        existingMode = yaml.getString("Shulkerboxes.Existing", "Ignore");
                }

                public boolean isPreventInBackpack()
                {
                        return preventInBackpack;
                }

                public boolean isDisableEntirely()
                {
                        return disableEntirely;
                }

                public boolean isExistingDropEnabled()
                {
                        return "Destroy".equalsIgnoreCase(existingMode);
                }

                public boolean isExistingDestroyEnabled()
                {
                        return "Destroy".equalsIgnoreCase(existingMode) || "Remove".equalsIgnoreCase(existingMode);
                }
        }

        public final class ItemFilterSettings
        {
                private boolean enabled;
                private boolean whitelistMode;
                private Collection<MinecraftMaterial> materials = Collections.emptySet();
                private Set<String> names = Collections.emptySet();
                private Set<String> lore = Collections.emptySet();

                private void reload(@NotNull YamlFileManager yaml)
                {
                        enabled = yaml.getBoolean("ItemFilter.Enabled", false);
                        whitelistMode = yaml.getString("ItemFilter.Mode", "blacklist").toLowerCase(Locale.ENGLISH).equals("whitelist") && enabled;
                        if(!enabled)
                        {
                                materials = Collections.emptySet();
                                names = Collections.emptySet();
                                lore = Collections.emptySet();
                                return;
                        }

                        List<String> stringMaterialList = new ArrayList<>(yaml.getStringList("ItemFilter.Materials", new LinkedList<>()));
                        if(whitelistMode) stringMaterialList.add("air");
                        Collection<MinecraftMaterial> parsedMaterials = new LinkedList<>();
                        for(String item : stringMaterialList)
                        {
                                MinecraftMaterial mat = MinecraftMaterial.fromInput(item);
                                if(mat != null) parsedMaterials.add(mat);
                        }
                        materials = parsedMaterials;

                        Set<String> parsedNames = new HashSet<>();
                        yaml.getStringList("ItemFilter.Names", new LinkedList<>()).forEach(name -> parsedNames.add(ChatColor.translateAlternateColorCodes('&', name)));
                        names = parsedNames;

                        Set<String> parsedLore = new HashSet<>();
                        yaml.getStringList("ItemFilter.Lore", new LinkedList<>()).forEach(entry -> parsedLore.add(ChatColor.translateAlternateColorCodes('&', entry)));
                        lore = parsedLore;
                }

                public boolean isEnabledWithoutShulkerRequirement()
                {
                        return enabled;
                }

                public boolean isEnabled(boolean shulkerPreventionEnabled)
                {
                        return enabled || shulkerPreventionEnabled;
                }

                public Collection<MinecraftMaterial> getFilteredMaterials()
                {
                        return enabled ? new LinkedList<>(materials) : new LinkedList<>();
                }

                public Set<String> getFilteredNames()
                {
                        return enabled ? new HashSet<>(names) : new HashSet<>();
                }

                public Set<String> getFilteredLore()
                {
                        return enabled ? new HashSet<>(lore) : new HashSet<>();
                }

                public boolean isWhitelistMode()
                {
                        return whitelistMode;
                }
        }

        public final class WorldSettings
        {
                        private boolean whitelistMode;
                        private Set<String> filteredWorlds = Collections.emptySet();
                        private WorldBlacklistMode blockMode;

                        private void reload(@NotNull YamlFileManager yaml)
                        {
                                whitelistMode = yaml.getString("WorldSettings.FilterType", "blacklist").equalsIgnoreCase("whitelist");
                                Set<String> worldList = new HashSet<>();
                                for(String world : yaml.getStringList("WorldSettings.FilteredWorlds", new ArrayList<>(0)))
                                {
                                        worldList.add(world.toLowerCase(Locale.ROOT));
                                }
                                filteredWorlds = worldList;
                                blockMode = resolveBlockMode(yaml.getString("WorldSettings.BlockMode", "Message"));
                        }

                        private WorldBlacklistMode resolveBlockMode(String mode)
                        {
                                try
                                {
                                        return WorldBlacklistMode.valueOf(mode);
                                }
                                catch(IllegalArgumentException ignored)
                                {
                                        logger.warning(ConsoleColor.YELLOW + "Unsupported mode \"" + mode + "\" for option \"WorldSettings.BlockMode\"" + ConsoleColor.RESET);
                                        return WorldBlacklistMode.Message;
                                }
                        }

                        public boolean isWhitelistMode()
                        {
                                return whitelistMode;
                        }

                        public Set<String> getFilteredWorlds()
                        {
                                return new HashSet<>(filteredWorlds);
                        }

                        public Set<String> getWorldBlacklist()
                        {
                                if(!whitelistMode) return new HashSet<>(filteredWorlds);
                                Set<String> blacklist = new HashSet<>();
                                for(World world : Bukkit.getServer().getWorlds())
                                {
                                        String worldName = world.getName().toLowerCase(Locale.ROOT);
                                        if(!filteredWorlds.contains(worldName)) blacklist.add(worldName);
                                }
                                return blacklist;
                        }

                        public WorldBlacklistMode getBlockMode()
                        {
                                return blockMode;
                        }
        }

        public final class ItemShortcutSettings
        {
                private boolean enabled;
                private String itemName;
                private String headTextureValue;
                private boolean improveDeathChestCompatibility;
                private boolean blockAsHat;
                private boolean rightClickOnContainerAllowed;
                private int preferredSlotId;
                private boolean blockItemFromMoving;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        enabled = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_8) && yaml.getBoolean("ItemShortcut.Enabled", true);
                        itemName = yaml.getString("ItemShortcut.ItemName", "&eBackpack");
                        headTextureValue = yaml.getString("ItemShortcut.HeadTextureValue", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGRjYzZlYjQwZjNiYWRhNDFlNDMzOTg4OGQ2ZDIwNzQzNzU5OGJkYmQxNzVjMmU3MzExOTFkNWE5YTQyZDNjOCJ9fX0=");
                        improveDeathChestCompatibility = yaml.getBoolean("ItemShortcut.ImproveDeathChestCompatibility", false);
                        blockAsHat = yaml.getBoolean("ItemShortcut.BlockAsHat", false);
                        rightClickOnContainerAllowed = yaml.getBoolean("ItemShortcut.OpenContainerOnRightClick", false) && MCVersion.isNewerOrEqualThan(MCVersion.MC_1_13);
                        preferredSlotId = yaml.getInt("ItemShortcut.PreferredSlotId", -1);
                        blockItemFromMoving = yaml.getBoolean("ItemShortcut.BlockItemFromMoving", false);
                }

                public boolean isEnabled()
                {
                        return enabled;
                }

                public String getItemName()
                {
                        return itemName;
                }

                public String getHeadTextureValue()
                {
                        return headTextureValue;
                }

                public boolean isImproveDeathChestCompatibilityEnabled()
                {
                        return improveDeathChestCompatibility;
                }

                public boolean isBlockAsHatEnabled()
                {
                        return blockAsHat;
                }

                public boolean isRightClickOnContainerAllowed()
                {
                        return rightClickOnContainerAllowed;
                }

                public int getPreferredSlotId()
                {
                        return preferredSlotId;
                }

                public boolean isBlockItemFromMoving()
                {
                        return blockItemFromMoving;
                }
        }

        public final class SoundSettings
        {
                private static final @NotNull String DEFAULT_SOUND_OPEN = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) ? "BLOCK_SHULKER_BOX_OPEN" : (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "BLOCK_CHEST_OPEN" : "CHEST_OPEN");
                private static final @NotNull String DEFAULT_SOUND_CLOSE = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_11) ? "BLOCK_SHULKER_BOX_CLOSE" : (MCVersion.isNewerOrEqualThan(MCVersion.MC_1_9_2) ? "BLOCK_CHEST_CLOSE" : "CHEST_CLOSE");

                private boolean enabled;
                private YamlFileManager yaml;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        this.yaml = yaml;
                        enabled = yaml.getBoolean("Sound.Enabled", true);
                }

                public Sound getOpenSound()
                {
                        return getSound("OpenSound", DEFAULT_SOUND_OPEN);
                }

                public Sound getCloseSound()
                {
                        return getSound("CloseSound", DEFAULT_SOUND_CLOSE);
                }

                private Sound getSound(String option, String autoValue)
                {
                        if(!enabled) return null;
                        String soundName = yaml.getString("Sound." + option, "auto").toUpperCase(Locale.ENGLISH);
                        if(soundName.equals("AUTO")) soundName = autoValue;
                        if(soundName.equals("DISABLED") || soundName.equals("FALSE")) return null;
                        try
                        {
                                if(MCVersion.isNewerOrEqualThan(MCVersion.MC_1_21))
                                {
                                        Field f = Reflection.getField(Sound.class, soundName);
                                        if(f != null) return (Sound) f.get(null);
                                }
                                else
                                {
                                        return Sound.valueOf(soundName);
                                }
                        }
                        catch(Exception ignored)
                        {
                                logger.warning("Unknown sound: " + soundName);
                        }
                        return null;
                }
        }

        public final class InventoryManagementSettings
        {
                private boolean clearCommandEnabled;

                private void reload(@NotNull YamlFileManager yaml)
                {
                        clearCommandEnabled = yaml.getBoolean("InventoryManagement.ClearCommand.Enabled", true);
                }

                public boolean isClearCommandEnabled()
                {
                        return clearCommandEnabled;
                }
        }
        //endregion
}
