package de.crew.client;
import java.util.ArrayDeque;
import java.util.function.LongSupplier;
/** Rolling one-second window; capped to bound memory even with synthetic input. */
public final class ClickCounter {
 private final ArrayDeque<Long> clicks=new ArrayDeque<>();
 private final LongSupplier clock;
 public ClickCounter(){this(System::nanoTime);}
 public ClickCounter(LongSupplier clock){this.clock=clock;}
 public void click(){long now=clock.getAsLong();prune(now);if(clicks.size()<500)clicks.addLast(now);}
 public int count(){prune(clock.getAsLong());return clicks.size();}
 private void prune(long now){while(!clicks.isEmpty()&&now-clicks.peekFirst()>=1_000_000_000L)clicks.removeFirst();}
 public void clear(){clicks.clear();}
}
