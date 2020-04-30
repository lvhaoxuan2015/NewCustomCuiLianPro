package lvhaoxuan.custom.cuilian.runnable;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lvhaoxuan.custom.cuilian.NewCustomCuiLianPro;
import lvhaoxuan.custom.cuilian.api.CuiLianAPI;
import lvhaoxuan.custom.cuilian.message.Message;
import lvhaoxuan.custom.cuilian.object.Level;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.serverct.ersha.jd.AttributeAPI;
import org.bukkit.entity.Player;

public class SyncEffectRunnable implements Runnable {

    public static HashMap<UUID, Level> tmpMap = new HashMap<>();

    @Override
    public void run() {
        for (LivingEntity le : getEntities()) {
            sync(le);
        }
    }

    public void sync(LivingEntity le) {
        Level minLevel = CuiLianAPI.getMinLevel(le, le.getEquipment());
        if (minLevel != null && minLevel.suitEffect != null) {
            for (String potionStr : minLevel.suitEffect.potionEffect) {
                String[] args = potionStr.split(" ");
                String potion = args[0];
                int level = Integer.parseInt(args[1]);
                Bukkit.getScheduler().callSyncMethod(NewCustomCuiLianPro.ins, () -> {
                    le.addPotionEffect(new PotionEffect(PotionEffectType.getByName(potion), 20, level), true);
                    return null;
                });
            }
            if (NewCustomCuiLianPro.apEnable && le instanceof Player) {
                AttributeAPI.addAttribute((Player) le, "NewCustomCuiLianPro", minLevel.suitEffect.attribute, false);
            }
            if (NewCustomCuiLianPro.sxEnable && le instanceof Player) {
                SXAttributeData basic = SXAttribute.getApi().getAPIStats(le.getUniqueId());
                SXAttributeData data = SXAttribute.getApi().getLoreData(null, null, minLevel.suitEffect.attribute);
                SXAttribute.getApi().setEntityAPIData(SyncEffectRunnable.class, le.getUniqueId(), basic.add(data));
            }
            if (!tmpMap.containsKey(le.getUniqueId())) {
                le.sendMessage(Message.ENABLE_SUIT_EFFECT.replace("%s", minLevel.lore.get(0)));
                tmpMap.put(le.getUniqueId(), minLevel);
            } else {
                Level level = tmpMap.get(le.getUniqueId());
                if (level != minLevel) {
                    le.sendMessage(Message.DISENABLE_SUIT_EFFECT.replace("%s", level.lore.get(0)));
                    tmpMap.remove(le.getUniqueId());
                }
            }
        } else if (tmpMap.containsKey(le.getUniqueId())) {
            le.sendMessage(Message.DISENABLE_SUIT_EFFECT.replace("%s", tmpMap.get(le.getUniqueId()).lore.get(0)));
            tmpMap.remove(le.getUniqueId());
            if (NewCustomCuiLianPro.apEnable && le instanceof Player) {
                AttributeAPI.deleteAttribute((Player) le, "NewCustomCuiLianPro");
            }
            if (NewCustomCuiLianPro.sxEnable && le instanceof Player) {
                SXAttribute.getApi().removeEntityAPIData(SyncEffectRunnable.class, le.getUniqueId());
            }
        }
    }

    public static List<LivingEntity> getEntities() {
        List<LivingEntity> entities = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            if (!NewCustomCuiLianPro.otherEntitySuitEffect) {
                for (LivingEntity le : world.getPlayers()) {
                    entities.add(le);
                }
            } else {
                for (LivingEntity le : world.getLivingEntities()) {
                    entities.add(le);
                }
            }
        }
        return entities;
    }
}
