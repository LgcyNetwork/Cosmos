package org.tron.core.vm;

public class VMConstant {

  public static final int CONTRACT_NAME_LENGTH = 32;
  public static final int MIN_TOKEN_ID = 1_000_000;
  // Numbers
  public static final int ONE_HUNDRED = 100;
  public static final int ONE_THOUSAND = 1000;
  public static final long SUN_PER_KANDY = 100; // 1 us = 100 SUN = 100 * 10^-6 USDL
  public static final long KANDY_LIMIT_IN_CONSTANT_TX = 3_000_000L; // ref: 1 us = 1 kandy


  private VMConstant() {
  }
}
