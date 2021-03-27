/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tron.core.vm.config;


import static org.tron.common.parameter.CommonParameter.KANDY_LIMIT_HARD_FORK;

import lombok.Setter;

/**
 * For developer only
 */
public class VMConfig {

  public static final int MAX_FEE_LIMIT = 1_000_000_000; //1000 USDL

  private static boolean vmTraceCompressed = false;

  @Setter
  private static boolean vmTrace = false;

  private static boolean ALLOW_LVM_TRANSFER_TRC10 = false;

  private static boolean ALLOW_LVM_CONSTANTINOPLE = false;

  private static boolean ALLOW_MULTI_SIGN = false;

  private static boolean ALLOW_LVM_SOLIDITY_059 = false;

  private static boolean ALLOW_SHIELDED_TRC20_TRANSACTION = false;

  private static boolean ALLOW_LVM_ISTANBUL = false;

  private static boolean ALLOW_LVM_STAKE = false;

  private static boolean ALLOW_LVM_ASSET_ISSUE = false;

  private VMConfig() {
  }

  public static VMConfig getInstance() {
    return SystemPropertiesInstance.INSTANCE;
  }

  public static boolean vmTrace() {
    return vmTrace;
  }

  public static boolean vmTraceCompressed() {
    return vmTraceCompressed;
  }

  public static void inilvmHardFork(boolean pass) {
    KANDY_LIMIT_HARD_FORK = pass;
  }

  public static void initAllowMultiSign(long allow) {
    ALLOW_MULTI_SIGN = allow == 1;
  }

  public static void initAllowLvmTransferTrc10(long allow) {
    ALLOW_LVM_TRANSFER_TRC10 = allow == 1;
  }

  public static void initAllowLvmConstantinople(long allow) {
    ALLOW_LVM_CONSTANTINOPLE = allow == 1;
  }

  public static void initAllowLvmSolidity059(long allow) {
    ALLOW_LVM_SOLIDITY_059 = allow == 1;
  }

  public static void initAllowShieldedTRC20Transaction(long allow) {
    ALLOW_SHIELDED_TRC20_TRANSACTION = allow == 1;
  }

  public static void initAllowLvmIstanbul(long allow) {
    ALLOW_LVM_ISTANBUL = allow == 1;
  }

  public static void initAllowLvmStake(long allow) {
    ALLOW_LVM_STAKE = allow == 1;
  }

  public static void initAllowLvmAssetIssue(long allow) {
    ALLOW_LVM_ASSET_ISSUE = allow == 1;
  }

  public static boolean getKandyLimitHardFork() {
    return KANDY_LIMIT_HARD_FORK;
  }

  public static boolean allowLvmTransferTrc10() {
    return ALLOW_LVM_TRANSFER_TRC10;
  }

  public static boolean allowLvmConstantinople() {
    return ALLOW_LVM_CONSTANTINOPLE;
  }

  public static boolean allowMultiSign() {
    return ALLOW_MULTI_SIGN;
  }

  public static boolean allowLvmSolidity059() {
    return ALLOW_LVM_SOLIDITY_059;
  }

  public static boolean allowShieldedTRC20Transaction() {
    return ALLOW_SHIELDED_TRC20_TRANSACTION;
  }

  public static boolean allowLvmIstanbul() {return ALLOW_LVM_ISTANBUL; }

  public static boolean allowLvmStake() {
    return ALLOW_LVM_STAKE;
  }

  public static boolean allowLvmAssetIssue() {
    return ALLOW_LVM_ASSET_ISSUE;
  }

  private static class SystemPropertiesInstance {

    private static final VMConfig INSTANCE = new VMConfig();
  }
}
