package interfaceApplication;

import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import esayhelper.JSONHelper;
import model.ContentGroupModel;
import model.ContentModel;

@SuppressWarnings("unchecked")
public class ContentGroup {
	private ContentGroupModel group = new ContentGroupModel();
	private HashMap<String, Object> defcol = new HashMap<>();
	private JSONObject _obj = new JSONObject();

	public ContentGroup() {
		defcol.put("ogid", ContentModel.getID());
		defcol.put("ownid", 0);
		defcol.put("fatherid", 0);
		defcol.put("sort", 0);
		defcol.put("isvisble", 0);
		defcol.put("slevel", 0);
		defcol.put("tempid", 0);
	}

	public String GroupInsert(String GroupInfo) {
		JSONObject groupinfo = group.AddMap(defcol, JSONHelper.string2json(GroupInfo));
		return group.resultMessage(group.AddGroup(groupinfo), "新增内容组成功");
	}

	public String GroupEdit(String ogid, String groupInfo) {
		return group.resultMessage(group.UpdateGroup(ogid, JSONHelper.string2json(groupInfo)),
				"更新内容组数据成功");
	}

	public String GroupDelete(String ogid) {
		int code = 0;
		// 根据内容组显示文章
		JSONArray array = (JSONArray) JSONValue.parse(new Content().showbygroupId(ogid));
		if (array.size() != 0) {
			// 含有该内容组的文章设置为默认值
			code = new ContentModel().setGroup(array, ogid);
		}
		if (code == 0) {
			code = group.DeleteGroup(ogid);
		}
		return group.resultMessage(code, "删除内容组成功");
	}

//	public String GroupSelect() {
//		_obj.put("records", group.select());
//		return StringEscapeUtils.unescapeJava(group.resultMessage(0, _obj.toString()));
//	}

	public String GroupFind(String groupinfo) {
		_obj.put("records", group.select(groupinfo));
		return StringEscapeUtils.unescapeJava(group.resultMessage(0, _obj.toString()));
	}

	public String GroupSort(String ogid, int num) {
		return group.resultMessage(group.setsort(ogid, num), "顺序调整成功");
	}

	public String GroupPage(int idx, int pageSize) {
		_obj.put("records", group.page(idx, pageSize));
		return StringEscapeUtils.unescapeJava(group.resultMessage(0, _obj.toString()));
	}

	public String GroupPageBy(int idx, int pageSize, String GroupInfo) {
		_obj.put("records", group.page(idx, pageSize, GroupInfo));
		return StringEscapeUtils.unescapeJava(group.resultMessage(0, _obj.toString()));
	}

	public String GroupSlevel(String ogid, int slevel) {
		return group.resultMessage(group.setslevel(ogid, slevel), "密级更新成功");
	}

	public String GroupSetTemp(String ogid, String tempid) {
		return group.resultMessage(group.setTempId(ogid, tempid), "更新模版成功");
	}

	public String GroupSetFatherid(String ogid, String fatherid) {
		return group.resultMessage(group.setfatherid(ogid, fatherid), "成功设置上级内容组");
	}

	public String GroupBatchDelete(String ogid) {
		return group.resultMessage(group.delete(ogid.split(",")), "删除成功");
	}

}
