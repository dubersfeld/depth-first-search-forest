package com.dub.spring.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dub.spring.depthFirstSearch.DFSGraph;
import com.dub.spring.depthFirstSearch.DFSResponse;
import com.dub.spring.depthFirstSearch.DFSVertex;
import com.dub.spring.depthFirstSearch.GraphInitRequest;
import com.dub.spring.depthFirstSearch.GraphServices;
import com.dub.spring.depthFirstSearch.JSONEdge;
import com.dub.spring.depthFirstSearch.JSONSnapshot;
import com.dub.spring.depthFirstSearch.JSONVertex;
import com.dub.spring.depthFirstSearch.SearchRequest;


@Controller
public class DFSController {
	
	@Autowired
	private GraphServices graphServices;
	
	/** Initialize graph from JSON object sent by browser
	 * This method is called only one time 
	 */
	@RequestMapping(value="initGraph")
	@ResponseBody
	public DFSResponse initGraph(@RequestBody GraphInitRequest message, 
				HttpServletRequest request) 
	{	
		System.out.println("controller: initGraph begin");
		
		HttpSession session = request.getSession();
	
		if (session.getAttribute("graph") != null) {
			session.removeAttribute("graph");
		}
		
		List<JSONEdge> jsonEdges = message.getJsonEdges();
		List<JSONVertex> jsonVertices = message.getJsonVertices();
	
		DFSGraph graph = new DFSGraph(jsonVertices.size());
			
		for (int i1 = 0; i1 < jsonVertices.size(); i1++) {
			DFSVertex v = new DFSVertex();
			v.setName(jsonVertices.get(i1).getName());
			v.setColor(DFSVertex.Color.BLACK);
			graph.getVertices()[i1] = v;
		}
		
		for (int i1 = 0; i1 < jsonEdges.size(); i1++) {
			JSONEdge edge = jsonEdges.get(i1);
			int from = edge.getFrom();
			int to = edge.getTo();
			DFSVertex v1 = (DFSVertex)graph.getVertices()[from];
			v1.getAdjacency().add(to);
		}
		
		/** Save the new graph to the session context */
		session.setAttribute("graph", graph);
			
		DFSResponse dfsResponse = new DFSResponse();
		dfsResponse.setStatus(DFSResponse.Status.OK);
	
		System.out.println("controller: graph built");
		
		// here the graph is ready for the search loop
		
		System.out.println("controller: initGraph completed");
			
		return dfsResponse;
	}
	

	@RequestMapping(value="searchGraph")
	@ResponseBody
	public DFSResponse searchGraph(@RequestBody SearchRequest message, 
											HttpServletRequest request) 
	{	
		System.out.println("searchGraph begin");
		
		DFSResponse dfsResponse = new DFSResponse();
		
		HttpSession session = request.getSession();
		DFSGraph graph = (DFSGraph)session.getAttribute("graph");
				
		// snapshots for display
		List<JSONSnapshot> snapshots = new ArrayList<>();
				
		while (!graph.isFinished()) {
			System.out.println("search while begin");
			graph.searchStep();
			JSONSnapshot snapshot = graphServices.graphToJSON(graph);
			System.out.println("search while ");
			snapshots.add(snapshot);
			
		}// while
				
		dfsResponse.setSnapshots(snapshots);
		dfsResponse.setStatus(DFSResponse.Status.OK);
		
		System.out.println("searchGraph return");
		
		return dfsResponse;
	}// searchGraph

}
