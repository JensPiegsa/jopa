
package cz.cvut.kbss.failures.model;

import java.util.Map;
import java.util.Set;
import cz.cvut.kbss.failures.Vocabulary;
import cz.cvut.kbss.jopa.CommonVocabulary;
import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jopa.model.annotations.Types;


/**
 * This class was generated by the OWL2Java tool version 0.2
 * 
 */
@OWLClass(iri = Vocabulary.s_c_Structure)
public class Structure {

    @OWLAnnotationProperty(iri = CommonVocabulary.RDFS_LABEL)
    protected String label;
    @Types
    protected Set<String> types;
    @Id(generated = true)
    protected String id;
    @Properties
    protected Map<String, Set<String>> properties;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasFloodVulnerability)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_FloodVulnerability, max = 1)
    })
    protected FloodVulnerability hasFloodVulnerability;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasMaterial)
    protected Set<RawMaterial> hasMaterial;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasGPS)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_Thing, min = 1, max = 1)
    })
    protected GPSPosition hasGPS;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasAddress)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_Address, max = 1)
    })
    protected Address hasAddress;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasExposure)
    protected Set<FloodExposure> hasExposure;
    @OWLDataProperty(iri = Vocabulary.s_p_hasHistoricalEraSpecification)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_d_PlainLiteral, min = 1, max = 1)
    })
    protected String hasHistoricalEraSpecification;
    @OWLDataProperty(iri = Vocabulary.s_p_hasDescription)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_d_PlainLiteral, min = 1, max = 1)
    })
    protected String hasDescription;

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setProperties(Map<String, Set<String>> properties) {
        this.properties = properties;
    }

    public Map<String, Set<String>> getProperties() {
        return properties;
    }

    public void setHasFloodVulnerability(FloodVulnerability hasFloodVulnerability) {
        this.hasFloodVulnerability = hasFloodVulnerability;
    }

    public FloodVulnerability getHasFloodVulnerability() {
        return hasFloodVulnerability;
    }

    public void setHasMaterial(Set<RawMaterial> hasMaterial) {
        this.hasMaterial = hasMaterial;
    }

    public Set<RawMaterial> getHasMaterial() {
        return hasMaterial;
    }

    public void setHasGPS(GPSPosition hasGPS) {
        this.hasGPS = hasGPS;
    }

    public GPSPosition getHasGPS() {
        return hasGPS;
    }

    public void setHasAddress(Address hasAddress) {
        this.hasAddress = hasAddress;
    }

    public Address getHasAddress() {
        return hasAddress;
    }

    public void setHasExposure(Set<FloodExposure> hasExposure) {
        this.hasExposure = hasExposure;
    }

    public Set<FloodExposure> getHasExposure() {
        return hasExposure;
    }

    public void setHasHistoricalEraSpecification(String hasHistoricalEraSpecification) {
        this.hasHistoricalEraSpecification = hasHistoricalEraSpecification;
    }

    public String getHasHistoricalEraSpecification() {
        return hasHistoricalEraSpecification;
    }

    public void setHasDescription(String hasDescription) {
        this.hasDescription = hasDescription;
    }

    public String getHasDescription() {
        return hasDescription;
    }

}
