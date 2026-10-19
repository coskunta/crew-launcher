package de.crew;
public record Style(int cape, int wings, int color, int emote) {
 public static final Style NONE = new Style(0,0,0,0);
 public static final int[] COLORS={0xff83f4cf,0xffa294ff,0xffffae72,0xff7dbeff,0xffff85ae};
 public boolean valid(){return cape>=0&&cape<=5&&wings>=0&&wings<=2&&color>=0&&color<COLORS.length&&emote>=0&&emote<=4;}
 public Style stop(){return new Style(cape,wings,color,0);}
}
