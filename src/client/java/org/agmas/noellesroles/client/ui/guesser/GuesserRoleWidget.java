package org.agmas.noellesroles.client.ui.guesser;

import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.gui.screen.ingame.LimitedInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.packet.GuessC2SPacket;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

public class GuesserRoleWidget extends TextFieldWidget {
    public final LimitedInventoryScreen screen;
    public final ChatInputSuggestor suggestor;
    public static boolean stopClosing = false;


    public GuesserRoleWidget(LimitedInventoryScreen screen, TextRenderer textRenderer, int x, int y) {
        super(textRenderer, x, y, 200, 16, Text.literal(""));
        this.screen = screen;
        suggestor = new ChatInputSuggestor(MinecraftClient.getInstance(),screen,this,textRenderer,true,true,-1,10,false, Color.TRANSLUCENT);
        suggestor.refresh();
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean original = super.charTyped(chr, modifiers);
        suggestor.refresh();
        return original;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            ClientPlayNetworking.send(new GuessC2SPacket(GuesserPlayerWidget.selectedPlayer, getText()));
            screen.close();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void eraseCharacters(int characterOffset) {
        super.eraseCharacters(characterOffset);
        suggestor.refresh();
    }


    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        stopClosing = isSelected();
        if (GuesserPlayerWidget.selectedPlayer != null) {
            ArrayList<String> suggestions = new ArrayList<>();
            WatheRoles.ROLES.forEach((m) -> {
                if (Harpymodloader.SPECIAL_ROLES.contains(m)) return;
                GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
                if (!gameWorldComponent.isInnocent(MinecraftClient.getInstance().player)) {
                    if (Noellesroles.KILLER_SIDED_NEUTRALS.contains(m)) return;
                    if (m.canUseKiller()) return;
                }
                if (m.identifier().getPath().startsWith(getText()) || getText().isEmpty())
                    suggestions.add(m.identifier().getPath());
            });
            if (!suggestions.isEmpty()) {
                setSuggestion(suggestions.getFirst().substring(getText().length()));
            } else {
                setSuggestion("");
            }
            super.renderWidget(context, mouseX, mouseY, delta);
            suggestor.render(context,mouseX,mouseY);
        }
    }
}
