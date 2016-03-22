package gugit.om.mapping;

import gugit.om.metadata.IEntityNameProvider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/***
 * A collection of WritePackets, as produced by the calls to AbstractWriter::write(entity)
 * 
 * @author urbonman
 *
 */
public class WriteBatch {

	private List<WritePacket> writePackets = new LinkedList<WritePacket>();
	
	private Set<Object> scheduledEntities = new HashSet<Object>();

	private IEntityNameProvider entityNameProvider;
	
	public WriteBatch(IEntityNameProvider entityNameProvider){
		this.entityNameProvider = entityNameProvider;
	}
	
	public boolean isEntityScheduledAlready(Object entity) {
		return scheduledEntities.contains(entity);
	}
	
	public WritePacket createWritePacket(Object entity){
		WritePacket d = new WritePacket(entity, entityNameProvider.getEntityName(entity.getClass()));
		writePackets.add(d);
		
		scheduledEntities.add(entity);
		return d;
	}
	
	public WritePacket getNext() {
		Iterator<WritePacket> i = writePackets.iterator();
		while(i.hasNext()){
			WritePacket writePacket = i.next();
			
			if (!writePacket.trySolveDependencies())
				continue;
			
			i.remove();
			
			return writePacket;
		}
		
		if (writePackets.size() == 0)
			return null;
		
		throw new RuntimeException(writePackets.size()+" entity(ies) could not be persisted due to unresolved (or circular) dependencies.");
	}

}
