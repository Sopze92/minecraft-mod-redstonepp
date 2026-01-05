package com.sopze.mc.redstonepp.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sopze.mc.redstonepp.block.I_OverlayInfoProvider;
import com.sopze.mc.redstonepp.mixin.HelperMixin;
import com.sopze.mc.redstonepp.mixin.RendererMixin;
import com.sopze.mc.redstonepp.state.ModSettings;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.*;

import java.lang.Math;
import java.util.*;

import static com.sopze.mc.redstonepp.Constants.*;

public class DebugOverlay {

  private static final ResourceLocation overlayId= ResourceLocation.fromNamespaceAndPath(MOD_ID, "overlay");

  private final ScreenOverlay overlay;

  public DebugOverlay(){
    overlay= new ScreenOverlay();
    //HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, overlayId, overlay);
    HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS, overlayId, overlay);

    WorldRenderEvents.END_EXTRACTION.register(Event.DEFAULT_PHASE, overlay::extraction);
    ClientTickEvents.END_WORLD_TICK.register(Event.DEFAULT_PHASE, overlay::tick);

    refreshGlobalState(ModSettings.active);
  }

  public void refreshGlobalState(boolean state){
    overlay.refreshGlobalState(state && ModSettings.debugRenderer);
  }

  // -------------------------------------------------------------------------------- IMPL

  private static class ScreenOverlay implements HudElement {

    private static final List<Object> CLEANUP= new ArrayList<>(6400);

    private boolean active= false;

    public final Map<BlockPos, DebugOverlay.OverlayEntry> entryMap = new HashMap<>(6400);
    public final Set<BlockPos> entryPositions= new HashSet<>(6400);

    private final Set<Vec3i> rangeOffsets= new HashSet<>(6017);
    private final Set<BlockPos> rangePositions= new HashSet<>(6017);

    public BlockPos targetPosCached = null;
    public BlockPos targetPos = null;

    public BlockPos rangeOrigin = null;
    private int rangeRadius = 0;

    public void refreshGlobalState(boolean state){
      active= state;
      if(!active) wipe();
    }

    private void wipe(){
      CLEANUP.clear();
      entryMap.clear();
      entryPositions.clear();
      rangeOffsets.clear();
      rangePositions.clear();
      targetPosCached = null;
      targetPos = null;
      rangeOrigin= null;
    }

    // -------------------------------------------------------------------------------- TICK

    public void tick(ClientLevel level){
      if(level== null || !shouldRender()) return;

      Minecraft client= Minecraft.getInstance();

      BlockHitResult hit= (BlockHitResult)client.hitResult;
      targetPos = hit != null && hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : null;

      if(ModSettings.debugShowRange){

        BlockPos origin = ModSettings.debugRangeFromTarget ? targetPos : client.getCameraEntity() instanceof Entity e ? e.blockPosition() : null;
        if(origin != null){

          boolean useCachedOffsets = rangeRadius == ModSettings.debugRange;
          if(!useCachedOffsets) {
            rangeRadius = ModSettings.debugRange;
            refreshRangeOffsets();
          }

          boolean useCachedPositions = origin.equals(rangeOrigin) && useCachedOffsets;
          if(!useCachedPositions) {
            rangeOrigin = origin;
            refreshRangePositions();
          }
        }

        tickEntries(level);
      }

      if(ModSettings.debugShowTarget) tickTarget(level);
    }

    public void tickEntries(Level level){

      BlockState state;
      Block block;
      float height;
      int color;
      String[] lines;
      boolean valid, present, range, create, target;

      OverlayEntry entry;

      CLEANUP.clear();

      for(BlockPos pos : entryPositions){

        entry= entryMap.get(pos);
        state= level.getBlockState(pos);

        present = entry != null;
        range= rangePositions.contains(pos);
        create= !present && range;
        target= pos.equals(targetPosCached);

        valid= false;

        if((create || (present && entry.hasChanged(state))) && !target){

          block = state.getBlock();
          height= .5f;
          lines= null;
          color= 0xFFC4C4C4;

          if(block instanceof RedStoneWireBlock) {

            int power= state.getValue(RedStoneWireBlock.POWER);
            if(power > 0 || ModSettings.debugShowUnpoweredWire) {
              int colorValue= power > 0 ? 0x5B + 0x07 * power : 0;
              lines = new String[]{String.valueOf(power)};
              color = power > 0 ? 0xFF000000 | (colorValue & 0xFF) << 16 | (colorValue & 0xFF) << 8 | (colorValue & 0xFF) : 0xFFD42000;
              height= .125f;
            }
            valid= true;
          }

          if(valid || (present && entry.valid)) {
            if(create) entryMap.put(pos, new OverlayEntry(state, height, color, lines));
            else entry.set(state, height, color, lines);
          }
        }

        if(present){

          if(entry.health <= 0) {
            entryMap.remove(pos);
            if(!range) CLEANUP.add(pos);
          }

          if(entry.valid && !range && !target) entry.setInvalid();

          entry.tick();
        }
      }

      if(!CLEANUP.isEmpty()) entryPositions.removeAll(CLEANUP);
    }

    public void tickTarget(Level level){

      if(targetPos == null){
        if(targetPosCached != null && entryMap.get(targetPosCached) instanceof OverlayEntry ec) {
          targetPosCached= null;
          ec.setDirty();
        }
        return;
      }

      OverlayEntry entry= entryMap.get(targetPos);
      boolean create = entry == null;
      boolean range= rangePositions.contains(targetPos);
      BlockState state= level.getBlockState(targetPos);

      if(create || !targetPos.equals(targetPosCached) || entry.hasChanged(state)){

        boolean valid= false;
        float height= .5f;
        int color= 0xFFFFFFFF;
        String[] lines= null;
        Block block = state.getBlock();

        if (block instanceof I_OverlayInfoProvider infoProvider) {
          valid = infoProvider.shouldRenderInfoOverlay(level, targetPos, state);
          if (valid) {
            lines = infoProvider.getOverlayInfo(level, targetPos, state);
            height = infoProvider.getOverlayHeight(level, targetPos, state);
          }
        }
        else if (block instanceof RedStoneWireBlock) {
          lines = new String[]{
            "power: " + state.getValue(BlockStateProperties.POWER),
            String.format("sides: %s", getRedstoneWireSidesInfo(state))
          };
          height = .125f;
          valid = true;
        }
        else if (block instanceof DaylightDetectorBlock) {
          lines = new String[]{
            "inverted: " + state.getValue(BlockStateProperties.INVERTED),
            "light: " + state.getValue(BlockStateProperties.POWER)
          };
          height = .5f;
          valid = true;
        }
        else if (block instanceof DiodeBlock) {

          String powered = "powered: " + state.getValue(BlockStateProperties.POWERED);
          Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

          if (block instanceof RepeaterBlock) {
            lines = new String[]{
              powered,
              "facing: " + direction,
              "locked: " + state.getValue(BlockStateProperties.LOCKED),
              "delay: " + state.getValue(BlockStateProperties.DELAY),
              "input: " + ((HelperMixin.I_DiodeBlockInvoker) block).i_getInputSignal(level, targetPos, state)
            };
            height = .334f;
            valid = true;
          }
          if (block instanceof ComparatorBlock) {

            int signal = ((HelperMixin.I_DiodeBlockInvoker) block).i_getInputSignal(level, targetPos, state);
            int signalside = ((HelperMixin.I_DiodeBlockInvoker) block).i_getAlternateSignal(level, targetPos, state);

            lines = new String[]{
              powered,
              "facing: " + direction,
              "mode: " + state.getValue(BlockStateProperties.MODE_COMPARATOR),
              signalside == 0 ? "input: " + signal : String.format("input: %d (%d side)", signal, signalside),
              "output: " + ((HelperMixin.I_ComparatorBlockInvoker) block).i_calculateOutputSignal(level, targetPos, state)
            };
            height = .334f;
            valid = true;
          }
        }
        else if (block instanceof DispenserBlock) {
          lines = new String[]{
            "triggered: " + state.getValue(BlockStateProperties.TRIGGERED),
            "facing: " + state.getValue(BlockStateProperties.FACING),
            "analog: " + AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(targetPos))
          };
          height = .95f;
          valid = true;
        }
        else if (block instanceof CopperBulbBlock) {

          boolean lit = state.getValue(BlockStateProperties.LIT);

          lines = new String[]{
            "powered: " + state.getValue(BlockStateProperties.POWERED),
            "lit: " + lit,
            "analog: " + (lit ? 15 : 0)
          };
          height = .95f;
          valid = true;
        }
        else if (block instanceof CrafterBlock) {

          BlockEntity entity = level.getBlockEntity(targetPos);

          lines = new String[]{
            "triggered: " + state.getValue(BlockStateProperties.TRIGGERED),
            "crafting: " + state.getValue(BlockStateProperties.CRAFTING),
            "orientation: " + state.getValue(BlockStateProperties.ORIENTATION),
            "analog: " + (entity instanceof CrafterBlockEntity crafterEntity ? crafterEntity.getRedstoneSignal() : 0)
          };
          height = .95f;
          valid = true;
        }
        else if (block instanceof HopperBlock) {

          Direction direction = state.getValue(BlockStateProperties.FACING_HOPPER);

          lines = new String[]{
            "facing: " + direction,
            "enabled: " + state.getValue(BlockStateProperties.ENABLED),
            "analog: " + AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(targetPos))
          };
          height = .95f;
          valid = true;
        }

        if(targetPosCached != null && !targetPosCached.equals(targetPos) && entryMap.get(targetPosCached) instanceof OverlayEntry ec) {
          ec.setDirty();
          ec.setInvalid();
        }
        targetPosCached= valid ? targetPos : null;

        if(valid){
          if(create) {
            entryMap.put(targetPosCached, new OverlayEntry(state, height, color, lines));
            entryPositions.add(targetPosCached);
          }
          else entry.set(state, height, color, lines);
        }
        else if(!create) entry.setInvalid();
      }

      if(!create){
        if(entry.health <= 0) {
          entryMap.remove(targetPosCached);
          if(!range) entryPositions.remove(targetPosCached);
        }
        entry.tick();
      }
    }

    // -------------------------------------------------------------------------------- EXTRACTION

    public void extraction(WorldExtractionContext context) {

      if(!(context.world() instanceof Level level) || !shouldRender()) return;

      Minecraft client= Minecraft.getInstance();
      Camera camera= context.camera();

      Matrix4fc pose = new Matrix4f(context.cullProjectionMatrix()).mul(getViewBob(context, client)).mul(context.viewMatrix());

      if(ModSettings.debugShowRange && rangeOrigin != null) refreshRangeCoords(client, level, camera, pose);
      if(ModSettings.debugShowTarget && targetPos != null) refreshTargetCoords(client, level, camera, pose);
    }

    public void refreshRangeCoords(Minecraft client, Level level, Camera camera, Matrix4fc pose){

      BlockHitResult hit;
      Vec3 blockPos;
      Vec3 cameraPos= camera.position();

      for(BlockPos pos : rangePositions){

        if(entryMap.get(pos) instanceof OverlayEntry entry) {

          blockPos = pos.getBottomCenter().add(.0f, entry.height, .0f);

          hit = level.clip(new ClipContext(cameraPos, blockPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
          if(hit.getType() == BlockHitResult.Type.MISS) {

            Vector2i coords = getScreenCoordinates(client, camera, pose, blockPos.toVector3f());
            if(coords == null) continue;

            if(entry.valid) entry.set(coords.x, coords.y);
          }
        }
      }
    }

    public void refreshTargetCoords(Minecraft client, Level level, Camera camera, Matrix4fc pose) {
      if(targetPosCached != null && entryMap.get(targetPosCached) instanceof OverlayEntry entry) {

        Vector2i coords= getScreenCoordinates(client, camera, pose, targetPosCached.getBottomCenter().add(.0f, entry.height, .0f).toVector3f());
        if(coords == null) return;

        if(entry.valid) entry.set(coords.x, coords.y);
      }
    }

    // -------------------------------------------------------------------------------- RENDER

    public void render(GuiGraphics context, DeltaTracker delta) {

      if(!shouldRender()) return;

      Minecraft client= Minecraft.getInstance();
      Font font= client.font;

      int scale= client.getWindow().getGuiScale();
      int height= font.lineHeight+2;

      OverlayEntry entry;

      if(ModSettings.debugShowRange){
        for(BlockPos pos : entryPositions){
          if(pos.equals(targetPosCached)) continue;

          entry= entryMap.get(pos);
          if(entry != null && entry.shouldRender()) renderInfo(entry, context, font, scale, height);
        }
      }

      if(ModSettings.debugShowTarget && targetPosCached != null && entryMap.get(targetPosCached) instanceof OverlayEntry targetEntry) {
        if(targetEntry.shouldRender()) renderInfo(targetEntry, context, font, scale, height);
      }
    }

    private void renderInfo(OverlayEntry data, GuiGraphics context, Font font, int scale, int height) {

      String[] lines= data.lines();
      String line;

      int count = lines.length;

      int x= data.x / scale;
      int oy= -height * count/2;
      int y= (oy + data.y) / scale;

      if(x < 0 || y < 0 || x > context.guiWidth() || y > context.guiHeight()) return;
      int w, h;

      int fa= (int)((float)0xFF/OverlayEntry.HEALTH) * Math.min(data.age, data.health) << 24;
      int ba= (int)((float)0x80/OverlayEntry.HEALTH) * Math.min(data.age, data.health) << 24;

      for(int i = 0; i< count; i++){
        line = lines[i];
        w= font.width(line)/2;
        h = height*i;
        context.fill(x-w-2, y+h-1, x+w+2, y+h+height-1, ba);
        context.drawString(font, line, x-w, y+h, (data.color & 0x00FFFFFF) | fa, true);
      }
    }

    // -------------------------------------------------------------------------------- HELPERS

    public boolean shouldRender() { return active && (ModSettings.debugShowRange || ModSettings.debugShowTarget); }

    private void refreshRangeOffsets(){
      int r= rangeRadius;
      rangeOffsets.clear();
      int ax, my, mz;
      for (int x = -r; x <= r; x++) {
        ax= Math.abs(x);
        my = r - ax;
        for (int y = -my; y <= my; y++) {
          mz = r - ax - Math.abs(y);
          for (int z = -mz; z <= mz; z++) rangeOffsets.add(new Vec3i(x,y,z));
        }
      }
    }

    private void refreshRangePositions(){
      rangePositions.clear();
      for(Vec3i pos : rangeOffsets) rangePositions.add(rangeOrigin.offset(pos));
      entryPositions.addAll(rangePositions);
    }

    public Vector2i getScreenCoordinates(Minecraft client, Camera camera, Matrix4fc pose, Vector3f pos){
      Vector3f viewPos= camera.position().toVector3f();
      Vec3 pos3 = new Vec3(pos.x - viewPos.x, pos.y - viewPos.y, pos.z - viewPos.z);
      Vector4f pos4 = new Vector4f((float)pos3.x, (float)pos3.y, (float)pos3.z, 1.0f);
      pos4.mul(pose);
      if(!(pos4.w > 0) || 1.0f - pos4.z / pos4.w < .0f) return null;
      return new Vector2i((int)((1.0f + (pos4.x / pos4.w)) * client.getWindow().getWidth() * .5f), (int)((1.0f - (pos4.y / pos4.w)) * client.getWindow().getHeight() * .5f));
    }

    private Matrix4fc getViewBob(WorldExtractionContext context, Minecraft client){
      PoseStack poseStack = new PoseStack();
      RendererMixin.I_GameRendererInvoker renderer= (RendererMixin.I_GameRendererInvoker)client.gameRenderer;
      float partialTick= context.tickCounter().getGameTimeDeltaPartialTick(true);
      renderer.i_bobHurt(poseStack, partialTick);
      renderer.i_bobView(poseStack, partialTick);
      return poseStack.last().copy().pose();
    }

    private String getRedstoneWireSidesInfo(BlockState state){
      List<String> chars= new ArrayList<>(4);
      if(state.getValue(BlockStateProperties.NORTH_REDSTONE).isConnected()) chars.add("N");
      if(state.getValue(BlockStateProperties.EAST_REDSTONE).isConnected()) chars.add("E");
      if(state.getValue(BlockStateProperties.SOUTH_REDSTONE).isConnected()) chars.add("S");
      if(state.getValue(BlockStateProperties.WEST_REDSTONE).isConnected()) chars.add("W");
      return chars.isEmpty() ? "none" : chars.size() == 4 ? "all" : String.join(" ", chars);
    }
  }

  // -------------------------------------------------------------------------------- ENTRIES

  public static class OverlayEntry {

    public static final int HEALTH = 3;

    private boolean dirty, valid;
    private int age, health;
    private BlockState state;
    private int x, y, color;
    private float height;
    private String[] lines;

    public OverlayEntry(){ this(null, .0f, 0, ""); }
    public OverlayEntry(BlockState state, float height, int color, String... lines){
      set(state, height, color, lines);
      this.age= 0;
      this.health= HEALTH;
    }

    public boolean hasChanged(BlockState state){
      return dirty || this.state != state;
    }

    public void set(int x, int y){
      this.x= x;
      this.y= y;
      health= HEALTH;
    }

    public void tick() { age++; health--; }

    public void setDirty() { dirty= true; }

    public void setInvalid() { valid= false; }

    public void set(BlockState state, float height, int color, String... lines){
      this.state= state;
      this.color= color;
      this.lines= lines;
      this.height= height;
      this.dirty= false;
      this.valid= true;
    }

    public boolean shouldRender(){ return lines() != null; }

    public String[] lines(){ return lines; }
  }
}
