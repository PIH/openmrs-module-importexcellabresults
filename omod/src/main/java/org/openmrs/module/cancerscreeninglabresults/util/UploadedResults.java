package org.openmrs.module.cancerscreeninglabresults.util;

import org.springframework.web.multipart.MultipartFile;

public class UploadedResults {
	
	private String encounterDate;
	
	private int selectedLocation;
	
	private int selectedEncounterType;
	
	private int selectedProvider;
	
	private MultipartFile excelFile;
	
	public UploadedResults() {
		
	}
	
	public UploadedResults(String encounterDate, int selectedEncounterType, int selectedProvider, MultipartFile excelFile) {
		this.encounterDate = encounterDate;
		this.selectedEncounterType = selectedEncounterType;
		this.selectedProvider = selectedProvider;
		this.excelFile = excelFile;
	}
	
	public String getEncounterDate() {
		return encounterDate;
	}
	
	public void setEncounterDate(String encounterDate) {
		this.encounterDate = encounterDate;
	}
	
	public int getSelectedEncounterType() {
		return selectedEncounterType;
	}
	
	public void setSelectedEncounterType(int selectedEncounterType) {
		this.selectedEncounterType = selectedEncounterType;
	}
	
	public int getSelectedProvider() {
		return selectedProvider;
	}
	
	public void setSelectedProvider(int selectedProvider) {
		this.selectedProvider = selectedProvider;
	}
	
	public MultipartFile getExcelFile() {
		return excelFile;
	}
	
	public void setExcelFile(MultipartFile excelFile) {
		this.excelFile = excelFile;
	}
	
	public int getSelectedLocation() {
		return selectedLocation;
	}
	
	public void setSelectedLocation(int selectedLocation) {
		this.selectedLocation = selectedLocation;
	}
	
	@Override
	public String toString() {
		return "LabResults{" + "encounterDate=" + encounterDate + ", selectedEncounterType=" + selectedEncounterType
		        + ", selectedProvider=" + selectedProvider + ", excelFile=" + excelFile + '}';
	}
}
