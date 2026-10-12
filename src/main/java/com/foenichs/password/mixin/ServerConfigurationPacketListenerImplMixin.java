package com.foenichs.password.mixin;

import com.foenichs.password.Password;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonPacketListenerImplAccessor;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketListenerImplMixin {
    @Inject(method = "finishCurrentTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;startNextTask()V"))
    private void server_password$runAfterRegistrySync(ConfigurationTask.Type taskTypeToFinish, CallbackInfo ci) {
        Password.LOGGER.info("TYPE = " + taskTypeToFinish.id());
        if (taskTypeToFinish.equals(SynchronizeRegistriesTask.TYPE)) {
            Password.setupForPassword((ServerConfigurationPacketListenerImpl)((Object)this), ((ServerCommonPacketListenerImplAccessor)this).getServer());
        }
    }
}
