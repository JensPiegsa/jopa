
package cz.cvut.kbss.failures.model;

import java.util.Date;
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
@OWLClass(iri = Vocabulary.s_c_Failure)
public class Failure {

    @OWLAnnotationProperty(iri = CommonVocabulary.RDFS_LABEL)
    protected String label;
    @Types
    protected Set<String> types;
    @Id(generated = true)
    protected String id;
    @Properties
    protected Map<String, Set<String>> properties;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasManifestation)
    protected Set<Manifestation> hasManifestation;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasFactor)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_Factor, min = 1)
    })
    protected Set<Factor> hasFactor;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasAttachment)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_InformationResource)
    })
    protected Set<InformationResource> hasAttachment;
    @OWLObjectProperty(iri = Vocabulary.s_p_isFailureOf)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_Structure, min = 1, max = 1)
    })
    protected Structure isFailureOf;
    @OWLObjectProperty(iri = Vocabulary.s_p_hasReporter)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_Person, min = 1, max = 1)
    })
    protected Set<Thing> hasReporter;
    @OWLDataProperty(iri = Vocabulary.s_p_hasInsertionTime)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_d_dateTime, min = 1, max = 1)
    })
    protected Date hasInsertionTime;

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

    public void setHasManifestation(Set<Manifestation> hasManifestation) {
        this.hasManifestation = hasManifestation;
    }

    public Set<Manifestation> getHasManifestation() {
        return hasManifestation;
    }

    public void setHasFactor(Set<Factor> hasFactor) {
        this.hasFactor = hasFactor;
    }

    public Set<Factor> getHasFactor() {
        return hasFactor;
    }

    public void setHasAttachment(Set<InformationResource> hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public Set<InformationResource> getHasAttachment() {
        return hasAttachment;
    }

    public void setIsFailureOf(Structure isFailureOf) {
        this.isFailureOf = isFailureOf;
    }

    public Structure getIsFailureOf() {
        return isFailureOf;
    }

    public void setHasReporter(Set<Thing> hasReporter) {
        this.hasReporter = hasReporter;
    }

    public Set<Thing> getHasReporter() {
        return hasReporter;
    }

    public void setHasInsertionTime(Date hasInsertionTime) {
        this.hasInsertionTime = hasInsertionTime;
    }

    public Date getHasInsertionTime() {
        return hasInsertionTime;
    }

}
