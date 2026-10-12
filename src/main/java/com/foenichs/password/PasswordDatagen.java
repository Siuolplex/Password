package com.foenichs.password;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.*;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.body.PlainMessage;
import net.minecraft.server.dialog.input.TextInput;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PasswordDatagen implements DataGeneratorEntrypoint {
    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.DIALOG, PasswordDialogGenerator::bootstrap);
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(PasswordDialogGenerator::new);
    }

    static class PasswordDialogGenerator extends FabricDynamicRegistryProvider {
        public PasswordDialogGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected void configure(HolderLookup.Provider registries, Entries entries) {
            entries.addAll(registries.lookupOrThrow(Registries.DIALOG));
        }

        @Override
        public String getName() {
            return "Dialogs";
        }
        /*
        registryBuilder.add(Registries.DIALOG, context -> );
         */

        public static void bootstrap(BootstrapContext<Dialog> context) {
            context.register(PasswordDialogs.PASSWORD_DIALOG, new MultiActionDialog(
                    new CommonDialogData(
                            Component.empty().append(Component.object(new AtlasSprite(Identifier.withDefaultNamespace("items"), Identifier.withDefaultNamespace("item/trial_key")))).append(Component.literal(" Password")),
                            Optional.empty(),
                            false,
                            true,
                            DialogAction.WAIT_FOR_RESPONSE,
                            List.of(
                                    new PlainMessage(Component.literal("This server requires you to enter a password before joining."), 200)
                            ),
                            List.of(
                                    new Input("pw", new TextInput(200, Component.literal("Enter Password"), true, "", 128, Optional.empty()))
                            )),
                    List.of(new ActionButton(new CommonButtonData(Component.literal("Submit"), Optional.empty(), 80), Optional.of(new CustomAll(Identifier.fromNamespaceAndPath("password", "check"), Optional.empty())))),
                    Optional.of(new ActionButton(new CommonButtonData(Component.literal("Cancel"), Optional.empty(), 80), Optional.of(new CustomAll(Identifier.fromNamespaceAndPath("password", "cancel"), Optional.empty())))),
                    1
            ));
        }
    }
}
