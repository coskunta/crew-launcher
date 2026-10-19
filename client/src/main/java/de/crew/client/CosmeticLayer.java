package de.crew.client;
import de.crew.Style;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
public class CosmeticLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
 private final ModelPart cape,wing;
 public CosmeticLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent){super(parent);
  var c=new MeshDefinition();c.getRoot().addOrReplaceChild("cape",CubeListBuilder.create().texOffs(0,0).addBox(-5,0,0,10,16,1),PartPose.ZERO);cape=LayerDefinition.create(c,64,32).bakeRoot().getChild("cape");
  var w=new MeshDefinition();var cubes=CubeListBuilder.create().texOffs(0,0).addBox(0,0,0,2,12,1);
  for(int i=0;i<5;i++)cubes.texOffs(4+i*4,0).addBox(2+i*2,1+i*0.6f,0,2,12-i*1.6f,1);
  w.getRoot().addOrReplaceChild("wing",cubes,PartPose.ZERO);wing=LayerDefinition.create(w,64,32).bakeRoot().getChild("wing");
 }
 public void submit(PoseStack pose,SubmitNodeCollector out,int light,AvatarRenderState state,float yaw,float pitch){
  if(!CrewClient.settings.showCosmetics||state.isInvisible||state.isSpectator)return;
  var style=((CrewState)state).crew$getStyle();
  if(style.cape()==0&&style.wings()==0)return;
  pose.pushPose();if(state.isCrouching){pose.translate(0,0.2,0);pose.rotate(Axis.XP.rotation(0.5f));}
  if(style.cape()>0&&!state.isFallFlying){
   pose.pushPose();pose.translate(0,0,0.16);pose.rotate(Axis.XP.rotationDegrees(7+Math.min(50,state.capeFlap)+3*(float)Math.sin(state.ageInTicks*0.08)));
   out.submitModelPart(cape,pose,RenderTypes.entityCutout((style.cape()==5&&((CrewState)state).crew$getCapeTexture()!=null?((CrewState)state).crew$getCapeTexture():Identifier.fromNamespaceAndPath("crew","textures/cosmetics/cape_"+Math.min(4,style.cape())+".png"))),light,OverlayTexture.NO_OVERLAY,null,0xffffffff,state.outlineColor);pose.popPose();
  }
  if(style.wings()>0){
   for(int side:new int[]{-1,1}){pose.pushPose();pose.translate(side*0.12,0.15,0.22);pose.rotate(Axis.YP.rotationDegrees(side*(28+18*(float)Math.sin(state.ageInTicks*0.12))));pose.rotate(Axis.ZP.rotationDegrees(side*-25));pose.scale(side,1,1);
    out.submitModelPart(wing,pose,RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("crew","textures/cosmetics/wing_"+style.wings()+".png")),light,OverlayTexture.NO_OVERLAY,null,Style.COLORS[style.color()],state.outlineColor);pose.popPose();}
  }
  pose.popPose();
 }
}
