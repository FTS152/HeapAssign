import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class MyListener extends HeapAssignBaseListener{

	private Map<String, String> DataFlow;
	private Map<String, String> VarToValues;
    
    public MyListener() {
        VarToValues = new HashMap<>();
        DataFlow = new HashMap<>();
    }

	private class Node<T> {
		private T data = null;
		private List<Node<T>> children = new ArrayList<>();
		private Node<T> parent = null;
		public Node(T data) {
			this.data = data;
		}
		public Node<T> addChild(Node<T> child) {
			child.setParent(this);
			this.children.add(child);
			return child;
		}
		public void addChildren(List<Node<T>> children) {
			children.forEach(each -> each.setParent(this));
			this.children.addAll(children);
		}
		public List<Node<T>> getChildren() {
			return children;
		}
		public T getData() {
			return data;
		}
		public void setData(T data) {
			this.data = data;
		}
		private void setParent(Node<T> parent) {
			this.parent = parent;
		}
		public Node<T> getParent() {
			return parent;
		}
	}

    private String ExpressionParse(HeapAssignParser.ExpressionContext ctx) {
		if(ctx.getChildCount()==2){  //object property
			if(VarToValues.get(ctx.VARIABLE().getText()) == null){
				Node<Pair<String,String>> root = new Node<>((ctx.VARIABLE().getText(),null));
			}
		}
		else{
			return  ctx.getText();
		}

	
    }

	@Override
	public void exitAssignment(HeapAssignParser.AssignmentContext ctx) {
		String left = ExpressionParse(ctx.expression(0));
		String right = ctx.expression(1).getText();

		DataFlow.put(left, right);
		System.out.println(left);
		System.out.println(right);
	}

/*
	@Override
	public void exitExpression(HeapAssignParser.ExpressionContext ctx) {
		if(ctx.getChildCount()==2){  //object property

		}
		else{

		}

	
	}

	@Override
	public void exitObj_property(HeapAssignParser.Obj_propertyContext ctx) {

	}

*/
	@Override
	public void exitProg(HeapAssignParser.ProgContext ctx) {
		System.out.println(VarToValues);
		System.out.println(DataFlow);		
	}

}