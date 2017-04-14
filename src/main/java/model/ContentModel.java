package model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import esayhelper.DBHelper;
import esayhelper.JSONHelper;
import esayhelper.StringHelper;
import esayhelper.formHelper;
import esayhelper.jGrapeFW_Message;
import esayhelper.formHelper.formdef;
import jodd.util.ArraysUtil;

@SuppressWarnings("unchecked")
public class ContentModel {
	private static DBHelper dbcontent;
	private static formHelper _form;
//	private static session session;
//	private static JSONObject _obj_session = new JSONObject();
	static {
//		session = new session();
		dbcontent = new DBHelper("mongodb", "content", "_id");
//		_obj_session.put("content", session.insertSession("content", dbcontent.select()
//				.toString()));
		_form = dbcontent.getChecker();
	}

	public ContentModel() {
		_form.putRule("mainName"/*,content,wbid"*/, formdef.notNull);
	}

	/**
	 * 发布文章
	 * 
	 * @param content
	 * @return 
	 * && 1：判断字段是否合法 
	 * && 2：必填信息没有填 
	 * && 3：同栏目下已存在该文章 
	 * && 4：同站点下已存在该文章
	 * && 5：是否含有敏感词 接入第三方插件
	 */
	// 不允许重复添加，但存在同名不同内容的文章
	public int insert(JSONObject content) {
		if (!_form.checkRuleEx(content)) {
			return 2;
		}
//		int ckcode = _form.check_forminfo(content);
//		if (ckcode == 1) {
//			return 2;
//		}
		if (content.get("mainName").toString().equals("")) {
			return 1;
		}
		if (!content.get("fatherid").toString().equals("0")) {
			content.remove("ogid");
		}
		return dbcontent.data(content).insertOnce() != null ? 0 : 99;
	}

	public int UpdateArticle(String oid,JSONObject content) {
		// 非空字段判断
		if (!_form.checkRuleEx(content)) {
			return 2;
		}
		if (content.get("mainName").toString().equals("")) {
			return 1;
		}
		return dbcontent.eq("_id", new ObjectId(oid)).data(content).update() != null ? 0 : 99;
	}

	public int DeleteArticle(String oid) {
		return dbcontent.eq("_id", new ObjectId(oid)).delete()!=null?0:99;
//		return dbcontent.delete(new ObjectId(oid)) == true ? 0 : 99;
	}

	/**
	 * 删除指定栏目下的指定文章
	 * 
	 * @param oid
	 *          文章id
	 * @param ogid
	 *          栏目id
	 */
	public int deleteByOgID(String oid, String ogid) {
		// 查询oid对应的文章
		JSONObject _obj = (JSONObject) dbcontent.eq("_id", new ObjectId(oid)).select().get(0);
		// 获取栏目id
		String values = _obj.get("ogid").toString();
		values = values.replace(ogid, "");
//		String[] value = _obj.get("ogid").toString().split(",");
//		String fieldValue = StringHelper.join(ArraysUtil.remove(value, ArraysUtil.indexOf(
//				value, ogid), 1));
		JSONObject obj = new JSONObject();
		obj.put("ogid", values);
		return dbcontent.eq("_id", new ObjectId(oid)).data(obj).update() != null ? 0 : 99;
	}

	/**
	 * 删除指定站点下的指定文章
	 * 
	 * @param oid
	 *          文章id
	 * @param wbid
	 *          站点id
	 * @return
	 */
	public int deleteByWbID(String oid, String wbid) {
		// 查询oid对应的文章
		JSONObject _obj = (JSONObject) dbcontent.eq("_id", new ObjectId(oid)).select().get(0);
		// 获取站点id
		String values = _obj.get("wbid").toString();
		values = values.replace(wbid, "");
//		String[] value = _obj.get("wbid").toString().split(",");
//		String fieldValue = StringHelper.join(ArraysUtil.remove(value, ArraysUtil.indexOf(
//				value, wbid), 1));
		JSONObject obj = new JSONObject();
		obj.put("wbid", values);
		return dbcontent.eq("_id", new ObjectId(oid)).data(obj).update() != null ? 0 : 99;
	}

	/**
	 * 文章已存在，设置栏目
	 * 
	 * @param oid
	 * @param ogid
	 * @return
	 */
	public int setGroup(String oid, String ogid) {
		// 查询oid对应的文章
		JSONObject _obj = (JSONObject) select(oid).get(0);
		// 获取栏目id
		String[] value = _obj.get("ogid").toString().split(",");
		// 判断该栏目是否存在
		if (ArraysUtil.contains(value, ogid)) {
			return 3; // 返回3 文章已存在于该栏目下
		}
		String values = StringHelper.join(ArraysUtil.append(value, ogid));
		JSONObject obj = new JSONObject();
		obj.put("ogid", values);
		return dbcontent.eq("_id", new ObjectId(oid)).data(obj).update() != null ? 0 : 99;
	}
	public int setGroup(JSONArray array,String ogid) {
		int code = 99;
		// 获取栏目id
		for (int i = 0; i < array.size(); i++) {
			JSONObject _obj = (JSONObject) array.get(i);
			String values = _obj.get("ogid").toString();
			values = values.replace(ogid, "0");
//			ArraysUtil.remove(value, ArraysUtil.indexOf(value, ogid), 1);
//			String values = StringHelper.join(ArraysUtil.append(value, "0"));
			JSONObject obj = new JSONObject();
			JSONObject obj1 = new JSONObject();
			obj.put("ogid", values);
			obj1 = (JSONObject) _obj.get("_id");
			code = dbcontent.eq("_id", new ObjectId(obj1.get("$oid").toString())).data(obj).update() != null ? 0 : 99;
		}
		return code;
	}
	public JSONObject page(int idx, int pageSize) {
		JSONArray array = dbcontent.page(idx, pageSize);
		JSONObject object = new JSONObject(){
			private static final long serialVersionUID = 1L;

			{
				put("totalSize", (int)Math.ceil((double)dbcontent.count()/pageSize));
				put("currentPage", idx);
				put("pageSize", pageSize);
				put("data", array);
				
			}
		};
		return object;
	}

	public JSONObject page(int idx, int pageSize,String content) {
		Set<Object> set = JSONHelper.string2json(content).keySet();
		for (Object object2 : set) {
			dbcontent.eq(object2.toString(), JSONHelper.string2json(content).get(object2.toString()));
		}
		JSONArray array = dbcontent.page(idx, pageSize);
		JSONObject object = new JSONObject(){
			private static final long serialVersionUID = 1L;

			{
				put("totalSize", (int)Math.ceil((double)dbcontent.count()/pageSize));
				put("currentPage", idx);
				put("pageSize", pageSize);
				put("data", array);
				
			}
		};
		return object;
	}

//	public JSONArray select() {
////		String content = session.get(_obj_session.get("content").toString());
////		JSONArray array = (JSONArray) JSONValue.parse(content);
//		JSONArray array = dbcontent.select();
//		for (int i = 0; i < array.size(); i++) {
//			JSONObject _obj = (JSONObject) array.get(i);
//			// String string = _obj.get("image").toString().split(",")[0];
//			// _obj.remove("image");
//			// _obj.put("display", string);
//			String state = showstate(_obj.get("state").toString());
//			_obj.put("state", state);
//			array.set(i, _obj);
//		}
////		session.deleteSession("content");
//		return array;
//	}

	public JSONArray select(String oid) {
		JSONArray array = dbcontent.eq("oid", oid).select();
		for (int i = 0; i < array.size(); i++) {
			JSONObject _obj = (JSONObject) array.get(i);
			String state = showstate(_obj.get("state").toString());
			_obj.put("state", state);
			array.set(i, _obj);
		}
		return array;
	}
	public JSONArray searchByUid(String uid) {
		JSONArray array = dbcontent.eq("ownid", uid).select();
		for (int i = 0; i < array.size(); i++) {
			JSONObject _obj = (JSONObject) array.get(i);
			String state = showstate(_obj.get("state").toString());
			_obj.put("state", state);
			array.set(i, _obj);
		}
		return array;
	}
	public int updatesort(String oid, int sortNo) {
		JSONObject object = new JSONObject();
		object.put("sort", sortNo);
		return dbcontent.eq("_id", new ObjectId(oid)).data(object).update()!=null?0:99;
	}

	public JSONArray search(JSONObject condString) {
		Set<Object> set = condString.keySet();
		for (Object object2 : set) {
			dbcontent.eq(object2.toString(), condString.get(object2.toString()));
		}
		JSONArray array = dbcontent.select();
		for (int i = 0; i < array.size(); i++) {
			JSONObject _obj = (JSONObject) array.get(i);
			String state = showstate(_obj.get("state").toString());
			_obj.put("state", state);
			array.set(i, _obj);
		}
		return array;
	}

	// 获取积分价值条件？？
	public void getpoint() {
		dbcontent.field("point").select();
	}

//	public JSONArray findByGroupID(String ogid) {
//		JSONArray _obj = new JSONArray();
//		String content = session.get(_obj_session.get("content").toString());
//		JSONArray array = (JSONArray) JSONValue.parse(content);
//		JSONObject object = new JSONObject();
//		for (int i = 0; i < array.size(); i++) {
//			object = (JSONObject) array.get(i);
//			if (ArraysUtil.contains(object.get("ogid").toString().split(","), ogid)) {
//				object.remove("ogid");
//			}
//			String state = showstate(object.get("state").toString());
//			object.put("state", state);
//			_obj.add(object);
//		}
//		return _obj;
//	}
	public JSONArray findByGroupID(String ogid) {
		JSONArray _obj = new JSONArray();
//		String content = session.get(_obj_session.get("content").toString());
		
//		JSONArray array = (JSONArray) JSONValue.parse(content);
		JSONArray array = dbcontent.select();
		JSONObject object = new JSONObject();
		for (int i = 0; i < array.size(); i++) {
			object = (JSONObject) array.get(i);
			if (ArraysUtil.contains(object.get("ogid").toString().split(","), ogid)) {
				String state = showstate(object.get("state").toString());
				object.put("state", state);
				_obj.add(object);
			}
		}
		return _obj;
	}
//	public JSONArray findByWebID(String wbid) {
//		JSONArray _obj = new JSONArray();
////		String content = session.get(_obj_session.get("content").toString());
////		JSONArray array = (JSONArray) JSONValue.parse(content);
//		JSONArray array =dbcontent.eq("wbid", wbid).select();
//		JSONObject object = new JSONObject();
//		for (int i = 0; i < array.size(); i++) {
//			object = (JSONObject) array.get(i);
//			if (ArraysUtil.contains(object.get("wbid").toString().split(","), wbid)) {
//				object.remove("wbid");
//			}
//			String state = showstate(object.get("state").toString());
//			object.put("state", state);
//			_obj.add(object);
//		}
//		return _obj;
//	}

	// 修改tempid
	public int setTempId(String oid, String tempid) {
		JSONObject object = new JSONObject();
		object.put("tempid", tempid);
		return dbcontent.eq("_id", new ObjectId(oid)).data(object).update() != null ? 0 : 99;
	}

	// 修改fatherid，同时删除ogid（ogid=""）
	public int setfatherid(String oid, String fatherid) {
		JSONObject object = new JSONObject();
		if (fatherid == null) {
			fatherid = "0";
		} else {
			object.put("ogid", "");
		}
		object.put("fatherid", fatherid);
		return dbcontent.eq("_id", new ObjectId(oid)).data(object).update() != null ? 0 : 99;
	}

	// 修改对象密级
	public int setslevel(String oid, String slevel) {
		JSONObject object = new JSONObject();
		object.put("tempid", slevel);
		return dbcontent.eq("_id", new ObjectId(oid)).data(object).update() != null ? 0 : 99;
	}

	// 文章审核 state：0 草稿，1 待审核，2 审核通过 3 审核不通过
	public int review(String oid, String managerid, String state) {
		JSONObject object = new JSONObject();
		object.put("manageid", managerid);
		object.put("state", state);
		return dbcontent.eq("_id", new ObjectId(oid)).data(object).update()!=null?0:99;
	}
	public int delete(String[] arr) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			int code = DeleteArticle(arr[i]);
			if (code != 0) {
				stringBuffer.append((i + 1) + ",");
			}
		}
		return stringBuffer.length() == 0 ? 0 : 3;
	}
	public String showstate(String state) {
		String msg = "";
		switch (state) {
		case "1":
			msg = "待审核";
			break;
		case "2":
			msg = "终审通过";
			break;
		case "3":
			msg = "终审不通过";
			break;
		default:
			msg = "草稿";
			break;
		}
		return msg;
	}

	/**
	 * 生成32位随机编码
	 * 
	 * @return
	 */
	public static String getID() {
		String str = UUID.randomUUID().toString().trim();
		return str.replace("-", "");
	}
	/**
	 * 将map添加至JSONObject中
	 * @param map
	 * @param object
	 * @return
	 */
	public JSONObject AddMap(HashMap<String, Object> map,JSONObject object) {
		if (map.entrySet()!=null) {
			Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
				if (!object.containsKey(entry.getKey())) {
					object.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return object;
	}
	public String resultMessage(int num, String msg) {
		String message = null;
		switch (num) {
		case 0:
			message = msg;
			break;
		case 1:
			message = "内容组名称长度不合法";
			break;
		case 2:
			message = "必填项没有填";
			break;
		case 3:
			message = "该栏目已存在本文章";
			break;
		case 4:
			message = "该站点已存在本文章";
			break;
		case 5:
			message = "存在敏感词";
			break;
		case 6:
			message = "超过限制字数";
			break;
		default:
			message = "其他异常";
		}
		return jGrapeFW_Message.netMSG(num, message);
	}
}
