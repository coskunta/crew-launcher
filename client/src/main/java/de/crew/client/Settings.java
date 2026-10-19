package de.crew.client;
import com.google.gson.*;
import java.nio.file.*;
import java.io.IOException;
import de.crew.Style;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.LoggerFactory;
public class Settings {
 public boolean fullbright=false,hud=true,coordinates=false,showCosmetics=true,showEmotes=true;
 public boolean zoom=true,keystrokes=false,cps=false,ping=true,armorHud=false,customCrosshair=false,clock=false,compass=false,shareCape=true;
 public int zoomFactor=4,crosshairStyle=0,crosshairSize=4,crosshairGap=2,hudScale=1;
 public int cape=1,wings=1,color=0,hudCorner=0;
 private static final Path FILE=FabricLoader.getInstance().getConfigDir().resolve("crew-client.json");
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 public static Settings load(){
  try {if(Files.exists(FILE)){var c=GSON.fromJson(Files.readString(FILE),Settings.class);if(c!=null){c.cape=Math.clamp(c.cape,0,5);c.wings=Math.clamp(c.wings,0,2);c.color=Math.clamp(c.color,0,4);c.hudCorner=Math.clamp(c.hudCorner,0,3);c.zoomFactor=Math.clamp(c.zoomFactor,2,8);c.crosshairStyle=Math.clamp(c.crosshairStyle,0,2);c.crosshairSize=Math.clamp(c.crosshairSize,2,8);c.crosshairGap=Math.clamp(c.crosshairGap,0,5);return c;}}}
  catch(Exception e){LoggerFactory.getLogger("crew").warn("Cannot load settings; keeping backup",e);try{Files.copy(FILE,FILE.resolveSibling("crew-client.invalid.json"),StandardCopyOption.REPLACE_EXISTING);}catch(IOException ignored){}}
  return new Settings();
 }
 public boolean save(){
  try {Files.createDirectories(FILE.getParent());var temp=FILE.resolveSibling("crew-client.json.tmp");Files.writeString(temp,GSON.toJson(this));try{Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,FILE,StandardCopyOption.REPLACE_EXISTING);}return true;}
  catch(IOException e){LoggerFactory.getLogger("crew").error("Cannot save settings",e);return false;}
 }
 public Style style(int emote){return new Style(cape,wings,color,emote);}
}
