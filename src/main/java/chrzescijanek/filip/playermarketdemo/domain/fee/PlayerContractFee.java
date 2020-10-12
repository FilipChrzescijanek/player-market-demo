package chrzescijanek.filip.playermarketdemo.domain.fee;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

@Value
@Builder
public class PlayerContractFee {

    @NotNull(message = "Contract fee must not be null")
    BigDecimal contractFee;

    @NotNull(message = "Transfer fee must not be null")
    BigDecimal transferFee;

    @NotNull(message = "Commission must not be null")
    BigDecimal commission;

    @NotNull(message = "Currency must not be null")
    Currency currency;

}
