import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;

public class MyListener extends HeapAssignBaseListener{

	private Map<String, ArrayList<String>> VarToValues;
	private Vector<Pair<String,String>> DataFlow;
	private Pair<String, String> pair;
	private ArrayList<String> variables;
	private ArrayList<String> values;
    
    public MyListener() {
        VarToValues = new HashMap<>();
        DataFlow = new Vector<>();
        pair = new Pair<String,String>(null,null);
        variables = new ArrayList<String>();
        values = new ArrayList<String>();
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

    //add a list of values in a variable
    private void valueUpdate(String left,String right){
		values = new ArrayList<String>();
		if(!(VarToValues.get(left)==null))
			values.addAll(VarToValues.get(left));
		if(!(VarToValues.get(right)==null))
			values.addAll(VarToValues.get(right));
		VarToValues.put(left, values);    	
    }

    //add a single value
    private void valueAdd(String left,String exp){
    	values = new ArrayList<String>();
		if(!(VarToValues.get(left)==null))
			values.addAll(VarToValues.get(left));
		values.add(exp);
		VarToValues.put(left,values);
    }

    private void AssignmentFrom(String left, HeapAssignParser.ExpressionContext right) {
		if(right.getChildCount()==2){  //object property
			String obj = right.VARIABLE().getText() + Obj_parse(right.obj_property());
			if(VarToValues.get(obj) == null){
				VarToValues.put(obj,null);
			}
			valueUpdate(left, obj);
			pair = new Pair<>(left,obj);
			DataFlow.addElement(pair);
		}
		else{
			try{  //variable
				String exp = right.VARIABLE().getText();
				if(VarToValues.get(exp) == null){
					VarToValues.put(exp,null);
				}
				valueUpdate(left, exp);
				pair = new Pair<>(left,exp);
				DataFlow.addElement(pair);
			}
			catch (NullPointerException e){}			
			try{  //constant
				String exp = right.CONSTANT().getText();
				valueAdd(left,exp);
			}
			catch (NullPointerException e){}
			try{  //concat
				ArrayList<String> tmp = new ArrayList<String>();
				tmp = VarToValues.get(left);
				HeapAssignParser.ExpressionContext exp1 = right.concat().expression(0);
				HeapAssignParser.ExpressionContext exp2 = right.concat().expression(1);
				AssignmentFrom(left,exp1);
				AssignmentFrom(left,exp2);
				VarToValues.put(left,tmp);

				//record concat value to VarToValues
				ArrayList<String> e1 = new ArrayList<String>();
				ArrayList<String> e2 = new ArrayList<String>();

				try{e1 = VarToValues.get(exp1.VARIABLE().getText());}
				catch (NullPointerException e){}
				try{e1.add(exp1.CONSTANT().getText());}
				catch (NullPointerException e){}
				try{e2 = VarToValues.get(exp2.VARIABLE().getText());}
				catch (NullPointerException e){}
				try{e2.add(exp2.CONSTANT().getText());}
				catch (NullPointerException e){}

				if (e1==null&&!(e2==null))
					for (int i = 0; i < e2.size(); i++)
						valueAdd(left,e2.get(i));
				else if (!(e1==null)&&e2==null)
					for (int i = 0; i < e1.size(); i++)
						valueAdd(left,e1.get(i));
				else if(!(e1==null)&&!(e2==null)){
					ArrayList<String> mix = new ArrayList<String>();
					for (int i = 0; i < e1.size(); i++)
						for (int j = 0; j < e2.size(); j++){
							mix.add(e1.get(i).substring(0,e1.get(i).length()-1)+e2.get(j).substring(1,e2.get(j).length()));
						}
					for (int i = 0; i < tmp.size(); i++)
						valueAdd(left,mix.get(i));
				}
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
				if(var1.length==var2.length && !(var1.length==1)){
					for(int k = 0; k < var1.length; k++){
						if( !var1[k].equals(var2[k]) && (isConstant(var1[k]) && isConstant(var2[k])) ){
							break;
						}
						if(k==var1.length-1){
							pair = new Pair<>(variables.get(i),variables.get(j));
							DataFlow.addElement(pair);
							pair = new Pair<>(variables.get(j),variables.get(i));
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