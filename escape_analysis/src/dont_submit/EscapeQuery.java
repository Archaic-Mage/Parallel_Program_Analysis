package dont_submit;

public class EscapeQuery {
	String className;
	String methodName;
	
	public EscapeQuery(String className, String methodName) {
		super();
		this.className = className;
		this.methodName = methodName;
		
	}
	public String getClassName() {
		return className;
	}
	public String getMethodName() {
		return methodName;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Can the synchronization construct be removed in ");
		sb.append(className).append(":").append(methodName).append("?");
		return sb.toString();
	}
	
}
