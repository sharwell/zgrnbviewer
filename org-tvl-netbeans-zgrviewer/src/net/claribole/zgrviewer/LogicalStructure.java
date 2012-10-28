/*
 * FILE: LogicalStructure.java DATE OF CREATION: Thu Mar 15 18:33:17 2007
 * Copyright (c) INRIA, 2007-2011. All Rights Reserved Licensed under the GNU
 * LGPL. For full terms see the file COPYING.
 *
 * $Id: LogicalStructure.java 4582 2011-07-26 10:25:17Z epietrig $
 */
package net.claribole.zgrviewer;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.svg.Metadata;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class LogicalStructure {

    static final String NODE_PREFIX = "node";
    static final String EDGE_PREFIX = "edge";

    public static LogicalStructure build(List<Glyph> glyphs) {
        Glyph g;
        Metadata md = null;
        // key = node title, value = vector of glyphs associated with this node
        Map<String, List<Glyph>> title2node = new HashMap<String, List<Glyph>>();
        // key = edge title, value = hashtable in which:
        //           key = closest ancestor group ID,
        //           value = vector of glyphs associated with the
        //                   edge whose id is key
        // this is necessary to avoid all glyphs of different edges linking the same nodes being associated with the same single LEdge
        Map<String, Map<String, List<Glyph>>> title2edgeGroup = new HashMap<String, Map<String, List<Glyph>>>();
        String title;
        List<Glyph> v;
        Map<String, List<Glyph>> t;
        String cagid, cgac;
        int edgeCount = 0;
        for (int i = 0; i < glyphs.size(); i++) {
            g = glyphs.get(i);
            if (g.getOwner() instanceof Metadata) {
                md = (Metadata)g.getOwner();
            } else if (g.getOwner() instanceof LElem) {
                md = ((LElem)g.getOwner()).getMetadata();
            }

            if (md != null && (title = md.getTitle()) != null) {
                cagid = md.getClosestAncestorGroupID();
                cgac = md.getClosestAncestorGroupClass();
                if (cgac.equals(EDGE_PREFIX) || cagid.startsWith(EDGE_PREFIX)) {
                    // dealing with a glyph that is part of an edge
                    if (title2edgeGroup.containsKey(title)) {
                        t = title2edgeGroup.get(title);
                        if (t.containsKey(cagid)) {
                            v = t.get(cagid);
                            v.add(g);
                        } else {
                            v = new ArrayList<Glyph>();
                            v.add(g);
                            t.put(cagid, v);
                            edgeCount++;
                        }
                    } else {
                        v = new ArrayList<Glyph>();
                        v.add(g);
                        // initial capacity set to 3 (path, arrow head, label)
                        t = new HashMap<String, List<Glyph>>(3);
                        t.put(cagid, v);
                        title2edgeGroup.put(title, t);
                        edgeCount++;
                    }
                } else if (cgac.equals(NODE_PREFIX) || cagid.startsWith(NODE_PREFIX)) {
                    // dealing with a glyph that is part of a node
                    if (title2node.containsKey(title)) {
                        v = title2node.get(title);
                        v.add(g);
                    } else {
                        v = new ArrayList<Glyph>();
                        v.add(g);
                        title2node.put(title, v);
                    }
                }
                // else, other stuff that is probably not part of the graph structure, like a graph's background
                // do nothing
            }
            // remain silent if structural information could not be extracted
        }
        LogicalStructure res = new LogicalStructure(title2node, title2edgeGroup, edgeCount);
        title2edgeGroup.clear();
        title2node.clear();
        return (res.isEmpty()) ? null : res;
    }

    /*
     * -----------------------------------
     */
    LNode[] nodes;
    LEdge[] edges;

    LogicalStructure(Map<String, List<Glyph>> title2node, Map<String, Map<String, List<Glyph>>> title2edgeGroup, int edgeCount) {
        String title;
        // construct nodes
        nodes = new LNode[title2node.size()];
        int i = 0;
        for (Iterator<String> e = title2node.keySet().iterator(); e.hasNext();) {
            title = e.next();
            nodes[i] = new LNode(title, title2node.get(title));
            i++;
        }
        // construct edges
        i = 0;
        edges = new LEdge[edgeCount];
        Map<String, List<Glyph>> group2edge;
        for (Iterator<String> e = title2edgeGroup.keySet().iterator(); e.hasNext();) {
            title = e.next();
            group2edge = title2edgeGroup.get(title);
            for (Iterator<List<Glyph>> e2 = group2edge.values().iterator(); e2.hasNext();) {
                // we do not save the group/edge's ID, not relevant for now
                // but we could if it prove to be useful (group ID is just the key)
                // and could be given to the LEdge constructor
                edges[i] = new LEdge(title, e2.next());
                i++;
            }
        }
        // link nodes and edges
        for (int j = 0; j < edges.length; j++) {
            int id = edges[j].title.indexOf(LEdge.DIRECTED_STR);
            if (id != -1) {
                edges[j].setDirected(true);
                edges[j].setTail(getNode(edges[j].title.substring(0, id)));
                edges[j].setHead(getNode(edges[j].title.substring(id + 2)));
            } else {
                id = edges[j].title.indexOf(LEdge.UNDIRECTED_STR);
                if (id != -1) {
                    edges[j].setDirected(false);
                    edges[j].setTail(getNode(edges[j].title.substring(0, id)));
                    edges[j].setHead(getNode(edges[j].title.substring(id + 2)));
                }
            }
        }
    }

    public void addEdge(LEdge e) {
        LEdge[] nedges = new LEdge[edges.length + 1];
        System.arraycopy(edges, 0, nedges, 0, edges.length);
        nedges[edges.length] = e;
        edges = nedges;
    }

    public void removeEdge(LEdge e) {
        int index = -1;
        // find edge index in array
        for (int i = 0; i < edges.length; i++) {
            if (edges[i] == e) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            // then remove it (if found)
            LEdge[] nedges = new LEdge[edges.length - 1];
            System.arraycopy(edges, 0, nedges, 0, index);
            System.arraycopy(edges, index + 1, nedges, index, edges.length - index - 1);
            edges = nedges;
        }
        e.tail.removeArc(e);
        e.head.removeArc(e);
    }

    public LNode[] getAllNodes() {
        return nodes;
    }

    public LEdge[] getAllEdges() {
        return edges;
    }

    public LNode getNode(String title) {
        LNode res = null;
        // starting with GV 2.24, edge titles now include port in situations where they did not previously
        // if we manage to find a matching node with port information, then use it
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].title.equals(title)) {
                return nodes[i];
            }
        }
        // if not, try to find a node matching the title without port information
        if (title.indexOf(LElem.PORT_SEPARATOR) != -1) {
            title = title.substring(0, title.indexOf(LElem.PORT_SEPARATOR));
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].title.equals(title)) {
                    return nodes[i];
                }
            }
        }
        // if this also fails, don't return anything
        return null;
    }

    boolean isEmpty() {
        return (nodes.length == 0 || edges.length == 0);
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < nodes.length; i++) {
            res += nodes[i].toString() + "\n";
        }
        for (int i = 0; i < edges.length; i++) {
            res += edges[i].toString() + "\n";
        }
        return res;
    }

    /**
     * Get the logical node corresponding to this glyph.
     *
     * @return null if g is not associated to a logical node.
     */
    public static LNode getNode(Glyph g) {
        Object o = (g != null) ? g.getOwner() : null;
        if (o != null) {
            return (o instanceof LNode) ? (LNode)o : null;
        }
        return null;
    }

    /**
     * Get the logical arc corresponding to this glyph.
     *
     * @return null if g is not associated to a logical arc.
     */
    public static LEdge getEdge(Glyph g) {
        Object o = (g != null) ? g.getOwner() : null;
        if (o != null) {
            return (o instanceof LEdge) ? (LEdge)o : null;
        }
        return null;
    }

}
