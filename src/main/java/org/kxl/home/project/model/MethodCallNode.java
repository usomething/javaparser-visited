package org.kxl.home.project.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.kxl.home.project.entity.MethodCall;

import java.util.List;

@Data
@NoArgsConstructor
public class MethodCallNode {

    private MethodCall self;

    private MethodCallNode parent;

    private List<MethodCallNode> children;
    //key代表是第几个上级调用者，value代表有几个子
    private Pair<Integer,Integer> coord;

    public void addChild(MethodCallNode child) {
        if(this.children == null) {
            this.children = new java.util.ArrayList<>();
        }
        if(!this.children.contains(child)) {
            this.children.add(child);
        }
    }

    @Override
    public String toString() {
        return self.getCallClassMethod();
    }
}
