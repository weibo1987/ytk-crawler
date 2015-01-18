package me.tony.action;

import javax.annotation.Resource;

import me.tony.service.YTKService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="ytk")
public class YTKAction {

	@Resource
	private YTKService ytkService;
	
	@ResponseBody
	@RequestMapping(value="/course")
	public String course(){
		this.ytkService.pullCourse();
		return "course";
	}
	
	@ResponseBody
	@RequestMapping(value="/courseVersionGrade")
	public String courseVersion(){
		this.ytkService.pullCourseVersionGrade();
		return "course version";
	}
	
	@ResponseBody
	@RequestMapping(value="/courseGrade")
	public String courseGrade(){
		this.ytkService.pullCourseGrade();
		return "course grade";
	}
	
	@ResponseBody
	@RequestMapping(value="/courseTree")
	public String courseTree(){
		this.ytkService.pullCourseTree();
		return "course tree";
	}
	
	@ResponseBody
	@RequestMapping(value="/questions")
	public String questions(){
		this.ytkService.pullQuestions();
		System.out.println("FINISH PULL questions.");
		return "course questions";
	}
	
	@ResponseBody
	@RequestMapping(value="/pullGKTypeCourse")
	public String pullGKTypeCourse(){
		this.ytkService.pullGKType();
		System.out.println("FINISH pullGKTypeCourse.");
		return "success";
	}
	
	
	@ResponseBody
	@RequestMapping(value="/pullGKCourseTree")
	public String pullGKCourseTree(){
		this.ytkService.pullGKCourseTree();
		System.out.println("FINISH pullGKCourseTree.");
		return "start do....";
	}
	
	@ResponseBody
	@RequestMapping(value="/pullGKQuestions")
	public String pullGKQuestions(){
		this.ytkService.pullGKQuestions();
		System.out.println("FINISH pullGKQuestions.");
		return "start do....";
	}
	
	@ResponseBody
	@RequestMapping(value="/parseImg")
	public String parseImg(){
		this.ytkService.parseJsonToDB();
		System.out.println("FINISH parseImg.");
		return "start do....";
	}
	
	@ResponseBody
	@RequestMapping(value="/downloadImg")
	public String downloadImg(){
		this.ytkService.downloadImg();
		System.out.println("FINISH downloadImg.");
		return "start do....";
	}
}
