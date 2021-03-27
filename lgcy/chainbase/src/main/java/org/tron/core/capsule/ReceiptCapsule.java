package org.tron.core.capsule;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.utils.Commons;
import org.tron.common.utils.ForkController;
import org.tron.common.utils.Sha256Hash;
import org.tron.common.utils.StringUtil;
import org.tron.core.Constant;
import org.tron.core.config.Parameter.ForkBlockVersionEnum;
import org.tron.core.db.KandyProcessor;
import org.tron.core.exception.BalanceInsufficientException;
import org.tron.core.store.AccountStore;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.protos.Protocol.ResourceReceipt;
import org.tron.protos.Protocol.Transaction.Result.contractResult;

public class ReceiptCapsule {

  private ResourceReceipt receipt;
  @Getter
  @Setter
  private long multiSignFee;

  private Sha256Hash receiptAddress;

  public ReceiptCapsule(ResourceReceipt data, Sha256Hash receiptAddress) {
    this.receipt = data;
    this.receiptAddress = receiptAddress;
  }

  public ReceiptCapsule(Sha256Hash receiptAddress) {
    this.receipt = ResourceReceipt.newBuilder().build();
    this.receiptAddress = receiptAddress;
  }

  public static ResourceReceipt copyReceipt(ReceiptCapsule origin) {
    return origin.getReceipt().toBuilder().build();
  }

  public static boolean checkForKandyLimit(DynamicPropertiesStore ds) {
    long blockNum = ds.getLatestBlockHeaderNumber();
    return blockNum >= CommonParameter.getInstance()
        .getBlockNumForKandyLimit();
  }

  public ResourceReceipt getReceipt() {
    return this.receipt;
  }

  public void setReceipt(ResourceReceipt receipt) {
    this.receipt = receipt;
  }

  public Sha256Hash getReceiptAddress() {
    return this.receiptAddress;
  }

  public void addNetFee(long netFee) {
    this.receipt = this.receipt.toBuilder().setNetFee(getNetFee() + netFee).build();
  }

  public long getKandyUsage() {
    return this.receipt.getKandyUsage();
  }

  public void setKandyUsage(long kandyUsage) {
    this.receipt = this.receipt.toBuilder().setKandyUsage(kandyUsage).build();
  }

  public long getKandyFee() {
    return this.receipt.getKandyFee();
  }

  public void setKandyFee(long kandyFee) {
    this.receipt = this.receipt.toBuilder().setKandyFee(kandyFee).build();
  }

  public long getOriginKandyUsage() {
    return this.receipt.getOriginKandyUsage();
  }

  public void setOriginKandyUsage(long kandyUsage) {
    this.receipt = this.receipt.toBuilder().setOriginKandyUsage(kandyUsage).build();
  }

  public long getKandyUsageTotal() {
    return this.receipt.getKandyUsageTotal();
  }

  public void setKandyUsageTotal(long kandyUsage) {
    this.receipt = this.receipt.toBuilder().setKandyUsageTotal(kandyUsage).build();
  }

  public long getNetUsage() {
    return this.receipt.getNetUsage();
  }

  public void setNetUsage(long netUsage) {
    this.receipt = this.receipt.toBuilder().setNetUsage(netUsage).build();
  }

  public long getNetFee() {
    return this.receipt.getNetFee();
  }

  public void setNetFee(long netFee) {
    this.receipt = this.receipt.toBuilder().setNetFee(netFee).build();
  }

  /**
   * payKandyBill pay receipt kandy bill by kandy processor.
   */
  public void payKandyBill(DynamicPropertiesStore dynamicPropertiesStore,
      AccountStore accountStore, ForkController forkController, AccountCapsule origin,
      AccountCapsule caller,
      long percent, long originKandyLimit, KandyProcessor kandyProcessor, long now)
      throws BalanceInsufficientException {
    if (receipt.getKandyUsageTotal() <= 0) {
      return;
    }

    if (Objects.isNull(origin) && dynamicPropertiesStore.getAllowLvmConstantinople() == 1) {
      payKandyBill(dynamicPropertiesStore, accountStore, forkController, caller,
          receipt.getKandyUsageTotal(), kandyProcessor, now);
      return;
    }

    if (caller.getAddress().equals(origin.getAddress())) {
      payKandyBill(dynamicPropertiesStore, accountStore, forkController, caller,
          receipt.getKandyUsageTotal(), kandyProcessor, now);
    } else {
      long originUsage = Math.multiplyExact(receipt.getKandyUsageTotal(), percent) / 100;
      originUsage = getOriginUsage(dynamicPropertiesStore, origin, originKandyLimit,
          kandyProcessor,
          originUsage);

      long callerUsage = receipt.getKandyUsageTotal() - originUsage;
      kandyProcessor.useKandy(origin, originUsage, now);
      this.setOriginKandyUsage(originUsage);
      payKandyBill(dynamicPropertiesStore, accountStore, forkController,
          caller, callerUsage, kandyProcessor, now);
    }
  }

  private long getOriginUsage(DynamicPropertiesStore dynamicPropertiesStore, AccountCapsule origin,
      long originKandyLimit,
      KandyProcessor kandyProcessor, long originUsage) {

    if (checkForKandyLimit(dynamicPropertiesStore)) {
      return Math.min(originUsage,
          Math.min(kandyProcessor.getAccountLeftKandyFromFreeze(origin), originKandyLimit));
    }
    return Math.min(originUsage, kandyProcessor.getAccountLeftKandyFromFreeze(origin));
  }

  private void payKandyBill(
      DynamicPropertiesStore dynamicPropertiesStore, AccountStore accountStore,
      ForkController forkController,
      AccountCapsule account,
      long usage,
      KandyProcessor kandyProcessor,
      long now) throws BalanceInsufficientException {
    long accountKandyLeft = kandyProcessor.getAccountLeftKandyFromFreeze(account);
    if (accountKandyLeft >= usage) {
      kandyProcessor.useKandy(account, usage, now);
      this.setKandyUsage(usage);
    } else {
      kandyProcessor.useKandy(account, accountKandyLeft, now);

      if (forkController.pass(ForkBlockVersionEnum.VERSION_3_6_5) &&
          dynamicPropertiesStore.getAllowAdaptiveKandy() == 1) {
        long blockKandyUsage =
            dynamicPropertiesStore.getBlockKandyUsage() + (usage - accountKandyLeft);
        dynamicPropertiesStore.saveBlockKandyUsage(blockKandyUsage);
      }

      long sunPerKandy = Constant.SUN_PER_KANDY;
      long dynamicKandyFee = dynamicPropertiesStore.getKandyFee();
      if (dynamicKandyFee > 0) {
        sunPerKandy = dynamicKandyFee;
      }
      long kandyFee =
          (usage - accountKandyLeft) * sunPerKandy;
      this.setKandyUsage(accountKandyLeft);
      this.setKandyFee(kandyFee);
      long balance = account.getBalance();
      if (balance < kandyFee) {
        throw new BalanceInsufficientException(
            StringUtil.createReadableString(account.createDbKey()) + " insufficient balance");
      }
      account.setBalance(balance - kandyFee);

      //send to blackHole
      Commons.adjustBalance(accountStore, accountStore.getBlackhole().getAddress().toByteArray(),
          kandyFee);
    }

    accountStore.put(account.getAddress().toByteArray(), account);
  }

  public contractResult getResult() {
    return this.receipt.getResult();
  }

  public void setResult(contractResult success) {
    this.receipt = receipt.toBuilder().setResult(success).build();
  }
}
