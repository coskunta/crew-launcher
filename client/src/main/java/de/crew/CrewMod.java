package de.crew;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
public class CrewMod implements ModInitializer {
 private record Entry(Style style,long started){}
 private static final Map<UUID,Entry> styles=new HashMap<>();
 private static final Map<UUID,byte[]> capes=new HashMap<>();
 private static final Map<UUID,Long> capeRates=new HashMap<>();
 private static final Map<UUID,Long> rates=new HashMap<>();
 public void onInitialize(){
  PayloadTypeRegistry.serverboundPlay().register(CapePayload.TYPE,CapePayload.CODEC);
  PayloadTypeRegistry.clientboundPlay().register(CapePayload.TYPE,CapePayload.CODEC);
  ServerPlayNetworking.registerGlobalReceiver(CapePayload.TYPE,(payload,ctx)->ctx.server().execute(()->{
   var id=ctx.player().getUUID();long now=System.nanoTime();
   if(!payload.valid()||now-capeRates.getOrDefault(id,0L)<1_000_000_000L)return;
   if(payload.pixels().length>0&&capes.size()>=256&&!capes.containsKey(id))return;
   capeRates.put(id,now);if(payload.pixels().length==0)capes.remove(id);else capes.put(id,payload.pixels().clone());
   sendCape(ctx.server(),new CapePayload(id,payload.pixels()));
  }));
  PayloadTypeRegistry.serverboundPlay().register(StylePayload.TYPE,StylePayload.CODEC);
  PayloadTypeRegistry.clientboundPlay().register(StylePayload.TYPE,StylePayload.CODEC);
  ServerPlayNetworking.registerGlobalReceiver(StylePayload.TYPE,(payload,ctx)->ctx.server().execute(()->{
   var player=ctx.player();var id=player.getUUID();long now=System.nanoTime();
   if(!payload.style().valid() || now-rates.getOrDefault(id,0L)<100_000_000L)return;
   rates.put(id,now);
   // Identity always comes from the authenticated connection, never from the payload.
   int elapsed=Math.clamp(payload.elapsed(),0,6000);
   long start=now-elapsed*1_000_000L;
   var entry=new Entry(payload.style(),start);styles.put(id,entry);
   broadcast(ctx.server(),packet(id,entry));
  }));
  ServerPlayConnectionEvents.JOIN.register((handler,sender,server)->{
   if(ServerPlayNetworking.canSend(handler.player,CapePayload.TYPE))capes.forEach((id,bytes)->ServerPlayNetworking.send(handler.player,new CapePayload(id,bytes)));
   if(ServerPlayNetworking.canSend(handler.player,StylePayload.TYPE))styles.forEach((id,e)->ServerPlayNetworking.send(handler.player,packet(id,e)));
  });
  ServerPlayConnectionEvents.DISCONNECT.register((handler,server)->{
   var id=handler.player.getUUID();capes.remove(id);capeRates.remove(id);sendCape(server,new CapePayload(id,new byte[0]));styles.remove(id);rates.remove(id);broadcast(server,new StylePayload(id,Style.NONE,0,true));
  });
  ServerLifecycleEvents.SERVER_STOPPED.register(server->{styles.clear();rates.clear();capes.clear();capeRates.clear();});
 }
 private static void sendCape(MinecraftServer server,CapePayload p){for(var to:server.getPlayerList().getPlayers())if(ServerPlayNetworking.canSend(to,CapePayload.TYPE))ServerPlayNetworking.send(to,p);}
 private static StylePayload packet(UUID id,Entry e){
  int elapsed=(int)Math.min(6000,(System.nanoTime()-e.started)/1_000_000L);
  return new StylePayload(id,elapsed>=6000?e.style.stop():e.style,elapsed,false);
 }
 private static void broadcast(MinecraftServer server,StylePayload p){
  for(ServerPlayer to:server.getPlayerList().getPlayers())if(ServerPlayNetworking.canSend(to,StylePayload.TYPE))ServerPlayNetworking.send(to,p);
 }
}
