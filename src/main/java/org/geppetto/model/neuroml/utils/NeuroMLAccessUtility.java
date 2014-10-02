/**
 * 
 */
package org.geppetto.model.neuroml.utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.model.ModelWrapper;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Lems;
import org.neuroml.model.AdExIaFCell;
import org.neuroml.model.Base;
import org.neuroml.model.Cell;
import org.neuroml.model.ComponentType;
import org.neuroml.model.DecayingPoolConcentrationModel;
import org.neuroml.model.FixedFactorConcentrationModel;
import org.neuroml.model.IafCell;
import org.neuroml.model.IonChannel;
import org.neuroml.model.IonChannelHH;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.util.NeuroMLConverter;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 */
public class NeuroMLAccessUtility
{

	public static final String DISCOVERED_COMPONENTS = "discoveredComponents";
	public static final String LEMS_ID = "lems";
	public static final String NEUROML_ID = "neuroml";
	public static final String URL_ID = "url";
	public static final String SUBENTITIES_MAPPING_ID = "entitiesMapping";
//	public static final String LEMS_UTILS_ID = "lemsUtils";
	
	private LEMSAccessUtility lemsAccessUtility = new LEMSAccessUtility();
	
	public NeuroMLAccessUtility() {
		super();
	}

	/**
	 * @param p
	 * @param neuroml
	 * @param url
	 * @return
	 * @throws ModelInterpreterException 
	 * @throws ContentError 
	 */
	public Object getComponent(String componentId, ModelWrapper model, Resources componentType) throws ModelInterpreterException
	{
		//Check if we have already discovered this component
		Object component = this.getComponentFromCache(componentId, model);
		
		if (component == null){
			component = this.lemsAccessUtility.getComponentFromCache(componentId, model);
		}
		
		// let's first check if the cell is of a predefined neuroml type
		if(component == null){
			try {
				component = getComponentById(componentId, model, componentType);
			} catch (ContentError e1) {
				throw new ModelInterpreterException("Can't find the componet " + componentId);
			}
		}

		if(component == null)
		{
			// sorry no luck!
			throw new ModelInterpreterException("Can't find the componet " + componentId);
		}
		return component;
	}
	
	public Base getComponentFromCache(String componentId, ModelWrapper model){
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
		
		//TODO Can we have the same id for two different components 
		if(_discoveredComponents.containsKey(componentId))
		{
			return _discoveredComponents.get(componentId);
		}
		
		return null;
	}
	
	
	private Object getComponentById(String componentId, ModelWrapper model, Resources componentType) throws ContentError
	{
		
//		Lems lems = (Lems) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.LEMS_ID);
		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
		
		NeuroMLDocument doc = (NeuroMLDocument) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.NEUROML_ID);
		
		switch (componentType) {
		case ION_CHANNEL:
			for (IonChannel ionChannel : doc.getIonChannel()){
				_discoveredComponents.put(ionChannel.getId(), ionChannel);
				if(ionChannel.getId().equals(componentId))
				{
					return ionChannel;
				}
			}
			for (IonChannelHH ionChannelHH : doc.getIonChannelHH()){
				_discoveredComponents.put(ionChannelHH.getId(), ionChannelHH);
				if(ionChannelHH.getId().equals(componentId))
				{
					return ionChannelHH;
				}
			}
		case CELL:	
			
			for(AdExIaFCell c : doc.getAdExIaFCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			for(IafCell c : doc.getIafCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			
			for(Cell c : doc.getCell())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			
		case CONCENTRATION_MODEL:
			for(FixedFactorConcentrationModel c : doc.getFixedFactorConcentrationModel())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			
			for(DecayingPoolConcentrationModel c : doc.getDecayingPoolConcentrationModel())
			{
				_discoveredComponents.put(c.getId(), c);
				if(c.getId().equals(componentId))
				{
					return c;
				}
			}
			
			
		default:
			return this.lemsAccessUtility.getComponentById(componentId, model);
		}
		
		
	}
	
	
	/**
	 * @param componentId
	 * @param url
	 * @return
	 * @throws JAXBException
	 * @throws MalformedURLException
	 */
//	public Base retrieveNeuroMLComponent(String componentId, ResourcesSuffix componentType, ModelWrapper model) throws JAXBException, MalformedURLException
//	{
//		URL url = (URL) ((ModelWrapper) model).getModel(NeuroMLAccessUtility.URL_ID);
//		NeuroMLConverter neuromlConverter = new NeuroMLConverter();
//		boolean attemptConnection = true;
//		String baseURL = url.getFile();
//		HashMap<String, Base> _discoveredComponents = ((HashMap<String, Base>)((ModelWrapper) model).getModel(NeuroMLAccessUtility.DISCOVERED_COMPONENTS));
//		if(url.getFile().endsWith("nml"))
//		{
//			baseURL = baseURL.substring(0, baseURL.lastIndexOf("/") + 1);
//		}
//		int attempts = 0;
//		NeuroMLDocument neuromlDocument = null;
//		while(attemptConnection)
//		{
//			try
//			{
//				attemptConnection = false;
//				attempts++;
//				URL componentURL = new URL(url.getProtocol() + "://" + url.getAuthority() + baseURL + componentId + componentType.get() + ".nml");
//
//				neuromlDocument = neuromlConverter.urlToNeuroML(componentURL);
//
//				List<? extends Base> components = null;
//				
//				switch (componentType) {
//				case ION_CHANNEL:
//					components = neuromlDocument.getIonChannel();
//				default:
//					break;
//				}
//				
//				if(components != null)
//				{
//					
//					
//					for(Base c : neuromlDocument.getIonChannel())
//					{
//						_discoveredComponents.put(componentId, c);
//						if(((Base)c).getId().equals(componentId))
//						{
//							return c;
//						}
//					}
//				}
//			}
//			catch(MalformedURLException e)
//			{
//				throw e;
//			}
//			catch(UnmarshalException e)
//			{
//				if(e.getLinkedException() instanceof IOException)
//				{
//					if(attempts < maxAttempts)
//					{
//						attemptConnection = true;
//					}
//				}
//			}
//			catch(Exception e)
//			{
//				throw e;
//			}
//		}
//		return null;
//	}
	
}
