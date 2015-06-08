package com.stuffaboutcode.canaryraspberryjuicetest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FooTest {
  @Test
  public void testPass() {
    assertEquals(1, 1);
  }

  @Test
  public void testFail() {
    assertEquals(1, 2);
  }
}
