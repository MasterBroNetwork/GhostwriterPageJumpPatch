package wafflestomper.ghostwriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ReadBookScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import wafflestomper.ghostwriter.modified_mc_files.ReadBookScreenMod;


@Mod("ghostwriter")
public class Ghostwriter{
	
	private Minecraft mc = Minecraft.getInstance();
	private Printer printer = new Printer();
	public Clipboard clipboard = new Clipboard();
	boolean devEnv = false;
	private long lastMessage = 0;
	private static final Logger LOGGER = LogManager.getLogger();
	
	
	public Ghostwriter(){
//		FMLCommonHandler.instance().bus().register(this);
//		MinecraftForge.EVENT_BUS.register(this);
//		// Detect development environment
//		this.devEnv = (Boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");
		
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
	}
	
	
	private void setup(final FMLClientSetupEvent event){
        LOGGER.info("Ghostwriter setup running...");
    }
	
	
	private void rateLimitedDebugMessage(String message){
		if (!this.devEnv){
			return;
		}
		if (System.currentTimeMillis() - this.lastMessage > 2000){
			this.printer.gamePrint(message);
			this.lastMessage = System.currentTimeMillis();
		}
	}
	
	
	/**
	 * This glorious bastard swaps the default book GUI for the Ghostwriter screen before it even loads
	 * I love the future
	 */
	@net.minecraftforge.eventbus.api.SubscribeEvent
	public void guiOpen(GuiOpenEvent event){
		Screen eventGui = event.getGui();
		if (eventGui == null){return;}
		LOGGER.debug(eventGui.toString());
		
		// TODO: Signed books are handled differently from unsigned books for some bizarre reason
		if (eventGui instanceof net.minecraft.client.gui.screen.EditBookScreen || eventGui instanceof ReadBookScreen){
			ClientPlayerEntity p = this.mc.player;
        	ItemStack currStack = p.getHeldItem(Hand.MAIN_HAND);
        	
			// Abort if the player is crouching
			if (p.isSneaking()) {
				LOGGER.debug("Aborting GUI replacement becuase the player is crouching");
				return;
			}
        	
        	if (currStack != null){
        		Item currItem = currStack.getItem();
        		if (currItem != null){
        			// If left shift is held down, let the standard Minecraft book GUI open
//        			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){ // TODO: Re-enable this
//        				return;
//        			}
        			//eventGui = new GuiGhostwriterBook(p, currStack, Hand.MAIN_HAND); // p, currStack, currItem.equals(Items.WRITABLE_BOOK), this.clipboard);
        			
        			if (eventGui instanceof net.minecraft.client.gui.screen.EditBookScreen){
        				eventGui = new GhostwriterEditBookScreen(p, currStack, Hand.MAIN_HAND, this.clipboard);
            			//eventGui = new EditBookScreenMod(p,currStack, Hand.MAIN_HAND);
        			}
        			else {
        				//ReadBookScreen factoryScreen = (ReadBookScreen)eventGui;
        				eventGui = new ReadBookScreenMod(new ReadBookScreenMod.WrittenBookInfo(currStack), false);
        			}
        			event.setGui(eventGui);
        			LOGGER.debug("GUI swap done!");
        		}
        		else{
            		rateLimitedDebugMessage("this.mc.thePlayer.getHeldItem().getItem() is null!");
            	}
        	}
        	else{
        		rateLimitedDebugMessage("this.mc.thePlayer.getHeldItem() is null!");
        	}
		}
	}
}
