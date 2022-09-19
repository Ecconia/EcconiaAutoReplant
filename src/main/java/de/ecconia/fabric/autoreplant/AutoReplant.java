package de.ecconia.fabric.autoreplant;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AutoReplant implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		MinecraftClient client = MinecraftClient.getInstance();
		UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hit) -> {
			BlockPos pos = hit.getBlockPos();
			BlockState state = world.getBlockState(hit.getBlockPos());
			Block block = state.getBlock();
			
			if(client.interactionManager == null || client.player == null)
			{
				return ActionResult.PASS;
			}
			
			if(block instanceof CropBlock cropBlock)
			{
				if(cropBlock.isMature(state))
				{
					if(!client.interactionManager.attackBlock(hit.getBlockPos(), hit.getSide()))
					{
						player.sendMessage(Text.literal("[AutoReplant] Not able to break block..."));
						//At this point, normal interaction may happen:
						return ActionResult.PASS;
					}
					//From here on, the script must fail.
					
					//Select wheat seeds in the hotbar (given there are some in there...)
					PlayerInventory playerInventory = player.getInventory();
					int matchingSlot = -1;
					Item seed = cropBlock.getSeedsItem().asItem();
					for(int i = 0; i < 9; i++)
					{
						if(playerInventory.getStack(i).getItem() == seed)
						{
							matchingSlot = i;
							break;
						}
					}
					if(matchingSlot == -1)
					{
						//End life:
						return ActionResult.FAIL;
					}
					playerInventory.selectedSlot = matchingSlot;
					
					BlockPos fieldBlockPos = pos.down();
					if(!client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, new BlockHitResult(
							new Vec3d(fieldBlockPos.getX() + 0.5f, fieldBlockPos.getY() + 0.9375f, fieldBlockPos.getZ() + 0.5f),
							Direction.UP,
							hit.getBlockPos().down(),
							false)
					).isAccepted())
					{
						player.sendMessage(Text.literal("[AutoReplant] Failed to interact with block..."));
					}
					return ActionResult.FAIL;
				}
			}
			return ActionResult.PASS;
		});
	}
}
