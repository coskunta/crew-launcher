package de.crew.client.mixin;
import de.crew.client.CrewClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Camera.class)
public class ZoomMixin {
 @Inject(method="calculateFov",at=@At("RETURN"),cancellable=true)
 private void crew$zoom(float delta,CallbackInfoReturnable<Float> cir){if(CrewClient.zooming())cir.setReturnValue(cir.getReturnValue()/CrewClient.settings.zoomFactor);}
}
