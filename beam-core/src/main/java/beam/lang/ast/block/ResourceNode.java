package beam.lang.ast.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import beam.core.diff.DiffableField;
import beam.core.diff.DiffableType;
import beam.lang.BeamLanguageException;
import beam.lang.Credentials;
import beam.lang.Resource;
import beam.lang.ast.Node;
import beam.lang.ast.scope.ResourceScope;
import beam.lang.ast.scope.Scope;
import beam.parser.antlr4.BeamParser;

public class ResourceNode extends BlockNode {

    private final String type;
    private final Node nameNode;

    public ResourceNode(String type, Node nameNode, List<Node> body) {
        super(body);

        this.type = type;
        this.nameNode = nameNode;
    }

    public ResourceNode(BeamParser.ResourceContext context) {
        super(context.resourceBody()
                .stream()
                .map(c -> Node.create(c.getChild(0)))
                .collect(Collectors.toList()));

        type = context.resourceType().IDENTIFIER().getText();
        nameNode = Node.create(context.resourceName().getChild(0));
    }

    @Override
    public Object evaluate(Scope scope) throws Exception {
        String name = (String) nameNode.evaluate(scope);
        ResourceScope resourceScope = new ResourceScope(scope);

        Optional.ofNullable(scope.getRootScope().getCurrent())
                .map(s -> s.findResource(name))
                .ifPresent(r -> {
                    for (DiffableField f : DiffableType.getInstance(r.getClass()).getFields()) {
                        if (!f.isSubresource()) {
                            resourceScope.put(f.getBeamName(), f.getValue(r));
                        }
                    }
                });

        for (Node node : body) {
            node.evaluate(resourceScope);
        }

        Resource resource = createResource(scope, type);

        resource.resourceIdentifier(name);
        resource.scope(resourceScope);

        // Find subresources.
        for (Map.Entry<String, Object> entry : resourceScope.entrySet()) {
            String subresourceType = type + "::" + entry.getKey();

            if (scope.getRootScope().getResourceClasses().get(subresourceType) != null) {
                Object value = entry.getValue();

                if (!(value instanceof List)) {
                    throw new IllegalArgumentException();
                }

                List<Resource> subresources = new ArrayList<>();

                for (ResourceScope subresourceScope : (List<ResourceScope>) value) {
                    Resource subresource = createResource(scope, subresourceType);

                    subresource.parent(resource);
                    subresource.resourceType(entry.getKey());
                    subresource.scope(subresourceScope);
                    subresource.initialize(subresourceScope);

                    subresources.add(subresource);
                }

                entry.setValue(subresources);
            }
        }

        resource.initialize(resourceScope);

        if (resource instanceof Credentials) {
            scope.getRootScope().getCredentialsMap().put(name, (Credentials) resource);

        } else {
            scope.getFileScope().getResources().put(name, resource);
        }

        return null;
    }

    @Override
    public void buildString(StringBuilder builder, int indentDepth) {
        buildNewline(builder, indentDepth);
        builder.append(type);

        if (nameNode != null) {
            builder.append(' ');
            builder.append(nameNode);
        }

        buildBody(builder, indentDepth + 1, body);

        buildNewline(builder, indentDepth);
        builder.append("end");
    }

    private Resource createResource(Scope scope, String type) {
        Class klass = scope.getRootScope().getResourceClasses().get(type);
        if (klass != null) {
            try {
                beam.lang.Resource resource = (beam.lang.Resource) klass.newInstance();
                resource.resourceType(type);

                return resource;
            } catch (InstantiationException | IllegalAccessException ex) {
                throw new BeamLanguageException("Unable to instantiate " + klass.getClass().getSimpleName());
            }
        }

        throw new BeamLanguageException("Unknown resource type: " + type);
    }

}
