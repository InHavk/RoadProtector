package fromgate.roadprotector;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RPListener implements Listener {
	RoadProtector plg;
	FGUtil u;

	public RPListener (RoadProtector plg) {
		this.plg = plg;
		this.u = plg.u;
	}


	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerMove (PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (plg.speedway&&p.isSprinting()&&
				p.hasPermission("roadprotector.speedway")&&
				(plg.PlaceGuarded(p.getLocation().getBlock()))){
			
			Block b = p.getLocation().getBlock();
			if (b.getType()!=Material.STEP) b = b.getRelative(BlockFace.DOWN);
			
			if (plg.inListId(b.getTypeId(), plg.speedblocks, false))			
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,15,plg.speed));
			
		}
			
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onEntityExplodeEvent (EntityExplodeEvent event) {
		if (plg.explosion_protect)
			if (event.blockList().size()>0)
				for (int i = event.blockList().size()-1;i>=0;i--)
					if (plg.PlaceGuarded(event.blockList().get(i))) event.blockList().remove(i);

	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockBreak (BlockBreakEvent event) {
		Player p = event.getPlayer();
		if ((!plg.EditMode (p))&&(!plg.inListId(event.getBlock().getTypeId(), plg.exclusion_break, false))&&(plg.PlaceGuarded (event.getBlock()))) {
			u.PrintMsgPX(p, "&c"+plg.prtmsg);
			event.setCancelled(true);			
		}
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockFromTo (BlockFromToEvent event) {
		if ((plg.lavaprotect||plg.waterprotect)&&(plg.PlaceGuarded (event.getToBlock()))){
			if ((plg.lavaprotect)&&((event.getBlock().getType()==Material.STATIONARY_LAVA)||
					(event.getBlock().getType()==Material.LAVA))) event.setCancelled(true);
			if ((plg.waterprotect)&&((event.getBlock().getType()==Material.STATIONARY_WATER)||
					(event.getBlock().getType()==Material.WATER))) event.setCancelled(true);	
		}
	}


	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onBlockPlace (BlockPlaceEvent event) {
		Player p = event.getPlayer();
		if ((!plg.EditMode (p))&&(!plg.inListId(event.getBlock().getTypeId(), plg.exclusion_place, false))&&(plg.PlaceGuarded (event.getBlock()))) {
			u.PrintMsgPX(p, "&c"+plg.prtmsg);
			event.setCancelled(true);			
		}
	}

	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerInteract (PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)||event.getAction().equals(Action.LEFT_CLICK_BLOCK))
			if (plg.inListId(event.getClickedBlock().getTypeId(), plg.switchprt, false)) {
				if ((!plg.EditMode (p)&&(plg.PlaceGuarded (event.getClickedBlock())))) {
					u.PrintMsgPX(p, "&c"+plg.prtclickmsg);
					event.setCancelled(true);
				}
			}

		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)&&
				(!plg.EditMode (p))&&
				((p.getItemInHand().getType()==Material.BUCKET)||
						(p.getItemInHand().getType()==Material.WATER_BUCKET)||
						(p.getItemInHand().getType()==Material.LAVA_BUCKET))&&
						(plg.PlaceGuarded (event.getClickedBlock()))){
			u.PrintMsgPX(p, "&c"+plg.prtmsg);
			event.setCancelled(true);
		}
	}


	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlayerJoin (PlayerJoinEvent event) {
		u.UpdateMsg(event.getPlayer());
	}

	@EventHandler(priority=EventPriority.NORMAL)
	public void onPlaceProtector (PlayerInteractEvent event) {
		Player p = event.getPlayer();
		if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK))&&
				(plg.wandmode.containsKey(p.getName()))&&
				(p.getItemInHand().getTypeId()==plg.rpwand)&&
				(plg.wandmode.get(p.getName()))&&
				(p.hasPermission("roadprotector.edit"))	){
			int dd = -1;
			if (plg.inListId(event.getClickedBlock().getTypeId(), plg.rails, false)) dd = -2;
			Block nb = event.getClickedBlock().getRelative(0, dd, 0);
			nb.setTypeId(plg.protector);
			if (plg.effect) plg.ShowEffect(nb.getRelative(0, 2, 0).getLocation());
			else u.PrintMSG (p,"msg_prtinstall", " ["+nb.getWorld().getName()+"] ("+Integer.toString(nb.getX())+", "+Integer.toString(nb.getY())+", "+Integer.toString(nb.getZ())+")"); 
		}
	}
}