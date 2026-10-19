package de.crew.client;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
/** Bounds checked before ImageIO decodes. Normalize to one small network format. */
public final class CapeImage {
 public static byte[] read(Path file)throws IOException {
  if(!Files.isRegularFile(file)||Files.size(file)>1_048_576)throw new IOException("Bitte eine PNG-Datei bis 1 MB auswählen.");
  byte[] bytes=Files.readAllBytes(file);if(bytes.length<33)throw new IOException("PNG ist unvollständig.");
  var b=ByteBuffer.wrap(bytes);if(b.getLong()!=0x89504e470d0a1a0aL||b.getInt()!=13||b.getInt()!=0x49484452)throw new IOException("Nur PNG-Dateien werden unterstützt.");
  int w=b.getInt(),h=b.getInt();if((w!=64&&w!=128&&w!=256&&w!=512)||h!=w/2)throw new IOException("Cape benötigt 64x32, 128x64, 256x128 oder 512x256 Pixel.");
  BufferedImage image=ImageIO.read(new ByteArrayInputStream(bytes));if(image==null||image.getWidth()!=w||image.getHeight()!=h)throw new IOException("PNG konnte nicht gelesen werden.");
  var out=ByteBuffer.allocate(8192);for(int y=0;y<32;y++)for(int x=0;x<64;x++)out.putInt(image.getRGB(x*w/64,y*h/32));return out.array();
 }
}
