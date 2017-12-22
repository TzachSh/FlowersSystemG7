package Branches;

import java.util.ArrayList;

import Survey.SurveyConclusion;
import Users.Permission;

public class ServiceExpert extends Employee {
	
	private ArrayList<SurveyConclusion> surveyConclusionList;

	public ArrayList<SurveyConclusion> getSurveyConclusionList() {
		return surveyConclusionList;
	}

	public void setSurveyConclusionList(ArrayList<SurveyConclusion> surveyConclusionList) {
		this.surveyConclusionList = surveyConclusionList;
	}

	public ServiceExpert(int uId, String user, String password, boolean isLogged,Permission permission, int eId, Role role, Branch branch,
			ArrayList<SurveyConclusion> surveyConclusionList) {
		super(uId, user, password, isLogged,permission, eId, role, branch);
		this.surveyConclusionList = surveyConclusionList;
	}
}