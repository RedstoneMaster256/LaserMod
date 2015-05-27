package lasermod.block;

import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.DOWN;

import java.util.Random;

import lasermod.LaserMod;
import lasermod.api.ILaser;
import lasermod.api.ILaserReceiver;
import lasermod.api.LaserInGame;
import lasermod.network.packet.PacketColourConverter;
import lasermod.network.packet.PacketSmallColourConverter;
import lasermod.tileentity.TileEntitySmallColourConverter;
import lasermod.util.BlockActionPos;
import lasermod.util.LaserUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author ProPercivalalb
 */
public class BlockSmallColourConverter extends BlockContainer {

	public IIcon inputIcon;
	public IIcon[] front = new IIcon[16];
	
	public BlockSmallColourConverter() {
		super(Material.rock);
		this.setHardness(1.0F);
		this.setCreativeTab(LaserMod.tabLaser);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySmallColourConverter();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
	    this.inputIcon = iconRegister.registerIcon("lasermod:colorConverterInput");
	    for(int i = 0; i < 16; ++i) {
	    	front[i] = iconRegister.registerIcon("lasermod:ColorConverter_" + i);
	    }
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if(meta == ForgeDirection.DOWN.ordinal())
        	 this.setBlockBounds(0.2F, 0.8F, 0.2F, 0.8F, 1.0F, 0.8F);
        else if(meta == ForgeDirection.UP.ordinal())
       	 	this.setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.2F, 0.8F);
    	else if(meta == ForgeDirection.SOUTH.ordinal())
    		this.setBlockBounds(0.2F, 0.2F, 0.0F, 0.8F, 0.8F, 0.2F);
       	else if(meta == ForgeDirection.NORTH.ordinal())
    		this.setBlockBounds(0.2F, 0.2F, 0.8F, 0.8F, 0.8F, 1.0F);
    	else if(meta == ForgeDirection.EAST.ordinal())
    		this.setBlockBounds(0.0F, 0.2F, 0.2F, 0.2F, 0.8F, 0.8F);
       	else if(meta == ForgeDirection.WEST.ordinal())
    		this.setBlockBounds(0.8F, 0.2F, 0.2F, 1.0F, 0.8F, 0.8F);
       	else
       		this.setBlockBounds(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F);
    }

	@Override
    public void setBlockBoundsForItemRender() {
		this.setBlockBounds(0.2F, 0.2F, 0.3F, 0.8F, 0.8F, 0.5F);
    }

	@Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
        return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
    }

	@Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
    }

	@Override
	@SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		TileEntitySmallColourConverter colourConverter = (TileEntitySmallColourConverter)world.getTileEntity(x, y, z);
		int meta = LaserUtil.getOrientation(world.getBlockMetadata(x, y, z));

		if (meta > 5)
		    return this.front[colourConverter.colour];
		if (side == meta)
		    return this.front[colourConverter.colour];
		else
		 	return side == Facing.oppositeSide[meta] ? inputIcon : Blocks.piston.getIcon(0, 1);
    }
	
	@Override
	public IIcon getIcon(int side, int meta) {
	    meta = 3;

	    if (meta > 5)
	        return this.front[14];
	    if (side == meta)
	        return this.front[14];
	    else
	    	return side == Facing.oppositeSide[meta] ? inputIcon : Blocks.piston.getIcon(0, 1);
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote) {
        	TileEntitySmallColourConverter colourconverter = (TileEntitySmallColourConverter)world.getTileEntity(x, y, z);
    		BlockActionPos reciver = LaserUtil.getFirstBlock(colourconverter, colourconverter.getBlockMetadata());
    		if(reciver != null && reciver.isLaserReciver(colourconverter.getBlockMetadata())) {
    			LaserInGame laserInGame = colourconverter.getOutputLaser(colourconverter.getBlockMetadata());
            	if(colourconverter.laser == null) {
            		reciver.getLaserReceiver(colourconverter.getBlockMetadata()).removeLasersFromSide(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()]);
            	}
            	else if(reciver.getLaserReceiver(colourconverter.getBlockMetadata()).canPassOnSide(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()], laserInGame)) {
            		reciver.getLaserReceiver(colourconverter.getBlockMetadata()).passLaser(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()], laserInGame);
    			}
    		}
        }
    }
	
	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        return (dir == NORTH && world.isSideSolid(x, y, z + 1, NORTH)) ||
               (dir == SOUTH && world.isSideSolid(x, y, z - 1, SOUTH)) ||
               (dir == WEST  && world.isSideSolid(x + 1, y, z, WEST)) ||
               (dir == EAST  && world.isSideSolid(x - 1, y, z, EAST)) ||
               (dir == UP  && world.isSideSolid(x, y - 1, z, UP)) ||
               (dir == DOWN  && world.isSideSolid(x, y + 1, z, DOWN));
    }

	@Override
    public boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_) {
        return (p_149742_1_.isSideSolid(p_149742_2_ - 1, p_149742_3_, p_149742_4_, EAST)) ||
               (p_149742_1_.isSideSolid(p_149742_2_ + 1, p_149742_3_, p_149742_4_, WEST)) ||
               (p_149742_1_.isSideSolid(p_149742_2_, p_149742_3_, p_149742_4_ - 1, SOUTH)) ||
               (p_149742_1_.isSideSolid(p_149742_2_, p_149742_3_, p_149742_4_ + 1, NORTH)) ||
               (p_149742_1_.isSideSolid(p_149742_2_, p_149742_3_ - 1, p_149742_4_, UP)) ||
               (p_149742_1_.isSideSolid(p_149742_2_, p_149742_3_ + 1, p_149742_4_, DOWN));
    }
	
	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
        return this.canPlaceBlockOnSide(world, x, y, z, world.getBlockMetadata(x, y, z));
    }

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		if (!world.isRemote) {
			TileEntitySmallColourConverter colourconverter = (TileEntitySmallColourConverter)world.getTileEntity(x, y, z);
			BlockActionPos reciver = LaserUtil.getFirstBlock(colourconverter, colourconverter.getBlockMetadata());
			if(reciver != null && reciver.isLaserReciver(colourconverter.getBlockMetadata())) {
				LaserInGame laserInGame = colourconverter.getOutputLaser(colourconverter.getBlockMetadata());
	        	if(colourconverter.laser == null) {
	        		reciver.getLaserReceiver(colourconverter.getBlockMetadata()).removeLasersFromSide(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()]);
	        	}
	        	else if(reciver.getLaserReceiver(colourconverter.getBlockMetadata()).canPassOnSide(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()], laserInGame)) {
	        		reciver.getLaserReceiver(colourconverter.getBlockMetadata()).passLaser(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()], laserInGame);
				}
			}
        }
		
		if(!this.canBlockStay(world, x, y, z)) {
			this.dropBlockAsItem(world, x, y, z, 0, 0);
			world.setBlockToAir(x, y, z);
        }
    }

	@Override
    public void updateTick(World world, int x, int y, int z, Random random) {
		TileEntitySmallColourConverter colourconverter = (TileEntitySmallColourConverter)world.getTileEntity(x, y, z);
		BlockActionPos reciver = LaserUtil.getFirstBlock(colourconverter, colourconverter.getBlockMetadata());
		if(reciver != null && reciver.isLaserReciver(colourconverter.getBlockMetadata())) {
			LaserInGame laserInGame = colourconverter.getOutputLaser(colourconverter.getBlockMetadata());
        	if(laserInGame == null) {
        		reciver.getLaserReceiver(colourconverter.getBlockMetadata()).removeLasersFromSide(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()]);
        	}
        	else if(reciver.getLaserReceiver(colourconverter.getBlockMetadata()).canPassOnSide(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()], laserInGame)) {
        		reciver.getLaserReceiver(colourconverter.getBlockMetadata()).passLaser(world, x, y, z, Facing.oppositeSide[colourconverter.getBlockMetadata()], laserInGame);
			}
		}
		else if(reciver != null) {
			LaserInGame laserInGame = colourconverter.getOutputLaser(colourconverter.getBlockMetadata());
			
			if(laserInGame != null) {
				for(ILaser laser : laserInGame.getLaserType()) {
					laser.actionOnBlock(reciver);
				}
			}
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xHit, float yHit, float zHit) {
		ItemStack item = player.getCurrentEquippedItem();
		if(!world.isRemote && item != null) {
			if(item.getItem() == Items.dye) {
				TileEntitySmallColourConverter colourConverter = (TileEntitySmallColourConverter)world.getTileEntity(x, y, z);
				
				int colour = 15 - item.getItemDamage();
				if(colour > 15)
					colour = 15;
				else if(colour < 0)
					colour = 0;
				
				if(colour == colourConverter.colour)
					return true;
				
				colourConverter.colour = colour;
				
				if(!player.capabilities.isCreativeMode)
					item.stackSize--;
				if(item.stackSize <= 0)
					player.setCurrentItemOrArmor(0, (ItemStack)null);
				
				LaserMod.NETWORK_MANAGER.sendPacketToAllAround(new PacketSmallColourConverter(colourConverter), world.provider.dimensionId, x + 0.5D, y + 0.5D, z + 0.5D, 512);
				
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isOpaqueCube() {
	    return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
	    return false;
	}

	@Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float p_149660_6_, float p_149660_7_, float p_149660_8_, int meta) {
    	return side;
    }
}
