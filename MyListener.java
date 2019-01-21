import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class MyListener extends HeapAssignBaseListener{

	private Map<String, String> VarToValues;
	private Vector<Pair<String,String>> DataFlow;
	private Pair<String, String> pair;
	private ArrayList<String> variables;
    
    public MyListener() {
        VarToValues = new HashMap<>();
        DataFlow = new Vector<>();
        pair = new Pair<String,String>(null,null);
        variables = new ArrayList<String>();
    }

/*
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
*/

	private String Obj_parse(HeapAssignParser.Obj_propertyContext ctx){
		if(ctx.obj_property(0).getChildCount()==0){
			return '_' + ExpressionParse(ctx.expression(0));
		}
		else{
			return '_' + ExpressionParse(ctx.expression(0)) + Obj_parse(ctx.obj_property(0)); //recursive
		}
	}

    private String ExpressionParse(HeapAssignParser.ExpressionContext ctx) {
		if(ctx.getChildCount()==2){  //object property
			String obj = ctx.VARIABLE().getText() + Obj_parse(ctx.obj_property());
			if(VarToValues.get(obj) == null){
				VarToValues.put(obj,null);
			}
			return obj;
		}
		else{
			String exp = null;
			try{  //variable
				exp = ctx.VARIABLE().getText();
				if(VarToValues.get(exp) == null){
					VarToValues.put(exp,null);
				}
			}
			catch (NullPointerException e){}			
			try{  //constant
				exp = ctx.CONSTANT().getText();
			}
			catch (NullPointerException e){}
			return  exp;
		}
    }

    private void AssignmentFrom(String left, HeapAssignParser.ExpressionContext right) {
		if(right.getChildCount()==2){  //object property
			String obj = right.VARIABLE().getText() + Obj_parse(right.obj_property());
			if(VarToValues.get(obj) == null){
				VarToValues.put(obj,null);
			}
			VarToValues.put(left, VarToValues.get(obj));
			pair = new Pair<>(left,obj);
			DataFlow.addElement(pair);
		}
		else{
			try{  //variable
				String exp = right.VARIABLE().getText();
				if(VarToValues.get(exp) == null){
					VarToValues.put(exp,null);
				}
				VarToValues.put(left, VarToValues.get(exp));
				pair = new Pair<>(left,exp);
				DataFlow.addElement(pair);
			}
			catch (NullPointerException e){}			
			try{  //constant
				String exp = right.CONSTANT().getText();
				VarToValues.put(left, exp);
			}
			catch (NullPointerException e){}
			try{  //concat
				HeapAssignParser.ExpressionContext exp1 = right.concat().expression(0);
				HeapAssignParser.ExpressionContext exp2 = right.concat().expression(1);
				AssignmentFrom(left,exp1);
				AssignmentFrom(left,exp2);
			}
			catch (NullPointerException e){}
		}  	
    }

    private Boolean isConstant(String prop){
    	if(prop.charAt(0)=='\"'&&prop.charAt(prop.length()-1)=='\"')
    		return true;
    	else
    		return false;
    }

	@Override
	public void exitAssignment(HeapAssignParser.AssignmentContext ctx) {
		String left = ExpressionParse(ctx.expression(0));
		HeapAssignParser.ExpressionContext right = ctx.expression(1);
		AssignmentFrom(left,right);
	}

	@Override
	public void exitProg(HeapAssignParser.ProgContext ctx) {
		for (String key : VarToValues.keySet())
			variables.add(key);
		for (int i = 0; i < variables.size(); i++){
			for (int j = i+1; j < variables.size(); j++){
				String[] var1 = (variables.get(i)).split("_");
				String[] var2 = (variables.get(j)).split("_");
				if(var1.length==var2.length){
					for(int k = 0; k < var1.length; k++){
						if( !var1[k].equals(var2[k]) && !(isConstant(var1[k]) ^ isConstant(var2[k])) ){
							break;
						}
						if(k==var1.length-1){
							pair = new Pair<>(variables.get(i),variables.get(j));
							DataFlow.addElement(pair);
							pair = new Pair<>(variables.get(j),variables.get(j));
							DataFlow.addElement(pair);
						}
					}
				}
			}
		}

		System.out.println(VarToValues);
		System.out.println(DataFlow);		
	}

}