package me.tony.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.Resource;

import me.tony.YTKClient;
import me.tony.service.YTKService;
import me.tony.util.ClientUtils;
import me.tony.util.ImgUtils;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service("ytkService")
public class YTKServiceImpl implements YTKService{

	@Resource
	private JdbcTemplate jdbcTemplate;

	private HttpClient client=YTKClient.getClient();

	private ResourceBundle rb=ResourceBundle.getBundle("config/course");
	
	@Resource
	private ThreadPoolTaskExecutor taskExecutor;
	
	private static String IMG="0";
	private static String BASE64="1";
	
	@Override
	public void pullCourse() {
		JSONArray array=new JSONArray(ClientUtils.handle("course init", "http://www.yuantiku.com/iphone/"+rb.getString("type")+"/courses?version=3.0.1&quizId=285&av=2",client));
		System.out.println(array);
		for(int i=0;i<array.length();i++){
			JSONObject cc=array.getJSONObject(i);
			this.jdbcTemplate.update("insert into course(id,name,prefix) values(?,?,?)",new Object[]{cc.get("id"),cc.getString("name"),cc.getString("prefix")});

		}
	}

	@Override
	public void pullCourseVersionGrade() {
		String[] prefix=rb.getString("course").split(",");

		for(String p:prefix){
			JSONArray array=new JSONArray(ClientUtils.handle("course version","http://www.yuantiku.com/iphone/"+p+"/keypoint-trees?version=3.0.1&av=2",client));
			this.jdbcTemplate.update("insert into course_version(prefix,version_info) values(?,?)",new Object[]{p,array.toString()});
			for(int i=0;i<array.length();i++){
				String version=array.getJSONObject(i).get("id").toString();
				System.out.println("version--------"+version);
				if(switchCourse(p,version)){
					String tmp=ClientUtils.handle("course grade","http://www.yuantiku.com/iphone/"+p+"/categories?version=3.0.1&level=0&deep=false&av=2",client);
					this.jdbcTemplate.update("insert into course_grade(course,grade_info,version_id) values(?,?,?)",new Object[]{p,tmp,version});
				}
			}
		}
	}

	@Override
	public void pullCourseGrade() {

		String[] prefix=rb.getString("course").split(",");
		for(String p:prefix){
			this.jdbcTemplate.update("insert into course_grade(course,grade_info) values(?,?)",new Object[]{p,ClientUtils.handle("course grade","http://www.yuantiku.com/iphone/"+p+"/categories?version=3.0.1&level=0&deep=false&av=2",client)});
		}
	}

	@Override
	public void pullCourseTree() {
		final List<Map<String, Object>> list=this.jdbcTemplate.queryForList("select * from course_grade");

		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for( Map<String,Object> map:list){

					JSONArray array=new JSONArray(map.get("grade_info").toString());
					String version=map.get("version_id").toString();							// version
					String course=map.get("course").toString();
					System.out.println(course+"------------------------------------------------");
					if(switchCourse(map.get("course").toString(),version)){
						for(int i=0;i<array.length();i++){
							String id=array.getJSONObject(i).get("id").toString();
							String url="http://www.yuantiku.com/iphone/"+map.get("course")+"/categories/"+id+"/keypoints?version=3.0.1&level=0&deep=true&av=2";
							JSONArray treeArray=new JSONArray(ClientUtils.handle(array.getJSONObject(i).getString("name"), url, client));
							parseCourseTree(id,map.get("course").toString(),ClientUtils.handle(array.getJSONObject(i).getString("name"), url, client),version,"0");
						}
					}

				}
				System.out.println("FINISH PULL COURSE TREE.");
			}
		});

	}


	@Override
	public void pullQuestions() {
		
		

		File dir=new File("d:/"+rb.getString("type"));
		if(!dir.exists())dir.mkdirs();
		final List<Map<String, Object>> list=this.jdbcTemplate.queryForList("SELECT id,count,course,question_ids FROM course_tree WHERE question_ids != ''");
		
		taskExecutor.execute(new Runnable() {
			
			@Override
			public void run() {

					for(Map<String,Object> map:list){
						
						int count=Integer.parseInt(map.get("count").toString());

						if(count<=100){
							String url="http://www.yuantiku.com/iphone/"+map.get("course").toString()+"/solutions?version=3.0.1&format=json&ids="+map.get("question_ids").toString()+"&av=2";
							
							try{
								String tmp=ClientUtils.handle("questions_handle", url, client);
								FileUtils.writeStringToFile(new File("d:/"+rb.getString("type")+"/"+map.get("id").toString()+".json"),tmp);
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}else{
							String question_ids=map.get("question_ids").toString();
							String[] array=question_ids.split(",");
							int mark=1;
							String tmp="";	//// collect question_ids every 100 .
							for(int i=0;i<array.length;i++){
								if(i==Math.min(mark*100, array.length)-1){
									//// do task
									tmp=tmp+array[i];
									String url="http://www.yuantiku.com/iphone/"+map.get("course").toString()+"/solutions?version=3.0.1&format=json&ids="+tmp+"&av=2";
									try{
										String _tmp=ClientUtils.handle("questions_handle", url, client);
										FileUtils.writeStringToFile(new File("d:/"+rb.getString("type")+"/"+map.get("id").toString()+"_"+mark+".json"),_tmp);
									}catch(Exception ex){
										ex.printStackTrace();
									}
									///// reset
									tmp="";
									mark++;
								}else{
									tmp+=array[i]+",";
								}
							}
							
						}
					}
				System.out.println("FINISH DOWNLOAD GK QUESTIONS!!!");
			}
		});
	}

	private void parseCourseTree(String id, String course, String tree,String version,String parent) {
		if(tree==null||tree.equals("null"))return;
		JSONArray treeArray=new JSONArray(tree);
		for(int i=0;i<treeArray.length();i++){
			JSONObject obj=treeArray.getJSONObject(i);

			///// 处理自身节点
			try{
				String question_ids="";
				String is_leaf="N";
				if(!hasChildren(obj)){
					// 如果没有子节点，记录question_ids， 标记为 叶子节点。
					question_ids=getQuestionIds(obj.get("count").toString(),obj.get("id").toString(),course);
					is_leaf="Y";
				}
				this.jdbcTemplate.update("insert into course_tree(v_id,course,id,name,count,parent,question_ids,version_id,is_leaf) values(?,?,?,?,?,?,?,?,?)", new Object[]{id,course,obj.get("id").toString(),obj.getString("name"),obj.get("count").toString(),parent,question_ids,version,is_leaf});
			}catch(Exception e){
				e.printStackTrace();
				continue;
			}

			///// 处理子节点
			if(hasChildren(obj)){
				parseCourseTree(id,course,obj.get("children").toString(),version,obj.get("id").toString());
			}
		};
	}

	private boolean hasChildren(JSONObject obj) {
		Object children=obj.get("children");

		if(children==null) return false;

		String childrenStr=children.toString();

		boolean isEmpty=childrenStr.equals("null")||childrenStr.trim().equals("")||children.equals("[]");
		return !isEmpty;
	}


	private String getQuestionIds(String count,String nodeId,String course){

		if(count.equals("0"))return "";

		Map<String,String> map=new HashMap<String,String>();
		map.put("limit", count);
		map.put("type", "3");
		map.put("keypointId", nodeId);
		String url="http://www.yuantiku.com/iphone/"+course+"/exercises?version=3.0.1&av=2";
		String cc=ClientUtils.handlePost("solutions_"+course, url,map, client);

		try{
			if(cc!=null){
				JSONObject obj=new JSONObject(cc);
				String tt=obj.getJSONObject("sheet").get("questionIds").toString();
				return tt.substring(1, tt.length()-1);
			}
		}catch(Exception e){
			return "";
		}
		return "";

	}


	/**
	 * auto-switch course version.
	 * @param course
	 * @param version
	 * @return
	 */
	private boolean switchCourse(String course,String version){
		String url="http://www.yuantiku.com/iphone/"+course+"/users/keypoint-trees?version=3.0.1&av=2";
		Map<String,String> map=new HashMap<String,String>();
		map.put("treeId", version);
		return ClientUtils.handlePost("switch-course", url, map, client)!=null;
	}


	@Override
	public void pullGKType() {
		String url="http://www.yuantiku.com/iphone/gaokao/quizzes?version=3.0.1&av=2";
		String result=ClientUtils.handle("gao kao type", url, client);
		if(result==null||result.equals("[]")) return;
		JSONArray gkTypeArray=new JSONArray(result);

		for(int i=0;i<gkTypeArray.length();i++){
			JSONObject obj=gkTypeArray.getJSONObject(i);
			this.jdbcTemplate.update("insert into gk_type(type_id,type_name) values(?,?)", new Object[]{obj.get("id").toString(),obj.getString("name")});
			pullGKCourse(obj.get("id").toString());
			System.out.println(">>>>>>>"+obj.getString("name")+"-------------------------");
		}
	}

	@Override
	public void pullGKCourse(String typeID) {
		if(switchGKType(typeID)){
			//// switch gaokao type success.
			String url="http://www.yuantiku.com/iphone/gaokao/courses?version=3.0.1&quizId="+typeID+"&av=2";
			String result=ClientUtils.handle("pull gaokao course", url, client);
			if(result==null||result.equals("[]"))return;
			JSONArray array=new JSONArray(result);
			for(int i=0;i<array.length();i++){
				JSONObject obj=array.getJSONObject(i);
				this.jdbcTemplate.update("insert into course(course_id,course_code,course_name,gk_type_id) values(?,?,?,?)",new Object[]{obj.get("id").toString(),obj.getString("prefix"),obj.getString("name"),typeID});
			}
		}
	}


	private JdbcTemplate getJdbcTemplate(){
		return this.jdbcTemplate;
	}


	@Override
	public void pullGKCourseTree(String course_code,String typeID) {
		/////////	handle parents/root node.

		String url="http://www.yuantiku.com/iphone/"+course_code+"/categories?version=3.0.1&level=0&deep=true&av=2";
		String result=ClientUtils.handle(course_code, url, client);
		if(result==null||result.equals("[]"))return;
		JSONArray array;
		try{
			array=new JSONArray(result);
		}catch(Exception e){
			return;
		}
		handleGKTreeParse("0",array,course_code,typeID);
	}

	private void handleGKTreeParse(String parent,JSONArray array,String course_code,String typeID){
		for(int i=0;i<array.length();i++){
			JSONObject obj=array.getJSONObject(i);

			String node_id=obj.get("id").toString();
			String name=obj.getString("name");
			String count=obj.get("count").toString();
			String question_ids="";
			String is_leaf="N";// default is N

			////// if no children ,mark as leaf node. question_ids need into DB.
			if(!hasChildren(obj)){
				is_leaf="Y";
				question_ids=getQuestionIds(count, node_id, course_code);
			}

			try{
				this.jdbcTemplate.update("insert into course_tree(name,count,parent,question_ids,gk_type_id,is_leaf,node_id,course_code) values(?,?,?,?,?,?,?,?)", new Object[]{
						name,count,parent,question_ids,typeID,is_leaf,node_id,course_code
				});
			}catch(Exception e){
				e.printStackTrace();
				return ;
			}

			if(is_leaf.equals("N")){
				JSONArray c_array=new JSONArray(obj.get("children").toString());
				handleGKTreeParse(node_id,c_array,course_code,typeID);
			}
		}
	}



	@Override
	public void pullGKCourseTree() {
		final List<Map<String, Object>> list=this.jdbcTemplate.queryForList("select type_id from gk_type");
		taskExecutor.execute(new Runnable() {

			@Override
			public void run() {

				for(Map<String,Object> map:list){

					String typeId=map.get("type_id").toString();
					//////// switch gaokao type.
					if(!switchGKType(typeId))return;	
					System.out.println(">>>>>>>"+typeId+"-------------------------");
					List<Map<String, Object>> subjectList=getJdbcTemplate().queryForList("select course_code from course where gk_type_id='"+typeId+"'");

					for(Map<String,Object> m:subjectList){
						String course_code=m.get("course_code").toString();
						pullGKCourseTree(course_code,typeId);
					}

				}
			}
		});

	}

	@Override
	public void pullGKQuestions() {
		File dir=new File("d:/"+rb.getString("type"));
		if(!dir.exists())dir.mkdirs();
		final List<Map<String, Object>> typeList=this.jdbcTemplate.queryForList("select type_id from gk_type");
		
		taskExecutor.execute(new Runnable() {
			
			@Override
			public void run() {

				for(Map<String,Object> m:typeList){
					String gkType=m.get("type_id").toString();
					switchGKType(gkType);
					List<Map<String, Object>> list=getJdbcTemplate().queryForList("SELECT id,course_code,count,question_ids FROM course_tree WHERE question_ids != '' and gk_type_id ='"+gkType+"'");
					for(Map<String,Object> map:list){
						
						int count=Integer.parseInt(map.get("count").toString());

						if(count<=100){
							String url="http://www.yuantiku.com/iphone/"+map.get("course_code").toString()+"/solutions?version=3.0.1&format=json&ids="+map.get("question_ids").toString()+"&av=2";
							
							try{
								String tmp=ClientUtils.handle("questions_handle", url, client);
								FileUtils.writeStringToFile(new File("d:/"+rb.getString("type")+"/"+map.get("id").toString()+".json"),tmp);
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}else{
							String question_ids=map.get("question_ids").toString();
							String[] array=question_ids.split(",");
							int mark=1;
							String tmp="";	//// collect question_ids every 100 .
							for(int i=0;i<array.length;i++){
								if(i==Math.min(mark*100, array.length)-1){
									//// do task
									tmp=tmp+array[i];
									String url="http://www.yuantiku.com/iphone/"+map.get("course_code").toString()+"/solutions?version=3.0.1&format=json&ids="+tmp+"&av=2";
									try{
										String _tmp=ClientUtils.handle("questions_handle", url, client);
										FileUtils.writeStringToFile(new File("d:/"+rb.getString("type")+"/"+map.get("id").toString()+"_"+mark+".json"),_tmp);
									}catch(Exception ex){
										ex.printStackTrace();
									}
									///// reset
									tmp="";
									mark++;
								}else{
									tmp+=array[i]+",";
								}
							}
							
						}
					}
				}
				System.out.println("FINISH DOWNLOAD GK QUESTIONS!!!");
			}
		});
	}
	private  boolean switchGKType(String gkType) {
		String url="http://www.yuantiku.com/iphone/gaokao/users/quiz/"+gkType+"?version=3.0.1&av=2";
		return ClientUtils.handlePut(gkType, url, client)!=null;
	}

	@Override
	public void parseJsonToDB() {
		String course_spell="course";
		if(rb.getString("type").equals("gaokao"))course_spell="course_code";
		final String[] courseArray=new String[]{course_spell};
		final List<Map<String, Object>> list=this.jdbcTemplate.queryForList("SELECT id,"+courseArray[0]+",count FROM course_tree WHERE question_ids != ''");
		taskExecutor.execute(new Runnable() {
			public void run() {
				for(Map<String, Object> map:list){
					String id=map.get("id").toString();
					System.out.println("开始解析题目"+id+"中的图片--------------------------------------------------------------------------");
					String course=map.get(courseArray[0]).toString();
					int count=Integer.parseInt(map.get("count").toString());
					String tmp="";
					try{
						if(count<=100){
							tmp=FileUtils.readFileToString(new File("d:/"+rb.getString("type")+"/"+id+".json"));
							parseBase64Key(tmp,course);
							parseImg(tmp,course);
						}else{
							for(int i=0;i<count/100;i++){
								try{
									tmp=FileUtils.readFileToString(new File("d:/"+rb.getString("type")+"/"+id+"_"+(i+1)+".json"));
									parseBase64Key(tmp,course);
									parseImg(tmp,course);
								}catch(Exception e){
									continue;
								}
							}
						}
					}catch(Exception e){
						continue;
					}
				}
				
				// move data.
				getJdbcTemplate().execute("insert into img_cache(code,type,course) select code,type,course from img_cache_temp group by code");
				System.out.println("解析图片完毕");
			}
		});
	}

	private  void parseBase64Key(String objStr,String course) {
		if(!objStr.contains("widthRatio="))return;
		String[] tmpArray=objStr.split("widthRatio=");
		
		for(int i=0;i<tmpArray.length-1;i++){
			String tmp=tmpArray[i];
			tmp=tmp.substring(tmp.lastIndexOf("\"")+1,tmp.length()-1);
			this.jdbcTemplate.update("insert into img_cache_temp(code,type,course) values(?,?,?)", new Object[]{tmp,"1",course});
//			System.out.println(tmp+"--------------");
		}
	}	
	
	
	private void doParse(String id,String course) throws IOException{
		String tmp=FileUtils.readFileToString(new File("d:/"+rb.getString("type")+"/"+id+".json"));
		parseBase64Key(tmp,course);
		parseImg(tmp,course);
	}
	
	private  void parseImg(String objStr,String course) {
		if(!objStr.contains("width="))return;
		String[] tmpArray=objStr.split("width=");
		
		for(int i=0;i<tmpArray.length-1;i++){
			String tmp=tmpArray[i];
			tmp=tmp.substring(tmp.lastIndexOf("\"")+1,tmp.length()-1);
			this.jdbcTemplate.update("insert into img_cache_temp(code,type,course) values(?,?,?)", new Object[]{tmp,"0",course});
//			System.out.println(tmp+"--------------");
		}
	}

	@Override
	public void downloadImg() {
		startDownload(0);
	}

	private void startDownload(int i) {
		List<Map<String, Object>> list=this.jdbcTemplate.queryForList("SELECT * FROM img_cache GROUP BY CODE ORDER BY id LIMIT "+i+",1000");
		if(list==null||list.isEmpty())return;
		///// do logic
		int j=0;
		for(Map<String, Object> map:list){
			String code=map.get("code").toString();
			String type=map.get("type").toString();
			String course=map.get("course").toString();
			System.out.println(i*1000+(j++)+"-----------------------");
			if(BASE64.equals(type)){
				downloadBASE64(code,course);
			}
			
			else if(IMG.equals(type)){
				System.out.println("download img style");
				downloadImgType(code,course);
			}
			
		}
		
		
		//// -- do logic
		startDownload(i+1);
	}

	private void downloadImgType(String code, String course) {
		String url="http://ytk.fbcontent.cn/iphone/"+course+"/images/"+code+"?version=3.0.1&av=2";
		
		File dir=new File(rb.getString("dst")+"/"+course+"/");
		if(!dir.exists())dir.mkdirs();
		try {
			FileUtils.copyURLToFile(new URL(url), new File(dir+"/"+code), 30000, 30000);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private void downloadBASE64(String code, String course) {
		code=URLEncoder.encode(code);
		try {
			String url="http://ytk.fbcontent.cn/iphone/chuzhong/accessories/formulas/batch?version=3.0.1&latexs="+code+"&color=666666&fontSize=30&av=2";
			
			String tmp=ClientUtils.handle("get base64 code", url, client);
			if(tmp==null||tmp.equals("")||tmp.equals("[]")) return;
			JSONArray array=new JSONArray(tmp);
			String base64Code=array.getString(0);
			
			File dir=new File(rb.getString("dst")+"/"+course+"/");
			if(!dir.exists())dir.mkdirs();
			ImgUtils.GenerateImage(base64Code, dir.getAbsolutePath()+"/"+code+".jpg");
			

		}  catch (Exception e) {
			e.printStackTrace();
		}		
	}	
	
	
	
	
//	public static void main(String[] args) {
//
//		File dir=new File("d:/gaozhong/");
//
//		File[] c_file=dir.listFiles();
//		for(File f:c_file){
//			String tmp;
//			try {
//				tmp = FileUtils.readFileToString(f);
//				if(tmp==null) continue;
////				parseBase64Key(tmp);
//				parseImg(tmp);
//			} catch (IOException e) {
//				e.printStackTrace();
//				continue;
//			}
//		}
//	}
}
