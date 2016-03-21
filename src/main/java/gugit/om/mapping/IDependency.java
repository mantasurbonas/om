package gugit.om.mapping;

/***
 * abstraction for "dependence upon some unresolved condition" relationship.
 * 
 * implementations shall return: 
 *    null if the condition is still unresolved, 
 *    or some resolution state. 
 * 
 * @author urbonman
 *
 */
public interface IDependency{
	
	public Object solve(Object param);
	
}
