package de.crew.client.mixin;
import de.crew.client.*;
import net.minecraft.world.entity.Avatar;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"))
 private void crew$extract(Avatar entity,AvatarRenderState state,float delta,CallbackInfo ci){
  var look=CrewClient.look(entity.getUUID());
  ((CrewState)state).crew$setCapeTexture(CapeTextures.texture(entity.getUUID()));
  ((CrewState)state).crew$set(look.style(),look.seconds());
  if(CrewClient.settings.showCosmetics&&look.style().cape()>0)state.showCape=false;
 }
}
