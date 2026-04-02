package walksy.optimizer.command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import com.mojang.brigadier.arguments.IntegerArgumentType;

public class EnableOptimizerCommand {

    public static boolean fastCrystal = true;
    public static int fastCrystalChance = 100;

    public void initializeToggleCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Toggle command
            dispatcher.register(ClientCommands.literal("walksyfastcrystal")
                    .executes(context -> {
                        if (fastCrystal) {
                            fastCrystal = false;
                            displayMessage("Walksy's Fast crystals disabled!");
                        } else {
                            fastCrystal = true;
                            displayMessage("Walksy's Fast crystals enabled");
                        }
                        return 1;
                    })
            );

            dispatcher.register(ClientCommands.literal("walksyfastcrystalchance")
                    .then(ClientCommands.argument("chance", IntegerArgumentType.integer(0, 100))
                            .executes(context -> {
                                int chance = IntegerArgumentType.getInteger(context, "chance");
                                fastCrystalChance = chance;
                                displayMessage("Walksy's Fast crystals chance set to " + chance + "%");
                                return 1;
                            })
                    )
                    .executes(context -> {
                        displayMessage("Current fast crystal chance: " + fastCrystalChance + "%");
                        return 1;
                    })
            );
        });
    }

    public static void displayMessage(String message) {
        // Make sure that they are in game.
        if (!inGame()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.sendSystemMessage(Component.literal(message));
        }
    }
    public static Boolean inGame() {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && client.getConnection() != null;
    }
}