package gyro.core.diff;

import java.util.Set;
import java.util.stream.Collectors;

import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.workflow.Workflow;
import gyro.core.scope.State;

public class Replace extends Change {

    private final Diffable currentDiffable;
    private final Diffable pendingDiffable;
    private final Set<DiffableField> changedFields;
    private final Workflow workflow;

    public Replace(Diffable currentDiffable, Diffable pendingDiffable, Set<DiffableField> changedFields) {
        this.currentDiffable = currentDiffable;
        this.pendingDiffable = pendingDiffable;
        this.changedFields = changedFields;

        if (pendingDiffable instanceof Resource) {
            Resource pendingResource = (Resource) pendingDiffable;

            this.workflow = pendingResource.scope()
                    .getRootScope()
                    .getWorkflows()
                    .stream()
                    .filter(w -> w.getForType().equals(pendingResource.resourceType()))
                    .findFirst()
                    .orElse(null);
        } else {
            workflow = null;
        }
    }

    @Override
    public Diffable getDiffable() {
        return pendingDiffable;
    }

    private void writeFields(GyroUI ui) {
        if (!ui.isVerbose()) {
            return;
        }

        for (DiffableField field : DiffableType.getInstance(pendingDiffable.getClass()).getFields()) {
            if (!field.shouldBeDiffed()) {
                if (changedFields.contains(field)) {
                    writeDifference(ui, field, currentDiffable, pendingDiffable);

                } else {
                    ui.write("\n· %s: %s",
                            field.getGyroName(),
                            stringify(field.getValue(pendingDiffable)));
                }
            }
        }
    }

    @Override
    public void writePlan(GyroUI ui) {
        ui.write("@|cyan ⇅ Replace %s|@", currentDiffable.toDisplayString());
        ui.write(" (because of %s, ", changedFields.stream()
                .filter(f -> !f.isUpdatable())
                .map(DiffableField::getGyroName)
                .collect(Collectors.joining(", ")));

        if (workflow != null) {
            ui.write("using %s", workflow.getName());

        } else {
            ui.write("skipping without a workflow");
        }

        ui.write(")");
        writeFields(ui);
    }

    @Override
    public void writeExecution(GyroUI ui) {
        ui.write("@|magenta ⇅ Replacing %s|@", currentDiffable.toDisplayString());
        writeFields(ui);
    }

    @Override
    public boolean execute(GyroUI ui, State state) throws Exception {
        if (workflow == null) {
            return false;
        }

        if (ui.isVerbose()) {
            ui.write("\n");
        }

        ui.write("\n@|magenta ~ Executing %s workflow|@", workflow.getName());
        workflow.execute(ui, state, (Resource) pendingDiffable);
        return true;
    }

}
