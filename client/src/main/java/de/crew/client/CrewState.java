package de.crew.client;
import de.crew.Style;
public interface CrewState {
 net.minecraft.resources.Identifier crew$getCapeTexture();
 void crew$setCapeTexture(net.minecraft.resources.Identifier texture);
 Style crew$getStyle();
 float crew$getTime();
 void crew$set(Style style,float time);
}
