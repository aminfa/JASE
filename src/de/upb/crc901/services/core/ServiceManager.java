package de.upb.crc901.services.core;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jaicore.basic.FileUtil;

public class ServiceManager {
	
	private final static ServiceManager singlton = new ServiceManager();
	
	private final static int maxChanceCachedService = 20; 
	
	private static List<ServiceData> cachedServices = new ArrayList<>();
	
	class ServiceData {
		final ServiceHandle handler;
		private int chance;
		public ServiceData(ServiceHandle h) {
			this.handler = h;
			resetChance();
		}
		public void resetChance() {
			chance = 0;
		}
		public void addChance() {
			chance++;
		}
		public boolean hasChanceLeft() {
			return chance < maxChanceCachedService;
		}
	}
	
	private void maintainCache() {
		Iterator<ServiceData> iterator = cachedServices.iterator();
		while(iterator.hasNext()) {
			ServiceData cachedService = iterator.next();
			if(cachedService.hasChanceLeft()) {
				cachedService.addChance();
			} else {
				iterator.remove();
				// write to disk
				Object service = cachedService.handler.getService();
				if (service instanceof Serializable) {  
					/* serialize result */
					try {
						FileUtil.serializeObject(service, 
								getServicePath(cachedService.handler.getClasspath(), cachedService.handler.getId()));
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
	
	private void removeServiceWithId(String id) {
		Iterator<ServiceData> iterator = cachedServices.iterator();
		while(iterator.hasNext()) {
			ServiceData cachedService = iterator.next();
			if(cachedService.handler.getId().equals(id)) {
				// same id. remove cached service:
				iterator.remove();
			}
		}
	}
	
	public synchronized void addService(ServiceHandle handle) {
		maintainCache();
		// Delete already already cached services
		removeServiceWithId(handle.getId());
		// add the e
		ServiceData newCachedService = new ServiceData(handle);
		cachedServices.add(newCachedService);
	}
	
	public synchronized ServiceHandle getHandle(String classpath, String id_) throws ClassNotFoundException, IOException {
		for(ServiceData cachedService : cachedServices) {
			if(cachedService.handler.getId().equals(id_)) {
				cachedService.resetChance();
				return cachedService.handler;
			}
		}
		// wasn't found in cache.
		Object service = FileUtil.unserializeObject(getServicePath(classpath, id_));
		ServiceHandle sh = new ServiceHandle(classpath, id_).withService(service);
		addService(sh);
		return sh;
//		throw new RuntimeException("This service is not cached anymore: " + classpath + ":" + id_ + ". nor was it found on disk.");
	}
	
	
	public static ServiceManager SINGLETON() {
		return singlton;
	}
	
	/**
	 * Creates the file path for the given classpath and serviceid.
	 * @param serviceClasspath classpath of the service.
	 * @param serviceId id of the service.
	 * @return file path to the service.
	 */
	private String getServicePath(String serviceClasspath, String serviceId) {
		return "http" + File.separator + "objects" + File.separator + serviceClasspath + File.separator + serviceId;
	}

	
}
