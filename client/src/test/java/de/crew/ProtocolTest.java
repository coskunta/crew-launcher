package de.crew;
import java.util.UUID;
import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
public class ProtocolTest {
 public static void main(String[] args){
  int checked=0;
  for(int cape=0;cape<=5;cape++)for(int wings=0;wings<=2;wings++)for(int color=0;color<5;color++)for(int emote=0;emote<=4;emote++){
   var p=new StylePayload(UUID.randomUUID(),new Style(cape,wings,color,emote),5999,false);
   var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),null);
   try{StylePayload.CODEC.encode(b,p);if(!p.equals(StylePayload.CODEC.decode(b))||b.readableBytes()!=0)throw new AssertionError("Wire format mismatch");checked++;}finally{b.release();}
  }
  if(new Style(-1,0,0,0).valid()||new Style(6,0,0,0).valid()||new Style(0,3,0,0).valid()||new Style(0,0,5,0).valid()||new Style(0,0,0,5).valid())throw new AssertionError("Invalid network style accepted");
  var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),null);
  try{b.writeByte(0);try{StylePayload.CODEC.decode(b);throw new AssertionError("Truncated packet accepted");}catch(IndexOutOfBoundsException expected){}}finally{b.release();}
  System.out.println("PASS: "+checked+" packet round trips, invalid styles, truncated packet.");
 }
}
