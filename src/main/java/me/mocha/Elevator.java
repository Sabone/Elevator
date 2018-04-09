package me.mocha;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.event.player.PlayerToggleSneakEvent;
import cn.nukkit.level.Sound;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.AdventureSettings;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.level.particle.SmokeParticle;

import static cn.nukkit.utils.TextFormat.*;

import java.util.*;

public class Elevator extends PluginBase implements Listener {

    Map<String, Integer> cp = new HashMap<>();
    List<String> dp = new ArrayList<>();
    List<String> passeger = new ArrayList<>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.getConfig().save();
        super.onDisable();
    }

    @Override
    public void onLoad() {
        this.getConfig().save();
        super.onLoad();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(RED + "This command use only player!");
            return false;
        }
        try {
            if (args[0].startsWith("c")) {
                cp.put(sender.getName(), Integer.valueOf(args[1]));
                sender.sendMessage(AQUA + "[NOTICE] " + ITALIC + GRAY + "Touch location you want to create the elevator.");
                return true;

            } else if (args[0].startsWith("d")) {
                dp.add(sender.getName());
                sender.sendMessage(AQUA + "[NOTICE] " + ITALIC + GRAY + "Touch location you want to delete the elevator");
                return true;
            } else if (args[0].startsWith("r")) {
                this.getConfig().setAll(new LinkedHashMap<>());
                this.getConfig().save();
                return true;
            }
        }catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
        return false;
    }


    @EventHandler
    public void onTouch(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (cp.containsKey(p.getName())) {
            // x;y;z;level : height
            Block b = e.getBlock();
            String loc = b.getFloorX()+";"+b.getFloorY()+";"+b.getFloorZ()+";"+b.getLevel().getFolderName();
            this.getConfig().set(loc, cp.get(p.getName()));
            this.getConfig().save();
            cp.remove(p.getName());
            p.sendMessage(AQUA+"[NOTICE] "+ITALIC+GRAY+"Elevator was created on "+loc);
            return;
        } else if (dp.contains(p.getName())) {
            Block b = e.getBlock();
            String loc = b.getFloorX()+";"+b.getFloorY()+";"+b.getFloorZ()+";"+b.getLevel().getFolderName();
            if (this.getConfig().exists(loc)) {
                this.getConfig().remove(loc);
                this.getConfig().save();
                dp.remove(p.getName());
                p.sendMessage(AQUA+"[NOTICE] "+ITALIC+GRAY+"Removed this elevator");
                return;
            } else {
                p.sendMessage(AQUA+"[NOTICE] "+ITALIC+GRAY+"This location is not elevator.");
                return;
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent e){
        Player p = e.getPlayer();
        String loc = p.getFloorX()+";"+(p.getFloorY()-1)+";"+p.getFloorZ()+";"+p.getLevel().getFolderName();
        if (this.getConfig().exists(loc)) {
            passeger.add(p.getName());
            int h = this.getConfig().getInt(loc);
            AdventureSettings adventure=p.getAdventureSettings();
            boolean[] settings={false,false,false};
            if(adventure.get(AdventureSettings.Type.FLYING)){
            	settings[0]=true;
            }
            if(adventure.get(AdventureSettings.Type.ALLOW_FLIGHT)){
            	settings[1]=true;
            }
            if(adventure.get(AdventureSettings.Type.NO_CLIP)){
            	settings[2]=true;
            }
            adventure.set(AdventureSettings.Type.FLYING,true);
            adventure.set(AdventureSettings.Type.ALLOW_FLIGHT,true);
            adventure.set(AdventureSettings.Type.NO_CLIP,true);
            p.getAdventureSettings().update();

            for (int i = 0; i < Math.abs(h);i++) {
                this.getServer().getScheduler().scheduleDelayedTask(() -> {
                    //p.teleport(p.add(0, 1));
                    
                    p.teleport(p.getLocation().add(0,1));
                    p.getLevel().addSound(p,Sound.RANDOM_ORB);
                    p.getLevel().addParticle(new SmokeParticle(p,5));
                }, i*120);
            }
            this.getServer().getScheduler().scheduleDelayedTask(() -> {
            	if(!settings[0]){
            		p.getAdventureSettings().set(AdventureSettings.Type.FLYING,false);
            	}
            	if(!settings[1]){
            		p.getAdventureSettings().set(AdventureSettings.Type.ALLOW_FLIGHT,false);
            	}
            	if(!settings[2]){
            		p.getAdventureSettings().set(AdventureSettings.Type.NO_CLIP,false);
            	}
                p.getAdventureSettings().update()
              	p.getLevel().addParticle(new ExplodeParticle(p,5));;
                passeger.remove(p.getName());
            }, Math.abs(h)100);
            return;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (passeger.contains(p.getName())) {
            e.setCancelled();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (passeger.contains(e.getEntity().getName())) {
            e.setCancelled();
        }
    }
}