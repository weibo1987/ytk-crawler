package me.tony.service;

public interface YTKService {

	/**
	 * pull course info .
	 */
	public void pullCourse();

	/**
	 * pull course version
	 */
	public void pullCourseVersionGrade();
	
	public void pullCourseGrade();

	public void pullCourseTree();

	public void pullQuestions();

	/////// gao kao 
	public void pullGKType();
	
	public void pullGKCourse(String typeID);
	
	public void pullGKCourseTree();
	
	public void pullGKCourseTree(String course,String typeID);
	
	public void pullGKQuestions();
	
	
	////////// parse json to DB
	public void parseJsonToDB();
	
	public void downloadImg();
}
