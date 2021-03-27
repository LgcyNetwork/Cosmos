package org.tron.core.vm.config;


import static org.tron.core.capsule.ReceiptCapsule.checkForKandyLimit;

import lombok.extern.slf4j.Slf4j;
import org.tron.common.parameter.CommonParameter;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.core.store.StoreFactory;

@Slf4j(topic = "VMConfigLoader")
public class ConfigLoader {

  //only for unit test
  public static boolean disable = false;

  public static void load(StoreFactory storeFactory) {
    if (!disable) {
      DynamicPropertiesStore ds = storeFactory.getChainBaseManager().getDynamicPropertiesStore();
      VMConfig.setVmTrace(CommonParameter.getInstance().isVmTrace());
      if (ds != null) {
        VMConfig.inilvmHardFork(checkForKandyLimit(ds));
        VMConfig.initAllowMultiSign(ds.getAllowMultiSign());
        VMConfig.initAllowLvmTransferTrc10(ds.getAllowLvmTransferTrc10());
        VMConfig.initAllowLvmConstantinople(ds.getAllowLvmConstantinople());
        VMConfig.initAllowLvmSolidity059(ds.getAllowLvmSolidity059());
        VMConfig.initAllowShieldedTRC20Transaction(ds.getAllowShieldedTRC20Transaction());
        VMConfig.initAllowLvmIstanbul(ds.getAllowLvmIstanbul());
        VMConfig.initAllowLvmStake(ds.getAllowLvmStake());
        VMConfig.initAllowLvmAssetIssue(ds.getAllowLvmAssetIssue());
      }
    }
  }
}
