package chrzescijanek.filip.playermarketdemo.domain.fee;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@RestController
@RequestMapping("/fees")
@AllArgsConstructor
@Api(tags = "Fees")
@Validated
public class FeeController {

    private final FeeService feeService;

    @GetMapping("/calculate/{teamId}/{playerId}")
    public PlayerContractFee calculateFee(@PathVariable final Long teamId, @PathVariable final Long playerId,
                                          @RequestParam @Min(value = 0) @Max(value = 10) final BigDecimal commissionPercentage) {
        return feeService.calculateFee(teamId, playerId, commissionPercentage);
    }

}
