package de.crew.client;
import java.util.*;
import de.crew.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
public class CrewClient implements ClientModInitializer {
 public static Settings settings=new Settings();
 public record Look(Style style,long started){public float seconds(){return Math.max(0,(System.nanoTime()-started)/1_000_000_000f);}}
 private static final Map<UUID,Look> remote=new HashMap<>();
 private static int emote=0,ticks=0;private static long started=0;private static boolean pending=true;
 private static KeyMapping menu,dance,bright,zoom;
 public static String notice="";
 public void onInitializeClient(){
  settings=Settings.load();CapeTextures.initialize();var category=KeyMapping.Category.register(id("controls"));
  menu=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.crew.menu",InputConstants.KEY_RSHIFT,category));
  dance=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.crew.emotes",InputConstants.KEY_G,category));
  bright=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.crew.fullbright",InputConstants.KEY_B,category));
  zoom=KeyMappingHelper.registerKeyMapping(new KeyMapping("key.crew.zoom",InputConstants.KEY_C,category));
  LivingEntityRenderLayerRegistrationCallback.EVENT.register((type,renderer,helper,ctx)->{if(renderer instanceof AvatarRenderer<?> avatar)helper.register(new CosmeticLayer(avatar));});
  ClientPlayNetworking.registerGlobalReceiver(StylePayload.TYPE,(packet,ctx)->ctx.client().execute(()->{
   if(packet.remove())remote.remove(packet.player());
   else if(packet.style().valid())remote.put(packet.player(),new Look(packet.style(),System.nanoTime()-Math.clamp(packet.elapsed(),0,6000)*1_000_000L));
  }));
  ClientPlayConnectionEvents.JOIN.register((h,s,c)->{remote.clear();emote=0;pending=true;CapeTextures.resetConnection();ModuleHud.LEFT.clear();ModuleHud.RIGHT.clear();});
  ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{remote.clear();emote=0;pending=true;CapeTextures.resetConnection();ModuleHud.LEFT.clear();ModuleHud.RIGHT.clear();});
  ClientTickEvents.END_CLIENT_TICK.register(c->{
   while(menu.consumeClick())if(c.player!=null)c.gui.setScreen(c.gui.screen() instanceof CrewScreen?null:new CrewScreen(0));
   while(dance.consumeClick())if(c.player!=null)c.gui.setScreen(new CrewScreen(2));
   while(bright.consumeClick()){settings.fullbright=!settings.fullbright;changed();}
   CapeTextures.tick();
   if(c.player==null)return;
   if(emote!=0&&((System.nanoTime()-started)>6_000_000_000L||c.options.keyUp.isDown()||c.options.keyDown.isDown()||c.options.keyLeft.isDown()||c.options.keyRight.isDown()||c.options.keyJump.isDown()||c.options.keyAttack.isDown()||c.options.keyUse.isDown()||c.player.isFallFlying()||c.player.isSwimming()||c.player.isPassenger())){emote=0;pending=true;}
   if(++ticks%5==0&&pending&&canSync()){
    int elapsed=emote==0?0:(int)Math.min(6000,(System.nanoTime()-started)/1_000_000L);
    ClientPlayNetworking.send(new StylePayload(c.player.getUUID(),settings.style(emote),elapsed,false));pending=false;
   }
  });
  HudElementRegistry.addLast(id("hud"),(g,delta)->ModuleHud.render(g));
 }
 public static boolean zooming(){var c=Minecraft.getInstance();return settings.zoom&&zoom!=null&&zoom.isDown()&&c.player!=null&&c.gui.screen()==null;}
 public static Identifier id(String p){return Identifier.fromNamespaceAndPath("crew",p);}
 public static boolean canSync(){return Minecraft.getInstance().getConnection()!=null&&ClientPlayNetworking.canSend(StylePayload.TYPE);}
 public static void changed(){notice=settings.save()?"Gespeichert":"Speichern fehlgeschlagen. Siehe Log.";pending=true;}
 public static void play(int i){emote=Math.clamp(i,0,4);started=System.nanoTime();pending=true;}
 public static Look look(UUID id){
  var p=Minecraft.getInstance().player;
  if(p!=null&&p.getUUID().equals(id))return new Look(settings.style(emote),started);
  var l=remote.getOrDefault(id,new Look(Style.NONE,0));return l.seconds()>=6?new Look(l.style.stop(),l.started):l;
 }
}
