package de.crew.client.mixin;
import de.crew.client.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Hud.class)
public class CrosshairMixin {
 @Inject(method="extractCrosshair",at=@At("HEAD"),cancellable=true)
 private void crew$crosshair(GuiGraphicsExtractor g,DeltaTracker delta,CallbackInfo ci){
  var c=Minecraft.getInstance();if(CrewClient.settings.customCrosshair&&c.player!=null&&!c.player.isSpectator()&&c.options.getCameraType().isFirstPerson()&&!c.gui.hud.isHidden()){
   ModuleHud.crosshair(g);ci.cancel();
  }
 }
}
