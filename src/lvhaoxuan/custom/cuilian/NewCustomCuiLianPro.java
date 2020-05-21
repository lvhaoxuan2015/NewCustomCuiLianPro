package lvhaoxuan.custom.cuilian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lvhaoxuan.custom.cuilian.commander.Commander;
import lvhaoxuan.custom.cuilian.message.Message;
import lvhaoxuan.custom.cuilian.listener.FurnaceListener;
import lvhaoxuan.custom.cuilian.listener.ProtectRuneListener;
import lvhaoxuan.custom.cuilian.loader.Loader;
import lvhaoxuan.custom.cuilian.metrics.Metrics;
import lvhaoxuan.custom.cuilian.movelevel.MoveLevelHandle;
import lvhaoxuan.custom.cuilian.runnable.ScriptRunnable;
import lvhaoxuan.custom.cuilian.runnable.SyncEffectRunnable;
import lvhaoxuan.llib.util.HttpUtil;
import lvhaoxuan.llib.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

public class NewCustomCuiLianPro extends JavaPlugin {

    public static String PROTECT_RUNE_JUDGE;
    public static String LEVEL_JUDGE;
    public static NewCustomCuiLianPro ins;
    public static HashMap<Material, String> typesInBag = new HashMap<>();
    public static List<ItemType> types = new ArrayList<>();
    public static boolean otherEntitySuitEffect;
    public static boolean judgeOffHand;
    public static String displayNameFormat;
    public static List<String> replaceLore;
    public static boolean apEnable = false;
    public static boolean sxEnable = false;

    @Override
    public void onEnable() {
        ins = this;
        new Metrics(this, 7315);
        this.getServer().getConsoleSender().sendMessage("§7[§e" + this.getName() + "§7]§a作者lvhaoxuan(隔壁老吕)|QQ3295134931");
        if (this.getServer().getPluginManager().getPlugin("AttributePlus") != null) {
            this.getServer().getConsoleSender().sendMessage("§7[§e" + this.getName() + "§7]§a检测到AttributePlus插件，属性模块加载");
            apEnable = true;
        }
        if (this.getServer().getPluginManager().getPlugin("SX-Attribute") != null) {
            this.getServer().getConsoleSender().sendMessage("§7[§e" + this.getName() + "§7]§a检测到SX-Attribute插件，属性模块加载");
            sxEnable = true;
        }
        enableConfig();
        //checkVersion();
        this.getServer().getPluginCommand("cuilian").setExecutor(new Commander());
        this.getServer().getPluginManager().registerEvents(new FurnaceListener(), this);
        this.getServer().getPluginManager().registerEvents(new ProtectRuneListener(), this);
        setRecipe();
        Bukkit.getScheduler().runTaskTimerAsynchronously(NewCustomCuiLianPro.ins, new SyncEffectRunnable(), 0, 10);
        Bukkit.getScheduler().runTaskTimerAsynchronously(NewCustomCuiLianPro.ins, new ScriptRunnable(), 0, 2);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
    }

    public void checkVersion() {
        String ip = "http://raw.githubusercontent.com/lvhaoxuan2015/NewCustomCuiLianPro/master/version.txt";
        String ret = HttpUtil.invokeUrl(ip, new HashMap<>(), "UTF-8", "GET");
        String[] lines = ret.split("\n");
        String selfVersion = getDescription().getVersion();
        String newestVersion = lines[0];
        if (!selfVersion.equals(newestVersion)) {
            this.getServer().getConsoleSender().sendMessage("§7[§e" + this.getName() + "§7]§a插件有更新版本: " + newestVersion);
            for (String line : lines) {
                this.getServer().getConsoleSender().sendMessage("§7[§e" + this.getName() + "§7]" + line);
            }
        }
    }

    public static void enableConfig() {
        Message.loadMessages();
        Loader.loadConfig();
        Loader.loadItems();
        Loader.loadLevels();
        Loader.loadStones();
        MoveLevelHandle.init();
    }

    public static void setRecipe() {
        for (ItemType type : types) {
            FurnaceRecipe recipe = new FurnaceRecipe(type.toItemStack(), type.mData);
            for (int durability = 0; durability <= type.type.getMaxDurability(); durability++) {
                recipe.setInput(type.type, durability);
                try {
                    ins.getServer().addRecipe(recipe);
                } catch (IllegalStateException ex) {
                }
            }
        }
    }

    public static class ItemType {

        public String typeInBag;
        public String baseType;
        public Material type;
        public MaterialData mData;

        public ItemType(String typeInBag, String baseType) {
            this.typeInBag = typeInBag;
            this.baseType = baseType;
            if (baseType.contains(":")) {
                String[] args = baseType.split(":");
                String strType = args[0];
                String strData = args[1];
                type = MathUtil.isNumeric(strType) ? Material.getMaterial(Integer.parseInt(strType)) : Material.getMaterial(strType);
                int data = Integer.parseInt(strData);
                mData = new MaterialData(type, (byte) data);
            } else {
                type = MathUtil.isNumeric(baseType) ? Material.getMaterial(baseType) : Material.getMaterial(baseType);
                mData = new MaterialData(type);
            }
        }

        public ItemStack toItemStack() {
            return mData.toItemStack(1);
        }
    }
}
