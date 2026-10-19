package de.crew.client.mixin;
import de.crew.client.*;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerModel.class)
public class PlayerModelMixin {
 @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",at=@At("TAIL"))
 private void crew$dance(AvatarRenderState state,CallbackInfo ci){
  if(!CrewClient.settings.showEmotes||state.isSpectator||state.isInvisible||state.isFallFlying||state.isVisuallySwimming||state.isPassenger||state.isUsingItem||state.walkAnimationSpeed>0.2f)return;
  var data=(CrewState)state;int emote=data.crew$getStyle().emote();float t=data.crew$getTime();
  if(emote==0||t>=6)return;
  var m=(PlayerModel)(Object)this;float a=(float)Math.sin(t*Math.PI*3),b=(float)Math.cos(t*Math.PI*3);
  float blend=Math.min(1,Math.min(t*5,(6-t)*5));
  switch(emote){
   case 1->{m.rightArm.xRot=-2.5f*blend;m.rightArm.zRot=(-0.45f+a*0.35f)*blend;}
   case 2->{m.leftArm.zRot=(0.7f+a*0.65f)*blend;m.rightArm.zRot=(-0.7f+a*0.65f)*blend;m.leftArm.xRot=-0.7f*blend;m.rightArm.xRot=-0.7f*blend;m.body.zRot=a*0.12f*blend;m.leftLeg.xRot=a*0.4f*blend;m.rightLeg.xRot=-a*0.4f*blend;}
   case 3->{m.leftArm.xRot=(-1.5f+a*0.6f)*blend;m.rightArm.xRot=(-1.5f-a*0.6f)*blend;m.leftArm.zRot=0.55f*blend;m.rightArm.zRot=-0.55f*blend;m.head.yRot=(float)Math.floor(t*4)%2*0.6f*blend;}
   case 4->{m.leftArm.xRot=-2.7f*blend;m.rightArm.xRot=-2.7f*blend;m.leftArm.zRot=0.35f*blend;m.rightArm.zRot=-0.35f*blend;m.root().y-=Math.abs(a)*1.2f*blend;m.leftLeg.zRot=0.1f*blend;m.rightLeg.zRot=-0.1f*blend;}
  }
 }
}
