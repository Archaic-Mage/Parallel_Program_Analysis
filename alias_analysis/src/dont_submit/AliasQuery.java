package dont_submit;

public class AliasQuery {
	String className;
	String methodName;
	String leftVar;
	String rightVar;
	public AliasQuery(String className, String methodName, String leftVar, String rightVar) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.leftVar = leftVar;
		this.rightVar = rightVar;
	}
	public String getClassName() {
		return className;
	}
	public String getMethodName() {
		return methodName;
	}
	public String getLeftVar() {
		return leftVar;
	}
	public String getRightVar() {
		return rightVar;
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(className).append(":").append(methodName).append(":");
		sb.append(leftVar).append(" alias? ").append(rightVar);
		return sb.toString();
	}
	
}
