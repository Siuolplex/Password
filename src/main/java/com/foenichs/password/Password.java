package com.foenichs.password;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.permission.v1.MutablePermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserWhiteListEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Password implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Password.class);
    private static final Map<UUID, CompletableFuture<Boolean>> awaiting = new ConcurrentHashMap<>();
    private static final Identifier CHECK_ID = Identifier.fromNamespaceAndPath("password", "check");
    private static final Identifier CANCEL_ID = Identifier.fromNamespaceAndPath("password", "cancel");
    private static String serverPassword = "test";
    private static boolean operatorBypass = false;
    private static boolean whitelistBypass = false;
    private static boolean autoWhitelist = false;

    @Override
    public void onInitialize() {
        ServerConfigurationConnectionEvents.CONFIGURE.register(Password::setupForPassword);
        ServerConfigurationConnectionEvents.DISCONNECT.register((listener, server) -> awaiting.remove(listener.getOwner().id()));
    }

    public static void setupForPassword(ServerConfigurationPacketListenerImpl listener, MinecraftServer server) {
        NameAndId owner = new NameAndId(listener.getOwner());
        UUID uuid = owner.id();
        CompletableFuture<MutablePermissionContext> context = PermissionContext.offlinePlayer(uuid, server);
        try {
            if (context.get().checkPermission(Identifier.fromNamespaceAndPath("server_password", "password.bypass"), false)) {
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        boolean whitelistBypassValid = whitelistBypass && server.getPlayerList().getWhiteList().isWhiteListed(owner);

        /*if (whitelistBypassValid && whitelistBypassExpirationEnabled) {
            long lastSeen = offline.getLastSeen();

            if (lastSeen > 0) {
                long expirationMillis = TimeUnit.DAYS.toMillis(whitelistBypassExpirationDays);
                whitelistBypassValid =
                        (System.currentTimeMillis() - lastSeen) < expirationMillis;
            }
        } else */
        if (whitelistBypassValid) {
            return;
        } else if (operatorBypass && server.getPlayerList().isOp(owner)) {
            return;
        }


        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.completeOnTimeout(false, 1, TimeUnit.MINUTES);

        awaiting.put(uuid, future);
        listener.send(new ClientboundShowDialogPacket(server.registryAccess().getOrThrow(PasswordDialogs.PASSWORD_DIALOG)));

        /*if (!future.join()) {
            listener.send(ClientboundClearDialogPacket.INSTANCE);
            listener.disconnect(Component.literal("Server requires correct password."));
        }*/
    }

    // Todo: I COULD move this to Gremlib and make it an event there.
    public static void handleOurClicks(ServerConfigurationPacketListenerImpl listener, MinecraftServer server, ServerboundCustomClickActionPacket packet) {
        NameAndId owner = new NameAndId(listener.getOwner());
        Identifier id = packet.id();

        if (id.equals(CANCEL_ID)) {
            listener.disconnect(Component.literal("Server requires correct password."));
            CompletableFuture<Boolean> fCancel = awaiting.get(owner.id());
            if (fCancel != null) fCancel.complete(false);
            return;
        } else if (id.equals(CHECK_ID) && packet.payload().get() instanceof CompoundTag tag) {
            String entered = tag.getStringOr("pw", "");
            boolean correct = serverPassword.equals(entered);

            if (correct && autoWhitelist) {
                // ????????????????????
                // Seriously Im just the porter dont blame me I dont understand it
                server.schedule(server.wrapRunnable(()-> {
                    if (!server.isEnforceWhitelist()) {
                        server.getPlayerList().getWhiteList().add(new UserWhiteListEntry(owner));
                    }
                }));
            }

            CompletableFuture<Boolean> f = awaiting.get(owner.id());
            if (f != null) f.complete(correct);
        }
    }
}
