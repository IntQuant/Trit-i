package com.handtruth.trit.util;

import net.minecraft.util.math.BlockPos;

public class WeightedBlockPos implements Comparable<WeightedBlockPos> {
    public int weight;
    public BlockPos pos;
    public WeightedBlockPos(int weight, BlockPos pos) {
        this.weight = weight;
        this.pos = pos;
    }

    public int compareTo(WeightedBlockPos other) {
        return Integer.compare(weight, other.weight);
    }
}
