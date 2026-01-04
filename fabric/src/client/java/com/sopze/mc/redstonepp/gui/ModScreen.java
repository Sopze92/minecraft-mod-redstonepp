package com.sopze.mc.redstonepp.gui;

import com.sopze.mc.redstonepp.MainClient;
import com.sopze.mc.redstonepp.state.ModOptions;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import static com.sopze.mc.redstonepp.gui.ModGuiUtil.*;

public class ModScreen extends ModScreenBase {
  private static final Component TITLE = Component.translatable("gui.redstonepp.title.menu");
  private HeaderAndFooterLayout layout;

  public ModScreen(Screen screen, ModOptions options) {
    super(screen, screen, options, TITLE);
  }

  protected void init(){
    layout = new HeaderAndFooterLayout(this, 40, 33);
    TransientKeyHelper kh= new TransientKeyHelper(KEY_BASE);

    LinearLayout linearLayout = layout.addToHeader(LinearLayout.vertical().spacing(8));
    linearLayout.addChild(new StringWidget(TITLE, font), LayoutSettings::alignHorizontallyCenter);
    GridLayout gridLayout = new GridLayout();

    gridLayout.defaultCellSetting().paddingHorizontal(4).paddingBottom(4).alignHorizontallyCenter();

    kh.setScope("menu");
    GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
    rowHelper.addChild(modoptions.active().createButton(minecraft.options, 0, 0, 312, (o)->{ MainClient.onGlobalStateChanged(o); super.repositionElements(); }), 2);
    rowHelper.addChild(button(modoptions.menuButton(), 312), 2);
    rowHelper.addChild(createSpacer(4), 2);
    rowHelper.addChild(screen(kh, "options", new ModOptionsScreen(outterScreen, this, modoptions), 200), 2);

    layout.addToContents(gridLayout);
    layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (b) -> onClose()).width(200).build());
    layout.visitWidgets((guiEventListener) -> { AbstractWidget aw = addRenderableWidget(guiEventListener); });
    repositionElements();
  }

  protected void repositionElements() {
    layout.arrangeElements();
  }
}
