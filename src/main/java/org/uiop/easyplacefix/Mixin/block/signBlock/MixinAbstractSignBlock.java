package org.uiop.easyplacefix.mixin.block.signBlock;

import com.tick_ins.tick.RunnableWithCountDown;
import com.tick_ins.tick.TickThread;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.uiop.easyplacefix.IBlock;
import org.uiop.easyplacefix.IClientWorld;
import org.uiop.easyplacefix.util.PlayerBlockAction;

@Mixin(SignBlock.class)
public class MixinAbstractSignBlock implements IBlock {

    @Override
    public void BlockAction(BlockState blockState, BlockHitResult blockHitResult) {
        ClientPacketListener clientPlayNetworkHandler = Minecraft.getInstance().getConnection();
        if (clientPlayNetworkHandler == null || Minecraft.getInstance().level == null) {
            return;
        }
        SignBlockEntity blockEntity = (SignBlockEntity) SchematicWorldHandler.getSchematicWorld().getBlockEntity(blockHitResult.getBlockPos());
        if (blockEntity == null) {
            TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                    .setCount(3)
                    .build(() -> PlayerBlockAction.openSignEditorAction.count = Math.max(PlayerBlockAction.openSignEditorAction.count - 1, 0))
            );
            return;
        }
        SignText backText = blockEntity.getText(SignTextSlot.BACK);
        SignText frontText = blockEntity.getText(SignTextSlot.FRONT);

        clientPlayNetworkHandler.send(
                new ServerboundSignUpdatePacket(blockHitResult.getBlockPos(), java.util.List.of(frontText.getMessages(false).get(0).getString(), frontText.getMessages(false).get(1).getString(), frontText.getMessages(false).get(2).getString(), frontText.getMessages(false).get(3).getString()), SignTextSlot.FRONT)
        );

        for (int i = 0; i < backText.getMessages(false).size(); i++) {
            if (!backText.getMessages(false).get(i).getString().isEmpty()) {
                clientPlayNetworkHandler.send(new ServerboundUseItemOnPacket(
                        InteractionHand.MAIN_HAND,
                        blockHitResult,
                        ((IClientWorld) Minecraft.getInstance().level).Sequence()

                ));

                clientPlayNetworkHandler.send(
                        new ServerboundSignUpdatePacket(blockHitResult.getBlockPos(), java.util.List.of(backText.getMessages(false).get(0).getString(), backText.getMessages(false).get(1).getString(), backText.getMessages(false).get(2).getString(), backText.getMessages(false).get(3).getString()), SignTextSlot.BACK)
                );


                break;
            }
        }

        TickThread.addCountDownTask(new RunnableWithCountDown.Builder()
                .setCount(3)
                .build(() -> PlayerBlockAction.openSignEditorAction.count = Math.max(PlayerBlockAction.openSignEditorAction.count - 1, 0))
        );
    }
}
