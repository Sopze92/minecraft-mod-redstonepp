package com.sopze.mc.redstonepp.gui;

import com.google.common.collect.ImmutableList;
import com.sopze.mc.redstonepp.state.ModOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModGuiUtil {

  // ---------------------------------------------------------------- BASES

  public static abstract class ModScreenBase extends Screen {

    public static final String KEY_BASE;

    protected final Screen outterScreen;
    protected final Screen lastScreen;
    protected final ModOptions modoptions;

   // protected final List<AbstractWidget> dynamic= new ArrayList<AbstractWidget>();

    public ModScreenBase(Screen outter, Screen screen, ModOptions modoptions, Component title) {
      super(title);
      outterScreen = outter;
      lastScreen = screen;
      this.modoptions = modoptions;
    }

    public void forceRedraw() { super.repositionElements(); }

    public void onClose() { modoptions.save(); minecraft.setScreen(lastScreen); }

    public LayoutElement createSpacer(int h){ return new EqualSpacingLayout(0, 0, 2, h, EqualSpacingLayout.Orientation.HORIZONTAL); }

    public AbstractWidget button(OptionInstance instance, int size){ return button(instance, size, (o)->{}); }
    public AbstractWidget button(OptionInstance instance, int size, Consumer<Object> change){ return instance.createButton(minecraft.options, 0, 0, size, change); }

    public AbstractWidget screen(TransientKeyHelper kh, String titleKey, Screen screen, int size){ return Button.builder(kh.component(titleKey), (b) -> minecraft.setScreen(screen)).width(size).build(); }
    public AbstractWidget dynamic(AbstractWidget widget, Supplier<Boolean> activeSupplier){
      widget.active= widget.active && activeSupplier.get();
      return widget;
    }

    public AbstractWidget dynamicCustom(AbstractWidget widget, Supplier<Boolean> activeSupplier, Tooltip disabledTooltip){
      boolean active= activeSupplier.get();
      widget.active= widget.active && active;
      if(!active) widget.setTooltip(disabledTooltip);
      return widget;
    }

    static{
      KEY_BASE= "gui.redstonepp.%s.%s";
    }
  }

  public static class SpacerWidget extends AbstractWidget {
    SpacerWidget(int w, int h) {
      super(0, 0, w, h, Component.empty());
      active= false;
    }
    public static SpacerWidget horizontal(int w) { return new SpacerWidget(w, 0); }
    public static SpacerWidget vertical(int h) { return new SpacerWidget(0, h); }
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {}
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
  }

  public static class ModAbstractWidgetList extends ContainerObjectSelectionList<ModAbstractWidgetList.Entry> {
    private Screen screen;
    private int defaultMarginX = 4;
    private int marginY = 4;
    private Entry.Alignment defaultAlignment= Entry.Alignment.CENTER;

    public ModAbstractWidgetList(Minecraft minecraft, int w, int h, int y, Screen screen) {
      super(minecraft, w, h, y, 20);
      this.screen= screen;
    }

    public void addSpacer(int size){
      addEntry(Entry.create(SpacerWidget.vertical(4), screen, defaultAlignment), size + marginY);
    }

    public void addChild(AbstractWidget element) {
      addEntry(Entry.create(element, screen, defaultAlignment), element.getHeight()+ marginY);
    }

    public void addChild(AbstractWidget widgetLeft, AbstractWidget widgetRight) {
      addEntry(Entry.create(widgetLeft, widgetRight, screen, defaultMarginX, defaultAlignment), (int)(Math.max(widgetLeft.getHeight(), widgetRight.getHeight()))+ marginY);
    }

    public void addChild(AbstractWidget widgetLeft, AbstractWidget widgetRight, int margin) {
      addEntry(Entry.create(widgetLeft, widgetRight, screen, margin, defaultAlignment), (int)(Math.max(widgetLeft.getHeight(), widgetRight.getHeight()))+ marginY);
    }

    public void addChild(AbstractWidget element, Entry.Alignment alignment) {
      addEntry(Entry.create(element, screen, alignment), element.getHeight()+ marginY);
    }

    public void addChild(AbstractWidget widgetLeft, AbstractWidget widgetRight, int margin, Entry.Alignment alignment) {
      addEntry(Entry.create(widgetLeft, widgetRight, screen, margin, alignment), (int)(Math.max(widgetLeft.getHeight(), widgetRight.getHeight()))+ marginY);
    }

    public void defaultMarginX(int margin) { defaultMarginX = margin; }
    public void marginY(int margin) { marginY = margin; }
    public void defaultAlignment(Entry.Alignment alignment) { defaultAlignment= alignment; }

    public int getRowWidth() {
      return 340;
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

      public enum Alignment { CENTER, LEFT, RIGHT }

      private final List<AbstractWidget> children;
      private final Screen screen;
      private final int margin;
      private final int size;
      private final Alignment alignment;

      Entry(List<AbstractWidget> list, Screen screen, int size, int margin, Alignment alignment) {
        super();
        this.children = ImmutableList.copyOf(list);
        this.screen = screen;
        this.size= size;
        this.margin= margin;
        this.alignment= alignment;
      }

      public static Entry create(AbstractWidget widget, Screen screen, Alignment alignment) {
        return new Entry(Arrays.stream((new AbstractWidget[]{widget})).toList(), screen, widget.getWidth(), 0, alignment);
      }

      public static Entry create(AbstractWidget widgetLeft, AbstractWidget widgetRight, Screen screen, int margin, Alignment alignment) {
        return new Entry(Arrays.stream((new AbstractWidget[]{widgetLeft, widgetRight})).toList(), screen, widgetLeft.getWidth()+widgetRight.getWidth(), margin, alignment);
      }

      @Override
      public void renderContent(GuiGraphics guiGraphics, int i, int j, boolean bl, float f) {
        int k = 0;
        int l = (int)(alignment == Alignment.CENTER ? screen.width*.5f - (size+margin*(children.size()-1))*.5f : alignment == Alignment.RIGHT ? -(size+margin*(children.size()-1))*.5f : 0);

        for(AbstractWidget abstractWidget : this.children) {
          abstractWidget.setPosition(l + k, this.getContentY());
          abstractWidget.render(guiGraphics, i, j, f);
          k += abstractWidget.getWidth() + margin;
        }

      }

      public List<? extends GuiEventListener> children() { return this.children; }
      public List<? extends NarratableEntry> narratables() { return this.children; }
    }
  }

  // ---------------------------------------------------------------- HELPERS

  public static class TransientKeyHelper {
    private String key, tooltip, subtooltip;
    private String _name, _scope;
    public TransientKeyHelper(String base){ setBase(base); }
    public void setBase(String base){ key = base; tooltip = key + ".tooltip"; subtooltip = key + ".tooltip.%s"; }
    public void setScope(String scope){ _scope = scope; }
    public String name(String name){ _name = name; return String.format(key, _scope, _name); }
    public Component component(String name){ _name = name; return Component.translatable(String.format(key, _scope, _name)); }
    public Component makeComponent(String scope, String name){ return Component.translatable(String.format(key, scope, name)); }
    public <T> OptionInstance.TooltipSupplier<T> tooltipSupplier(){ return makeTooltipSupplier(_scope, _name); }
    public <T> OptionInstance.TooltipSupplier<T> tooltipSupplier(String sub){ return makeTooltipSupplier(_scope, _name, sub); }
    public <T> OptionInstance.TooltipSupplier<T> makeTooltipSupplier(String scope, String name){ return OptionInstance.cachedConstantTooltip(Component.translatable(String.format(tooltip, scope, name))); }
    public <T> OptionInstance.TooltipSupplier<T> makeTooltipSupplier(String scope, String name, String sub){ return OptionInstance.cachedConstantTooltip(Component.translatable(String.format(subtooltip, scope, name, sub))); }
    public Tooltip tooltip(){ return makeTooltip(_scope, _name); }
    public Tooltip tooltip(String sub){ return makeTooltip(_scope, _name, sub); }
    public Tooltip makeTooltip(String scope, String name){ return Tooltip.create(Component.translatable(String.format(tooltip, scope, name))); }
    public Tooltip makeTooltip(String scope, String name, String sub){ return Tooltip.create(Component.translatable(String.format(subtooltip, scope, name, sub))); }
  }

}
