package de.crew.client.mixin;
import de.crew.Style;
import de.crew.client.CrewState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.*;
@Mixin(AvatarRenderState.class)
public class AvatarStateMixin implements CrewState {
 @Unique private net.minecraft.resources.Identifier crew$cape;
 public net.minecraft.resources.Identifier crew$getCapeTexture(){return crew$cape;}
 public void crew$setCapeTexture(net.minecraft.resources.Identifier texture){crew$cape=texture;}
 @Unique private Style crew$style=Style.NONE;
 @Unique private float crew$time;
 public Style crew$getStyle(){return crew$style;}
 public float crew$getTime(){return crew$time;}
 public void crew$set(Style s,float t){crew$style=s;crew$time=t;}
}
