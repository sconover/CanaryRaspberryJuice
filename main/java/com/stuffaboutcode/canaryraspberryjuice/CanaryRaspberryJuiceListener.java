package com.stuffaboutcode.canaryraspberryjuice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.ChatHook;
import net.canarymod.hook.system.ServerTickHook;
import net.canarymod.plugin.PluginListener;

//import net.canarymod.chat.Colors;
//import net.canarymod.hook.player.ConnectionHook;

public class CanaryRaspberryJuiceListener implements PluginListener {
	
	private final RemoteSessionsHolder remoteSessionsHolder;
	
	// Tools (swords) which can be used to hit blocks
	public static final Set<Integer> blockHitDetectionTools = new HashSet<Integer>(Arrays.asList(
			ItemType.DiamondSword.getId(),
			ItemType.GoldSword.getId(), 
			ItemType.IronSword.getId(), 
			ItemType.StoneSword.getId(), 
			ItemType.WoodSword.getId()));
	
	// Class constructor
	public CanaryRaspberryJuiceListener(RemoteSessionsHolder remoteSessionsHolder) {
		this.remoteSessionsHolder = remoteSessionsHolder;
	}
    
	/*@HookHandler
    public void onLogin(ConnectionHook hook) {
		hook.getPlayer().message(Colors.YELLOW+"Hello " + hook.getPlayer().getName() + " Raspberry Juice is running");
    }*/

	@HookHandler
	public void onTick(ServerTickHook tickHook) {
		//called each tick of the server it gets all the remote sessions to run
		Iterator<RemoteSession> sI = remoteSessionsHolder.get().iterator();
		while(sI.hasNext()) {
			RemoteSession s = sI.next();
			if (s.isPendingRemoval()) {
				s.close();
				sI.remove();
			} else {
				s.tick();
			}
		}
	}
	
	@HookHandler
	public void onBlockHit(BlockRightClickHook hitHook) {
		//DEBUG
		//plugin.getLogman().info("BlockRightHitHook fired");
		//get the player
		Player playerWhoHit = hitHook.getPlayer();
		//get what the player is holding
		Item itemHeld = playerWhoHit.getItemHeld();
		//are they holding something!
		if (itemHeld != null) {
			// is the player holding a sword
			if (blockHitDetectionTools.contains(itemHeld.getId())) {
				System.out.println("block hit!");
				// add the hook event to each session, the session can then decide what to do with it
				for (RemoteSession session: remoteSessionsHolder.get()) {
					session.queueBlockHit(hitHook);
				}
			}
		}
	}
	
	@HookHandler
	public void onChatPost(ChatHook chatHook) {
		//DEBUG
		//plugin.getLogman().info("ChatHook fired");
		
		// add the chat hook event to each session, the session can then decide what to do with it
		for (RemoteSession session: remoteSessionsHolder.get()) {
			session.queueChatPost(chatHook);
		}
	}
	
}