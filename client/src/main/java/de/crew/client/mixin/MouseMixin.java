package de.crew.client.mixin;
import de.crew.client.ModuleHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MouseHandler.class)
public class MouseMixin {
 @Inject(method="onButton",at=@At("HEAD"))
 private void crew$click(long window,MouseButtonInfo button,int action,CallbackInfo ci){
  var c=Minecraft.getInstance();if(c.player==null||c.gui.screen()!=null||action!=InputConstants.PRESS)return;
  if(button.button()==InputConstants.MOUSE_BUTTON_LEFT)ModuleHud.LEFT.click();if(button.button()==InputConstants.MOUSE_BUTTON_RIGHT)ModuleHud.RIGHT.click();
 }
}
