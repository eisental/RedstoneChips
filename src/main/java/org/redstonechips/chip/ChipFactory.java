package org.redstonechips.chip;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.redstonechips.CircuitLoader;
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
 *
 * @author taleisenberg
 */
public class ChipFactory {   

    public enum MaybeChip { 
        NotAChip, ChipError, AChip;
        
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
     
        public Chip getChip() { return chip; }
        public String getError() { return error; }
    }
        
    /**
     * Scans a chip starting at params.signBlock, debug level set to 0.
     * 
     * @param params An initialized ScanParameters object.
     * @param activator The circuit activator
     * @return The new chip id or -1 if a chip was not found or -2 if an error occurred.
     */
    public static MaybeChip maybeCreateChip(ChipParameters params, CommandSender activator) {
        return maybeCreateChip(params, activator, 0);
    }
    
    public static MaybeChip maybeCreateChip(ChipParameters params, CommandSender activator, int debugLevel) {
        RedstoneChips rc = RedstoneChips.inst();
        ChatColor infoColor = rc.prefs().getInfoColor();
        ChatColor errorColor = rc.prefs().getErrorColor();
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
        
        if (!rc.permissionManager().checkChipPermission(activator, signClass, true)) {
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
        
        return maybeInstantiateChip(params, chips, 
                                    signClass, 
                                    Signs.readArgsFromSign(sign));
    }
    
    private static void scan(ChipParameters params, CommandSender debugger, int debugLevel) throws ChipScanException {
        IOChipScanner scanner = new RecursiveChipScanner();
        scanner.setDebugger(debugger, debugLevel);
        
        if (scanner.isTypeAllowed(params, params.chipMaterial, params.origin.getData())) {
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

    public static MaybeChip maybeInstantiateChip(ChipParameters params, ChipCollection chips, String type, String[] args) {
        try {
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

            Circuit cr = CircuitLoader.getCircuitInstance(type);
            cr.constructWith(c, c);
            c.circuit = cr;
            
            return MaybeChip.AChip.withChip(c);
            
        } catch (InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
            return MaybeChip.ChipError.withError(ex.toString());
        } catch (IllegalArgumentException ex) {
            // unknown circuit name. shouldn't happen at this stage.
            return MaybeChip.ChipError.withError(ex.toString());
        }

 
    }
    
    /**
     * Finds the circuit's chunks according to its activation block, output power blocks, input power blocks and interface blocks.
     *
     * @param c The circuit to check.
     * @return All chunks used by this circuit.
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
