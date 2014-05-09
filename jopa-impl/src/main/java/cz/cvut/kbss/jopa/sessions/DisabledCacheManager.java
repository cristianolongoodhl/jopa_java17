package cz.cvut.kbss.jopa.sessions;

import java.net.URI;
import java.util.Set;

public class DisabledCacheManager implements CacheManager {

	@Override
	public boolean contains(Class<?> cls, Object primaryKey) {
		return false;
	}

	@Override
	public void clearInferredObjects() {
		// Do nothing
	}

	@Override
	public boolean acquireReadLock() {
		return true;
	}

	@Override
	public void releaseReadLock() {
		// Do nothing
	}

	@Override
	public boolean acquireWriteLock() {
		return true;
	}

	@Override
	public void releaseWriteLock() {
		// Do nothing
	}

	@Override
	public void setInferredClasses(Set<Class<?>> inferredClasses) {
		// Do nothing
	}

	@Override
	public boolean contains(Class<?> cls, Object primaryKey, URI context) {
		return false;
	}

	@Override
	public void evict(Class<?> cls, Object primaryKey, URI context) {
		// Do nothing
	}

	@Override
	public void evict(Class<?> cls) {
		// Do nothing
	}

	@Override
	public void evict(URI contextUri) {
		// Do nothing
	}

	@Override
	public void evictAll() {
		// Do nothing
	}

	@Override
	public void add(Object primaryKey, Object entity, URI context) {
		// Do nothing
	}

	@Override
	public <T> T get(Class<T> cls, Object primaryKey, URI context) {
		return null;
	}
}
