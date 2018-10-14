package nl.knokko.main;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

public interface IEntityAttackListener {
	
	public void onEntityAttack(EntityDamageByEntityEvent event);
}
