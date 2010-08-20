/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Element;

/**
 * Layout processing action to reset an attribute to the value specified by the
 * owning fragment.
 * 
 * @author Mark Boyd
 */
public class LPAResetAttribute implements ILayoutProcessingAction
{
    private String nodeId = null;
    private String name = null;
    private IPerson person = null;
    private Element ilfNode = null;
    private String fragmentValue = null;
    
    LPAResetAttribute(String nodeId, String name, String fragmentValue, IPerson p, 
            Element ilfNode)
    {
        this.nodeId = nodeId;
        this.name = name;
        this.person = p;
        this.ilfNode = ilfNode;
        this.fragmentValue = fragmentValue;
    }
    
    /**
     * Reset a parameter to not override the value specified by a fragment.
     * This is done by removing the parm edit in the PLF and setting the value
     * in the ILF to the passed-in fragment value.
     */
    public void perform() throws PortalException
    {
        /*
         * push the change into the PLF
         */
        if (nodeId.startsWith(Constants.FRAGMENT_ID_USER_PREFIX))
        {
            // remove the parm edit
            EditManager.removeEditDirective(nodeId, name, person);
            
            // handle label localization updates
            final IFolderLabelPolicy labelPolicy = DistributedLayoutManager.ContextHolder.getLabelPolicy();
            if (name.equals(Constants.ATT_NAME) && labelPolicy != null)
            {
                Element plfNode = HandlerUtils
                    .getPLFNode( ilfNode, person,
                          false, // create node if not found
                          false ); // don't create children
                if (plfNode != null) // will always be non-null if we get here
                {
                    String plfId = plfNode.getAttribute(Constants.ATT_PLF_ID);
                    labelPolicy.deleteNodeLabel(nodeId, plfId, 
                            false, // only remove for current locale
                            person.getID(), false); // not a fragment owner
                }
            }
        }
        /*
         * push the fragment value into the ILF if not the name element. For the
         * name element the locale specific value will be injected during
         * layout rendering.
         */
        if (DistributedLayoutManager.ContextHolder.getLabelPolicy() == null || !name.equals(Constants.ATT_NAME))
        {
            ilfNode.setAttribute(name, fragmentValue);
        }
    }
}