package com.sopze.mc.redstonepp.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

public final class BlockUtil {

  public static Vec3i minCorner(BlockPos pos){ return new Vec3i(pos.getX(), pos.getY(), pos.getZ()); }
  public static Vec3i maxCorner(BlockPos pos){ return new Vec3i(pos.getX()+1, pos.getY()+1, pos.getZ()+1); }

}
