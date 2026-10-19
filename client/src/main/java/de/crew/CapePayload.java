package de.crew;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
/** Fixed 64x32 ARGB pixels; no image parser on the server. Empty means remove. */
public record CapePayload(UUID player,byte[] pixels) implements CustomPacketPayload {
 public static final int BYTES=64*32*4;
 public static final Type<CapePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("crew","cape_v1"));
 public static final StreamCodec<RegistryFriendlyByteBuf,CapePayload> CODEC=StreamCodec.of((b,p)->{b.writeUUID(p.player);b.writeByteArray(p.pixels);},b->new CapePayload(b.readUUID(),b.readByteArray(BYTES)));
 public boolean valid(){return pixels.length==0||pixels.length==BYTES;}
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
