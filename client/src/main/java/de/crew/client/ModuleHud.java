package de.crew.client;
import de.crew.Style;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
public final class ModuleHud {
 public static final ClickCounter LEFT=new ClickCounter(),RIGHT=new ClickCounter();
 private static final DateTimeFormatter TIME=DateTimeFormatter.ofPattern("HH:mm");
 public static void render(GuiGraphicsExtractor g){
  var c=Minecraft.getInstance();var s=CrewClient.settings;if(c.player==null||c.gui.hud.isHidden())return;
  var lines=new ArrayList<String>();
  if(s.hud)lines.add("CREW  /  "+c.getFps()+" FPS");
  if(s.ping){var info=c.getConnection()==null?null:c.getConnection().getPlayerInfo(c.player.getUUID());lines.add("PING  "+(info==null?"--":info.getLatency()+" ms"));}
  if(s.coordinates)lines.add("XYZ  "+c.player.getBlockX()+"  "+c.player.getBlockY()+"  "+c.player.getBlockZ());
  if(s.cps)lines.add("CPS  "+LEFT.count()+" L  /  "+RIGHT.count()+" R");
  if(s.clock)lines.add("ZEIT  "+LocalTime.now().format(TIME));
  if(s.compass)lines.add("BLICK  "+c.player.getDirection().getName().toUpperCase(Locale.ROOT));
  if(c.options.toggleSprint().get())lines.add(c.player.isSprinting()?"SPRINT  aktiv":"SPRINT  bereit");
  int textW=lines.stream().mapToInt(c.font::width).max().orElse(0);
  int width=Math.max(textW+20,s.keystrokes?96:0);if(s.armorHud)width=Math.max(width,112);
  int height=lines.size()*14+(lines.isEmpty()?0:10)+(s.keystrokes?91:0)+(s.armorHud?96:0);
  if(height==0)return;
  int x=s.hudCorner%2==0?10:Math.max(0,g.guiWidth()-width-10),y=s.hudCorner<2?10:Math.max(0,g.guiHeight()-height-10);
  g.fill(x,y,x+width,y+height,0xd9111520);g.fill(x,y,x+2,y+height,Style.COLORS[s.color]);
  int at=y+6;for(String line:lines){g.text(c.font,line,x+9,at,0xffeef3fb,false);at+=14;}
  if(s.keystrokes){
   int kx=x+8;key(g,c.options.keyUp,kx+25,at,23);key(g,c.options.keyLeft,kx,at+25,23);key(g,c.options.keyDown,kx+25,at+25,23);key(g,c.options.keyRight,kx+50,at+25,23);key(g,c.options.keyJump,kx,at+50,73);at+=84;
  }
  if(s.armorHud){
   for(EquipmentSlot slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}){
    var stack=c.player.getItemBySlot(slot);String label=switch(slot){case HEAD->"Kopf";case CHEST->"Brust";case LEGS->"Beine";default->"Fuesse";};
    if(!stack.isEmpty()){
     g.item(stack,x+8,at);g.itemDecorations(c.font,stack,x+8,at);
     String durability=stack.isDamageableItem()?Math.max(0,stack.getMaxDamage()-stack.getDamageValue())+" / "+stack.getMaxDamage():"--";
     int col=stack.isDamageableItem()&&stack.getDamageValue()>stack.getMaxDamage()*0.85?0xffff8f85:0xffb8c7d9;
     g.text(c.font,durability,x+30,at+5,col,false);
    }else g.text(c.font,label+"  --",x+9,at+5,0xff66788e,false);
    at+=22;
   }
  }
 }
 private static void key(GuiGraphicsExtractor g,KeyMapping key,int x,int y,int w){
  var f=Minecraft.getInstance().font;g.fill(x,y,x+w,y+22,key.isDown()?Style.COLORS[CrewClient.settings.color]:0xff263244);
  String label=f.plainSubstrByWidth(key.getTranslatedKeyMessage().getString(),w-4);g.text(f,label,x+(w-f.width(label))/2,y+7,key.isDown()?0xff10221f:0xffc6d2e3,false);
 }
 public static void crosshair(GuiGraphicsExtractor g){
  var c=Minecraft.getInstance();var s=CrewClient.settings;int x=g.guiWidth()/2,y=g.guiHeight()/2,size=s.crosshairSize,gap=s.crosshairGap,color=Style.COLORS[s.color];
  if(s.crosshairStyle==1){g.fill(x-2,y-2,x+3,y+3,0xff071017);g.fill(x-1,y-1,x+2,y+2,color);}
  else if(s.crosshairStyle==2){g.outline(x-size-1,y-size-1,size*2+3,size*2+3,0xff071017);g.outline(x-size,y-size,size*2+1,size*2+1,color);}
  else{
   g.fill(x-gap-size-1,y-1,x-gap+1,y+2,0xff071017);g.fill(x+gap,y-1,x+gap+size+2,y+2,0xff071017);g.fill(x-1,y-gap-size-1,x+2,y-gap+1,0xff071017);g.fill(x-1,y+gap,x+2,y+gap+size+2,0xff071017);
   g.fill(x-gap-size,y,x-gap,y+1,color);g.fill(x+gap+1,y,x+gap+size+1,y+1,color);g.fill(x,y-gap-size,x+1,y-gap,color);g.fill(x,y+gap+1,x+1,y+gap+size+1,color);
  }
  float strength=c.player.getAttackStrengthScale(0);if(strength<1){int by=y+gap+size+5;g.fill(x-8,by,x+8,by+2,0xff263244);g.fill(x-8,by,x-8+(int)(16*strength),by+2,color);}
 }
}
