package jbse.apps;

import java.util.Map;

import jbse.bc.Signature;
import jbse.common.Type;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;

/**
 * A {@link Formatter} which renders just the heap of a {@link State} as a 
 * Graphviz DOT graph.
 * 
 * @author Pietro
 *
 */
public class StateFormatterGraphviz implements Formatter {
	private static final String nullStyle = "[shape=invtriangle,label=\"null\",regular=true]";

	private int nextFreshNode;
	private boolean hasNull;
	private String currentNodePrefix;
	private String currentNodeName;
	private String currentNode;
	private String nullNodeName;
	private String nodes;
	private String edges;

	protected String output = "";
	
	public StateFormatterGraphviz() { }

	@Override
	public void formatState(State s) {
		this.output = "";
		this.output += "digraph \"" + s.getIdentifier() + "[" + s.getSequenceNumber() + "]\"" + " { ";
		this.output += this.formatHeap(s);
		//this.formatOutput += this.formatStaticMethodArea(s);
		this.output += "}\n";
	}
	
	@Override
	public final String emit() {
	    return this.output;
	}

	@Override
	public final void cleanup() {
		this.output = "";
	}

	private String formatHeap(State s) {
		final Map<Long, Objekt> h = s.getHeap();
		String retVal = ""; //= "subgraph cluster_heap { label=\"heap\" labeljust=l ";
		this.currentNodePrefix = "H";
		this.nodes = "";
		this.edges = "";
		this.nextFreshNode = 0;
		
		for (Map.Entry<Long, Objekt> e : h.entrySet()) {
			this.hasNull = false;
			this.nullNodeName = "";
			this.currentNodeName = this.currentNodePrefix + e.getKey(); 
			this.currentNode = this.currentNodeName + "[shape=box,label=\"" + e.getKey() + ":";
			this.currentNode += e.getValue().getType();
			this.formatObject(s, e.getValue());
			this.currentNode += "\"]";
			this.nodes += currentNode;
		}
		if (this.nodes.equals("")) {
			this.nodes += this.currentNodePrefix + "H[style=invis]"; //to force visualization of subgraph without nodes
		}
		retVal += this.nodes + this.edges; //+ "}";
		return retVal;
	}

	/*
	private String formatStaticMethodArea(State s) {
		final Map<String, Klass> a = s.getStaticMethodArea();
		String retVal = "subgraph cluster_staticstore { label=\"static store\" labeljust=l ";
		this.currentNodePrefix = "S";
		this.nodes = "";
		this.edges = "";

		for (Map.Entry<String, Klass> e : a.entrySet()) {
			this.hasNull = false;
			this.nullNodeName = "";
			this.currentNodeName = this.currentNodePrefix + e.getKey(); 
			this.currentNode = this.currentNodeName + "[shape=box,label=\"" + e.getKey() + ":";
			this.currentNode += e.getValue().getType();
			formatObject(s, e.getValue());
		}

		if (this.nodes.equals("")) {
			this.nodes += this.currentNodePrefix + "H[style=invis]"; //to force visualization of subgraph without nodes
		}
		retVal += this.nodes + this.edges + "}";
		return retVal;
	}*/
	
	private String formatObject(State s, Objekt o) {
		if (o instanceof Instance || o instanceof Klass) {
			for (Signature sig : o.getFieldSignatures()) {
				if (Type.isArray(sig.getDescriptor()) ||
					Type.isReference(sig.getDescriptor())) {
					Reference r = (Reference) o.getFieldValue(sig);
					ReferenceSymbolic sr = null;
					if (r instanceof ReferenceSymbolic) {
						sr = (ReferenceSymbolic) r;
					}
					if (s.isNull(r)) {
						if (!this.hasNull) { 
							this.hasNull = true;
							this.nullNodeName = this.currentNodePrefix + "N" + this.nextFreshNode; 
							this.nodes += this.nullNodeName + nullStyle;
							this.nextFreshNode++;
						}
						edges += currentNodeName + "->" + nullNodeName;  
					} else if (sr == null) {
						edges += currentNodeName + "->" + "H" + ((ReferenceConcrete) r).getHeapPosition();
					} else if (s.resolved(sr)) {
						edges += currentNodeName + "->" + "H" + s.getResolution(sr);
					} else {
						String dummyNodeName = this.currentNodePrefix + "I" + this.nextFreshNode;
						nodes += dummyNodeName + "[label=\"?\" style=invis]";
						edges += this.currentNodeName + "->" + dummyNodeName;
						this.nextFreshNode++;
					}
					this.edges += "[label=\"" + sig.getName(); 
					if (sr != null) { 
						this.edges += " " + sr.getValue();
					}
					this.edges += "\"]";
				} else if (sig.getDescriptor().charAt(0) == Type.NULLREF) {
					if (!this.hasNull) { 
						this.hasNull = true;
						this.nullNodeName = this.currentNodePrefix + "N" + this.nextFreshNode; 
						this.nodes += this.nullNodeName + nullStyle;
						this.nextFreshNode++;
					}
					this.edges += this.currentNodeName + "->" + this.nullNodeName;
					this.edges += "[label=\"" + sig.getName() + "\"]";
				/*} else {
					this.currentNode += "\\n" + sig.getName() + " = " + o.getFieldValue(sig);*/
				}
			}
		} else { //is an array
			//TODO
		}
		return ""; //TODO
	}
}
