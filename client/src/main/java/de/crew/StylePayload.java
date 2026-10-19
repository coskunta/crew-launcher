package de.crew;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
public record StylePayload(UUID player, Style style, int elapsed, boolean remove) implements CustomPacketPayload {
 public static final Type<StylePayload> TYPE=new Type<>(Identifier.fromNamespaceAndPath("crew","style_v2"));
 public static final StreamCodec<RegistryFriendlyByteBuf,StylePayload> CODEC=StreamCodec.of((b,p)->{
  b.writeUUID(p.player);b.writeVarInt(p.style.cape());b.writeVarInt(p.style.wings());b.writeVarInt(p.style.color());b.writeVarInt(p.style.emote());b.writeVarInt(p.elapsed);b.writeBoolean(p.remove);
 },b->new StylePayload(b.readUUID(),new Style(b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readVarInt()),b.readVarInt(),b.readBoolean()));
 public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
