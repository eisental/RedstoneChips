package org.redstonechips.chip;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.redstonechips.RCPermissions;
import org.redstonechips.RCPrefs;
import org.redstonechips.circuit.CircuitLoader;
import org.redstonechips.RedstoneChips;
import org.redstonechips.chip.io.InputPin;
import org.redstonechips.chip.io.InterfaceBlock;
import org.redstonechips.chip.io.OutputPin;
import org.redstonechips.chip.scan.ChipScanner.ChipScanException;
import org.redstonechips.chip.scan.IOChipScanner;
import org.redstonechips.chip.scan.RecursiveChipScanner;
import org.redstonechips.chip.scan.ChipParameters;
import org.redstonechips.chip.scan.SingleBlockChipScanner;
import org.redstonechips.util.ChunkLocation;
import org.redstonechips.util.Signs;

/**
 * A static class responsible for creating new chips.
 * 
 * @author taleisenberg
 */
public class ChipFactory {   

    /**
     * A result object for chip factory methods.
     */
    public enum MaybeChip { 
        /**
         * A chip could not be created since there was no chip found at the location.
         */
        NotAChip, 
        /**
         * A chip could not be created since an error was encountered while creating the chip.
         * Use getError() to retrieve the error message.
         */
        ChipError, 
        /**
         * A chip was created. Use getChip() to retrieve the chip instance.
         */
        AChip;
        
        private Chip chip = null;
        private String error = null;
        
        MaybeChip withChip(Chip c) {
            chip = c;
            return this;
        }
        
        MaybeChip withError(String error) {
            this.error = error;
            return this;
        }
     
        /**
         * 
         * @return The new chip instance or null if the result was not MaybeChip.AChip.
         */
        public Chip getChip() { return chip; }
        
        /**
         * 
         * @return The chip error when the result is MaybeChip.ChipError.
         */
        public String getError() { return error; }
    }
        
    private ChipFactory() {}
    
    /**
     * Try to create a chip according to params. Debug level is set to 0.
     * 
     * @param params An initialized ChipParameters object.
     * @param activator The chip activator
     * @return A {@link MaybeChip} object representing the result.
     */
    public static MaybeChip maybeCreateChip(ChipParameters params, CommandSender activator) {
        return maybeCreateChip(params, activator, 0);
    }
    
    /**
     * Try to create a chip according to params. 
     * 
     * @param params An initialized ChipParameters object.
     * @param activator The chip activator
     * @param debugLevel When greater than 0, activator will receive various debug messages when the chip is scanned.
     * @return A {@link MaybeChip} object representing the result.
     */
    public static MaybeChip maybeCreateChip(ChipParameters params, CommandSender activator, int debugLevel) {
        RedstoneChips rc = RedstoneChips.inst();
        ChatColor infoColor = RCPrefs.getInfoColor();
        ChatColor errorColor = RCPrefs.getErrorColor();
        ChipCollection chips = rc.chipManager().getAllChips();
        
        if (params==null) return MaybeChip.NotAChip;
        
        Block signBlock = params.signBlock;        
        BlockState state = signBlock.getState();
        
        if (!(state instanceof Sign)) 
            return MaybeChip.NotAChip;

        Sign sign = (Sign)state;
        String signClass = Signs.readClassFromSign(sign);

        // check if the sign text points to a known circuit type.
        if (!CircuitLoader.getCircuitClasses().containsKey(signClass)) return MaybeChip.NotAChip;
        // check if it belongs to an active chip.
        Chip existingChip = chips.getByActivationBlock(signBlock.getLocation());
        if (existingChip != null) {
            return MaybeChip.ChipError.withError(infoColor + "Chip is already active - " + existingChip + ".");
        }
        
        if (!RCPermissions.checkChipPermission(activator, signClass, true)) {
            return MaybeChip.ChipError.withError(errorColor + "You do not have permission to create circuits of type " + signClass + ".");
        }

        try { // Only scan if there are no detected IO blocks in params.
            if ((params.outputs.isEmpty() && params.inputs.isEmpty() && params.interfaces.isEmpty()))
                scan(params, activator, debugLevel);
        } catch (ChipScanException e) {
            return MaybeChip.ChipError.withError(errorColor + e.getMessage());
        }
        
        // make sure a chip without any IO is not created.
        if ((params.outputs.isEmpty() && params.inputs.isEmpty() && params.interfaces.isEmpty())) return MaybeChip.NotAChip;

        for (Block b : params.structure) {
            Chip c = chips.getByStructureBlock(b.getLocation());
            if (c!=null) {
                String error = errorColor + "One of the chip blocks (" + 
                        infoColor + b.getType().name().toLowerCase() + 
                        errorColor + ") already belongs to another chip: " + 
                        infoColor + c.toString();
                return MaybeChip.ChipError.withError(error);
            }
        }
        
        return MaybeChip.AChip.withChip(
                instantiateChip(params, chips, signClass, Signs.readArgsFromSign(sign)));
    }

    /**
     * Instantiate a {@link org.redstonechips.chip.Chip Chip} object according to 
     * Various parameters. The validity of the parameters is not checked or enforced.
     * The new chip does not have a {@link org.redstonechips.circuit.Circuit Circuit} attached yet.
     * 
     * @param params An initialized ChipParameters object, representing a scanned chip.
     * @param chips All chips running on the server.
     * @param type The chip type.
     * @param args The sign arguments of the chip.
     * @return A new Chip object.
     */
    public static Chip instantiateChip(ChipParameters params, ChipCollection chips, String type, String[] args) {
        Chip c = new Chip();
        c.setType(type);

        c.world = params.signBlock.getWorld();

        c.activationBlock = params.signBlock.getLocation();

        c.structure = new Location[params.structure.size()];
        for (int i = 0; i < params.structure.size(); i++) {
            c.structure[i] = params.structure.get(i).getLocation();
        }

        c.inputPins = new InputPin[params.inputs.size()];
        for (int i = 0; i < params.inputs.size(); i++) {
            Location l = params.inputs.get(i).getLocation();
            InputPin ip = new InputPin(c, l, i);
            c.inputPins[i] = ip;
        }

        c.outputPins = new OutputPin[params.outputs.size()];
        for (int i = 0; i < params.outputs.size(); i++) {
            Location l = params.outputs.get(i).getLocation();
            OutputPin op = new OutputPin(c, l, i);
            c.outputPins[i] = op;
        }

        c.interfaceBlocks = new InterfaceBlock[params.interfaces.size()];
        for (int i = 0; i < params.interfaces.size(); i++) {
            InterfaceBlock ib = new InterfaceBlock(c, params.interfaces.get(i).getLocation(), i);
            c.interfaceBlocks[i] = ib;
        }

        c.chunks = findChipChunks(c);

        c.args = args;

        return c;
    }
    
    /**
     * Scans a chip, populating params with discovered values.
     * @param params An initialized ChipParameters object.
     * @param debugger A command sender that will receive debug messages while scanning.
     * @param debugLevel The amount of debug messages. When greater than 0 messages are sent to debugger.
     * 
     * @throws org.redstonechips.chip.scan.ChipScanner.ChipScanException 
     */
    private static void scan(ChipParameters params, CommandSender debugger, int debugLevel) throws ChipScanException {
        IOChipScanner scanner = new RecursiveChipScanner();
        scanner.setDebugger(debugger, debugLevel);
        
        if (scanner.isTypeAllowed(params, params.chipMaterial)) {
            scanner.scan(params);
        } else {
            try { // a Single-block chip
                scanner = new SingleBlockChipScanner();
                scanner.setDebugger(debugger, debugLevel);
                scanner.scan(params);
            } catch (ChipScanException e) { // fail
                throw new ChipScanException("You can't build a chip using this material (" + params.chipMaterial.name() + ").");
            }
        }
    }
    
    /**
     * Finds the chip chunks according to its activation block, output power blocks, input power blocks and interface blocks.
     *
     * @param c A chip to check.
     * @return All chunks used by this chip.
     */
    private static ChunkLocation[] findChipChunks(Chip c) {
        List<ChunkLocation> circuitChunks = new ArrayList<>();

        circuitChunks.add(ChunkLocation.fromLocation(c.activationBlock));
        
        for (OutputPin output : c.outputPins) {
            for (Location out : output.getOutputBlocks()) {
                ChunkLocation chunk = ChunkLocation.fromLocation(out);

                if (!circuitChunks.contains(chunk))
                    circuitChunks.add(chunk);
            }
        }
        
        for (InputPin input : c.inputPins) {
            for (Location in : input.getSourceBlocks()) {
                ChunkLocation chunk = ChunkLocation.fromLocation(in);

                if (!circuitChunks.contains(chunk))
                    circuitChunks.add(chunk);
            }
        }
        
        for (InterfaceBlock interfaceBlock : c.interfaceBlocks) {
            ChunkLocation chunk = ChunkLocation.fromLocation(interfaceBlock.getLocation());
            if (!circuitChunks.contains(chunk))
                circuitChunks.add(chunk);
        }

        return circuitChunks.toArray(new ChunkLocation[circuitChunks.size()]);
    }    
}
