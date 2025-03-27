/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.cancerscreeninglabresults.web.controller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.cancerscreeninglabresults.api.CancerscreeninglabresultsService;
import org.openmrs.module.cancerscreeninglabresults.api.impl.CancerscreeninglabresultsServiceImpl;
import org.openmrs.module.cancerscreeninglabresults.util.labResultsObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * This class configured as controller using annotation and mapped with the URL of
 * 'module/${rootArtifactid}/${rootArtifactid}Link.form'.
 */
@Controller("${rootrootArtifactid}.CancerscreeninglabresultsController")
@RequestMapping(value = "module/cancerscreeninglabresults/cancerscreeninglabresults.form")
public class CancerscreeninglabresultsController extends ParameterizableViewController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	@Autowired
	EncounterService encounterService;
	
	@Autowired
	ProviderService providerService;
	
	@Autowired
	LocationService locationService;
	
	@Autowired
	ConceptService conceptService;
	
	@Autowired
	PatientService patientService;
	
	@Autowired
	FormService formService;
	
	@Autowired
	ObsService obsService;
	
	/** Success form view name */
	private final String VIEW = "/module/cancerscreeninglabresults/cancerscreeninglabresults";
	
	/**
	 * Initially called after the getUsers method to get the landing form name
	 * 
	 * @return String form view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String onGet() {
		return VIEW;
	}
	
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		ModelAndView modelAndView = new ModelAndView();
		
		List<EncounterType> encounterTypes = encounterService.getAllEncounterTypes();
		modelAndView.addObject("encounterTypes", encounterTypes);
		
		List<Provider> providers = providerService.getAllProviders();
		modelAndView.addObject("providers", providers);
		
		List<Location> locations = locationService.getAllLocations();
		modelAndView.addObject("locations", locations);
		
		InputStream inputStream = null;
		if (request instanceof MultipartHttpServletRequest) {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multipartRequest.getFile("excelFile");
			if (file != null) {
				inputStream = file.getInputStream();
				
				try {
					XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
					XSSFSheet sheet = workbook.getSheetAt(0);
					int rows = sheet.getLastRowNum();
					int cols = sheet.getRow(1).getLastCellNum();
					List<labResultsObject> labResultsObjects = new ArrayList<labResultsObject>();
					for (int r = 1; r <= rows; r++) {
						
						XSSFRow row = sheet.getRow(r);
						
						String patientID = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? "" : row
						        .getCell(4).getStringCellValue();
						List<Patient> pats = Context.getPatientService().getPatients(null, patientID, null, true);
						Patient patient = null;
						for (Patient pat : pats) {
							patient = pat.getPatientIdentifier().getIdentifier().equalsIgnoreCase(patientID) ? pat : null;
						}
						
						if (patient != null) {
							EncounterRole unknownEncounterRole = encounterService
							        .getEncounterRoleByUuid("a0b03050-c99b-11e0-9572-0800200c9a66");
							Encounter encounter = new Encounter();
							
							encounter.setPatient(patient);
							SimpleDateFormat availDate = new SimpleDateFormat("yyyy-MM-dd");
							Date encounterDate = availDate.parse(request.getParameter("encounterDate"));
							encounter.setEncounterDatetime(encounterDate);
							encounter.setLocation(locationService.getLocation(Integer.parseInt(request
							        .getParameter("selectedLocation"))));
							
							encounter.setEncounterType(encounterService.getEncounterType(Integer.parseInt(request
							        .getParameter("selectedEncounterType"))));
							
							encounter.setProvider(unknownEncounterRole,
							    providerService.getProvider(Integer.parseInt(request.getParameter("selectedProvider"))));
							
							encounter.setForm(formService.getForm("Oncology Screening Lab Results"));
							// add Spacemen code
							Obs specimenCodeObs = new Obs();
							specimenCodeObs.setConcept(conceptService
							        .getConceptByUuid("16cd65e3-45af-4291-88fd-fe4d91847e4f"));
							specimenCodeObs.setValueText(row.getCell(1).getStringCellValue());
							specimenCodeObs.setObsDatetime(encounterDate);
							specimenCodeObs.setPerson(patient.getPerson());
							encounter.addObs(specimenCodeObs);
							
							// add test type code
							
							encounter.addObs(createCodedObs(patient.getPerson(),
							    conceptService.getConceptByUuid("7e4e6554-d6c5-4ca3-b371-49806a754992"),
							    conceptService.getConceptByUuid("f7c2d59d-2043-42ce-b04d-08564d54b0c7"), encounterDate));
							
							// add lab Results code
							String result = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? "" : row
							        .getCell(2).getStringCellValue();
							Obs labResults = null;
							Obs nextVisit = null;
							Calendar currentDate = Calendar.getInstance();
							currentDate.setTime(encounterDate);
							
							Obs HIVStatus = obsService.getObservationsByPersonAndConcept(patient.getPerson(),
							    conceptService.getConceptByUuid("aec6ad18-f4dd-4cfa-b68d-3d7bb6ea908e")).isEmpty() ? null
							        : obsService.getObservationsByPersonAndConcept(patient.getPerson(),
							            conceptService.getConceptByUuid("aec6ad18-f4dd-4cfa-b68d-3d7bb6ea908e")).get(0);
							
							if (result == null) {
								
							} else if (result.equalsIgnoreCase("POSITIVE")) {
								labResults = createCodedObs(patient.getPerson(),
								    conceptService.getConceptByUuid("bfb3eb1e-db98-4846-9915-0168511c6298"),
								    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"), encounterDate);
							} else if (result.equalsIgnoreCase("NEGATIVE")) {
								labResults = createCodedObs(patient.getPerson(),
								    conceptService.getConceptByUuid("bfb3eb1e-db98-4846-9915-0168511c6298"),
								    conceptService.getConceptByUuid("64c23192-54e4-4750-9155-2ed0b736a0db"), encounterDate);
								
								if (HIVStatus != null
								        && HIVStatus.getValueCoded().getUuid()
								                .equalsIgnoreCase("3cd3a7a2-26fe-102b-80cb-0017a47871b2")) {
									currentDate.add(Calendar.YEAR, 3);
								} else {
									currentDate.add(Calendar.YEAR, 5);
								}
								nextVisit = createDatetimeObs(patient.getPerson(),
								    conceptService.getConceptByUuid("3ce94df0-26fe-102b-80cb-0017a47871b2"),
								    currentDate.getTime(), encounterDate);
								encounter.addObs(nextVisit);
								
							} else if (result.equalsIgnoreCase("FAIL")) {
								labResults = createCodedObs(patient.getPerson(),
								    conceptService.getConceptByUuid("bfb3eb1e-db98-4846-9915-0168511c6298"),
								    conceptService.getConceptByUuid("3b989534-ca6b-4bef-b99c-cd8397b1cdbe"), encounterDate);
							}
							if (labResults != null) {
								
								encounter.addObs(labResults);
								
								// add positive type code
								
								String typeOfPositiveInText = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
								        : row.getCell(3).getStringCellValue();
								if (typeOfPositiveInText == "") {
									
								}
								if (typeOfPositiveInText.contains("HPV 16")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("059fddd3-711f-47ab-818f-087984aeecc3"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 18")) {
									
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("b672c3ff-96c9-41cd-9ae6-aa0811ce347f"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 31")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("198b9d57-3485-44d9-a217-854f54b0fa71"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 33")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("8447c8d5-2b25-4493-a73b-0260eba89a59"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 35")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("cd560cd5-aa27-4bee-af53-34bd25085a04"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 39")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("65488086-9214-4023-ae07-9645d02fa072"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 45")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("e25fe88d-807c-460f-9af6-5014c0b34215"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 51")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("abce5039-0895-4086-8ca3-20c9c20841ce"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 52")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("2470fab8-d2ac-4fa9-9339-53459d337c5a"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 53")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("4b1380c7-2a04-4b2c-89df-05458cb4217f"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 56")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("fa898d97-dd36-44e0-9fbe-f217a5560f9d"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 58")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("bd7ef4b1-c84b-4d1b-9771-5cf5aa55d7f6"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 59")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("8d166674-9325-436d-95c6-a8321692496c"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 66")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("523b0f10-91eb-43de-bcc3-f1594929b514"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 68")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("3ca76719-dc99-42b7-842c-570d0dc55b8c"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 73")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("80ed3ab9-5989-429c-9754-9a1a691ff8bf"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("HPV 82")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("4ad7e1b2-0444-4f97-9e73-f24b7298c990"),
									    encounterDate));
								}
								if (typeOfPositiveInText.contains("OTHER HR HPV")) {
									encounter.addObs(createCodedObs(patient.getPerson(),
									    conceptService.getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4"),
									    conceptService.getConceptByUuid("6c3428c6-f406-4ef9-b2dd-4fe5a79f3432"),
									    encounterDate));
								}
								
								encounterService.saveEncounter(encounter);
								sendMessage(patient, labResults, nextVisit);
							} else {
								
								String columnZero = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
								        : row.getCell(0).getStringCellValue();
								String columnOne = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
								        : row.getCell(1).getStringCellValue();
								String columnTwo = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
								        : row.getCell(2).getStringCellValue();
								String columnThree = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
								        : row.getCell(3).getStringCellValue();
								String columnFour = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
								        : row.getCell(4).getStringCellValue();
								
								labResultsObjects.add(new labResultsObject(columnZero, columnOne, columnTwo, columnThree,
								        columnFour));
								
							}
							
						} else {
							
							String columnZero = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
							        : row.getCell(0).getStringCellValue();
							String columnOne = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? "" : row
							        .getCell(1).getStringCellValue();
							String columnTwo = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? "" : row
							        .getCell(2).getStringCellValue();
							String columnThree = row.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
							        : row.getCell(3).getStringCellValue();
							String columnFour = row.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) == null ? ""
							        : row.getCell(4).getStringCellValue();
							
							labResultsObjects.add(new labResultsObject(columnZero, columnOne, columnTwo, columnThree,
							        columnFour));
							
						}
						
					}
					
					modelAndView.addObject("failedRows", labResultsObjects);
					//						mav.setViewName(getViewName());
					
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("request doesn't contain a fileeeeeeeeeeeeeeeeeeeeee");
			}
		} else {
			System.out.println("request not a MultipartHttpServletRequest");
			
		}
		
		return modelAndView;
	}
	
	/**
	 * All the parameters are optional based on the necessity
	 * 
	 * @param httpSession
	 * @param anyRequestObject
	 * @param errors
	 * @return
	 */
	
	private Obs createCodedObs(Person person, Concept question, Concept Answer, Date obsDate) {
		Obs createdObs = new Obs();
		createdObs.setConcept(question);
		createdObs.setValueCoded(Answer);
		createdObs.setObsDatetime(obsDate);
		createdObs.setPerson(person);
		return createdObs;
	}
	
	private Obs createDatetimeObs(Person person, Concept question, Date Answer, Date obsDate) {
		Obs createdObs = new Obs();
		createdObs.setConcept(question);
		createdObs.setValueDatetime(Answer);
		createdObs.setObsDatetime(obsDate);
		createdObs.setPerson(person);
		return createdObs;
	}
	
	private void sendMessage(Patient p, Obs o, Obs nextVisit) {
		// ================ SMS =======================
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String phoneNumber = null;
		if (p.getPerson().getAttribute("Phone number") != null) {
			phoneNumber = p.getPerson().getAttribute("Phone number").getValue();
		}
		//		PersonAddress pa = null;
		//		Set<PersonAddress> personAddresses = p.getAddresses();
		//		for (PersonAddress pad : personAddresses) {
		//			if (pad.getCountyDistrict() != null
		//			        && (pad.getCountyDistrict().equalsIgnoreCase("Kayonza") || pad.getCountyDistrict().contains("Kayonza"))) {
		//				pa = pad;
		//			}
		//		}
		
		try {
			
			String apiUsername = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.username");
			String apiPassword = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.password");
			String authorizationURL = Context.getAdministrationService().getGlobalProperty(
			    "rbcmessaging.mohpih.authorizationURL");
			String postURL = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.postURL");
			String sender = Context.getAdministrationService().getGlobalProperty("rbcmessaging.mohpih.sender");
			
			// String jsonCredential = "{\"api_username\":
			// \"20D13C54-CC1B-4D5A-ADCE-40189C57D728\", \"api_password\":
			// \"49931A9A-DB96-499C-878C-98D13C358D2B\"}";
			String jsonCredential = "{\"api_username\": \"" + apiUsername + "\",  \"api_password\": \"" + apiPassword
			        + "\"}";
			// HttpPost httpPostCredential = new
			// HttpPost("https://messaging.fdibiz.com/api/v1/auth/");
			HttpPost httpPostCredential = new HttpPost(authorizationURL);
			
			httpPostCredential.setEntity(new StringEntity(jsonCredential));
			
			httpPostCredential.setHeader("Content-type", "application/json");
			
			CloseableHttpClient clientCredential = HttpClients.createDefault();
			CloseableHttpResponse responseCredential = clientCredential.execute(httpPostCredential);
			String resultsCredential = EntityUtils.toString(responseCredential.getEntity());
			// System.out.println(resultsCredential);
			
			String access_token = "";
			String[] resultsStringArray = resultsCredential.split(",");
			int i = 0;
			for (String st : resultsStringArray) {
				
				int j = st.indexOf("access_token");
				if (j > 0) {
					// System.out.println(st.split("\"")[3]);
					access_token = "Bearer " + st.split("\"")[3];
				} else {
					continue;
				}
			}
			clientCredential.close();
			UUID uuid = UUID.randomUUID();
			String uuidAsString = uuid.toString();
			
			int licationId = Context
			        .getLocationService()
			        .getLocation(
			            Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
			                "rbcmessaging.mohpih.locationId"))).getLocationId();
			
			Concept hpvPositive = null;
			Concept hpvNegative = null;
			Concept hpvFail = null;
			String nextVisitDate = "";
			
			//			List<Obs> obs = session.getSubmissionActions().getObsToCreate();
			//			for (Obs o : obs) {
			if (o != null) {
				if (o.getConcept().getUuid().equalsIgnoreCase("bfb3eb1e-db98-4846-9915-0168511c6298")) {
					if (o.getValueCoded().getUuid().equalsIgnoreCase("1b4a5f67-6106-4a4d-a389-2f430be543e4")) {
						hpvPositive = o.getValueCoded();
					}
					if (o.getValueCoded().getUuid().equalsIgnoreCase("64c23192-54e4-4750-9155-2ed0b736a0db")) {
						hpvNegative = o.getValueCoded();
					}
					if (o.getValueCoded().getUuid().equalsIgnoreCase("3b989534-ca6b-4bef-b99c-cd8397b1cdbe")) {
						hpvFail = o.getValueCoded();
					}
				}
			}
			if (nextVisit != null) {
				if (nextVisit.getConcept().getUuid().equalsIgnoreCase("3ce94df0-26fe-102b-80cb-0017a47871b2")) {
					nextVisitDate = formatter.format(nextVisit.getValueDate());
				}
			}
			//			}
			
			String messageBody = null;
			
			if (hpvPositive != null) {
				
				messageBody = p.getFamilyName()
				        + " "
				        + p.getGivenName()
				        + ". Igisubizo cy' ikizamini gisuzuma virus itera kanseri y'inkondo y'umura-HPV cyabonetse, murasabwa kujya ku ivuriro  mwisuzumishirijeho";
			}
			if (hpvNegative != null) {
				
				messageBody = p.getFamilyName()
				        + " "
				        + p.getGivenName()
				        + ". Igisubizo cy'ikizamini gisuzuma virus itera kanseri y'inkondo y'umura-HPV, kigaragaza ko mudafite iyo virus, murasabwa kugaruka nyuma y'itariki ya "
				        + nextVisitDate;
			}
			
			if (hpvFail != null) {
				
				messageBody = p.getFamilyName()
				        + " "
				        + p.getGivenName()
				        + ". Murasabwa kujya ku ivuriro  mwisuzumishirijeho, kugirango basubiremo ikizamini gisuzuma virus itera kanseri y'inkondo y'umura-HPV";
			}
			
			if (messageBody != null && phoneNumber != null) {
				String json = "{\"msisdn\": \"" + phoneNumber + "\",  \"message\": \"" + messageBody + "\",  \"msgRef\": \""
				        + uuidAsString + "\", \"sender_id\": \"" + sender + "\"}";
				
				HttpPost httpPost = new HttpPost(postURL);
				
				httpPost.setEntity(new StringEntity(json));
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				httpPost.setHeader("Authorization", access_token);
				
				CloseableHttpClient client = HttpClients.createDefault();
				CloseableHttpResponse response = client.execute(httpPost);
				System.out.println(response.getStatusLine());
				// System.out.println("access_token: " + access_token);
				client.close();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		// =============== end SMS ====================
		
	}
	
	/**
	 * This class returns the form backing object. This can be a string, a boolean, or a normal java
	 * pojo. The bean name defined in the ModelAttribute annotation and the type can be just defined
	 * by the return type of this method
	 */
	
}
