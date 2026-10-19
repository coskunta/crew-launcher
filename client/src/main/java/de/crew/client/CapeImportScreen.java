package de.crew.client;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryUtil;
public final class CapeImportScreen extends Screen {
 private EditBox path;private static volatile boolean picking=false;
 // One callback for the application lifetime; SDL can invoke it asynchronously.
 private static final SDL_DialogFileCallback CALLBACK=SDL_DialogFileCallback.create((userdata,list,filter)->{
  String selected=list==0?null:(MemoryUtil.memGetAddress(list)==0?null:MemoryUtil.memUTF8(MemoryUtil.memGetAddress(list)));
  Minecraft.getInstance().execute(()->{picking=false;if(selected!=null)CapeTextures.importFile(Path.of(selected));});
 });
 public CapeImportScreen(){super(Component.literal("Eigenes Cape"));}
 protected void init(){
  int w=Math.min(430,width-30),x=(width-w)/2,y=Math.max(20,height/2-85);
  addRenderableWidget(Button.builder(Component.literal("PNG auswählen"),b->{if(!picking){picking=true;try{SDLDialog.SDL_ShowOpenFileDialog(CALLBACK,0,minecraft.getWindow().handle(),null,(String)null,false);}catch(Throwable e){picking=false;CapeTextures.message="Dateiauswahl nicht verfügbar. PNG hierher ziehen.";}}}).bounds(x,y+43,w,24).build());
  path=addRenderableWidget(new EditBox(font,x,y+77,w-84,22,Component.literal("Dateipfad")));path.setMaxLength(2048);path.setHint(Component.literal("Alternativ: Dateipfad einfügen"));
  addRenderableWidget(Button.builder(Component.literal("Import"),b->{try{CapeTextures.importFile(Path.of(path.getValue().strip().replaceAll("^\"|\"$","")));}catch(Exception e){CapeTextures.message="Ungültiger Dateipfad.";}}).bounds(x+w-78,y+77,78,22).build());
  addRenderableWidget(Button.builder(Component.literal("Zurück"),b->onClose()).bounds(x,y+138,100,22).build());
 }
 public void onFilesDrop(List<Path> files){if(!files.isEmpty())CapeTextures.importFile(files.getFirst());}
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  g.fill(0,0,width,height,0xef0c131e);int w=Math.min(430,width-30),x=(width-w)/2,y=Math.max(20,height/2-85);
  g.text(font,"DEIN EIGENES CAPE",x,y,0xff83f4cf,false);g.text(font,"PNG auswählen oder ins Fenster ziehen. Max. 1 MB.",x,y+18,0xffa8bbd2,false);
  super.extractRenderState(g,mx,my,delta);g.text(font,font.plainSubstrByWidth(CapeTextures.message,w),x,y+112,0xffd9e5f2,false);
  g.text(font,"64x32 bis 512x256 / wird auf 64x32 normalisiert",x,y+171,0xff8193ab,false);
  g.text(font,"Teilen lässt sich im Cosmetics-Menü ausschalten.",x,y+185,0xff8193ab,false);
 }
 public boolean isPauseScreen(){return false;}
 public void onClose(){minecraft.gui.setScreen(new CrewScreen(1));}
}
