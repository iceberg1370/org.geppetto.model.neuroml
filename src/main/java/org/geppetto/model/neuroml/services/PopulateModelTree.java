/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011, 2013 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.geppetto.model.neuroml.services;

import java.util.List;

import org.geppetto.core.model.quantities.PhysicalQuantity;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.DynamicsSpecificationNode;
import org.geppetto.core.model.runtime.FunctionNode;
import org.geppetto.core.model.runtime.ParameterSpecificationNode;
import org.geppetto.core.model.runtime.TextMetadataNode;
import org.geppetto.core.model.values.FloatValue;
import org.geppetto.core.model.values.StringValue;
import org.neuroml.model.BiophysicalProperties;
import org.neuroml.model.Cell;
import org.neuroml.model.ChannelDensity;
import org.neuroml.model.ChannelPopulation;
import org.neuroml.model.InitMembPotential;
import org.neuroml.model.IntracellularProperties;
import org.neuroml.model.MembraneProperties;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Resistivity;
import org.neuroml.model.Species;
import org.neuroml.model.SpecificCapacitance;
import org.neuroml.model.SpikeThresh;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;

/**
 * Populates the Model Tree of Aspect
 * 
 * @author  Jesus R. Martinez (jesus@metacell.us)
 *
 */
public class PopulateModelTree {

	private boolean _populated = false;
	
	public PopulateModelTree() {		
	}
	
	/**
	 * Method that is contacted to start populating the model tree
	 * 
	 * @param modelTree - Model tree that is to be populated
	 * @param neuroml - NeuroMLDocument used to populate the tree, values are in here
	 * @return 
	 */
	public boolean populateModelTree(AspectSubTreeNode modelTree, NeuroMLDocument neuroml)
	{		
		//FIXME : Remove and apply data accurately
 		List<Cell> cells = neuroml.getCell();
 		for(Cell c : cells){
 			addProperties(modelTree, c.getBiophysicalProperties());
 		}
 		
 		return _populated;
	}

	/**
	 * Using properties found in NeuroMLDocument to populate the tree, this is where 
	 * the creationg of the nodes happen
	 * 
	 * @param modelTree - Object containing model tree
	 * @param properties - Model properties extracted from neuroml document object
	 */
	public void addProperties(AspectSubTreeNode modelTree,BiophysicalProperties properties){
		if(properties != null)
		{
			CompositeNode biophysicalPropertiesNode = new CompositeNode(Resources.BIOPHYSICAL_PROPERTIES.get(), properties.getId());
			
			MembraneProperties membraneProperties = properties.getMembraneProperties();
			if(membraneProperties != null)
			{
				CompositeNode membranePropertiesNode = new CompositeNode(Resources.MEMBRANE_P.get(), "Membrane Properties");
				
				
				List<ChannelDensity> channelDensities = membraneProperties.getChannelDensity();
//				List<ChannelPopulation> channelPopulations = membraneProperties.getChannelPopulation();
				List<SpecificCapacitance> specificCapacitances = membraneProperties.getSpecificCapacitance();
				List<SpikeThresh> spikeThreshs = membraneProperties.getSpikeThresh();
				List<InitMembPotential> initMembPotentials = membraneProperties.getInitMembPotential();

				// Channel Density
				for(ChannelDensity channelDensity : channelDensities)
				{
					CompositeNode channelDensityNode = new CompositeNode(Resources.CHANNEL_DENSITY.get() + "_" + channelDensity.getId(), channelDensity.getId());
					
					// Passive conductance density				
					channelDensityNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.COND_DENSITY, "condDensity_"+channelDensity.getId(), channelDensity.getCondDensity()));
					
					// ION	
					channelDensityNode.addChild(new TextMetadataNode(Resources.ION.get(), "ion_"+channelDensity.getId(),  new StringValue(channelDensity.getIon())));
					
					// Ion Channel
					//TODO: We need to work on this nested element
					channelDensityNode.addChild(new CompositeNode(Resources.ION_CHANNEL.get(), channelDensity.getIonChannel()));
										
					// Reverse Potential					
					channelDensityNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.EREV, "erev_"+channelDensity.getId(), channelDensity.getErev()));
					
					// Segment Group
					//TODO: Point to a visualization group?
					
					membranePropertiesNode.addChild(channelDensityNode);
				}

				// Spike threshold
				for(int i = 0; i < spikeThreshs.size(); i++)
				{
					membranePropertiesNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.SPIKE_THRESHOLD, "spikeThresh_"+i, spikeThreshs.get(i).getValue()));
				}

				// Specific Capacitance
				for(int i = 0; i < specificCapacitances.size(); i++)
				{
					membranePropertiesNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.SPECIFIC_CAPACITANCE, "specificCapacitance_"+i, specificCapacitances.get(i).getValue()));
				}
				
				// Initial Membrance Potentials
				for(int i = 0; i < initMembPotentials.size(); i++)
				{
					membranePropertiesNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_MEMBRANE_POTENTIAL, "initMembPotential_"+i, initMembPotentials.get(i).getValue()));
				}
				
				biophysicalPropertiesNode.addChild(membranePropertiesNode);
			}
			
			IntracellularProperties intracellularProperties = properties.getIntracellularProperties();
			if(intracellularProperties != null)
			{
				CompositeNode intracellularPropertiesNode = new CompositeNode(Resources.INTRACELLULAR_P.get(), "intracellularProperties");
				
				List<Resistivity> resistivities = intracellularProperties.getResistivity();
				List<Species> species = intracellularProperties.getSpecies();
				
				// Resistivity
				for(int i = 0; i < resistivities.size(); i++)
				{
					intracellularPropertiesNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.RESISTIVITY, "resistivity_"+i, resistivities.get(i).getValue()));
				}
				
				// Specie
				for(Species specie : species)
				{
					CompositeNode specieNode = new CompositeNode(Resources.SPECIES.get(), specie.getId());
					
					// Initial Concentration
					specieNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_CONCENTRATION, "initialConcentration_"+specie.getId(), specie.getInitialConcentration()));
					
					// Initial External Concentration
					specieNode.addChild(PopulateModelTreeUtils.createParameterSpecificationNode(Resources.INIT_EXT_CONCENTRATION, "initialExtConcentration_"+specie.getId(), specie.getInitialExtConcentration()));
					
					// Ion
					specieNode.addChild(new TextMetadataNode(Resources.ION.get(), "ion_"+specie.getId(),  new StringValue(specie.getIon())));
					
					// Concentration Model
					//TODO: We need to work on this nested element
					specieNode.addChild(new CompositeNode(Resources.CONCENTRATION_MODEL.get(),specie.getConcentrationModel()));
				}
				biophysicalPropertiesNode.addChild(intracellularPropertiesNode);
			}

			_populated = true;
			modelTree.addChild(biophysicalPropertiesNode);

		}
	}
}
