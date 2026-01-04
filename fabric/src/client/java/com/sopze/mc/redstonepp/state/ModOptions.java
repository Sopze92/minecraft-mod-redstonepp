package com.sopze.mc.redstonepp.state;

import com.google.common.base.Charsets;
import com.sopze.mc.redstonepp.MainClient;
import com.sopze.mc.redstonepp.Logger;
import com.sopze.mc.redstonepp.MainCommon.ModExceptionSimple;
import com.sopze.mc.redstonepp.gui.ModGuiUtil.TransientKeyHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;

import java.io.*;

import static net.minecraft.client.Options.genericValueLabel;

public class ModOptions {
  private final File optionsFile;

  private final OptionInstance<Boolean> active;
  private final OptionInstance<Boolean> menuButton;

  private final OptionInstance<Boolean> debugRenderer;

  private final OptionInstance<Boolean> debugShowRange;
  private final OptionInstance<Boolean> debugShowTarget;
  private final OptionInstance<Boolean> debugRangeFromTarget;
  private final OptionInstance<Boolean> debugShowUnpoweredWire;

  private final OptionInstance<Integer> debugRange;

  // ---------------------------------------------------------------- OPTIONS

  public ModOptions(Minecraft minecraft, File path)
  {
    TransientKeyHelper kh= new TransientKeyHelper("gui.redstonepp.%s.%s");

    kh.setScope("menu");
    this.active = OptionInstance.createBoolean(kh.name("active"), OptionInstance.noTooltip(), true, (b)->{ ModSettings.active = b; MainClient.onGlobalStateChanged(b); });
    this.menuButton = OptionInstance.createBoolean(kh.name("menubutton"), kh.tooltipSupplier(), true, (b)->{ ModSettings.menuButton = b; });

    kh.setScope("debug");
    this.debugRenderer = OptionInstance.createBoolean(kh.name("renderer"), kh.tooltipSupplier(), false, (b)->{ ModSettings.debugRenderer = b; MainClient.onGlobalStateChanged(ModSettings.active); });
    this.debugShowRange = OptionInstance.createBoolean(kh.name("showrange"), kh.tooltipSupplier(), true, (b)->{ ModSettings.debugShowRange= b; });
    this.debugShowTarget = OptionInstance.createBoolean(kh.name("showtarget"), kh.tooltipSupplier(), true, (b)->{ ModSettings.debugShowTarget= b; });
    this.debugRangeFromTarget = OptionInstance.createBoolean(kh.name("rangefromtarget"), kh.tooltipSupplier(), false, (b)->{ ModSettings.debugRangeFromTarget= b; });
    this.debugShowUnpoweredWire = OptionInstance.createBoolean(kh.name("unpoweredwire"), kh.tooltipSupplier(), false, (b)->{ ModSettings.debugShowUnpoweredWire= b; });
    this.debugRange = new OptionInstance<Integer>(kh.name("range"), kh.tooltipSupplier(), (c, i) -> genericValueLabel(c, Component.translatable("options.value", i)), new OptionInstance.IntRange(4, 16), 8, (i) -> ModSettings.debugRange = i);

    optionsFile= new File(path, "sop_config_redstonepp");
    load();
  }

  // ---------------------------------------------------------------- GETTERS

  public OptionInstance<Boolean> active() { return active; }
  public OptionInstance<Boolean> menuButton() { return menuButton; }

  public OptionInstance<Boolean> debugRenderer() { return debugRenderer; }
  public OptionInstance<Boolean> debugShowRange() { return debugShowRange; }
  public OptionInstance<Boolean> debugShowTarget() { return debugShowTarget; }
  public OptionInstance<Boolean> debugRangeFromTarget() { return debugRangeFromTarget; }
  public OptionInstance<Boolean> debugShowUnpoweredWire() { return debugShowUnpoweredWire; }
  public OptionInstance<Integer> debugRange() { return debugRange; }

  // ---------------------------------------------------------------- IO

  public void save(){
    try {

      if(optionsFile.getParentFile() instanceof File parent && !parent.exists()) parent.mkdirs();

      try (final DataOutputStream output = new DataOutputStream(new FileOutputStream(optionsFile))){

        output.write("sop".getBytes(Charsets.UTF_8));
        /*byte[] v= MainCommon.getLocalVersion();
        byte c;
        for(int i=0; i< 3; i++) {
          c= v[i];
          output.writeByte(c);
        }*/
        output.write("|This file cannot be read in human mode|".getBytes(Charsets.UTF_8));

        int v0 = ModSettings.active ? 0b1 : 0;
        int v1 = ModSettings.menuButton ? 0b1 << 1 : 0;
        int v2 = ModSettings.debugRenderer ? 0b1 << 2 : 0;
        output.writeByte(v0 | v1 | v2);

        int v3 = ModSettings.debugShowRange ? 0b1 : 0;
        int v4 = ModSettings.debugShowTarget ? 0b1 << 1 : 0;
        int v5 = ModSettings.debugRangeFromTarget ? 0b1 << 2 : 0;
        int v6 = ModSettings.debugShowUnpoweredWire ? 0b1 << 3 : 0;
        int v7 = ((ModSettings.debugRange-4) & 0xF) << 12;
        output.writeShort(v3 | v4 | v5 | v6 | v7);
      }
    }
    catch (Exception e) { Logger.slogErr("Failed writing options file.\n%s", e); }
  }

  public void load(){
    try {
      if (!this.optionsFile.exists()) save();

      try (DataInputStream input = new DataInputStream(new FileInputStream(optionsFile))) {

        String h= new String(input.readNBytes(3), Charsets.UTF_8);
        if(!h.equals("sop")) throw new ModExceptionSimple("invalid header");
        /*byte[] v= MainCommon.getLocalVersion();

        byte[] version= input.readNBytes(3);
        for(int i=0; i< 3; i++){
          if(v[i] != version[i]) throw new ModExceptionSimple("mismatched version");
        }*/

        input.skipBytes(40);

        // manual update cos minecraft is written by people with down syndrome

        int states= input.readByte();
        ModSettings.active= (states & 0b1) != 0;
        this.active.set(ModSettings.active);
        ModSettings.menuButton= ((states >> 1) & 0b1) != 0;
        this.menuButton.set(ModSettings.menuButton);
        ModSettings.debugRenderer= ((states >> 2) & 0b1) != 0;
        this.debugRenderer.set(ModSettings.debugRenderer);

        int debug= input.readShort();
        ModSettings.debugShowRange= (debug & 0b1) != 0;
        this.debugShowRange.set(ModSettings.debugShowRange);
        ModSettings.debugShowTarget= ((debug >> 1) & 0b1) != 0;
        this.debugShowTarget.set(ModSettings.debugShowTarget);
        ModSettings.debugRangeFromTarget= ((debug >> 2) & 0b1) != 0;
        this.debugRangeFromTarget.set(ModSettings.debugRangeFromTarget);
        ModSettings.debugShowUnpoweredWire= ((debug >> 3) & 0b1) != 0;
        this.debugShowUnpoweredWire.set(ModSettings.debugShowUnpoweredWire);
        ModSettings.debugRange= ((debug >> 12) & 0xF) + 4;
        this.debugRange.set(ModSettings.debugRange);
      }
    }
    catch (ModExceptionSimple e) { Logger.slogErr("Failed to read options file, defaults will be used: %s\n%s", e.getMessage(), e); }
    catch (Exception e) { Logger.slogErr("Failed to read options file, defaults will be used.\n%s", e); }
  }
}
