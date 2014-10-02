package it.polimi.modaclouds.monitoring.appleveldc;

import it.polimi.modaclouds.qos_models.monitoring_ontology.Method;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MonitoringPlatformSynch {
	

	
	public static class SynchMethod{
		private String id;
		
		public SynchMethod(String id){
			this.id = id;
		}
		public String getId(){
			return id;
		}
	}

	public static void sendMethods(List<SynchMethod> methods, String appId ){
		Model update = new Model();
		for (SynchMethod m : methods){
			Method method = new Method();
			method.setId(m.getId());
			update.add(method);
		}
		Gson gson = new GsonBuilder().serializeNulls().create();
		String json = gson.toJson(update);
		int result = HttpRequest.put(url).send(json).code();
		}
	
}
