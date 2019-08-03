package mimc;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class MigratePlugin extends JavaPlugin implements PluginMessageListener {
    private final String CHANNEL_NAME = "mimc:channel";

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL_NAME, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL_NAME);
        getLogger().warning("Plugin was enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().warning("Plugin was disabled!");
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equalsIgnoreCase(CHANNEL_NAME)) {
            return;
        }

        getLogger().warning(String.format( "MIMC | Got a message over the MIMC channel" ));

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equalsIgnoreCase("prepare-migration")) {
            MimcServer server = (MimcServer)getServer();

            String serverName = in.readUTF();
            String playerName = in.readUTF();

            getLogger().warning(String.format( "Migration request received for player %s to node %s", playerName, serverName ));

            if (server.preparePlayerForMigration(playerName)) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("migration-prepared");
                out.writeUTF(serverName);
                out.writeUTF(playerName);

                getLogger().warning(String.format( "Migration preparation succeeded for player %s", playerName ));

                player.sendPluginMessage(this, CHANNEL_NAME, out.toByteArray());
            } else {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("migration-preparation-failed");
                out.writeUTF(serverName);
                out.writeUTF(playerName);

                getLogger().warning(String.format( "Migration preparation failed for player %s", playerName ));

                player.sendPluginMessage(this, CHANNEL_NAME, out.toByteArray());
            }
        }
    }
}
