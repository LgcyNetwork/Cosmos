package org.tron.core.db;

import static java.lang.Long.max;
import static org.tron.core.config.Parameter.ChainConstant.BLOCK_PRODUCED_INTERVAL;
import static org.tron.core.config.Parameter.ChainConstant.USDL_PRECISION;

import lombok.extern.slf4j.Slf4j;
import org.tron.common.parameter.CommonParameter;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.Parameter.AdaptiveResourceLimitConstants;
import org.tron.core.exception.AccountResourceInsufficientException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.store.AccountStore;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.protos.Protocol.Account.AccountResource;

@Slf4j(topic = "DB")
public class KandyProcessor extends ResourceProcessor {

  public KandyProcessor(DynamicPropertiesStore dynamicPropertiesStore, AccountStore accountStore) {
    super(dynamicPropertiesStore, accountStore);
  }

  public static long getHeadSlot(DynamicPropertiesStore dynamicPropertiesStore) {
    return (dynamicPropertiesStore.getLatestBlockHeaderTimestamp() -
        Long.parseLong(CommonParameter.getInstance()
            .getGenesisBlock().getTimestamp()))
        / BLOCK_PRODUCED_INTERVAL;
  }

  @Override
  public void updateUsage(AccountCapsule accountCapsule) {
    long now = getHeadSlot();
    updateUsage(accountCapsule, now);
  }

  private void updateUsage(AccountCapsule accountCapsule, long now) {
    AccountResource accountResource = accountCapsule.getAccountResource();

    long oldKandyUsage = accountResource.getKandyUsage();
    long latestConsumeTime = accountResource.getLatestConsumeTimeForPower();

    accountCapsule.setKandyUsage(increase(oldKandyUsage, 0, latestConsumeTime, now));
  }

  public void updateTotalKandyAverageUsage() {
    long now = getHeadSlot();
    long blockKandyUsage = dynamicPropertiesStore.getBlockKandyUsage();
    long totalKandyAverageUsage = dynamicPropertiesStore
        .getTotalKandyAverageUsage();
    long totalKandyAverageTime = dynamicPropertiesStore.getTotalKandyAverageTime();

    long newPublicKandyAverageUsage = increase(totalKandyAverageUsage, blockKandyUsage,
        totalKandyAverageTime, now, averageWindowSize);

    dynamicPropertiesStore.saveTotalKandyAverageUsage(newPublicKandyAverageUsage);
    dynamicPropertiesStore.saveTotalKandyAverageTime(now);
  }

  public void updateAdaptiveTotalKandyLimit() {
    long totalKandyAverageUsage = dynamicPropertiesStore
        .getTotalKandyAverageUsage();
    long targetTotalKandyLimit = dynamicPropertiesStore.getTotalKandyTargetLimit();
    long totalKandyCurrentLimit = dynamicPropertiesStore
        .getTotalKandyCurrentLimit();
    long totalKandyLimit = dynamicPropertiesStore.getTotalKandyLimit();

    long result;
    if (totalKandyAverageUsage > targetTotalKandyLimit) {
      result = totalKandyCurrentLimit * AdaptiveResourceLimitConstants.CONTRACT_RATE_NUMERATOR
          / AdaptiveResourceLimitConstants.CONTRACT_RATE_DENOMINATOR;
      // logger.info(totalKandyAverageUsage + ">" + targetTotalKandyLimit + "\n" + result);
    } else {
      result = totalKandyCurrentLimit * AdaptiveResourceLimitConstants.EXPAND_RATE_NUMERATOR
          / AdaptiveResourceLimitConstants.EXPAND_RATE_DENOMINATOR;
      // logger.info(totalKandyAverageUsage + "<" + targetTotalKandyLimit + "\n" + result);
    }

    result = Math.min(
        Math.max(result, totalKandyLimit),
        totalKandyLimit * dynamicPropertiesStore.getAdaptiveResourceLimitMultiplier()
    );

    dynamicPropertiesStore.saveTotalKandyCurrentLimit(result);
    logger.debug(
        "adjust totalKandyCurrentLimit, old[" + totalKandyCurrentLimit + "], new[" + result
            + "]");
  }

  @Override
  public void consume(TransactionCapsule usdl,
      TransactionTrace trace)
      throws ContractValidateException, AccountResourceInsufficientException {
    throw new RuntimeException("Not support");
  }

  public boolean useKandy(AccountCapsule accountCapsule, long kandy, long now) {

    long kandyUsage = accountCapsule.getKandyUsage();
    long latestConsumeTime = accountCapsule.getAccountResource().getLatestConsumeTimeForPower();
    long kandyLimit = calculateGlobalKandyLimit(accountCapsule);

    long newKandyUsage = increase(kandyUsage, 0, latestConsumeTime, now);

    if (kandy > (kandyLimit - newKandyUsage)) {
      return false;
    }

    latestConsumeTime = now;
    long latestOperationTime = dynamicPropertiesStore.getLatestBlockHeaderTimestamp();
    newKandyUsage = increase(newKandyUsage, kandy, latestConsumeTime, now);
    accountCapsule.setKandyUsage(newKandyUsage);
    accountCapsule.setLatestOperationTime(latestOperationTime);
    accountCapsule.setLatestConsumeTimeForPower(latestConsumeTime);

    accountStore.put(accountCapsule.createDbKey(), accountCapsule);

    if (dynamicPropertiesStore.getAllowAdaptiveKandy() == 1) {
      long blockKandyUsage = dynamicPropertiesStore.getBlockKandyUsage() + kandy;
      dynamicPropertiesStore.saveBlockKandyUsage(blockKandyUsage);
    }

    return true;
  }

  public long calculateGlobalKandyLimit(AccountCapsule accountCapsule) {
    long frozeBalance = accountCapsule.getAllFrozenBalanceForKandy();
    if (frozeBalance < USDL_PRECISION) {
      return 0;
    }

    long kandyWeight = frozeBalance / USDL_PRECISION;
    long totalKandyLimit = dynamicPropertiesStore.getTotalKandyCurrentLimit();
    long totalKandyWeight = dynamicPropertiesStore.getTotalKandyWeight();

    assert totalKandyWeight > 0;

    return (long) (kandyWeight * ((double) totalKandyLimit / totalKandyWeight));
  }

  public long getAccountLeftKandyFromFreeze(AccountCapsule accountCapsule) {
    long now = getHeadSlot();
    long kandyUsage = accountCapsule.getKandyUsage();
    long latestConsumeTime = accountCapsule.getAccountResource().getLatestConsumeTimeForPower();
    long kandyLimit = calculateGlobalKandyLimit(accountCapsule);

    long newKandyUsage = increase(kandyUsage, 0, latestConsumeTime, now);

    return max(kandyLimit - newKandyUsage, 0); // us
  }

  private long getHeadSlot() {
    return getHeadSlot(dynamicPropertiesStore);
  }


}


