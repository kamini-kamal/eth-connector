package hyperledger.besu.java.rest.client.dto;

import hyperledger.besu.java.rest.client.util.HelperModule;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.RawTransaction;

@Slf4j
@Data
@Builder
public class Transaction {
  private String functionName;
  private String[] params;
  private String contractAddress;
  private Function functionRef;
  private String compiledHexBinary;
  private BigInteger gasPrice;
  private BigInteger gasLimit;

  private Optional<Function> getFunction() {
    // This method converts the raw input of function name and parameters into
    // the object Function that gets consumed by the library.
    List<String> inputParams = Arrays.stream(params).collect(Collectors.toList());
    List<TypeReference<?>> outputParams = new ArrayList<>();
    try {
      this.functionRef =
          new Function(functionName, Utils.typeMap(inputParams, Type.class), outputParams);
    } catch (Exception ex) {
      // handle exceptions
    }
    return Optional.ofNullable(functionRef);
    // Collections.singletonList(new Uint(BigInteger.valueOf(0)))
  }

  public String getEncodedFunction() {
    Optional<Function> function = getFunction();
    // TODO: Error if function is empty
    return FunctionEncoder.encode(function.get());
  }

  public List<TypeReference<Type>> getOutputParams() {
    return this.getFunctionRef().getOutputParameters();
  }

  public RawTransaction getRawTransaction() {
    // TODO: Error if function is empty
    // Construct the payload for a transaction
    BigInteger nonce = HelperModule.getNonce(HelperModule.CREDENTIALS.getAddress());
    log.debug("Generated nonce: {}", nonce);

    // get the gas price and gas limit from the configuration
    // i.e. A transaction object created out of input configuration
    RawTransaction rawTransaction =
        RawTransaction.createTransaction(
            nonce, this.gasPrice, this.gasLimit, contractAddress, getEncodedFunction());
    return rawTransaction;
  }

  public RawTransaction getContractTransaction() {
    // create contract transaction

    // Construct the payload for a transaction
    BigInteger nonce = HelperModule.getNonce(HelperModule.CREDENTIALS.getAddress());
    return RawTransaction.createContractTransaction(
        nonce,
        this.gasPrice,
        this.gasLimit,
        BigInteger.ZERO, // gas price needed in the smart contract
        this.compiledHexBinary); // binary is
  }
}
