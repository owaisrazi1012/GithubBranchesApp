package com.nisum.util;

public final class Constants {

	private Constants() {
	}

	public static final String INPUT_FIELDS_EMPTY_MSG = "Input is not correct. Some fields are empty";
	public static final String NO_RECORD_FOUND="No Record found";
	public static final String INVALID_TOKEN = "Invalid token";
	public static final String NOT_FOUND="NotFound";

	public static final String NO_RECORD_FOUND_OR_USER_IS_NOT_ACTIVE="No Record found or User is not Active";

	public static final String INVALID_EMAIL_OR_PASSWORD="Invalid email or password";

	public static final String RECORD_CREATED="Record Created Successfully";
	public static final String RECORD_UPDATED="Record Updated Successfully";
	public static final String RECORD_DELETED="Record Deleted Successfully";

	public static final String ELK_INDEX_FOR_PROJECTS = "projects";

	public static final String AUTHORIZATION = "Authorization";

	public static final String BEARER ="Bearer ";

	public static final String TOTAL_ELASTIC_SEARCH_LOG_STRING = "Total logHits retrieved from elastic search {}";
	public static final String COUNT_SCENARIO_ELASTIC_SEARCH_LOG_STRING = "Count Scenarios for Project: {} with Build: {} ";
	public static final String CALCULATING_SCENARIO_ELASTIC_SEARCH_LOG_STRING =  "Calculating scenarios pass fail counts...";
	public static final String CALCULATING_SCENARIO_ELASTIC_SEARCH_LOG_STRING2 =  "Calculating scenarios pass fail counts...";


}
