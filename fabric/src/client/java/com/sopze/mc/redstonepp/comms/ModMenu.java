package com.sopze.mc.redstonepp.comms;

import com.sopze.mc.redstonepp.MainClient;
import com.sopze.mc.redstonepp.gui.ModScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenu implements ModMenuApi {
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return screen -> new ModScreen(screen, MainClient.options);
  }
}
