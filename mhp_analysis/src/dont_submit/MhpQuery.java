package dont_submit;

public class MhpQuery {
	String leftVar;
	String leftField;
	String rightVar;
	String rightField;
	
	
	
	public MhpQuery(String leftVar, String leftField, String rightVar, String rightField) {
		super();
		this.leftVar = leftVar;
		this.leftField = leftField;
		this.rightVar = rightVar;
		this.rightField = rightField;
	}



	public String getLeftVar() {
		return leftVar;
	}



	public String getLeftField() {
		return leftField;
	}



	public String getRightVar() {
		return rightVar;
	}



	public String getRightField() {
		return rightField;
	}



	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Can the accesses to fields "+leftVar+"."+leftField+" and "+rightVar+"."+rightField+" lead to data race?");
		
		return sb.toString();
	}
	
}
