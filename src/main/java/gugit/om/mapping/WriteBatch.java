package gugit.om.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gugit.om.metadata.IEntityNameProvider;
import gugit.om.wrapping.EntityMarkingHelper;

/***
 * A collection of WritePackets, as produced by the calls to AbstractWriter::write(entity)
 * 
 * @author urbonman
 *
 */
public class WriteBatch {

	private List<IWritePacket> writePackets = new ArrayList<IWritePacket>();
	
	private Set<Object> scheduledEntities = new HashSet<Object>();

	private IEntityNameProvider entityNameProvider;
	
	public WriteBatch(IEntityNameProvider entityNameProvider){
		this.entityNameProvider = entityNameProvider;
	}
	
	public boolean isEntityScheduledAlready(Object entity) {
		return scheduledEntities.contains(entity);
	}
	
	public EntityWritePacket createWritePacket(Object entity){
		EntityWritePacket d = new EntityWritePacket(entity, entityNameProvider.getEntityName(entity.getClass()));
		
		if (EntityMarkingHelper.isDirty(entity))
			writePackets.add(d);
		
		scheduledEntities.add(entity);
		return d;
	}
	
	public M2MWritePacket createManyToManyWritePacket(Object entity, String tableName){
		M2MWritePacket ret = new M2MWritePacket(tableName);
		
		writePackets.add(ret);
		
		return ret;
	}
	
	public IWritePacket getNext() {
		Iterator<IWritePacket> i = writePackets.iterator();
		while(i.hasNext()){
			IWritePacket writePacket = i.next();
			
			if (!writePacket.trySolveDependencies())
				continue;
			
			i.remove();
			
			// EntityMarkingHelper.setDirty(writePacket.getEntity(), false);
			
			return writePacket;
		}
		
		if (writePackets.size() == 0)
			return null;
		
		throw new RuntimeException(writePackets.size()+" entity(ies) could not be persisted due to unresolved (or circular) dependencies.");
	}

}
