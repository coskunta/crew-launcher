package de.crew.client.mixin;
import de.crew.client.CrewClient;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(LightmapRenderStateExtractor.class)
public class FullbrightMixin {
 @Inject(method="extract",at=@At("TAIL"))
 private void crew$light(LightmapRenderState s,float delta,CallbackInfo ci){
  if(CrewClient.settings.fullbright){s.needsUpdate=true;s.ambientColor=new Vector3f(1,1,1);s.brightness=1;s.darknessEffectScale=0;s.bossOverlayWorldDarkening=0;s.nightVisionEffectIntensity=1;s.nightVisionColor=new Vector3f(1,1,1);}
 }
}
