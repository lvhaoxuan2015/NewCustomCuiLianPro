package lvhaoxuan.custom.cuilian.api;

import java.util.ArrayList;
import java.util.List;
import lvhaoxuan.custom.cuilian.NewCustomCuiLianPro;
import lvhaoxuan.custom.cuilian.message.Message;
import lvhaoxuan.custom.cuilian.object.Level;
import lvhaoxuan.custom.cuilian.object.ProtectRune;
import lvhaoxuan.custom.cuilian.object.Stone;
import lvhaoxuan.llib.api.LLibAPI;
//import lvhaoxuan.llib.util.NBT;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CuiLianAPI {

    public static boolean hasOffHand;

    static {
        hasOffHand = NewCustomCuiLianPro.judgeOffHand;
        if (hasOffHand) {
            try {
                EntityEquipment.class.getMethod("getItemInOffHand");
            } catch (NoSuchMethodException | SecurityException ex) {
                hasOffHand = false;
            }
        }
    }

    public static boolean canCuiLian(ItemStack item) {
        if (LLibAPI.checkItemNull(item)) {
            for (NewCustomCuiLianPro.ItemType type : NewCustomCuiLianPro.types) {
                if (type.type == item.getType()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack cuilian(Stone stone, ItemStack item, Player p) {
        if (canCuiLian(item)) {
            Level basicLevelObj = Level.byItemStack(item);
            int basicLevel = (basicLevelObj != null ? basicLevelObj.value : 0);
            Level toLevel;
            double probability = LLibAPI.getRandom(0, 100);
            boolean success = probability <= stone.chance.get(Level.levels.get(basicLevel + stone.riseLevel));
            String sendMessage = null;
            if (success) {
                toLevel = Level.levels.get(basicLevel + stone.riseLevel);
                item = setItemLevel(item, toLevel);
                sendMessage = Message.SUCCESS.replace("%s", toLevel.lore.get(0));
                if (toLevel.value >= 5) {
                    Bukkit.broadcastMessage(Message.SERVER_SUCCESS.replace("%p", p.getDisplayName()).replace("%d", stone.item.getItemMeta().getDisplayName()).replace("%s", toLevel.lore.get(0)));
                }
            } else {
                int dropLevel = stone.dropLevel.toInteger();
                Level protectRune = Level.byProtectRune(item);
                if (protectRune != null) {
                    if (protectRune.value <= basicLevel) {
                        if (basicLevel - protectRune.value <= dropLevel) {
                            dropLevel = basicLevel - protectRune.value != 0 ? LLibAPI.getRandom(0, basicLevel - protectRune.value) : 0;
                        }
                        toLevel = Level.levels.get(basicLevel - dropLevel);
                        item = setItemLevel(item, toLevel);
                        sendMessage = Message.CUILIAN_FAIL_PROTECT_RUNE.replace("%s", toLevel.lore.get(0)).replace("%d", String.valueOf(dropLevel));
                    } else {
                        toLevel = Level.levels.get(basicLevel - dropLevel);
                        item = setItemLevel(item, toLevel);
                        sendMessage = Message.CUILIAN_FAIL.replace("%s", toLevel.lore.get(0)).replace("%d", String.valueOf(dropLevel));
                    }
                } else {
                    toLevel = Level.levels.get(basicLevel - dropLevel);
                    item = setItemLevel(item, toLevel);
                    sendMessage = Message.CUILIAN_FAIL.replace("%s", toLevel != null ? toLevel.lore.get(0) : "§c§l淬炼消失").replace("%d", String.valueOf(dropLevel));
                }
            }
            p.sendMessage(sendMessage);
        }
        return item;
    }

    public static ItemStack setItemLevel(ItemStack item, Level level) {
        if (canCuiLian(item)) {
            int basicLevel = (level != null ? level.value : 0);
            ItemMeta meta = item.getItemMeta();
            setDisplayName(meta, basicLevel);
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore = cleanLevel(lore);
            lore = cleanProtectRune(lore);
            lore = replaceLore(lore);
            if (level != null) {
                if (!Message.UNDER_LINE.isEmpty()) {
                    lore.add(NewCustomCuiLianPro.LEVEL_JUDGE + Message.UNDER_LINE);
                }
                for (String line : level.lore) {
                    lore.add(NewCustomCuiLianPro.LEVEL_JUDGE + line);
                }
                for (String line : level.attribute.get(NewCustomCuiLianPro.typesInBag.get(item.getType()))) {
                    lore.add(NewCustomCuiLianPro.LEVEL_JUDGE + line);
                }
            }
            Level protectRuneLevel = Level.byProtectRune(item);
            if (protectRuneLevel != null && protectRuneLevel.protectRune != null) {
                lore.add(NewCustomCuiLianPro.PROTECT_RUNE_JUDGE + protectRuneLevel.protectRune.lore);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void setDisplayName(ItemMeta meta, int basicLevel) {
        String displayName = meta.hasDisplayName() ? meta.getDisplayName() : "";
        displayName = displayName.replaceAll("\\+[0-9]*", "");
        displayName = NewCustomCuiLianPro.displayNameFormat.replace("%level%", "+" + basicLevel).replace("%name%", displayName);
        meta.setDisplayName(displayName);
    }

    public static ItemStack addProtectRune(ItemStack item, ProtectRune protectRune) {
        if (LLibAPI.checkItemNull(item)) {
            if (protectRune != null) {
                ItemMeta meta = item.getItemMeta();
                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                lore = cleanProtectRune(lore);
                lore.add(NewCustomCuiLianPro.PROTECT_RUNE_JUDGE + protectRune.lore);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    public static List<String> cleanLevel(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains(NewCustomCuiLianPro.LEVEL_JUDGE)) {
                lore.remove(i--);
            }
        }
        return lore;
    }

    public static List<String> cleanProtectRune(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains(NewCustomCuiLianPro.PROTECT_RUNE_JUDGE)) {
                lore.remove(i--);
            }
        }
        return lore;
    }

    public static List<String> replaceLore(List<String> lore) {
        for (int i = 0; i < lore.size(); i++) {
            for (String replace : NewCustomCuiLianPro.replaceLore) {
                if (lore.get(i).contains(replace)) {
                    lore.remove(i--);
                }
            }
        }
        return lore;
    }

    public static Level getMinLevel(LivingEntity entity, EntityEquipment equipment) {
        int ret = -1;
        for (ItemStack item : equipment.getArmorContents()) {
            Level level = Level.byItemStack(item);
            int basicLevel = (level != null ? level.value : 0);
            ret = (ret == -1 ? basicLevel : Math.min(ret, basicLevel));
        }
        ItemStack item = LLibAPI.getItemInHand(entity);
        Level level = Level.byItemStack(item);
        int basicLevel = (level != null ? level.value : 0);
        ret = (ret == -1 ? basicLevel : Math.min(ret, basicLevel));
        if (hasOffHand) {
            item = LLibAPI.getItemInOffHand(entity);
            level = Level.byItemStack(item);
            basicLevel = (level != null ? level.value : 0);
            ret = (ret == -1 ? basicLevel : Math.min(ret, basicLevel));
        }
        return Level.levels.get(ret);
    }
}
