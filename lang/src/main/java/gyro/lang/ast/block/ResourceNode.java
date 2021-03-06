/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.lang.ast.block;

import java.util.List;

import com.google.common.base.Preconditions;
import gyro.lang.ast.Node;
import gyro.lang.ast.NodeVisitor;
import gyro.parser.antlr4.GyroParser;

public class ResourceNode extends BlockNode {

    private final String type;
    private final Node name;

    public ResourceNode(String type, Node name, List<Node> body) {
        super(null, body);

        this.type = Preconditions.checkNotNull(type);
        this.name = Preconditions.checkNotNull(name);
    }

    public ResourceNode(GyroParser.ResourceContext context) {
        super(Preconditions.checkNotNull(context), Node.create(context.body()));

        this.type = context.type().getText();
        this.name = Node.create(context.name());
    }

    public String getType() {
        return type;
    }

    public Node getName() {
        return name;
    }

    @Override
    public <C, R, X extends Throwable> R accept(NodeVisitor<C, R, X> visitor, C context) throws X {
        return visitor.visitResource(this, context);
    }

}
