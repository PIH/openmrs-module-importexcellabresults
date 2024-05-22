package org.openmrs.module.cancerscreeninglabresults.util;

public class labResultsObject {
	
	private String position;
	
	private String specimenCode;
	
	private String HPVResults;
	
	private String positiveType;
	
	private String patientIdentifier;
	
	public labResultsObject(String position, String specimenCode, String HPVResults, String positiveType,
	    String patientIdentifier) {
		this.position = position;
		this.specimenCode = specimenCode;
		this.HPVResults = HPVResults;
		this.positiveType = positiveType;
		this.patientIdentifier = patientIdentifier;
	}
	
	public String getPatientIdentifier() {
		return patientIdentifier;
	}
	
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public String getSpecimenCode() {
		return specimenCode;
	}
	
	public void setSpecimenCode(String specimenCode) {
		this.specimenCode = specimenCode;
	}
	
	public String getHPVResults() {
		return HPVResults;
	}
	
	public void setHPVResults(String HPVResults) {
		this.HPVResults = HPVResults;
	}
	
	public String getPositiveType() {
		return positiveType;
	}
	
	public void setPositiveType(String positiveType) {
		this.positiveType = positiveType;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
}
