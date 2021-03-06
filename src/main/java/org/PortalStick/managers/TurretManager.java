package org.PortalStick.managers;

import java.util.ArrayList;
import java.util.UUID;

import org.PortalStick.components.Turret;
import com.sanjay900.nmsUtil.util.V10Location;
import org.bukkit.entity.Entity;

class TurretManager {

    private final ArrayList<Turret> turrets = new ArrayList<Turret>();
    
    public void check(Entity entity, V10Location loc) {
        UUID uuid = entity.getUniqueId();
        for(Turret turret: turrets) {
            if(turret.target == null)
                turret.checkPossibleTarget(uuid, loc);
            else if(uuid.equals(turret.target))
                turret.checkMovement(loc);
        }
    }
}
