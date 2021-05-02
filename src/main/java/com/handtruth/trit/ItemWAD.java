package com.handtruth.trit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.handtruth.trit.util.WeightedBlockPos;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.BaseText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class ItemWAD extends Item{
    public ItemWAD(Settings settings) {
        super(settings);
    }

    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return true;
     }

    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
       return 10.0F;
    }

    public boolean isEffectiveOn(BlockState state) {
        return true;
    }

    private void destroyBlock(World world, BlockPos blockPos, PlayerEntity user, LinkedList<ItemStack> drops) {
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (blockState.getHardness(world, blockPos) == -1) return;
        if (world.getBlockEntity(blockPos) != null) return;
        BlockEntity blockEntity = block.hasBlockEntity() ? world.getBlockEntity(blockPos) : null;
        LootContext.Builder builder = (new LootContext.Builder((ServerWorld)world)).random(world.random).parameter(LootContextParameters.ORIGIN, new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ())).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity).optionalParameter(LootContextParameters.THIS_ENTITY, user);
        for (ItemStack stack : blockState.getDroppedStacks(builder)) {
            for (ItemStack dstack : drops) {
                if (dstack.isItemEqual(stack)) {
                    int amount = Math.min(dstack.getMaxCount()-dstack.getCount(), stack.getCount());
                    dstack.increment(amount);
                    stack.decrement(amount);
                }
            }
            if (stack.getCount() > 0) drops.addLast(stack);
            //world.spawnEntity(new ItemEntity(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), stack));
        }
        world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            user.getItemCooldownManager().set(this, 10);
        }
        
        if (!world.isClient()) {
            Vec3d ipos = new Vec3d(user.getX(), user.getEyeY(), user.getZ());
            BlockHitResult hit = world.raycast(new RaycastContext(ipos, ipos.add(user.getRotationVector().multiply(60)), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));
            BlockPos center = hit.getBlockPos();
            
            PriorityQueue<WeightedBlockPos> list = new PriorityQueue<>();
            Set<BlockPos> visited = new HashSet<BlockPos>();
            list.add(new WeightedBlockPos(0, center));
            
            if (user.isSneaking()) {
                double power = 1000f;
                while (power > 0 && !list.isEmpty()) {
                    WeightedBlockPos w_current = list.remove();
                    BlockPos current = w_current.pos;
                    //System.out.println(power);
                    if (visited.contains(current)) continue;
                    power--;
                    visited.add(current);
                    List<BlockPos> neighbours = new LinkedList<>();
                    neighbours.add(current.up());
                    neighbours.add(current.down());
                    neighbours.add(current.east());
                    neighbours.add(current.north());
                    neighbours.add(current.south());
                    neighbours.add(current.west());
                    for (BlockPos pos : neighbours) {
                        int cost = w_current.weight;
                        cost -= (int)(Math.max(0, world.getBlockState(pos).getHardness(world, pos))*4);
                        cost += pos.getSquaredDistance(center)*10;
                        list.add(new WeightedBlockPos(cost, pos));
                    }
                    power -= Math.max(0, world.getBlockState(current).getHardness(world, current));
                }
            }
            visited.add(center);
            
            LinkedList<ItemStack> drops = new LinkedList<>();

            for (BlockPos lblock : visited)
            destroyBlock(world, lblock, user, drops);

            for (ItemStack drop : drops) {
                //world.spawnEntity(new ItemEntity(world, user.getX(), user.getY(), user.getZ(), drop));
                user.inventory.offerOrDrop(world, drop);
            }
        }
        
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        BaseText text_lore = new TranslatableText("item.trit.world_alteration_device.tooltip.lore");
        BaseText text1 = new TranslatableText("item.trit.world_alteration_device.tooltip.0");
        tooltip.add(text1.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xa0a0a0))));
        BaseText text2 = new TranslatableText("item.trit.world_alteration_device.tooltip.1");
        tooltip.add(text2.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xa0a0a0))));
        BaseText text3 = new TranslatableText("item.trit.world_alteration_device.tooltip.2");
        tooltip.add(text3.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xa0a0a0))));
        tooltip.add(text_lore.setStyle(Style.EMPTY.withItalic(true).withColor(TextColor.fromRgb(Trit.COLOR_LORE))));
    }    
}