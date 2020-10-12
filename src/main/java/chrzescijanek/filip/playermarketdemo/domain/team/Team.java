package chrzescijanek.filip.playermarketdemo.domain.team;

import chrzescijanek.filip.playermarketdemo.domain.player.Player;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Team {

    @Id
    @GeneratedValue
    @ApiModelProperty(hidden = true)
    Long id;

    @NotNull(message = "Name must not be null")
    String name;

    @NotNull(message = "Currency must not be null")
    Currency currency;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "PLAYER_TEAM",
            joinColumns = @JoinColumn(name = "TEAM_ID"),
            inverseJoinColumns = @JoinColumn(name = "PLAYER_ID"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ApiModelProperty(hidden = true)
    Set<Player> players = new HashSet<>();

}
