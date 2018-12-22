package beam.core;

import beam.lang.nodes.ContainerNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class BeamLocalState extends BeamState {

    @Override
    public ContainerNode load(String name, BeamCore core) throws IOException {

        File stateFile = new File(name);
        if (stateFile.exists() && !stateFile.isDirectory()) {
            return core.parse(name);
        } else {
            return new ContainerNode();
        }
    }

    @Override
    public void save(String name, ContainerNode block) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(name));
            out.write(block.toString());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String name) {

    }

    @Override
    public void execute() {

    }

}
