package beam.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class BeamReference extends BeamLiteral {

    private List<BeamConfigKey> scopeChain;

    private List<String> referenceChain;

    private Object value;

    public BeamReference() {
    }

    public BeamReference(List<BeamConfigKey> scopeChain, String chain) {
        this.scopeChain = scopeChain;
        this.referenceChain = parseReferenceChain(chain);
    }

    public List<BeamConfigKey> getScopeChain() {
        if (scopeChain == null) {
            scopeChain = new ArrayList<>();
        }

        return scopeChain;
    }

    public void setScopeChain(List<BeamConfigKey> scopeChain) {
        this.scopeChain = scopeChain;
    }

    public List<String> getReferenceChain() {
        if (referenceChain == null) {
            referenceChain = new ArrayList<>();
        }

        return referenceChain;
    }

    public void setReferenceChain(List<String> referenceChain) {
        this.referenceChain = referenceChain;
    }

    public List<String> parseReferenceChain(String chain) {
        if (chain == null) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(chain.split("\\."));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('$');
        sb.append('(');
        for (BeamConfigKey scope : getScopeChain()) {
            sb.append(scope);
            sb.append(" | ");
        }
        if (!getReferenceChain().isEmpty()) {
            sb.append(String.join(".", getReferenceChain()));
        } else {
            sb.setLength(sb.length() - 3);
        }

        sb.append(')');
        return sb.toString();
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean resolve(BeamConfig config) {
        BeamResolvable resolvable = config;
        for (BeamConfigKey scope : getScopeChain()) {
            if (config.getContext().containsKey(scope)) {
                resolvable = config.getContext().get(scope);
                if (resolvable instanceof BeamConfig) {
                    config = (BeamConfig) resolvable;
                } else {
                    throw new BeamLangException(String.format("Unable to resolve %s, expecting %s as BeamConfig found %s", this, scope, resolvable.getClass()));
                }
            }
        }

        if (resolvable == null) {
            return false;
        }

        BeamResolvable nextResolvable = resolvable;
        for (String key : getReferenceChain()) {
            if (nextResolvable instanceof BeamCollection) {
                nextResolvable = ((BeamCollection) nextResolvable).get(key);
            } else {
                throw new BeamLangException(String.format("Illegal reference %s, %s is not a collection", this, nextResolvable));
            }

            if (nextResolvable == null || nextResolvable.getValue() == null) {
                return false;
            }
        }

        if (value != null && value.getClass() == nextResolvable.getValue().getClass()) {
            return false;
        } else {
            value = nextResolvable.getValue();
            return true;
        }
    }

    @Override
    public Set<BeamReference> getDependencies(BeamConfig config) {
        Set<BeamReference> dependencies = new HashSet<>();
        if (getValue() != null) {
            return dependencies;
        }

        BeamResolvable resolvable = config;
        for (BeamConfigKey scope : getScopeChain()) {
            if (config.getContext().containsKey(scope)) {
                resolvable = config.getContext().get(scope);
                if (resolvable instanceof BeamConfig) {
                    config = (BeamConfig) resolvable;
                } else {
                    throw new BeamLangException(String.format("Unable to resolve %s, expecting %s as BeamConfig found %s", this, scope, resolvable.getClass()));
                }
            }
        }

        if (resolvable == null) {
            throw new BeamLangException("Unable to find BeamConfig");
        }

        BeamReference reference = new BeamReference();
        reference.setScopeChain(getScopeChain());
        reference.value = resolvable;

        BeamResolvable nextResolvable = resolvable;
        for (String key : getReferenceChain()) {
            if (nextResolvable instanceof BeamCollection) {
                nextResolvable = ((BeamCollection) nextResolvable).get(key);
            } else {
                throw new BeamLangException(String.format("Illegal reference %s, %s is not a collection", this, nextResolvable));
            }

            if (nextResolvable == null || nextResolvable.getValue() == null) {
                dependencies.add(reference);
            }
        }

        return dependencies;
    }
}
