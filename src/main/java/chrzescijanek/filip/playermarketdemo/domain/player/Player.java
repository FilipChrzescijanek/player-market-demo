package chrzescijanek.filip.playermarketdemo.domain.player;

import chrzescijanek.filip.playermarketdemo.domain.team.Team;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Player {

    @Id
    @GeneratedValue
    @ApiModelProperty(hidden = true)
    Long id;

    @NotNull(message = "First name must not be null")
    String firstName;

    @NotNull(message = "Last name must not be null")
    String lastName;

    @NotNull(message = "Date of birth must not be null")
    LocalDate dateOfBirth;

    @NotNull(message = "Career start date must not be null")
    LocalDate careerStartDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PLAYER_TEAM",
            joinColumns = @JoinColumn(name = "PLAYER_ID"),
            inverseJoinColumns = @JoinColumn(name = "TEAM_ID"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ApiModelProperty(hidden = true)
    Set<Team> teams = new HashSet<>();

}
