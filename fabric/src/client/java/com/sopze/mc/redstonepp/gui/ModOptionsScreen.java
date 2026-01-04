package com.sopze.mc.redstonepp.gui;

import com.sopze.mc.redstonepp.MainClient;
import com.sopze.mc.redstonepp.state.ModOptions;
import com.sopze.mc.redstonepp.state.ModSettings;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import static com.sopze.mc.redstonepp.gui.ModGuiUtil.*;

public class ModOptionsScreen extends ModScreenBase {
  private static final Component TITLE = Component.translatable("gui.redstonepp.title.options");
  private HeaderAndFooterLayout layout;
  private ModAbstractWidgetList list;

  public ModOptionsScreen(Screen outter, Screen screen, ModOptions options) {
    super(outter, screen, options, TITLE);
  }

  protected void init(){
    layout = new HeaderAndFooterLayout(this);
    LinearLayout linearLayout = layout.addToHeader(LinearLayout.vertical().spacing(8));
    linearLayout.addChild(new StringWidget(TITLE, font), LayoutSettings::alignHorizontallyCenter);

    TransientKeyHelper kh= new TransientKeyHelper("gui.redstonepp.%s.%s");

    list= new ModAbstractWidgetList(minecraft, width, height - (HeaderAndFooterLayout.DEFAULT_HEADER_AND_FOOTER_HEIGHT*2), HeaderAndFooterLayout.DEFAULT_HEADER_AND_FOOTER_HEIGHT, this);
    list.defaultAlignment(ModAbstractWidgetList.Entry.Alignment.CENTER);
    list.defaultMarginX(8);
    list.marginY(4);

    kh.setScope("options");
    list.addChild(new StringWidget(kh.component("debug"), font));

    boolean active= ModSettings.active;
    boolean debug= active && ModSettings.debugRenderer;

    list.addChild(
      dynamic(modoptions.debugRenderer().createButton(minecraft.options, 0,0, 312, (o)->{ super.repositionElements(); } ), ()->active)
    );
    list.addChild(
      dynamic(modoptions.debugShowRange().createButton(minecraft.options, 0,0, 152, (o)->{ super.repositionElements(); } ), ()->debug),
      dynamic(modoptions.debugShowTarget().createButton(minecraft.options, 0,0, 152), ()->debug)
    );

    boolean range= debug && ModSettings.debugShowRange;

    list.addChild(
      dynamic(modoptions.debugRange().createButton(minecraft.options, 0,0, 152), ()->range),
      dynamic(modoptions.debugRangeFromTarget().createButton(minecraft.options, 0,0, 152), ()->range)
    );
    list.addChild(
      dynamic(modoptions.debugShowUnpoweredWire().createButton(minecraft.options, 0,0, 152), ()->range),
      new SpacerWidget(152, 4)
    );

    layout.addToContents(list);
    layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, (b)-> onClose()).width(200).build());

    layout.visitWidgets((guiEventListener) -> { AbstractWidget aw = addRenderableWidget(guiEventListener); });
    repositionElements();
  }

  @Override
  protected void repositionElements() {
    layout.arrangeElements();
    list.updateSize(this.width, this.layout);
  }
}
