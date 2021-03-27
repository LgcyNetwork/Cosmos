package org.tron.core.actuator;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.tron.core.vm.utils.MUtil.transfer;
import static org.tron.core.vm.utils.MUtil.transferToken;

import com.google.protobuf.ByteString;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.logsfilter.trigger.ContractTrigger;
import org.tron.common.parameter.CommonParameter;
import org.tron.common.runtime.InternalTransaction;
import org.tron.common.runtime.InternalTransaction.ExecutorType;
import org.tron.common.runtime.InternalTransaction.USDLType;
import org.tron.common.runtime.ProgramResult;
import org.tron.common.utils.StorageUtils;
import org.tron.common.utils.StringUtil;
import org.tron.common.utils.WalletUtil;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.ContractCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.db.TransactionContext;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.utils.TransactionUtil;
import org.tron.core.vm.KandyCost;
import org.tron.core.vm.LogInfoTriggerParser;
import org.tron.core.vm.VM;
import org.tron.core.vm.VMConstant;
import org.tron.core.vm.VMUtils;
import org.tron.core.vm.config.ConfigLoader;
import org.tron.core.vm.config.VMConfig;
import org.tron.core.vm.program.Program;
import org.tron.core.vm.program.Program.JVMStackOverFlowException;
import org.tron.core.vm.program.Program.OutOfTimeException;
import org.tron.core.vm.program.Program.TransferException;
import org.tron.core.vm.program.ProgramPrecompile;
import org.tron.core.vm.program.invoke.ProgramInvoke;
import org.tron.core.vm.program.invoke.ProgramInvokeFactory;
import org.tron.core.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.tron.core.vm.repository.Repository;
import org.tron.core.vm.repository.RepositoryImpl;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.Protocol.Transaction.Result.contractResult;
import org.tron.protos.contract.SmartContractOuterClass.CreateSmartContract;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;
import org.tron.protos.contract.SmartContractOuterClass.TriggerSmartContract;

@Slf4j(topic = "VM")
public class VMActuator implements Actuator2 {

  private Transaction usdl;
  private BlockCapsule blockCap;
  private Repository repository;
  private InternalTransaction rootInternalTransaction;
  private ProgramInvokeFactory programInvokeFactory;


  private VM vm;
  private Program program;
  private VMConfig vmConfig = VMConfig.getInstance();

  @Getter
  @Setter
  private InternalTransaction.USDLType usdlType;
  private ExecutorType executorType;

  @Getter
  @Setter
  private boolean isConstantCall = false;

  @Setter
  private boolean enableEventListener;

  private LogInfoTriggerParser logInfoTriggerParser;


  public VMActuator(boolean isConstantCall) {
    this.isConstantCall = isConstantCall;
    programInvokeFactory = new ProgramInvokeFactoryImpl();
  }

  private static long getKandyFee(long callerKandyUsage, long callerKandyFrozen,
      long callerKandyTotal) {
    if (callerKandyTotal <= 0) {
      return 0;
    }
    return BigInteger.valueOf(callerKandyFrozen).multiply(BigInteger.valueOf(callerKandyUsage))
        .divide(BigInteger.valueOf(callerKandyTotal)).longValueExact();
  }

  @Override
  public void validate(Object object) throws ContractValidateException {

    TransactionContext context = (TransactionContext) object;
    if (Objects.isNull(context)) {
      throw new RuntimeException("TransactionContext is null");
    }

    //Load Config
    ConfigLoader.load(context.getStoreFactory());
    usdl = context.getUsdlCap().getInstance();
    blockCap = context.getBlockCap();
    //Route Type
    ContractType contractType = this.usdl.getRawData().getContract(0).getType();
    //Prepare Repository
    repository = RepositoryImpl.createRoot(context.getStoreFactory());

    enableEventListener = context.isEventPluginLoaded();

    //set executorType type
    if (Objects.nonNull(blockCap)) {
      this.executorType = ExecutorType.ET_NORMAL_TYPE;
    } else {
      this.blockCap = new BlockCapsule(Block.newBuilder().build());
      this.executorType = ExecutorType.ET_PRE_TYPE;
    }
    if (isConstantCall) {
      this.executorType = ExecutorType.ET_PRE_TYPE;
    }

    switch (contractType.getNumber()) {
      case ContractType.TriggerSmartContract_VALUE:
        usdlType = USDLType.USDL_CONTRACT_CALL_TYPE;
        call();
        break;
      case ContractType.CreateSmartContract_VALUE:
        usdlType = USDLType.USDL_CONTRACT_CREATION_TYPE;
        create();
        break;
      default:
        throw new ContractValidateException("Unknown contract type");
    }
  }

  @Override
  public void execute(Object object) throws ContractExeException {
    TransactionContext context = (TransactionContext) object;
    if (Objects.isNull(context)) {
      throw new RuntimeException("TransactionContext is null");
    }

    ProgramResult result = context.getProgramResult();
    try {
      if (vm != null) {
        if (null != blockCap && blockCap.generatedByMyself && blockCap.hasWitnessSignature()
            && null != TransactionUtil.getContractRet(usdl)
            && contractResult.OUT_OF_TIME == TransactionUtil.getContractRet(usdl)) {
          result = program.getResult();
          program.spendAllKandy();

          OutOfTimeException e = Program.Exception.alreadyTimeOut();
          result.setRuntimeError(e.getMessage());
          result.setException(e);
          throw e;
        }

        vm.play(program);
        result = program.getResult();

        if (isConstantCall) {
          long callValue = TransactionCapsule.getCallValue(usdl.getRawData().getContract(0));
          long callTokenValue = TransactionUtil
              .getCallTokenValue(usdl.getRawData().getContract(0));
          if (callValue > 0 || callTokenValue > 0) {
            result.setRuntimeError("constant cannot set call value or call token value.");
            result.rejectInternalTransactions();
          }
          if (result.getException() != null) {
            result.setRuntimeError(result.getException().getMessage());
            result.rejectInternalTransactions();
          }
          context.setProgramResult(result);
          return;
        }

        if (USDLType.USDL_CONTRACT_CREATION_TYPE == usdlType && !result.isRevert()) {
          byte[] code = program.getResult().getHReturn();
          long saveCodeKandy = (long) getLength(code) * KandyCost.getInstance().getCREATE_DATA();
          long afterSpend = program.getKandyLimitLeft().longValue() - saveCodeKandy;
          if (afterSpend < 0) {
            if (null == result.getException()) {
              result.setException(Program.Exception
                  .notEnoughSpendKandy("save just created contract code",
                      saveCodeKandy, program.getKandyLimitLeft().longValue()));
            }
          } else {
            result.spendKandy(saveCodeKandy);
            if (VMConfig.allowLvmConstantinople()) {
              repository.saveCode(program.getContractAddress().getNoLeadZeroesData(), code);
            }
          }
        }

        if (result.getException() != null || result.isRevert()) {
          result.getDeleteAccounts().clear();
          result.getLogInfoList().clear();
          result.resetFutureRefund();
          result.rejectInternalTransactions();
          result.getDeleteVotes().clear();
          result.getDeleteDelegation().clear();

          if (result.getException() != null) {
            if (!(result.getException() instanceof TransferException)) {
              program.spendAllKandy();
            }
            result.setRuntimeError(result.getException().getMessage());
            throw result.getException();
          } else {
            result.setRuntimeError("REVERT opcode executed");
          }
        } else {
          repository.commit();

          if (logInfoTriggerParser != null) {
            List<ContractTrigger> triggers = logInfoTriggerParser
                .parseLogInfos(program.getResult().getLogInfoList(), repository);
            program.getResult().setTriggerList(triggers);
          }

        }
      } else {
        repository.commit();
      }
    } catch (JVMStackOverFlowException e) {
      program.spendAllKandy();
      result = program.getResult();
      result.setException(e);
      result.rejectInternalTransactions();
      result.setRuntimeError(result.getException().getMessage());
      logger.info("JVMStackOverFlowException: {}", result.getException().getMessage());
    } catch (OutOfTimeException e) {
      program.spendAllKandy();
      result = program.getResult();
      result.setException(e);
      result.rejectInternalTransactions();
      result.setRuntimeError(result.getException().getMessage());
      logger.info("timeout: {}", result.getException().getMessage());
    } catch (Throwable e) {
      if (!(e instanceof TransferException)) {
        program.spendAllKandy();
      }
      result = program.getResult();
      result.rejectInternalTransactions();
      if (Objects.isNull(result.getException())) {
        logger.error(e.getMessage(), e);
        result.setException(new RuntimeException("Unknown Throwable"));
      }
      if (StringUtils.isEmpty(result.getRuntimeError())) {
        result.setRuntimeError(result.getException().getMessage());
      }
      logger.info("runtime result is :{}", result.getException().getMessage());
    }
    //use program returned fill context
    context.setProgramResult(result);

    if (VMConfig.vmTrace() && program != null) {
      String traceContent = program.getTrace()
          .result(result.getHReturn())
          .error(result.getException())
          .toString();

      if (VMConfig.vmTraceCompressed()) {
        traceContent = VMUtils.zipAndEncode(traceContent);
      }

      String txHash = Hex.toHexString(rootInternalTransaction.getHash());
      VMUtils.saveProgramTraceFile(txHash, traceContent);
    }

  }

  private void create()
      throws ContractValidateException {
    if (!repository.getDynamicPropertiesStore().supporlvm()) {
      throw new ContractValidateException("vm work is off, need to be opened by the committee");
    }

    CreateSmartContract contract = ContractCapsule.getSmartContractFromTransaction(usdl);
    if (contract == null) {
      throw new ContractValidateException("Cannot get CreateSmartContract from transaction");
    }
    SmartContract newSmartContract = contract.getNewContract();
    if (!contract.getOwnerAddress().equals(newSmartContract.getOriginAddress())) {
      logger.info("OwnerAddress not equals OriginAddress");
      throw new ContractValidateException("OwnerAddress is not equals OriginAddress");
    }

    byte[] contractName = newSmartContract.getName().getBytes();

    if (contractName.length > VMConstant.CONTRACT_NAME_LENGTH) {
      throw new ContractValidateException("contractName's length cannot be greater than 32");
    }

    long percent = contract.getNewContract().getConsumeUserResourcePercent();
    if (percent < 0 || percent > VMConstant.ONE_HUNDRED) {
      throw new ContractValidateException("percent must be >= 0 and <= 100");
    }

    byte[] contractAddress = WalletUtil.generateContractAddress(usdl);
    // insure the new contract address haven't exist
    if (repository.getAccount(contractAddress) != null) {
      throw new ContractValidateException(
          "Trying to create a contract with existing contract address: " + StringUtil
              .encode58Check(contractAddress));
    }

    newSmartContract = newSmartContract.toBuilder()
        .setContractAddress(ByteString.copyFrom(contractAddress)).build();
    long callValue = newSmartContract.getCallValue();
    long tokenValue = 0;
    long tokenId = 0;
    if (VMConfig.allowLvmTransferTrc10()) {
      tokenValue = contract.getCallTokenValue();
      tokenId = contract.getTokenId();
    }
    byte[] callerAddress = contract.getOwnerAddress().toByteArray();
    // create vm to constructor smart contract
    try {
      long feeLimit = usdl.getRawData().getFeeLimit();
      if (feeLimit < 0 || feeLimit > VMConfig.MAX_FEE_LIMIT) {
        logger.info("invalid feeLimit {}", feeLimit);
        throw new ContractValidateException(
            "feeLimit must be >= 0 and <= " + VMConfig.MAX_FEE_LIMIT);
      }
      AccountCapsule creator = this.repository
          .getAccount(newSmartContract.getOriginAddress().toByteArray());

      long kandyLimit;
      // according to version

      if (StorageUtils.getKandyLimitHardFork()) {
        if (callValue < 0) {
          throw new ContractValidateException("callValue must be >= 0");
        }
        if (tokenValue < 0) {
          throw new ContractValidateException("tokenValue must be >= 0");
        }
        if (newSmartContract.getOriginKandyLimit() <= 0) {
          throw new ContractValidateException("The originKandyLimit must be > 0");
        }
        kandyLimit = getAccountKandyLimitWithFixRatio(creator, feeLimit, callValue);
      } else {
        kandyLimit = getAccountKandyLimitWithFloatRatio(creator, feeLimit, callValue);
      }

      checkTokenValueAndId(tokenValue, tokenId);

      byte[] ops = newSmartContract.getBytecode().toByteArray();
      rootInternalTransaction = new InternalTransaction(usdl, usdlType);

      long maxCpuTimeOfOneTx = repository.getDynamicPropertiesStore()
          .getMaxCpuTimeOfOneTx() * VMConstant.ONE_THOUSAND;
      long thisTxCPULimitInUs = (long) (maxCpuTimeOfOneTx * getCpuLimitInUsRatio());
      long vmStartInUs = System.nanoTime() / VMConstant.ONE_THOUSAND;
      long vmShouldEndInUs = vmStartInUs + thisTxCPULimitInUs;
      ProgramInvoke programInvoke = programInvokeFactory
          .createProgramInvoke(USDLType.USDL_CONTRACT_CREATION_TYPE, executorType, usdl,
              tokenValue, tokenId, blockCap.getInstance(), repository, vmStartInUs,
              vmShouldEndInUs, kandyLimit);
      this.vm = new VM();
      this.program = new Program(ops, programInvoke, rootInternalTransaction, vmConfig
      );
      byte[] txId = TransactionUtil.getTransactionId(usdl).getBytes();
      this.program.setRootTransactionId(txId);
      if (enableEventListener && isCheckTransaction()) {
        logInfoTriggerParser = new LogInfoTriggerParser(blockCap.getNum(), blockCap.getTimeStamp(),
            txId, callerAddress);
      }
    } catch (Exception e) {
      logger.info(e.getMessage());
      throw new ContractValidateException(e.getMessage());
    }
    program.getResult().setContractAddress(contractAddress);

    repository.createAccount(contractAddress, newSmartContract.getName(),
        Protocol.AccountType.Contract);

    repository.createContract(contractAddress, new ContractCapsule(newSmartContract));
    byte[] code = newSmartContract.getBytecode().toByteArray();
    if (!VMConfig.allowLvmConstantinople()) {
      repository.saveCode(contractAddress, ProgramPrecompile.getCode(code));
    }
    // transfer from callerAddress to contractAddress according to callValue
    if (callValue > 0) {
      transfer(this.repository, callerAddress, contractAddress, callValue);
    }
    if (VMConfig.allowLvmTransferTrc10() && tokenValue > 0) {
      transferToken(this.repository, callerAddress, contractAddress, String.valueOf(tokenId),
          tokenValue);
    }

  }

  /**
   * **
   */

  private void call()
      throws ContractValidateException {

    if (!repository.getDynamicPropertiesStore().supporlvm()) {
      logger.info("vm work is off, need to be opened by the committee");
      throw new ContractValidateException("VM work is off, need to be opened by the committee");
    }

    TriggerSmartContract contract = ContractCapsule.getTriggerContractFromTransaction(usdl);
    if (contract == null) {
      return;
    }

    if (contract.getContractAddress() == null) {
      throw new ContractValidateException("Cannot get contract address from TriggerContract");
    }

    byte[] contractAddress = contract.getContractAddress().toByteArray();

    ContractCapsule deployedContract = repository.getContract(contractAddress);
    if (null == deployedContract) {
      logger.info("No contract or not a smart contract");
      throw new ContractValidateException("No contract or not a smart contract");
    }

    long callValue = contract.getCallValue();
    long tokenValue = 0;
    long tokenId = 0;
    if (VMConfig.allowLvmTransferTrc10()) {
      tokenValue = contract.getCallTokenValue();
      tokenId = contract.getTokenId();
    }

    if (StorageUtils.getKandyLimitHardFork()) {
      if (callValue < 0) {
        throw new ContractValidateException("callValue must be >= 0");
      }
      if (tokenValue < 0) {
        throw new ContractValidateException("tokenValue must be >= 0");
      }
    }

    byte[] callerAddress = contract.getOwnerAddress().toByteArray();
    checkTokenValueAndId(tokenValue, tokenId);

    byte[] code = repository.getCode(contractAddress);
    if (isNotEmpty(code)) {

      long feeLimit = usdl.getRawData().getFeeLimit();
      if (feeLimit < 0 || feeLimit > VMConfig.MAX_FEE_LIMIT) {
        logger.info("invalid feeLimit {}", feeLimit);
        throw new ContractValidateException(
            "feeLimit must be >= 0 and <= " + VMConfig.MAX_FEE_LIMIT);
      }
      AccountCapsule caller = repository.getAccount(callerAddress);
      long kandyLimit;
      if (isConstantCall) {
        kandyLimit = VMConstant.KANDY_LIMIT_IN_CONSTANT_TX;
      } else {
        AccountCapsule creator = repository
            .getAccount(deployedContract.getInstance().getOriginAddress().toByteArray());
        kandyLimit = getTotalKandyLimit(creator, caller, contract, feeLimit, callValue);
      }

      long maxCpuTimeOfOneTx = repository.getDynamicPropertiesStore()
          .getMaxCpuTimeOfOneTx() * VMConstant.ONE_THOUSAND;
      long thisTxCPULimitInUs =
          (long) (maxCpuTimeOfOneTx * getCpuLimitInUsRatio());
      long vmStartInUs = System.nanoTime() / VMConstant.ONE_THOUSAND;
      long vmShouldEndInUs = vmStartInUs + thisTxCPULimitInUs;
      ProgramInvoke programInvoke = programInvokeFactory
          .createProgramInvoke(USDLType.USDL_CONTRACT_CALL_TYPE, executorType, usdl,
              tokenValue, tokenId, blockCap.getInstance(), repository, vmStartInUs,
              vmShouldEndInUs, kandyLimit);
      if (isConstantCall) {
        programInvoke.setConstantCall();
      }
      this.vm = new VM();
      rootInternalTransaction = new InternalTransaction(usdl, usdlType);
      this.program = new Program(code, programInvoke, rootInternalTransaction, vmConfig);
      byte[] txId = TransactionUtil.getTransactionId(usdl).getBytes();
      this.program.setRootTransactionId(txId);

      if (enableEventListener && isCheckTransaction()) {
        logInfoTriggerParser = new LogInfoTriggerParser(blockCap.getNum(), blockCap.getTimeStamp(),
            txId, callerAddress);
      }
    }

    program.getResult().setContractAddress(contractAddress);
    //transfer from callerAddress to targetAddress according to callValue

    if (callValue > 0) {
      transfer(this.repository, callerAddress, contractAddress, callValue);
    }
    if (VMConfig.allowLvmTransferTrc10() && tokenValue > 0) {
      transferToken(this.repository, callerAddress, contractAddress, String.valueOf(tokenId),
          tokenValue);
    }

  }

  public long getAccountKandyLimitWithFixRatio(AccountCapsule account, long feeLimit,
      long callValue) {

    long sunPerKandy = VMConstant.SUN_PER_KANDY;
    if (repository.getDynamicPropertiesStore().getKandyFee() > 0) {
      sunPerKandy = repository.getDynamicPropertiesStore().getKandyFee();
    }

    long leftFrozenKandy = repository.getAccountLeftKandyFromFreeze(account);

    long kandyFromBalance = max(account.getBalance() - callValue, 0) / sunPerKandy;
    long availableKandy = Math.addExact(leftFrozenKandy, kandyFromBalance);

    long kandyFromFeeLimit = feeLimit / sunPerKandy;
    return min(availableKandy, kandyFromFeeLimit);

  }

  private long getAccountKandyLimitWithFloatRatio(AccountCapsule account, long feeLimit,
      long callValue) {

    long sunPerKandy = VMConstant.SUN_PER_KANDY;
    if (repository.getDynamicPropertiesStore().getKandyFee() > 0) {
      sunPerKandy = repository.getDynamicPropertiesStore().getKandyFee();
    }
    // can change the calc way
    long leftKandyFromFreeze = repository.getAccountLeftKandyFromFreeze(account);
    callValue = max(callValue, 0);
    long kandyFromBalance = Math
        .floorDiv(max(account.getBalance() - callValue, 0), sunPerKandy);

    long kandyFromFeeLimit;
    long totalBalanceForKandyFreeze = account.getAllFrozenBalanceForKandy();
    if (0 == totalBalanceForKandyFreeze) {
      kandyFromFeeLimit =
          feeLimit / sunPerKandy;
    } else {
      long totalKandyFromFreeze = repository
          .calculateGlobalKandyLimit(account);
      long leftBalanceForKandyFreeze = getKandyFee(totalBalanceForKandyFreeze,
          leftKandyFromFreeze,
          totalKandyFromFreeze);

      if (leftBalanceForKandyFreeze >= feeLimit) {
        kandyFromFeeLimit = BigInteger.valueOf(totalKandyFromFreeze)
            .multiply(BigInteger.valueOf(feeLimit))
            .divide(BigInteger.valueOf(totalBalanceForKandyFreeze)).longValueExact();
      } else {
        kandyFromFeeLimit = Math
            .addExact(leftKandyFromFreeze,
                (feeLimit - leftBalanceForKandyFreeze) / sunPerKandy);
      }
    }

    return min(Math.addExact(leftKandyFromFreeze, kandyFromBalance), kandyFromFeeLimit);
  }

  public long getTotalKandyLimit(AccountCapsule creator, AccountCapsule caller,
      TriggerSmartContract contract, long feeLimit, long callValue)
      throws ContractValidateException {
    if (Objects.isNull(creator) && VMConfig.allowLvmConstantinople()) {
      return getAccountKandyLimitWithFixRatio(caller, feeLimit, callValue);
    }
    //  according to version
    if (StorageUtils.getKandyLimitHardFork()) {
      return getTotalKandyLimitWithFixRatio(creator, caller, contract, feeLimit, callValue);
    } else {
      return getTotalKandyLimitWithFloatRatio(creator, caller, contract, feeLimit, callValue);
    }
  }


  public void checkTokenValueAndId(long tokenValue, long tokenId) throws ContractValidateException {
    if (VMConfig.allowLvmTransferTrc10() && VMConfig.allowMultiSign()) {
      // tokenid can only be 0
      // or (MIN_TOKEN_ID, Long.Max]
      if (tokenId <= VMConstant.MIN_TOKEN_ID && tokenId != 0) {
        throw new ContractValidateException("tokenId must be > " + VMConstant.MIN_TOKEN_ID);
      }
      // tokenid can only be 0 when tokenvalue = 0,
      // or (MIN_TOKEN_ID, Long.Max]
      if (tokenValue > 0 && tokenId == 0) {
        throw new ContractValidateException("invalid arguments with tokenValue = " + tokenValue +
            ", tokenId = " + tokenId);
      }
    }
  }


  private double getCpuLimitInUsRatio() {

    double cpuLimitRatio;

    if (ExecutorType.ET_NORMAL_TYPE == executorType) {
      // self witness generates block
      if (this.blockCap != null && blockCap.generatedByMyself &&
          !this.blockCap.hasWitnessSignature()) {
        cpuLimitRatio = 1.0;
      } else {
        // self witness or other witness or fullnode verifies block
        if (usdl.getRet(0).getContractRet() == contractResult.OUT_OF_TIME) {
          cpuLimitRatio = CommonParameter.getInstance().getMinTimeRatio();
        } else {
          cpuLimitRatio = CommonParameter.getInstance().getMaxTimeRatio();
        }
      }
    } else {
      // self witness or other witness or fullnode receives tx
      cpuLimitRatio = 1.0;
    }

    return cpuLimitRatio;
  }

  public long getTotalKandyLimitWithFixRatio(AccountCapsule creator, AccountCapsule caller,
      TriggerSmartContract contract, long feeLimit, long callValue)
      throws ContractValidateException {

    long callerKandyLimit = getAccountKandyLimitWithFixRatio(caller, feeLimit, callValue);
    if (Arrays.equals(creator.getAddress().toByteArray(), caller.getAddress().toByteArray())) {
      // when the creator calls his own contract, this logic will be used.
      // so, the creator must use a BIG feeLimit to call his own contract,
      // which will cost the feeLimit USDL when the creator's frozen kandy is 0.
      return callerKandyLimit;
    }

    long creatorKandyLimit = 0;
    ContractCapsule contractCapsule = repository
        .getContract(contract.getContractAddress().toByteArray());
    long consumeUserResourcePercent = contractCapsule.getConsumeUserResourcePercent();

    long originKandyLimit = contractCapsule.getOriginKandyLimit();
    if (originKandyLimit < 0) {
      throw new ContractValidateException("originKandyLimit can't be < 0");
    }

    if (consumeUserResourcePercent <= 0) {
      creatorKandyLimit = min(repository.getAccountLeftKandyFromFreeze(creator),
          originKandyLimit);
    } else {
      if (consumeUserResourcePercent < VMConstant.ONE_HUNDRED) {
        // creatorKandyLimit =
        // min(callerKandyLimit * (100 - percent) / percent, creatorLeftFrozenKandy, originKandyLimit)

        creatorKandyLimit = min(
            BigInteger.valueOf(callerKandyLimit)
                .multiply(BigInteger.valueOf(VMConstant.ONE_HUNDRED - consumeUserResourcePercent))
                .divide(BigInteger.valueOf(consumeUserResourcePercent)).longValueExact(),
            min(repository.getAccountLeftKandyFromFreeze(creator), originKandyLimit)
        );
      }
    }
    return Math.addExact(callerKandyLimit, creatorKandyLimit);
  }

  private long getTotalKandyLimitWithFloatRatio(AccountCapsule creator, AccountCapsule caller,
      TriggerSmartContract contract, long feeLimit, long callValue) {

    long callerKandyLimit = getAccountKandyLimitWithFloatRatio(caller, feeLimit, callValue);
    if (Arrays.equals(creator.getAddress().toByteArray(), caller.getAddress().toByteArray())) {
      return callerKandyLimit;
    }

    // creatorKandyFromFreeze
    long creatorKandyLimit = repository.getAccountLeftKandyFromFreeze(creator);

    ContractCapsule contractCapsule = repository
        .getContract(contract.getContractAddress().toByteArray());
    long consumeUserResourcePercent = contractCapsule.getConsumeUserResourcePercent();

    if (creatorKandyLimit * consumeUserResourcePercent
        > (VMConstant.ONE_HUNDRED - consumeUserResourcePercent) * callerKandyLimit) {
      return Math.floorDiv(callerKandyLimit * VMConstant.ONE_HUNDRED, consumeUserResourcePercent);
    } else {
      return Math.addExact(callerKandyLimit, creatorKandyLimit);
    }
  }

  private boolean isCheckTransaction() {
    return this.blockCap != null && !this.blockCap.getInstance().getBlockHeader()
        .getWitnessSignature().isEmpty();
  }


}


