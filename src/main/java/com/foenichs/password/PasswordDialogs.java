package com.foenichs.password;

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

import java.util.List;
import java.util.Optional;

public class PasswordDialogs {
    public static ResourceKey<Dialog> PASSWORD_DIALOG = ResourceKey.create(Registries.DIALOG, Identifier.fromNamespaceAndPath("server_password", "password"));
}
