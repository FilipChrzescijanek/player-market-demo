package chrzescijanek.filip.playermarketdemo.domain.fee;

import chrzescijanek.filip.playermarketdemo.domain.player.PlayerRepository;
import chrzescijanek.filip.playermarketdemo.domain.team.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@AllArgsConstructor
public class FeeService {

    private final TeamRepository teamRepository;

    @Transactional
    public PlayerContractFee calculateFee(final Long teamId, final Long playerId, final BigDecimal commissionPercentage) {
        val team = teamRepository.findById(teamId).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Team with ID: " + teamId + " was not found"));
        val player = team.getPlayers()
                .stream()
                .filter(it -> playerId.equals(it.getId()))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Player with ID: " + playerId + " was not found"));
        val monthsOfExperience = ChronoUnit.MONTHS.between(LocalDate.now(), player.getCareerStartDate());
        val age = ChronoUnit.YEARS.between(LocalDate.now(), player.getDateOfBirth());
        val transferFee = BigDecimal.valueOf(monthsOfExperience)
                .multiply(BigDecimal.valueOf(100_000))
                .divide(BigDecimal.valueOf(age), 2, RoundingMode.HALF_UP);
        val commission = commissionPercentage
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .multiply(transferFee)
                .setScale(2, RoundingMode.HALF_UP);
        val contractFee = transferFee.add(commission);
        return PlayerContractFee.builder()
                .commission(commission)
                .contractFee(contractFee)
                .currency(team.getCurrency())
                .transferFee(transferFee)
                .build();
    }

}
