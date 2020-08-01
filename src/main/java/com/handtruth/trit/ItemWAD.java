package com.handtruth.trit;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
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
        LootContext.Builder builder = (new LootContext.Builder((ServerWorld)world)).random(world.random).parameter(LootContextParameters.POSITION, blockPos).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity).optionalParameter(LootContextParameters.THIS_ENTITY, user);
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
            BlockHitResult hit = world.rayTrace(new RayTraceContext(ipos, ipos.add(user.getRotationVector().multiply(60)), RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, user));
            BlockPos blockPos = hit.getBlockPos();
            
            LinkedList<BlockPos> list = new LinkedList<>();
            Set<BlockPos> visited = new HashSet<BlockPos>();
            list.add(blockPos);
            
            if (user.isSneaking()) {
                double power = 1000f;
                while (power > 0 && !list.isEmpty()) {
                    BlockPos current = list.removeLast();
                    //System.out.println(power);
                    if (visited.contains(current)) continue;
                    power--;
                    visited.add(current);
                    int displacement = 16;
                    list.add(Math.abs(world.random.nextInt()) % Math.min(list.size()+1, displacement), current.up());
                    list.add(Math.abs(world.random.nextInt()) % Math.min(list.size()+1, displacement), current.down());
                    list.add(Math.abs(world.random.nextInt()) % Math.min(list.size()+1, displacement), current.east());
                    list.add(Math.abs(world.random.nextInt()) % Math.min(list.size()+1, displacement), current.north());
                    list.add(Math.abs(world.random.nextInt()) % Math.min(list.size()+1, displacement), current.south());
                    list.add(Math.abs(world.random.nextInt()) % Math.min(list.size()+1, displacement), current.west());
                    power -= Math.max(0, world.getBlockState(current).getHardness(world, current));
                }
            }
            visited.add(blockPos);
            
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
       
    
}