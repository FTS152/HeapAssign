import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class MyListener extends HeapAssignBaseListener{

	private Map<String, Integer> variables;
    
    public MyListener() {
        variables = new HashMap<>();
    }

	@Override
	public void exitAssignment(HeapAssignParser.AssignmentContext ctx) {
		Token left = ctx.expression(0).getTokens();
		Token right = ctx.expression(1).getStart();
		System.out.println(left);
		System.out.println(right);

	}

}