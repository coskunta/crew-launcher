package de.crew.client;
import de.crew.CapePayload;
import java.util.*;
import java.nio.*;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
public final class CapeTextures {
 private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("crew-cape.argb");
 private static final Map<UUID,Identifier> textures=new HashMap<>();
 private static final Identifier LOCAL=CrewClient.id("dynamic/local_cape");
 private static byte[] local;private static byte[] sent;private static long nextSend;private static boolean loading=false,importing=false;
 public static String message="PNG auswählen oder in dieses Fenster ziehen.";
 public static void initialize(){
  ClientPlayNetworking.registerGlobalReceiver(CapePayload.TYPE,(p,c)->c.client().execute(()->{
   if(!p.valid())return;if(c.player()!=null&&p.player().equals(c.player().getUUID()))return;
   if(p.pixels().length==0){var id=textures.remove(p.player());if(id!=null)c.client().getTextureManager().release(id);return;}
   if(textures.size()>=256&&!textures.containsKey(p.player()))return;
   var id=CrewClient.id("dynamic/cape_"+p.player().toString().replace("-",""));register(id,p.pixels());textures.put(p.player(),id);
  }));
 }
 public static void tick(){
  var c=Minecraft.getInstance();
  if(local==null&&!loading&&Files.exists(FILE)){loading=true;CompletableFuture.supplyAsync(()->{try{return Files.size(FILE)==8192?Files.readAllBytes(FILE):null;}catch(Exception e){return null;}}).thenAccept(bytes->c.execute(()->{if(bytes!=null){local=bytes;register(LOCAL,bytes);}}));}
  if(c.player==null||c.getConnection()==null||!ClientPlayNetworking.canSend(CapePayload.TYPE)||System.nanoTime()<nextSend)return;
  byte[] desired=CrewClient.settings.shareCape&&CrewClient.settings.cape==5&&local!=null?local:new byte[0];
  if(sent==null||!Arrays.equals(desired,sent)){ClientPlayNetworking.send(new CapePayload(c.player.getUUID(),desired));sent=desired.clone();nextSend=System.nanoTime()+1_200_000_000L;}
 }
 public static void importFile(Path file){
  if(importing)return;importing=true;message="Cape wird geprüft …";var c=Minecraft.getInstance();
  CompletableFuture.supplyAsync(()->{try{byte[] bytes=CapeImage.read(file);Files.createDirectories(FILE.getParent());Path tmp=FILE.resolveSibling("crew-cape.tmp");Files.write(tmp,bytes);Files.move(tmp,FILE,StandardCopyOption.REPLACE_EXISTING);return bytes;}catch(Exception e){throw new java.util.concurrent.CompletionException(e);}}).whenComplete((bytes,error)->c.execute(()->{
   importing=false;if(error!=null){message=error.getCause()==null?"Import fehlgeschlagen":error.getCause().getMessage();return;}
   local=bytes;loading=true;register(LOCAL,bytes);CrewClient.settings.cape=5;CrewClient.changed();message="Cape importiert und ausgewählt.";
  }));
 }
 private static void register(Identifier id,byte[] bytes){
  var image=new NativeImage(64,32,false);var b=ByteBuffer.wrap(bytes);for(int y=0;y<32;y++)for(int x=0;x<64;x++)image.setPixel(x,y,b.getInt());
  Minecraft.getInstance().getTextureManager().register(id,new DynamicTexture(()->"CREW personal cape",image));
 }
 public static Identifier texture(UUID player){var c=Minecraft.getInstance();return c.player!=null&&player.equals(c.player.getUUID())?(local==null?null:LOCAL):textures.get(player);}
 public static Identifier localTexture(){return local==null?null:LOCAL;}
 public static void resetConnection(){for(var id:textures.values())Minecraft.getInstance().getTextureManager().release(id);textures.clear();sent=null;nextSend=0;}
}
