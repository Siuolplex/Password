package com.foenichs.password.mixin;

import com.foenichs.password.Password;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin {
    @Shadow
    @Final
    protected MinecraftServer server;

    @Shadow
    @Final
    protected Connection connection;

    @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
    private void server_password$getOurClickAction(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        if ((Object)this instanceof ServerConfigurationPacketListenerImpl configurationPacketListener) {
            Password.handleOurClicks(configurationPacketListener, this.server, packet);
        }

    }
}
