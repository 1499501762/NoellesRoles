package org.agmas.noellesroles.client.renderer;

import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.client.WatheClient;
import dev.doctor4t.wathe.entity.FirecrackerEntity;
import dev.doctor4t.wathe.entity.NoteEntity;
import dev.doctor4t.wathe.index.WatheItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.entities.RoleMineEntity;
import org.jetbrains.annotations.NotNull;

public class RoleMineEntityRenderer extends EntityRenderer<RoleMineEntity> {
    float scale = 0;
    ItemRenderer itemRenderer;

    public RoleMineEntityRenderer(EntityRendererFactory.Context ctx, float scale) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
        this.scale = scale;
    }

    public RoleMineEntityRenderer(EntityRendererFactory.Context context) {
        this(context, 1.0F);
    }

    @Override
    public Identifier getTexture(RoleMineEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }

    public void render(@NotNull RoleMineEntity roleMine, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if ((roleMine.age >= 2 || !(this.dispatcher.camera.getFocusedEntity().squaredDistanceTo(roleMine) < (double)12.25F)) && (GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld()).isRole(MinecraftClient.getInstance().player, Noellesroles.TRAPPER) || WatheClient.isPlayerSpectatingOrCreative())) {
            matrices.push();
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-roleMine.getYaw()));
            matrices.translate(0.0F, (float)roleMine.hashCode() % 24.0F * 1.0E-4F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
            matrices.scale(this.scale * 0.4F, this.scale * 0.4F, this.scale * 0.4F);
            this.itemRenderer.renderItem(ModItems.ROLE_MINE.getDefaultStack(), ModelTransformationMode.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, roleMine.getWorld(), roleMine.getId());
            matrices.pop();
            super.render(roleMine, yaw, tickDelta, matrices, vertexConsumers, light);
        }

    }
}
