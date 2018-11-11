import org.bukkit.entity.Player;
import org.powermock.api.mockito.PowerMockito;

import java.util.UUID;

public enum TestPlayerEnum {

    I_JEDI("i_Jedi", UUID.fromString("edb708e2-afb8-42c3-a365-7014245bf114"));

    private String name;
    private UUID playerId;

    TestPlayerEnum(String name, UUID playerId){
        this.name = name;
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Player getMockPlayer(){
        Player mockPlayer = PowerMockito.mock(Player.class);
        PowerMockito.when(mockPlayer.getName()).thenReturn(name);
        PowerMockito.when(mockPlayer.getUniqueId()).thenReturn(playerId);
        return mockPlayer;
    }
}
