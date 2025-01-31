/*
 * Copyright 2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30;

import static org.apache.commons.lang3.Validate.noNullElements;
import static org.apache.commons.lang3.Validate.notNull;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.*;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rim.*;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for requests and responses that contain various ebXML 3.0
 * objects.
 * @author Jens Riemschneider
 */
public abstract class EbXMLObjectContainer30 implements EbXMLObjectContainer {
    private final EbXMLObjectLibrary objectLibrary;

    @Override
    public EbXMLObjectLibrary getObjectLibrary() {
        return objectLibrary;
    }
    
    /**
     * Fills the object Library based on the contents.
     */
    protected void fillObjectLibrary() {
        for (var obj : getContents()) {
            var id = obj.getValue().getId();
            if (id != null) {
                objectLibrary.put(id, obj.getValue());
            }
        }
    }

    /**
     * Constructs the container.
     * @param objectLibrary
     *          the object library to use.
     */
    EbXMLObjectContainer30(EbXMLObjectLibrary objectLibrary) {
        notNull(objectLibrary, "objLibrary cannot be null");
        this.objectLibrary = objectLibrary;
    }
    
    @Override
    public void addAssociation(EbXMLAssociation association) {
        if (association != null) {
            var internal = ((EbXMLAssociation30)association).getInternal();
            getContents().add(EbXMLFactory30.RIM_FACTORY.createAssociation(internal));
        }        
    }

    @Override
    public void addExtrinsicObject(EbXMLExtrinsicObject extrinsic) {
        if (extrinsic != null) {
            var internal = ((EbXMLExtrinsicObject30)extrinsic).getInternal();
            getContents().add(EbXMLFactory30.RIM_FACTORY.createExtrinsicObject(internal));
        }        
    }

    @Override
    public void addRegistryPackage(EbXMLRegistryPackage regPackage) {
        if (regPackage != null) {
            var internal = ((EbXMLRegistryPackage30)regPackage).getInternal();
            getContents().add(EbXMLFactory30.RIM_FACTORY.createRegistryPackage(internal));
        }        
    }

    @Override
    public List<EbXMLAssociation> getAssociations() {
        var results = new ArrayList<EbXMLAssociation>();
        for (var identifiable : getContents()) {
            var association = cast(identifiable, AssociationType1.class);
            if (association != null) {
                results.add(new EbXMLAssociation30(association, objectLibrary));
            }
        }
        
        return results;
    }

    @Override
    public List<EbXMLClassification> getClassifications() {
        var results = new ArrayList<EbXMLClassification>();
        for (var identifiable : getContents()) {
            var classification = cast(identifiable, ClassificationType.class);
            if (classification != null) {
                results.add(new EbXMLClassification30(classification));
            }
        }
        
        return results;
    }
    
    @Override
    public List<EbXMLExtrinsicObject> getExtrinsicObjects(String... objectTypes) {
        noNullElements(objectTypes, "objectTypes cannot be null or contain null elements");

        var results = new ArrayList<EbXMLExtrinsicObject>();
        for (var identifiable : getContents()) {
            var extrinsic = cast(identifiable, ExtrinsicObjectType.class);
            if (extrinsic != null) {
                for (var objectType : objectTypes) {
                    if (objectType.equals(extrinsic.getObjectType())) {
                        results.add(new EbXMLExtrinsicObject30(extrinsic, objectLibrary));
                        break;
                    }
                }
            }
        }
        
        return results;
    }

    @Override
    public List<EbXMLExtrinsicObject> getExtrinsicObjects() {
        var results = new ArrayList<EbXMLExtrinsicObject>();
        for (var identifiable : getContents()) {
            var extrinsic = cast(identifiable, ExtrinsicObjectType.class);
            if (extrinsic != null) {
                results.add(new EbXMLExtrinsicObject30(extrinsic, objectLibrary));
            }
        }
        
        return results;
    }

    @Override
    public List<EbXMLRegistryPackage> getRegistryPackages(String classificationNode) {
        notNull(classificationNode, "classificationNode cannot be null");

        var acceptedIds = getAcceptedIds(classificationNode);
        
        var results = new ArrayList<EbXMLRegistryPackage>();
        for (var identifiable : getContents()) {
            var regPackage = cast(identifiable, RegistryPackageType.class);
            if (matchesFilter(regPackage, acceptedIds, classificationNode)) {
                results.add(new EbXMLRegistryPackage30(regPackage, objectLibrary));
            }
        }
        
        return results;
    }

    @Override
    public List<EbXMLRegistryPackage> getRegistryPackages() {
        var results = new ArrayList<EbXMLRegistryPackage>();
        for (var identifiable : getContents()) {
            var regPackage = cast(identifiable, RegistryPackageType.class);
            if (regPackage != null) {
                results.add(new EbXMLRegistryPackage30(regPackage, objectLibrary));
            }
        }
        
        return results;
    }

    @Override
    public void addClassification(EbXMLClassification classification) {
        if (classification != null) {
            var internal = ((EbXMLClassification30)classification).getInternal();
            getContents().add(EbXMLFactory30.RIM_FACTORY.createClassification(internal));
        }
    }

    private boolean matchesFilter(RegistryPackageType regPackage, Set<String> acceptedIds, String classificationNode) {
        return regPackage != null && (acceptedIds.contains(regPackage.getId()) || hasClassificationNode(regPackage, classificationNode));

    }

    private boolean hasClassificationNode(RegistryPackageType regPackage, String classificationNode) {
        var id = regPackage.getId();
        if (id == null) {
            return false;
        }
        
        for (var classification : regPackage.getClassification()) {
            if (classificationNode.equals(classification.getClassificationNode()) 
                    && id.equals(classification.getClassifiedObject())) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getAcceptedIds(String classificationNode) {
        var acceptedIds = new HashSet<String>();
        for (var identifiable : getContents()) {
            var classification = cast(identifiable, ClassificationType.class);
            if (classification != null && classificationNode.equals(classification.getClassificationNode())) {
                acceptedIds.add(classification.getClassifiedObject());
            }
        }
        return acceptedIds;
    }

    /**
     * Casts an object from the contents into the given type.
     * @param <T>
     *          the type to cast to.
     * @param identifiable
     *          the object to cast.
     * @param type
     *          the type to cast to.
     * @return the result of the cast or <code>null</code> if the object wasn't of the given type.
     */
    protected <T extends IdentifiableType> T cast(JAXBElement<? extends IdentifiableType> identifiable, Class<T> type) {
        if ((identifiable.getDeclaredType() == type) || identifiable.getValue().getClass() == type) {
            return type.cast(identifiable.getValue());
        }
        return null;
    }
    
    /**
     * @return retrieves the list of contained objects.
     */
    abstract List<JAXBElement<? extends IdentifiableType>> getContents();
}