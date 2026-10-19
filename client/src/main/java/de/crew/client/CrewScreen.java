package de.crew.client;
import de.crew.Style;
import java.util.function.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.blaze3d.platform.InputConstants;
public class CrewScreen extends Screen {
 private int page=0;
 private int tab,x,y,w,h,nav=100,content;
 private static final String[] TABS={"Module","Cosmetics","Emotes","Performance"};
 private static final String[] CAPES={"Aus","Mint Grid","Violet Night","Ember","Ressourcenpaket","Importiertes PNG"};
 private static final String[] WINGS={"Aus","Feather","Mecha"};
 public CrewScreen(int tab){super(Component.literal("CREW Client"));this.tab=tab;}
 protected void init(){
  w=Math.min(620,width-16);h=Math.min(356,height-16);x=(width-w)/2;y=(height-h)/2;nav=w<450?78:100;content=x+nav+18;
  for(int i=0;i<4;i++){final int t=i;addRenderableWidget(new Tile(x+10,y+67+i*34,nav-20,28,()->TABS[t],()->"",()->tab==t,()->{tab=t;rebuildWidgets();}));}
  addRenderableWidget(new Tile(x+w-38,y+14,24,22,()->"X",()->"",()->false,this::onClose));
  int available=w-nav-36,col=(available-10)/2,top=y+100,row=Math.max(20,Math.min(57,(h-150)/3));
  var s=CrewClient.settings;
  if(tab==0){
   addRenderableWidget(new Tile(content,y+70,26,20,()->"<",()->"",()->false,()->{page=(page+3)%4;rebuildWidgets();}));
   addRenderableWidget(new Tile(content+32,y+70,110,20,()->new String[]{"Spiel / 1 von 4","HUD / 2 von 4","Details / 3 von 4","Extras / 4 von 4"}[page],()->"",()->false,()->{page=(page+1)%4;rebuildWidgets();}));
   addRenderableWidget(new Tile(content+148,y+70,26,20,()->">",()->"",()->false,()->{page=(page+1)%4;rebuildWidgets();}));
   if(page==0){
    toggle(0,"Fullbright","Dunkle Bereiche aufhellen",()->s.fullbright,()->s.fullbright=!s.fullbright,col,top,row);
    toggle(1,"Zoom","C gedrückt halten",()->s.zoom,()->s.zoom=!s.zoom,col,top,row);
    option(2,"Toggle Sprint","Sprint-Taste schaltet um",()->minecraft.options.toggleSprint().get(),()->minecraft.options.toggleSprint().set(!minecraft.options.toggleSprint().get()),col,top,row);
    option(3,"Kamerawackeln","Bewegung beim Laufen",()->minecraft.options.bobView().get(),()->minecraft.options.bobView().set(!minecraft.options.bobView().get()),col,top,row);
    option(4,"Schadenwackeln","Kamera bei Treffern",()->minecraft.options.damageTiltStrength().get()>0,()->minecraft.options.damageTiltStrength().set(minecraft.options.damageTiltStrength().get()>0?0d:1d),col,top,row);
    option(5,"FOV-Effekte","Dynamisches Sichtfeld",()->minecraft.options.fovEffectScale().get()>0,()->minecraft.options.fovEffectScale().set(minecraft.options.fovEffectScale().get()>0?0d:1d),col,top,row);
   }else if(page==1){
    toggle(0,"FPS-Anzeige","Bildrate",()->s.hud,()->s.hud=!s.hud,col,top,row);
    toggle(1,"Ping","Server-Latenz",()->s.ping,()->s.ping=!s.ping,col,top,row);
    toggle(2,"CPS","Linke und rechte Klicks",()->s.cps,()->s.cps=!s.cps,col,top,row);
    toggle(3,"Keystrokes","Bewegungstasten anzeigen",()->s.keystrokes,()->s.keystrokes=!s.keystrokes,col,top,row);
    toggle(4,"Rüstungs-HUD","Items und Haltbarkeit",()->s.armorHud,()->s.armorHud=!s.armorHud,col,top,row);
    toggle(5,"Koordinaten","XYZ-Position",()->s.coordinates,()->s.coordinates=!s.coordinates,col,top,row);
   }else if(page==2){
    toggle(0,"Fadenkreuz","Eigene Form und Akzent",()->s.customCrosshair,()->s.customCrosshair=!s.customCrosshair,col,top,row);
    tile(1,()->"Form",()->new String[]{"Kreuz","Punkt","Quadrat"}[s.crosshairStyle],()->s.customCrosshair,()->{s.crosshairStyle=(s.crosshairStyle+1)%3;CrewClient.changed();},col,top,row);
    tile(2,()->"Größe",()->s.crosshairSize+" px",()->false,()->{s.crosshairSize=s.crosshairSize>=8?2:s.crosshairSize+1;CrewClient.changed();},col,top,row);
    tile(3,()->"Abstand",()->s.crosshairGap+" px",()->false,()->{s.crosshairGap=(s.crosshairGap+1)%6;CrewClient.changed();},col,top,row);
    toggle(4,"Uhrzeit","Lokale Systemzeit",()->s.clock,()->s.clock=!s.clock,col,top,row);
    toggle(5,"Kompass","Blickrichtung",()->s.compass,()->s.compass=!s.compass,col,top,row);
   }else{
    toggle(0,"Cosmetics","Capes und Wings anzeigen",()->s.showCosmetics,()->s.showCosmetics=!s.showCosmetics,col,top,row);
    toggle(1,"Emotes","Animationen anzeigen",()->s.showEmotes,()->s.showEmotes=!s.showEmotes,col,top,row);
    tile(2,()->"HUD-Position",()->new String[]{"Oben links","Oben rechts","Unten links","Unten rechts"}[s.hudCorner],()->false,()->{s.hudCorner=(s.hudCorner+1)%4;CrewClient.changed();},col,top,row);
    tile(3,()->"Zoom-Stärke",()->s.zoomFactor+"x",()->s.zoom,()->{s.zoomFactor=s.zoomFactor>=8?2:s.zoomFactor*2;CrewClient.changed();},col,top,row);
    option(4,"Portal-Effekte","Bildschirm-Verzerrung",()->minecraft.options.screenEffectScale().get()>0,()->minecraft.options.screenEffectScale().set(minecraft.options.screenEffectScale().get()>0?0d:1d),col,top,row);
    tile(5,()->"Perspektive",()->minecraft.options.getCameraType().name(),()->false,()->minecraft.options.setCameraType(minecraft.options.getCameraType().cycle()),col,top,row);
   }
  }else if(tab==1){
   tile(0,()->"Cape",()->CAPES[s.cape],()->s.cape>0,()->{s.cape=(s.cape+1)%6;CrewClient.changed();},col,top,row);
   tile(1,()->"Wings",()->WINGS[s.wings],()->s.wings>0,()->{s.wings=(s.wings+1)%3;CrewClient.changed();},col,top,row);
   tile(2,()->"Akzentfarbe",()->new String[]{"Mint","Violet","Ember","Ice","Rose"}[s.color],()->true,()->{s.color=(s.color+1)%5;CrewClient.changed();},col,top,row);
   tile(3,()->"PNG importieren",()->"Dein eigenes Cape",()->false,()->minecraft.gui.setScreen(new CapeImportScreen()),col,top,row);
   toggle(4,"Cape teilen","Mit CREW auf diesem Server",()->s.shareCape,()->s.shareCape=!s.shareCape,col,top,row);
   tile(5,()->"Dritte Person",()->"Deinen Look im Spiel ansehen",()->false,()->{minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);minecraft.gui.setScreen(null);},col,top,row);
  }else if(tab==2){
   String[] names={"Wave","Side Step","Robot","Victory"};String[] desc={"Sag deiner Crew Hallo","Der eigene CREW Dance","Eckig. Genau. Robot.","Zeit zum Feiern"};
   for(int i=0;i<4;i++){final int e=i+1;tile(i,()->names[e-1],()->desc[e-1],()->false,()->{CrewClient.play(e);minecraft.gui.setScreen(null);},col,top,row);}
   tile(4,()->"Stop",()->"Animation beenden",()->false,()->CrewClient.play(0),col,top,row);
   tile(5,()->"Dritte Person",()->"Perspektive wechseln",()->false,()->minecraft.options.setCameraType(minecraft.options.getCameraType().cycle()),col,top,row);
  }else{
   tile(0,()->"Sodium",()->installed("sodium"),()->loaded("sodium"),()->{},col,top,row);
   tile(1,()->"Lithium",()->installed("lithium"),()->loaded("lithium"),()->{},col,top,row);
   tile(2,()->"FPS-Profil",()->"8 Chunks / weniger Partikel",()->false,()->{minecraft.options.renderDistance().set(8);minecraft.options.simulationDistance().set(5);minecraft.options.cloudStatus().set(CloudStatus.OFF);minecraft.options.entityShadows().set(false);minecraft.options.particles().set(net.minecraft.server.level.ParticleStatus.DECREASED);minecraft.options.save();CrewClient.notice="FPS-Profil angewendet";},col,top,row);
   tile(3,()->"Ausgewogen",()->"12 Chunks / normale Effekte",()->false,()->{minecraft.options.renderDistance().set(12);minecraft.options.simulationDistance().set(8);minecraft.options.cloudStatus().set(CloudStatus.FAST);minecraft.options.entityShadows().set(true);minecraft.options.particles().set(net.minecraft.server.level.ParticleStatus.ALL);minecraft.options.save();CrewClient.notice="Ausgewogenes Profil angewendet";},col,top,row);
  }
 }
 private void option(int n,String title,String desc,BooleanSupplier val,Runnable action,int col,int top,int row){toggle(n,title,desc,val,()->{action.run();minecraft.options.save();},col,top,row);}
 private static boolean loaded(String id){return FabricLoader.getInstance().isModLoaded(id);}
 private static String installed(String id){return loaded(id)?"Installiert":"Nicht installiert";}
 private void toggle(int n,String title,String desc,BooleanSupplier val,Runnable action,int col,int top,int row){tile(n,()->title,()->(val.getAsBoolean()?"AN | ":"AUS | ")+desc,val,()->{action.run();CrewClient.changed();},col,top,row);}
 private void tile(int n,Supplier<String> title,Supplier<String> sub,BooleanSupplier active,Runnable action,int col,int top,int row){addRenderableWidget(new Tile(content+(n%2)*(col+10),top+(n/2)*(row+8),col,row,title,sub,active,action));}
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  g.fill(0,0,width,height,0x99060910);g.fill(x-2,y-2,x+w+2,y+h+2,0xff263041);g.fill(x,y,x+w,y+h,0xff10151e);g.fill(x,y,x+nav,y+h,0xff0b1018);
  int accent=Style.COLORS[CrewClient.settings.color];g.fill(x+14,y+19,x+17,y+43,accent);g.text(font,"CREW",x+24,y+22,0xfff2f5fb,false);g.text(font,"CLIENT",x+24,y+35,0xff6d7f96,false);
  g.text(font,TABS[tab].toUpperCase(),content,y+23,0xfff2f5fb,false);
  String sub=switch(tab){case 0->"Dein Spiel. Deine Einstellungen.";case 1->"Dein Look, direkt am Spieler.";case 2->"Ein Move fuer jeden Moment.";default->"Mehr Spiel. Weniger Wartezeit.";};
  g.text(font,sub,content,y+42,0xff92a0b4,false);g.fill(content,y+65,x+w-18,y+66,0xff283241);
  super.extractRenderState(g,mx,my,delta);
  String status=CrewClient.canSync()?"CREW Sync aktiv":"Lokal / Server ohne CREW";
  g.text(font,font.plainSubstrByWidth(status,w-nav-36),content,y+h-29,CrewClient.canSync()?accent:0xffb6a486,false);
  g.text(font,font.plainSubstrByWidth(CrewClient.notice.isEmpty()?"26.3  /  Rechts-Shift: Menue  /  G: Emotes":CrewClient.notice,w-nav-36),content,y+h-16,0xff75869c,false);
 }
 public boolean isPauseScreen(){return false;}
 public void onClose(){CrewClient.changed();minecraft.gui.setScreen(null);}
 private static class Tile extends AbstractWidget {
  private final Supplier<String> title,sub;private final BooleanSupplier on;private final Runnable action;
  Tile(int x,int y,int w,int h,Supplier<String> title,Supplier<String> sub,BooleanSupplier on,Runnable action){super(x,y,w,h,Component.literal(title.get()));this.title=title;this.sub=sub;this.on=on;this.action=action;}
  protected void extractWidgetRenderState(GuiGraphicsExtractor g,int mx,int my,float d){
   var f=Minecraft.getInstance().font;int x=getX(),y=getY();g.fill(x,y,x+width,y+height,isHoveredOrFocused()?0xff263448:0xff1a2330);
   if(on.getAsBoolean())g.fill(x,y,x+2,y+height,Style.COLORS[CrewClient.settings.color]);
   int ty=y+(sub.get().isEmpty()?(height-9)/2:9);g.text(f,f.plainSubstrByWidth(title.get(),width-16),x+9,ty,0xffedf3fb,false);
   if(!sub.get().isEmpty()&&height>=36)g.text(f,f.plainSubstrByWidth(sub.get(),width-16),x+9,y+height-15,0xff8fa1ba,false);
   if(isFocused())g.outline(x,y,width,height,0xffa7b6ca);
  }
  public void onClick(MouseButtonEvent event,boolean doubleClick){action.run();setMessage(Component.literal(title.get()));}
  public boolean keyPressed(KeyEvent event){if(isFocused()&&(event.key()==InputConstants.KEY_RETURN||event.key()==InputConstants.KEY_SPACE)){action.run();return true;}return false;}
  protected void updateWidgetNarration(NarrationElementOutput out){setMessage(Component.literal(title.get()+" "+sub.get()+(on.getAsBoolean()?" aktiv":"")));defaultButtonNarrationText(out);}
 }
}
