package gyro.core.workflow;

import gyro.lang.ast.Node;
import gyro.lang.ast.block.ResourceNode;
import gyro.core.scope.Scope;

public class Transition {

    private final String name;
    private final String to;
    private final String description;

    public Transition(Scope parent, ResourceNode node) throws Exception {
        Scope scope = new Scope(parent);

        for (Node item : node.getBody()) {
            item.evaluate(scope);
        }

        name = (String) node.getNameNode().evaluate(parent);
        to = (String) scope.get("to");
        description = (String) scope.get("description");
    }

    public String getTo() {
        return to;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
