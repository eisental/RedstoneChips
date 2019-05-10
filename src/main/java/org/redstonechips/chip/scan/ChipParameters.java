package org.redstonechips.chip.scan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.redstonechips.RCPrefs;
import org.redstonechips.chip.Chip;
import org.redstonechips.chip.io.IOBlock;
import org.redstonechips.chip.io.InputPin;
import org.redstonechips.chip.io.InterfaceBlock;
import org.redstonechips.chip.io.OutputPin;

/**
 * Used for passing arguments when creating a new chip.
 * 
 * @author Tal Eisenberg
 */
public class ChipParameters {
	/**
	 * The chip block material of the scanned chip.
	 */
	public Material chipMaterial;

	/**
	 * The wool color in case the chip material is wool.
	 */
//    public DyeColor woolColor;

	/**
	 * The input block material.
	 */
	public Material inputBlockType;

	/**
	 * The output block material.
	 */
	public Material outputBlockType;

	/**
	 * The interface block material.
	 */
	public Material interfaceBlockType;

	/**
	 * The activation sign block
	 */
	public Block signBlock;

	/**
	 * The 1st block right after the activation sign. The block to which the sign is
	 * attached.
	 */
	public Block origin;

	/**
	 * The current scanning direction.
	 */
	public BlockFace direction;

	/**
	 * List of discovered input blocks.
	 */
	public List<Block> inputs;

	/**
	 * List of discovered output blocks.
	 */
	public List<Block> outputs;

	/**
	 * List of discovered interface blocks.
	 */
	public List<Block> interfaces;

	/**
	 * List of all discovered structure blocks. Includes any block that would break
	 * the circuit when broken.
	 */
	public List<Block> structure;

	public static ChipParameters generate(Block signBlock, Map<IOBlock.Type, Material> ioMaterials) {
		if (!(signBlock.getBlockData() instanceof WallSign))
			return null;

		BlockFace direction = ((WallSign) signBlock.getBlockData()).getFacing().getOppositeFace();
		Block firstChipBlock = signBlock.getRelative(direction);
		
		ChipParameters params = new ChipParameters();
		params.signBlock = signBlock;
		params.chipMaterial = firstChipBlock.getType();
		params.inputBlockType = ioMaterials.get(IOBlock.Type.INPUT);
		params.outputBlockType = ioMaterials.get(IOBlock.Type.OUTPUT);
		params.interfaceBlockType = ioMaterials.get(IOBlock.Type.INTERFACE);
		params.origin = firstChipBlock;
		params.inputs = new ArrayList<>();
		params.outputs = new ArrayList<>();
		params.interfaces = new ArrayList<>();
		params.structure = new ArrayList<>();

		params.structure.add(signBlock);
		params.structure.add(firstChipBlock);
		params.direction = direction;

		return params;

	}

	public static ChipParameters generateDefaultParams(Block signBlock) {
		return generate(signBlock, defaultIOMaterials());
	}

	public static Map<IOBlock.Type, Material> defaultIOMaterials() {
		Map<IOBlock.Type, Material> iom = new EnumMap<>(IOBlock.Type.class);
		iom.put(IOBlock.Type.INPUT, RCPrefs.getInputBlockType());
		iom.put(IOBlock.Type.OUTPUT, RCPrefs.getOutputBlockType());
		iom.put(IOBlock.Type.INTERFACE, RCPrefs.getInterfaceBlockType());

		return iom;
	}

	public static ChipParameters generateFromChip(Chip c) {
		ChipParameters params = new ChipParameters();
		params.signBlock = c.activationBlock.getBlock();
		Block firstChipBlock = c.structure[1].getBlock();
		params.chipMaterial = firstChipBlock.getType();
		params.origin = firstChipBlock;

		params.inputs = new ArrayList<>();
		for (InputPin i : c.inputPins)
			params.inputs.add(i.getLocation().getBlock());
		if (!params.inputs.isEmpty())
			params.inputBlockType = params.inputs.get(0).getType();

		params.outputs = new ArrayList<>();
		for (OutputPin o : c.outputPins)
			params.outputs.add(o.getLocation().getBlock());
		if (!params.outputs.isEmpty())
			params.outputBlockType = params.outputs.get(0).getType();

		params.interfaces = new ArrayList<>();
		for (InterfaceBlock i : c.interfaceBlocks)
			params.interfaces.add(i.getLocation().getBlock());
		if (!params.interfaces.isEmpty())
			params.interfaceBlockType = params.interfaces.get(0).getType();

		params.structure = new ArrayList<>();
		for (Location l : c.structure)
			params.structure.add(l.getBlock());
		params.direction = ((org.bukkit.material.Sign) params.signBlock.getState().getData()).getAttachedFace();

		return params;
	}
}
