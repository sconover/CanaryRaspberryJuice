package com.stuffaboutcode.canaryraspberryjuice;

/**
 * A point in 3d space relative to "object".
 */
public class Relative<T> {
  public final T object;
  public final int x;
  public final int y;
  public final int z;

  public Relative(T object, int x, int y, int z) {
    this.object = object;
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
