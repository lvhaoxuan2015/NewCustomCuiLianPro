package lvhaoxuan.custom.cuilian.runnable;

import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import lvhaoxuan.custom.cuilian.api.CuiLianAPI;
import lvhaoxuan.custom.cuilian.object.Level;
import org.bukkit.entity.LivingEntity;

public class ScriptRunnable implements Runnable {

    public static boolean enbaleScript = true;

    @Override
    public void run() {
        for (LivingEntity le : SyncEffectRunnable.getEntities()) {
            sync(le);
        }
    }

    public void sync(LivingEntity le) {
        Level minLevel = CuiLianAPI.getMinLevel(le, le.getEquipment());
        if (minLevel != null && minLevel.suitEffect != null) {
            try {
                if (enbaleScript) {
                    for (ScriptEngine engine : minLevel.suitEffect.script) {
                        Invocable invocable = (Invocable) engine;
                        invocable.invokeFunction("onEffectTick", le);
                    }
                }
            } catch (NoSuchMethodException | ScriptException ex) {
                Logger.getLogger(SyncEffectRunnable.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                enbaleScript = false;
            }
        }
    }
}
