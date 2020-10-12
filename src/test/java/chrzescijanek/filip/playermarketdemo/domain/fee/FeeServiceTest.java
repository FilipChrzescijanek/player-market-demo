package chrzescijanek.filip.playermarketdemo.domain.fee;

import chrzescijanek.filip.playermarketdemo.domain.player.Player;
import chrzescijanek.filip.playermarketdemo.domain.player.PlayerRepository;
import chrzescijanek.filip.playermarketdemo.domain.team.Team;
import chrzescijanek.filip.playermarketdemo.domain.team.TeamRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {

    @Mock
    TeamRepository teamRepository;

    FeeService feeService;

    @BeforeEach
    void setUp() {
        feeService = new FeeService(teamRepository);
    }

    @Test
    void shouldThrow404WhenTeamNotFound() {
        final Throwable throwable = assertThrows(ResponseStatusException.class, () -> feeService.calculateFee(1L, 2L, BigDecimal.ZERO));
        assertEquals("404 NOT_FOUND \"Team with ID: " + 1L + " was not found\"", throwable.getMessage());
    }

    @Test
    void shouldThrow404WhenPlayerNotFound() {
        when(teamRepository.findById(eq(1L))).thenReturn(Optional.of(new Team()));
        final Throwable throwable = assertThrows(ResponseStatusException.class, () -> feeService.calculateFee(1L, 2L, BigDecimal.ZERO));
        assertEquals("404 NOT_FOUND \"Player with ID: " + 2L + " was not found\"", throwable.getMessage());
    }

    @Test
    void shouldCalculateContractFeeProperly() {
        final Player player = new Player();
        player.setId(2L);
        player.setCareerStartDate(LocalDate.now().minus(120, ChronoUnit.MONTHS));
        player.setDateOfBirth(LocalDate.now().minus(20, ChronoUnit.YEARS));

        final Team team = new Team();
        team.setCurrency(Currency.getInstance("PLN"));
        team.getPlayers().add(player);
        when(teamRepository.findById(eq(1L))).thenReturn(Optional.of(team));

        final PlayerContractFee result = feeService.calculateFee(1L, 2L, BigDecimal.TEN);

        assertEquals(Currency.getInstance("PLN"), result.getCurrency());
        assertEquals(0, result.getTransferFee().compareTo(BigDecimal.valueOf(600_000)));
        assertEquals(0, result.getCommission().compareTo(BigDecimal.valueOf(60_000)));
        assertEquals(0, result.getContractFee().compareTo(BigDecimal.valueOf(660_000)));
    }

}