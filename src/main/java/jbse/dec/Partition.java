package jbse.dec;

import java.util.LinkedHashMap;

/**
 * Union-find partition data structure.
 * 
 * @author Pietro Braione
 */
class Partition<X> {
	private class PartitionNode {
		private final X element;
		private PartitionNode parent;
		private int rank;
		
		PartitionNode(X element) {
			this.element = element;
			this.parent = this;
			this.rank = 0;
		}
		
		@Override
		public String toString() {
			return ">" + this.parent.element.toString() +"(r" + this.rank + ")";
		}
	}
	private final LinkedHashMap<X, PartitionNode> nodes = new LinkedHashMap<>();
	
	void union(X elemFirst, X elemSecond) {
		if (elemFirst.equals(elemSecond)) {
			return;
		}
		final int firstLength = elemFirst.toString().length();
		final int secondLength = elemSecond.toString().length();
		final boolean firstShorter = (firstLength < secondLength);
		final PartitionNode partitionFirst = (firstShorter ? rootNode(elemFirst) : rootNode(elemSecond));
		final PartitionNode partitionSecond = (firstShorter ? rootNode(elemSecond) : rootNode(elemFirst));
		final PartitionNode partitionLower, partitionHigher; 
		if (partitionFirst.rank < partitionSecond.rank) {
			partitionLower = partitionFirst;
			partitionHigher = partitionSecond;
		} else { 
			partitionLower = partitionSecond;
			partitionHigher = partitionFirst;
			if (partitionLower.rank == partitionHigher.rank) {
				++partitionHigher.rank;
			}
		}
		partitionLower.parent = partitionHigher;
	}
	
	X find (X elem) {
		PartitionNode node = this.nodes.get(elem);
		if (node == null) {
			return elem;
		}
		return findRootAndCompress(node).element;
	}
	
	void reset() {
		this.nodes.clear();
	}

	private PartitionNode findRootAndCompress(PartitionNode node) {
		if (node.parent != node) {
			node.parent = findRootAndCompress(node.parent);
		}
		return node.parent;
	}
	
	private PartitionNode rootNode(X elem) {
		PartitionNode elemNode = this.nodes.get(elem);
		if (elemNode == null) {
			elemNode = new PartitionNode(elem);
			this.nodes.put(elem, elemNode);
		}
		return findRootAndCompress(elemNode);
	}
	
}
