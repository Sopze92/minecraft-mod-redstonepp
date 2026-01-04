package com.sopze.mc.redstonepp.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.sopze.mc.redstonepp.MainClient;
import com.sopze.mc.redstonepp.gui.ModScreen;
import com.sopze.mc.redstonepp.state.ModSettings;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

public class GuiMixin {

  // -------------------------------------------------------------------------------- GUI

  @Mixin(OptionsScreen.class)
  public abstract static class OptionsScreenMixin{
    @Invoker("openScreenButton") abstract Button i_openScreenButton(Component c, Supplier<Screen> s);
    @Inject(method="init()V", at= @At(value="INVOKE", target="Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToContents(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    public void h_init_i(CallbackInfo ci, @Local GridLayout.RowHelper rh){
      if(ModSettings.menuButton) rh.addChild(this.i_openScreenButton(Component.translatable("gui.redstonepp.title.menu"), () -> new ModScreen((Screen)(Object)this, MainClient.options)));
    }
  }
}
