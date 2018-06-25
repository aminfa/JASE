package de.upb.crc901.services.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import de.upb.crc901.configurationsetting.compositiondomain.CompositionDomain;
import de.upb.crc901.configurationsetting.operation.OperationInvocation;
import de.upb.crc901.configurationsetting.operation.SequentialComposition;

/**
 * A utility class that wraps SequentialComposition and offers List operations for convenient access.
 * @author aminfaez
 *
 */
public class SequentialCompositionCollection extends SequentialComposition implements List<OperationInvocation>{
	
	/** filled with ops. All requested list methods will be delegated to it. */
	private final List<OperationInvocation> innerList;
	
	/**
	 * Invokes the base constructor from SequentialComposition. 
	 */
	public SequentialCompositionCollection(CompositionDomain domain) {
		super(domain);
		// now add the operation from the iterator to the inner list.
		innerList = new ArrayList<>();
		for(OperationInvocation op : this) {
			innerList.add(op);
		}
	}

	public SequentialCompositionCollection(SequentialComposition sc) {
		this(new CompositionDomain());
		for(OperationInvocation op : sc) {
			add(op);
		}
	}

	@Override
	public boolean isEmpty() {
		return innerList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return innerList.contains(o);
	}

	@Override
	public Object[] toArray() {
		return innerList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return innerList.toArray(a);
	}

	@Override
	public boolean add(OperationInvocation e) {
		super.addOperationInvocation(e);
		return innerList.add(e);
	}

	@Override
	public boolean remove(Object o) {
		throw cantRemove();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return innerList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends OperationInvocation> c) {
		for(OperationInvocation o : c) {
			add(o);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw cantRemove();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return innerList.retainAll(c);
	}

	@Override
	public void clear() {
		throw cantRemove();
	}

	@Override
	public boolean addAll(int index, Collection<? extends OperationInvocation> c) {
		throw notImplemented();
	}

	@Override
	public OperationInvocation get(int index) {
		return innerList.get(index);
	}

	@Override
	public OperationInvocation set(int index, OperationInvocation element) {
		throw notImplemented();
	}

	@Override
	public void add(int index, OperationInvocation element) {
		throw notImplemented();
	}

	@Override
	public OperationInvocation remove(int index) {
		throw cantRemove();
	}

	@Override
	public int indexOf(Object o) {
		return innerList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return innerList.lastIndexOf(o);
	}

	@Override
	public ListIterator<OperationInvocation> listIterator() {
		return innerList.listIterator();
	}

	@Override
	public ListIterator<OperationInvocation> listIterator(int index) {
		return innerList.listIterator(index);
	}

	@Override
	public List<OperationInvocation> subList(int fromIndex, int toIndex) {
		return innerList.subList(fromIndex, toIndex);
	}

	private RuntimeException cantRemove() {
		return new RuntimeException("Can't remove ops from a SequentialComposition object.");
	}
	private RuntimeException notImplemented() {
		return new RuntimeException("Can't be/Wasn't implemented");
	}
}
