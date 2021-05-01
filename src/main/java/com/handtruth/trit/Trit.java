package com.handtruth.trit;

import nerdhub.cardinal.components.api.event.ItemComponentCallbackV2;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosComponent;
import top.theillusivec4.curios.api.SlotTypeInfo;
import top.theillusivec4.curios.api.SlotTypeInfo.BuildScheme;
import top.theillusivec4.curios.api.type.component.ICurio;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

//todo list:
//World Alteration Device - Direct Active Rectification Fulfiller Edition

//Flux Flow Fixture

//Energy Control Circuit
//Boson Field Projector
//Spatial Flux Generator
//Heart of Unreality (Void given focus)
//Amalgam Amulet (Warranty voids when exposed to enriched matter)
//Matter Beacon

public class Trit implements ModInitializer {
	public static final Item ITEM_CRYSTAL_0 = new Item(new Item.Settings().group(ItemGroup.MISC));
	public static final Item ITEM_BLADEOFSHADES = new Item(new Item.Settings().group(ItemGroup.MISC));
	public static final Item ITEM_AMALGAM_AMULET = new Item(new Item.Settings().group(ItemGroup.MISC));
	public static final ItemWAD ITEM_WAD = new ItemWAD(new Item.Settings().group(ItemGroup.MISC).fireproof().maxCount(1));
	public static final Item ITEM_HEART_OF_UNREALITY = new ItemWithTooltip(new Item.Settings().group(ItemGroup.MISC).fireproof(), "item.trit.heart_of_unreality.tooltip");
	public static final Block FFF_NODE = new Block(FabricBlockSettings.of(Material.METAL).hardness(4.0f));


	@Override
	public void onInitialize() {
		CuriosApi.enqueueSlotType(BuildScheme.REGISTER, new SlotTypeInfo.Builder("necklace").size(1).build());

		System.out.println("Trit: Registering items");
		Registry.register(Registry.ITEM, new Identifier("trit", "crystal_flow"), ITEM_CRYSTAL_0);
		Registry.register(Registry.ITEM, new Identifier("trit", "blade_of_shades"), ITEM_BLADEOFSHADES);
		Registry.register(Registry.ITEM, new Identifier("trit", "world_alteration_device"), ITEM_WAD);
		Registry.register(Registry.ITEM, new Identifier("trit", "amalgam_amulet"), ITEM_AMALGAM_AMULET);
		Registry.register(Registry.ITEM, new Identifier("trit", "heart_of_unreality"), ITEM_HEART_OF_UNREALITY);
		Registry.register(Registry.BLOCK, new Identifier("trit", "fff_node"), FFF_NODE);
		Registry.register(Registry.ITEM, new Identifier("trit", "fff_node"), new BlockItem(FFF_NODE, new Item.Settings().group(ItemGroup.MISC)));

		ItemComponentCallbackV2.event(ITEM_AMALGAM_AMULET).register(
        ((item, itemStack, componentContainer) -> componentContainer
            .put(CuriosComponent.ITEM, new ICurio() {
				private void repairItem(PlayerEntity user, ItemStack stack) {
					if (user.totalExperience > 0) {
						if (stack.isDamaged()) {
							stack.setDamage(stack.getDamage()-1);
							user.addExperience(-1);
						}
					}
				}
				
				public void curioTick(String identifier, int index, LivingEntity livingEntity) {
					if (livingEntity.world.isClient) return;
					if (livingEntity instanceof PlayerEntity) {
						PlayerEntity user = (PlayerEntity)livingEntity;
						
						repairItem(user, user.inventory.getMainHandStack());

						for (ItemStack stack : user.inventory.armor) {
							repairItem(user, stack);
						}
						for (ItemStack stack : user.inventory.offHand) {
							repairItem(user, stack);
						}
						
					}
				}
            })));
	}
}
