import org.bukkit.entity.Player;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class PlayerTest {

    @Test
    public void playerEnumTest(){
        Player jedi = TestPlayerEnum.I_JEDI.getMockPlayer();
        assertEquals(jedi.getName(), TestPlayerEnum.I_JEDI.getName());
        assertEquals(jedi.getUniqueId(), TestPlayerEnum.I_JEDI.getPlayerId());
    }
}
