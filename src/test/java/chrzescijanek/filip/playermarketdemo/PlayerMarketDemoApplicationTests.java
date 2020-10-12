package chrzescijanek.filip.playermarketdemo;

import chrzescijanek.filip.playermarketdemo.domain.player.Player;
import chrzescijanek.filip.playermarketdemo.domain.team.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlayerMarketDemoApplicationTests {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testScenario1() {
        // given
        final LocalDate careerStartDate = LocalDate.now().minus(120, ChronoUnit.MONTHS);
        final LocalDate dateOfBirth = LocalDate.now().minus(20, ChronoUnit.YEARS);
        final Currency currency = Currency.getInstance("PLN");

        // when
        final ResponseEntity<Map<String, Object>> playerResponse = addPlayer("First name", "Last name", careerStartDate, dateOfBirth);
        final String player = ((Map<String, Map<String, String>>) playerResponse.getBody().get("_links")).get("self").get("href");
        final ResponseEntity<Map<String, Object>> teamResponse = addTeam("Name", currency);
        final String team = ((Map<String, Map<String, String>>) teamResponse.getBody().get("_links")).get("self").get("href");

        final ResponseEntity<Map<String, Object>> deletePlayerResponse = restTemplate.exchange(player,
                HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> deleteTeamResponse = restTemplate.exchange(team,
                HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));

        final ResponseEntity<Map<String, Object>> invalidPlayerResponse = restTemplate.exchange("http://localhost:" + port + "/players",
                HttpMethod.POST, new HttpEntity<>(new Player()), ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> invalidTeamResponse = restTemplate.exchange("http://localhost:" + port + "/teams",
                HttpMethod.POST, new HttpEntity<>(new Team()), ParameterizedTypeReference.forType(Map.class));

        // then
        assertEquals(HttpStatus.CREATED, playerResponse.getStatusCode());
        assertEquals("First name", playerResponse.getBody().get("firstName"));
        assertEquals("Last name", playerResponse.getBody().get("lastName"));
        assertEquals(dateOfBirth.toString(), playerResponse.getBody().get("dateOfBirth"));
        assertEquals(careerStartDate.toString(), playerResponse.getBody().get("careerStartDate"));

        assertEquals(HttpStatus.CREATED, teamResponse.getStatusCode());
        assertEquals("Name", teamResponse.getBody().get("name"));
        assertEquals(currency.toString(), teamResponse.getBody().get("currency"));

        assertEquals(HttpStatus.NO_CONTENT, deletePlayerResponse.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, deleteTeamResponse.getStatusCode());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, invalidPlayerResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, invalidTeamResponse.getStatusCode());
    }

    @Test
    void testScenario2() {
        // given
        final LocalDate careerStartDate = LocalDate.now().minus(120, ChronoUnit.MONTHS);
        final LocalDate dateOfBirth = LocalDate.now().minus(20, ChronoUnit.YEARS);
        final Currency currency = Currency.getInstance("PLN");

        // when
        final ResponseEntity<Map<String, Object>> player1Response = addPlayer("First name", "Last name", careerStartDate, dateOfBirth);
        final ResponseEntity<Map<String, Object>> player2Response = addPlayer("First name #2", "Last name #2", careerStartDate, dateOfBirth);
        final ResponseEntity<Map<String, Object>> teamResponse = addTeam("Name", currency);

        final String player1Teams = ((Map<String, Map<String, String>>) player1Response.getBody().get("_links")).get("teams").get("href");
        final String player1 = ((Map<String, Map<String, String>>) player1Response.getBody().get("_links")).get("self").get("href");
        final String player2Teams = ((Map<String, Map<String, String>>) player2Response.getBody().get("_links")).get("teams").get("href");
        final String player2 = ((Map<String, Map<String, String>>) player2Response.getBody().get("_links")).get("self").get("href");
        final String team = ((Map<String, Map<String, String>>) teamResponse.getBody().get("_links")).get("self").get("href");
        final List<String> teamPathSegments = UriComponentsBuilder.fromUriString(team).build().getPathSegments();
        final long teamId = Long.parseLong(teamPathSegments.get(teamPathSegments.size() - 1));

        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "text/uri-list");
        final ResponseEntity<Map<String, Object>> assignToTeamResponse = restTemplate.exchange(player1Teams,
                HttpMethod.POST, new HttpEntity<>(team, headers), ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> playersResponse = restTemplate.exchange("http://localhost:" + port + "/players",
                HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> player1TeamsResponse = restTemplate.exchange(player1Teams,
                HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> player2TeamsResponse = restTemplate.exchange(player2Teams,
                HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> deleteAssignmentToTeamResponse = restTemplate.exchange(player1Teams + "/" + teamId,
                HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));

        // then
        assertEquals(HttpStatus.NO_CONTENT, assignToTeamResponse.getStatusCode());
        assertEquals(HttpStatus.OK, playersResponse.getStatusCode());
        assertEquals(HttpStatus.OK, player1TeamsResponse.getStatusCode());
        assertEquals(HttpStatus.OK, player2TeamsResponse.getStatusCode());

        assertEquals(2, ((Map<String, List<?>>) playersResponse.getBody().get("_embedded")).get("players").size());
        assertEquals(1, ((Map<String, List<?>>) player1TeamsResponse.getBody().get("_embedded")).get("teams").size());
        assertEquals(0, ((Map<String, List<?>>) player2TeamsResponse.getBody().get("_embedded")).get("teams").size());

        assertEquals(HttpStatus.NO_CONTENT, deleteAssignmentToTeamResponse.getStatusCode());

        // clean-up
        restTemplate.exchange(player1, HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));
        restTemplate.exchange(player2, HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));
        restTemplate.exchange(team, HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));
    }

    @Test
    void testScenario3() {
        // given
        final LocalDate careerStartDate = LocalDate.now().minus(120, ChronoUnit.MONTHS);
        final LocalDate dateOfBirth = LocalDate.now().minus(20, ChronoUnit.YEARS);
        final Currency currency = Currency.getInstance("PLN");

        // when
        final ResponseEntity<Map<String, Object>> playerResponse = addPlayer("First name", "Last name", careerStartDate, dateOfBirth);
        final ResponseEntity<Map<String, Object>> teamResponse = addTeam("Name", currency);

        final String playerTeams = ((Map<String, Map<String, String>>) playerResponse.getBody().get("_links")).get("teams").get("href");
        final String player = ((Map<String, Map<String, String>>) playerResponse.getBody().get("_links")).get("self").get("href");
        final List<String> playerPathSegments = UriComponentsBuilder.fromUriString(player).build().getPathSegments();
        final long playerId = Long.parseLong(playerPathSegments.get(playerPathSegments.size() - 1));
        final String team = ((Map<String, Map<String, String>>) teamResponse.getBody().get("_links")).get("self").get("href");
        final List<String> teamPathSegments = UriComponentsBuilder.fromUriString(team).build().getPathSegments();
        final long teamId = Long.parseLong(teamPathSegments.get(teamPathSegments.size() - 1));

        final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Content-Type", "text/uri-list");
        restTemplate.exchange(playerTeams, HttpMethod.POST, new HttpEntity<>(team, headers), ParameterizedTypeReference.forType(Map.class));

        final ResponseEntity<Map<String, Object>> feeResponse = restTemplate.exchange("http://localhost:" + port + "/fees/calculate/" + teamId + "/" + playerId + "?commissionPercentage=10",
                HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class));

        final ResponseEntity<Map<String, Object>> notFoundResponse1 = restTemplate.exchange("http://localhost:" + port + "/fees/calculate/" + (teamId + 1337) + "/" + playerId + "?commissionPercentage=10",
                HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> notFoundResponse2 = restTemplate.exchange("http://localhost:" + port + "/fees/calculate/" + teamId + "/" + (playerId + 1337) + "?commissionPercentage=10",
                HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> invalidResponse1 = restTemplate.exchange("http://localhost:" + port + "/fees/calculate/" + teamId + "/" + playerId + "?commissionPercentage=11",
                HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class));
        final ResponseEntity<Map<String, Object>> invalidResponse2 = restTemplate.exchange("http://localhost:" + port + "/fees/calculate/" + teamId + "/" + playerId + "?commissionPercentage=-1",
                HttpMethod.GET, null, ParameterizedTypeReference.forType(Map.class));

        // then
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse1.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse2.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, invalidResponse1.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, invalidResponse2.getStatusCode());
        assertEquals(HttpStatus.OK, feeResponse.getStatusCode());
        assertEquals(0, new BigDecimal("660000").compareTo(new BigDecimal(feeResponse.getBody().get("contractFee").toString())));
        assertEquals(0, new BigDecimal("600000").compareTo(new BigDecimal(feeResponse.getBody().get("transferFee").toString())));
        assertEquals(0, new BigDecimal("60000").compareTo(new BigDecimal(feeResponse.getBody().get("commission").toString())));
        assertEquals("PLN", feeResponse.getBody().get("currency"));

        // clean-up
        restTemplate.exchange(playerTeams + "/" + teamId, HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));
        restTemplate.exchange(player, HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));
        restTemplate.exchange(team, HttpMethod.DELETE, null, ParameterizedTypeReference.forType(Map.class));
    }

    ResponseEntity<Map<String, Object>> addPlayer(final String firstName, final String lastName, final LocalDate careerStartDate, final LocalDate dateOfBirth) {
        final Player player = new Player();
        player.setFirstName(firstName);
        player.setLastName(lastName);
        player.setCareerStartDate(careerStartDate);
        player.setDateOfBirth(dateOfBirth);

        return restTemplate.exchange("http://localhost:" + port + "/players",
                HttpMethod.POST, new HttpEntity<>(player), ParameterizedTypeReference.forType(Map.class));
    }

    ResponseEntity<Map<String, Object>> addTeam(final String name, final Currency currency) {
        final Team team = new Team();
        team.setName(name);
        team.setCurrency(currency);

        return restTemplate.exchange("http://localhost:" + port + "/teams",
                HttpMethod.POST, new HttpEntity<>(team), ParameterizedTypeReference.forType(Map.class));
    }

}
