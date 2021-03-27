package org.tron.core.net.message;

import org.tron.common.overlay.message.Message;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.protos.Protocol.Transaction;

public class TransactionMessage extends TronMessage {

  private TransactionCapsule transactionCapsule;

  public TransactionMessage(byte[] data) throws Exception {
    super(data);
    this.transactionCapsule = new TransactionCapsule(getCodedInputStream(data));
    this.type = MessageTypes.USDL.asByte();
    if (Message.isFilter()) {
      compareBytes(data, transactionCapsule.getInstance().toByteArray());
      transactionCapsule
          .validContractProto(transactionCapsule.getInstance().getRawData().getContract(0));
    }
  }

  public TransactionMessage(Transaction usdl) {
    this.transactionCapsule = new TransactionCapsule(usdl);
    this.type = MessageTypes.USDL.asByte();
    this.data = usdl.toByteArray();
  }

  @Override
  public String toString() {
    return new StringBuilder().append(super.toString())
        .append("messageId: ").append(super.getMessageId()).toString();
  }

  @Override
  public Sha256Hash getMessageId() {
    return this.transactionCapsule.getTransactionId();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

  public TransactionCapsule getTransactionCapsule() {
    return this.transactionCapsule;
  }
}
