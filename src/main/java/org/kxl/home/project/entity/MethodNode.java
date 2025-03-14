package org.kxl.home.project.entity;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * 这是画图需要的node描述
 */
@Data
public class MethodNode {

    private String method;

    private List<MethodNode> parents = null;

    private List<MethodNode> children = null;

    public MethodNode(String method) {
        this.method = method;
    }

    public void addParent(MethodNode parent) {
        if(this.parents == null) {
            this.parents = new java.util.ArrayList<>();
        }
        if(!this.parents.contains(parent)) {
            this.parents.add(parent);
        }
    }

    public void addChild(MethodNode child) {
        if(this.children == null) {
            this.children = new java.util.ArrayList<>();
        }
        if(!this.children.contains(child)) {
            this.children.add(child);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        MethodNode that = (MethodNode)o;
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method);
    }

    @Override
    public String toString() {
        return method;
    }
}
