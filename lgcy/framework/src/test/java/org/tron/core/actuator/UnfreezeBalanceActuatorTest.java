package org.tron.core.actuator;

import static junit.framework.TestCase.fail;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tron.common.application.TronApplicationContext;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.DelegatedResourceAccountIndexCapsule;
import org.tron.core.capsule.DelegatedResourceCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.capsule.VotesCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.Manager;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Protocol.AccountType;
import org.tron.protos.Protocol.Transaction.Result.code;
import org.tron.protos.Protocol.Vote;
import org.tron.protos.contract.AssetIssueContractOuterClass;
import org.tron.protos.contract.BalanceContract.UnfreezeBalanceContract;
import org.tron.protos.contract.Common.ResourceCode;

@Slf4j
public class UnfreezeBalanceActuatorTest {

  private static final String dbPath = "output_unfreeze_balance_test";
  private static final String OWNER_ADDRESS;
  private static final String RECEIVER_ADDRESS;
  private static final String OWNER_ADDRESS_INVALID = "aaaa";
  private static final String OWNER_ACCOUNT_INVALID;
  private static final long initBalance = 10_000_000_000L;
  private static final long frozenBalance = 1_000_000_000L;
  private static final long smallTatalResource = 100L;
  private static Manager dbManager;
  private static TronApplicationContext context;

  static {
    Args.setParam(new String[]{"--output-directory", dbPath}, Constant.TEST_CONF);
    context = new TronApplicationContext(DefaultConfig.class);
    OWNER_ADDRESS = Wallet.getAddressPreFixString() + "548794500882809695a8a687866e76d4271a1abc";
    RECEIVER_ADDRESS = Wallet.getAddressPreFixString() + "abd4b9367799eaa3197fecb144eb71de1e049150";
    OWNER_ACCOUNT_INVALID =
        Wallet.getAddressPreFixString() + "548794500882809695a8a687866e76d4271a3456";
  }

  /**
   * Init data.
   */
  @BeforeClass
  public static void init() {
    dbManager = context.getBean(Manager.class);
    //    Args.setParam(new String[]{"--output-directory", dbPath},
    //        "config-junit.conf");
    //    dbManager = new Manager();
    //    dbManager.init();
  }

  /**
   * Release resources.
   */
  @AfterClass
  public static void destroy() {
    Args.clearParam();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }

  /**
   * create temp Capsule test need.
   */
  @Before
  public void createAccountCapsule() {
    AccountCapsule ownerCapsule = new AccountCapsule(ByteString.copyFromUtf8("owner"),
        ByteString.copyFrom(ByteArray.fromHexString(OWNER_ADDRESS)), AccountType.Normal,
        initBalance);
    dbManager.getAccountStore().put(ownerCapsule.createDbKey(), ownerCapsule);

    AccountCapsule receiverCapsule = new AccountCapsule(ByteString.copyFromUtf8("receiver"),
        ByteString.copyFrom(ByteArray.fromHexString(RECEIVER_ADDRESS)), AccountType.Normal,
        initBalance);
    dbManager.getAccountStore().put(receiverCapsule.getAddress().toByteArray(), receiverCapsule);
  }

  private Any getContractForCpu(String ownerAddress) {
    return Any.pack(UnfreezeBalanceContract.newBuilder()
        .setOwnerAddress(ByteString.copyFrom(ByteArray.fromHexString(ownerAddress)))
        .setResource(ResourceCode.USDL_POWER).build());
  }

  private Any getDelegatedContractForCpu(String ownerAddress, String receiverAddress) {
    return Any.pack(UnfreezeBalanceContract.newBuilder()
        .setOwnerAddress(ByteString.copyFrom(ByteArray.fromHexString(ownerAddress)))
        .setReceiverAddress(ByteString.copyFrom(ByteArray.fromHexString(receiverAddress)))
        .setResource(ResourceCode.USDL_POWER).build());
  }

  private Any getContract(String ownerAddress, ResourceCode resourceCode) {
    return Any.pack(UnfreezeBalanceContract.newBuilder()
        .setOwnerAddress(ByteString.copyFrom(ByteArray.fromHexString(ownerAddress)))
        .setResource(resourceCode).build());
  }

  @Test
  public void testUnfreezeBalanceForKandy() {
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule accountCapsule = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(OWNER_ADDRESS));
    accountCapsule.setFrozenForKandy(frozenBalance, now);
    Assert.assertEquals(accountCapsule.getAllFrozenBalanceForKandy(), frozenBalance);
    Assert.assertEquals(accountCapsule.getTronPower(), frozenBalance);

    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    actuator.setChainBaseManager(dbManager.getChainBaseManager())
        .setAny(getContractForCpu(OWNER_ADDRESS));
    TransactionResultCapsule ret = new TransactionResultCapsule();

    long totalKandyWeightBefore = dbManager.getDynamicPropertiesStore().getTotalKandyWeight();
    try {
      actuator.validate();
      actuator.execute(ret);
      Assert.assertEquals(ret.getInstance().getRet(), code.SUCESS);
      AccountCapsule owner = dbManager.getAccountStore()
          .get(ByteArray.fromHexString(OWNER_ADDRESS));

      Assert.assertEquals(owner.getBalance(), initBalance + frozenBalance);
      Assert.assertEquals(owner.getKandyFrozenBalance(), 0);
      Assert.assertEquals(owner.getTronPower(), 0L);
      long totalKandyWeightAfter = dbManager.getDynamicPropertiesStore().getTotalKandyWeight();
      Assert.assertEquals(totalKandyWeightBefore,
          totalKandyWeightAfter + frozenBalance / 1000_000L);
    } catch (ContractValidateException e) {
      Assert.assertFalse(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }
  }

  @Test
  public void testUnfreezeDelegatedBalanceForCpu() {
    dbManager.getDynamicPropertiesStore().saveAllowDelegateResource(1);
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule owner = dbManager.getAccountStore().get(ByteArray.fromHexString(OWNER_ADDRESS));
    owner.addDelegatedFrozenBalanceForKandy(frozenBalance);
    Assert.assertEquals(frozenBalance, owner.getTronPower());

    AccountCapsule receiver = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(RECEIVER_ADDRESS));
    receiver.addAcquiredDelegatedFrozenBalanceForKandy(frozenBalance);
    Assert.assertEquals(0L, receiver.getTronPower());

    dbManager.getAccountStore().put(owner.createDbKey(), owner);
    dbManager.getAccountStore().put(receiver.createDbKey(), receiver);

    DelegatedResourceCapsule delegatedResourceCapsule = new DelegatedResourceCapsule(
        owner.getAddress(), receiver.getAddress());
    delegatedResourceCapsule.setFrozenBalanceForPower(frozenBalance, now - 100L);
    dbManager.getDelegatedResourceStore().put(DelegatedResourceCapsule
        .createDbKey(ByteArray.fromHexString(OWNER_ADDRESS),
            ByteArray.fromHexString(RECEIVER_ADDRESS)), delegatedResourceCapsule);

    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    actuator.setChainBaseManager(dbManager.getChainBaseManager())
        .setAny(getDelegatedContractForCpu(OWNER_ADDRESS, RECEIVER_ADDRESS));
    TransactionResultCapsule ret = new TransactionResultCapsule();

    try {
      actuator.validate();
      actuator.execute(ret);
      Assert.assertEquals(ret.getInstance().getRet(), code.SUCESS);
      AccountCapsule ownerResult = dbManager.getAccountStore()
          .get(ByteArray.fromHexString(OWNER_ADDRESS));

      AccountCapsule receiverResult = dbManager.getAccountStore()
          .get(ByteArray.fromHexString(RECEIVER_ADDRESS));

      Assert.assertEquals(initBalance + frozenBalance, ownerResult.getBalance());
      Assert.assertEquals(0L, ownerResult.getTronPower());
      Assert.assertEquals(0L, ownerResult.getDelegatedFrozenBalanceForKandy());
      Assert.assertEquals(0L, receiverResult.getAllFrozenBalanceForKandy());
    } catch (ContractValidateException e) {
      logger.error("", e);
      Assert.assertFalse(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }
  }

  @Test
  public void testUnfreezeDelegatedBalanceForCpuWithDeletedReceiver() {
    dbManager.getDynamicPropertiesStore().saveAllowDelegateResource(1);
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule owner = dbManager.getAccountStore().get(ByteArray.fromHexString(OWNER_ADDRESS));
    owner.addDelegatedFrozenBalanceForKandy(frozenBalance);
    Assert.assertEquals(frozenBalance, owner.getTronPower());

    AccountCapsule receiver = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(RECEIVER_ADDRESS));
    receiver.addAcquiredDelegatedFrozenBalanceForKandy(frozenBalance);
    Assert.assertEquals(0L, receiver.getTronPower());

    dbManager.getAccountStore().put(owner.createDbKey(), owner);

    DelegatedResourceCapsule delegatedResourceCapsule = new DelegatedResourceCapsule(
        owner.getAddress(), receiver.getAddress());
    delegatedResourceCapsule.setFrozenBalanceForPower(frozenBalance, now - 100L);
    dbManager.getDelegatedResourceStore().put(DelegatedResourceCapsule
        .createDbKey(ByteArray.fromHexString(OWNER_ADDRESS),
            ByteArray.fromHexString(RECEIVER_ADDRESS)), delegatedResourceCapsule);

    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    actuator.setChainBaseManager(dbManager.getChainBaseManager())
        .setAny(getDelegatedContractForCpu(OWNER_ADDRESS, RECEIVER_ADDRESS));
    TransactionResultCapsule ret = new TransactionResultCapsule();

    dbManager.getDynamicPropertiesStore().saveAllowLvmConstantinople(0);
    dbManager.getAccountStore().delete(receiver.createDbKey());

    try {
      actuator.validate();
      actuator.execute(ret);
      Assert.fail();
    } catch (ContractValidateException e) {
      Assert.assertEquals(e.getMessage(),
          "Receiver Account[a0abd4b9367799eaa3197fecb144eb71de1e049150] does not exist");
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }

    dbManager.getDynamicPropertiesStore().saveAllowLvmConstantinople(1);

    try {
      actuator.validate();
      actuator.execute(ret);
      Assert.assertEquals(ret.getInstance().getRet(), code.SUCESS);
      AccountCapsule ownerResult = dbManager.getAccountStore()
          .get(ByteArray.fromHexString(OWNER_ADDRESS));

      Assert.assertEquals(initBalance + frozenBalance, ownerResult.getBalance());
      Assert.assertEquals(0L, ownerResult.getTronPower());
      Assert.assertEquals(0L, ownerResult.getDelegatedFrozenBalanceForKandy());
    } catch (ContractValidateException e) {
      Assert.assertFalse(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }
  }

  @Test
  public void testUnfreezeDelegatedBalanceForCpuWithRecreatedReceiver() {
    dbManager.getDynamicPropertiesStore().saveAllowDelegateResource(1);
    dbManager.getDynamicPropertiesStore().saveAllowLvmConstantinople(1);

    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule owner = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(OWNER_ADDRESS));
    owner.addDelegatedFrozenBalanceForKandy(frozenBalance);
    Assert.assertEquals(frozenBalance, owner.getTronPower());

    AccountCapsule receiver = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(RECEIVER_ADDRESS));
    receiver.addAcquiredDelegatedFrozenBalanceForKandy(frozenBalance);
    Assert.assertEquals(0L, receiver.getTronPower());

    dbManager.getAccountStore().put(owner.createDbKey(), owner);

    DelegatedResourceCapsule delegatedResourceCapsule = new DelegatedResourceCapsule(
        owner.getAddress(),
        receiver.getAddress()
    );
    delegatedResourceCapsule.setFrozenBalanceForPower(
        frozenBalance,
        now - 100L);
    dbManager.getDelegatedResourceStore().put(DelegatedResourceCapsule
        .createDbKey(ByteArray.fromHexString(OWNER_ADDRESS),
            ByteArray.fromHexString(RECEIVER_ADDRESS)), delegatedResourceCapsule);

    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    actuator.setChainBaseManager(dbManager.getChainBaseManager())
        .setAny(getDelegatedContractForCpu(OWNER_ADDRESS, RECEIVER_ADDRESS));
    TransactionResultCapsule ret = new TransactionResultCapsule();

    dbManager.getDynamicPropertiesStore().saveAllowShieldedTransaction(0);
    dbManager.getDynamicPropertiesStore().saveAllowLvmSolidity059(0);
    dbManager.getAccountStore().delete(receiver.createDbKey());
    receiver = new AccountCapsule(receiver.getAddress(), ByteString.EMPTY, AccountType.Normal);
    receiver.setAcquiredDelegatedFrozenBalanceForKandy(10L);
    dbManager.getAccountStore().put(receiver.createDbKey(), receiver);
    receiver = dbManager.getAccountStore().get(receiver.createDbKey());
    Assert.assertEquals(10, receiver.getAcquiredDelegatedFrozenBalanceForKandy());

    try {
      actuator.validate();
      actuator.execute(ret);
      Assert.fail();
    } catch (ContractValidateException e) {
      Assert.assertEquals(e.getMessage(),
          "AcquiredDelegatedFrozenBalanceForKandy[10] < delegatedKandy[1000000000]");
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }

    dbManager.getDynamicPropertiesStore().saveAllowShieldedTransaction(1);
    dbManager.getDynamicPropertiesStore().saveAllowLvmSolidity059(1);

    try {
      actuator.validate();
      actuator.execute(ret);
      Assert.assertEquals(ret.getInstance().getRet(), code.SUCESS);
      AccountCapsule ownerResult =
          dbManager.getAccountStore().get(ByteArray.fromHexString(OWNER_ADDRESS));

      Assert.assertEquals(initBalance + frozenBalance, ownerResult.getBalance());
      Assert.assertEquals(0L, ownerResult.getTronPower());
      Assert.assertEquals(0L, ownerResult.getDelegatedFrozenBalanceForKandy());
      receiver = dbManager.getAccountStore().get(receiver.createDbKey());
      Assert.assertEquals(0, receiver.getAcquiredDelegatedFrozenBalanceForKandy());
    } catch (ContractValidateException e) {
      Assert.fail();
    } catch (ContractExeException e) {
      Assert.fail();
    }
  }

  @Test
  public void invalidOwnerAddress() {
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule accountCapsule = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(OWNER_ADDRESS));
    accountCapsule.setFrozen(1_000_000_000L, now);
    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    TransactionResultCapsule ret = new TransactionResultCapsule();
    try {
      actuator.validate();
      actuator.execute(ret);
      fail("cannot run here.");

    } catch (ContractValidateException e) {
      Assert.assertTrue(e instanceof ContractValidateException);

      Assert.assertEquals("Invalid address", e.getMessage());

    } catch (ContractExeException e) {
      Assert.assertTrue(e instanceof ContractExeException);
    }

  }

  @Test
  public void invalidOwnerAccount() {
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule accountCapsule = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(OWNER_ADDRESS));
    accountCapsule.setFrozen(1_000_000_000L, now);
    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    TransactionResultCapsule ret = new TransactionResultCapsule();
    try {
      actuator.validate();
      actuator.execute(ret);
      fail("cannot run here.");
    } catch (ContractValidateException e) {
      Assert.assertTrue(e instanceof ContractValidateException);
      Assert.assertEquals("Account[" + OWNER_ACCOUNT_INVALID + "] does not exist",
          e.getMessage());
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }
  }

  @Test
  public void noFrozenBalance() {
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    TransactionResultCapsule ret = new TransactionResultCapsule();
    try {
      actuator.validate();
      actuator.execute(ret);
      fail("cannot run here.");

    } catch (ContractValidateException e) {
      Assert.assertTrue(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }
  }

  @Test
  public void notTimeToUnfreeze() {
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule accountCapsule = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(OWNER_ADDRESS));
    accountCapsule.setFrozen(1_000_000_000L, now + 60000);
    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    TransactionResultCapsule ret = new TransactionResultCapsule();
    try {
      actuator.validate();
      actuator.execute(ret);
      fail("cannot run here.");

    } catch (ContractValidateException e) {
      Assert.assertTrue(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }
  }

  @Test
  public void testClearVotes() {
    byte[] ownerAddressBytes = ByteArray.fromHexString(OWNER_ADDRESS);
    ByteString ownerAddress = ByteString.copyFrom(ownerAddressBytes);
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule accountCapsule = dbManager.getAccountStore().get(ownerAddressBytes);
    accountCapsule.setFrozen(1_000_000_000L, now);
    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    TransactionResultCapsule ret = new TransactionResultCapsule();

    dbManager.getVotesStore().reset();
    Assert.assertNull(dbManager.getVotesStore().get(ownerAddressBytes));
    try {
      actuator.validate();
      actuator.execute(ret);
      VotesCapsule votesCapsule = dbManager.getVotesStore().get(ownerAddressBytes);
      Assert.assertNotNull(votesCapsule);
      Assert.assertEquals(0, votesCapsule.getNewVotes().size());
    } catch (ContractValidateException e) {
      Assert.assertFalse(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }

    // if had votes
    List<Vote> oldVotes = new ArrayList<Vote>();
    VotesCapsule votesCapsule = new VotesCapsule(
        ByteString.copyFrom(ByteArray.fromHexString(OWNER_ADDRESS)), oldVotes);
    votesCapsule.addNewVotes(ByteString.copyFrom(ByteArray.fromHexString(OWNER_ADDRESS)),
        100);
    dbManager.getVotesStore().put(ByteArray.fromHexString(OWNER_ADDRESS), votesCapsule);
    accountCapsule.setFrozen(1_000_000_000L, now);
    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
    try {
      actuator.validate();
      actuator.execute(ret);
      votesCapsule = dbManager.getVotesStore().get(ownerAddressBytes);
      Assert.assertNotNull(votesCapsule);
      Assert.assertEquals(0, votesCapsule.getNewVotes().size());
    } catch (ContractValidateException e) {
      Assert.assertFalse(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertFalse(e instanceof ContractExeException);
    }

  }

  /*@Test
  public void InvalidTotalNetWeight(){
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);
    dbManager.getDynamicPropertiesStore().saveTotalNetWeight(smallTatalResource);

    AccountCapsule accountCapsule = dbManager.getAccountStore()
            .get(ByteArray.fromHexString(OWNER_ADDRESS));
    accountCapsule.setFrozen(frozenBalance, now);
    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);

    Assert.assertTrue(frozenBalance/1000_000L > smallTatalResource );
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator(
            getContract(OWNER_ADDRESS), dbManager);
    TransactionResultCapsule ret = new TransactionResultCapsule();
    try {
      actuator.validate();
      actuator.execute(ret);

      Assert.assertTrue(dbManager.getDynamicPropertiesStore().getTotalNetWeight() >= 0);
    } catch (ContractValidateException e) {
      Assert.assertTrue(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertTrue(e instanceof ContractExeException);
    }
  }

  @Test
  public void InvalidTotalKandyWeight(){
    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);
    dbManager.getDynamicPropertiesStore().saveTotalKandyWeight(smallTatalResource);

    AccountCapsule accountCapsule = dbManager.getAccountStore()
            .get(ByteArray.fromHexString(OWNER_ADDRESS));
    accountCapsule.setFrozenForKandy(frozenBalance, now);
    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);

    Assert.assertTrue(frozenBalance/1000_000L > smallTatalResource );
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator(
            getContract(OWNER_ADDRESS, Contract.ResourceCode.USDL_POWER), dbManager);
    TransactionResultCapsule ret = new TransactionResultCapsule();
    try {
      actuator.validate();
      actuator.execute(ret);

      Assert.assertTrue(dbManager.getDynamicPropertiesStore().getTotalKandyWeight() >= 0);
    } catch (ContractValidateException e) {
      Assert.assertTrue(e instanceof ContractValidateException);
    } catch (ContractExeException e) {
      Assert.assertTrue(e instanceof ContractExeException);
    }
  }*/


  @Test
  public void commonErrorCheck() {
    UnfreezeBalanceActuator actuator = new UnfreezeBalanceActuator();
    ActuatorTest actuatorTest = new ActuatorTest(actuator, dbManager);
    actuatorTest.noContract();

    Any invalidContractTypes = Any.pack(AssetIssueContractOuterClass.AssetIssueContract.newBuilder()
        .build());
    actuatorTest.setInvalidContract(invalidContractTypes);
    actuatorTest.setInvalidContractTypeMsg("contract type error",
        "contract type error, expected type [UnfreezeBalanceContract], real type[");
    actuatorTest.invalidContractType();

    long now = System.currentTimeMillis();
    dbManager.getDynamicPropertiesStore().saveLatestBlockHeaderTimestamp(now);

    AccountCapsule accountCapsule = dbManager.getAccountStore()
        .get(ByteArray.fromHexString(OWNER_ADDRESS));
    accountCapsule.setFrozen(frozenBalance, now);
    Assert.assertEquals(accountCapsule.getFrozenBalance(), frozenBalance);
    Assert.assertEquals(accountCapsule.getTronPower(), frozenBalance);

    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);

    actuatorTest.nullTransationResult();

    actuatorTest.setNullDBManagerMsg("No account store or dynamic store!");
    actuatorTest.nullDBManger();
  }

}

